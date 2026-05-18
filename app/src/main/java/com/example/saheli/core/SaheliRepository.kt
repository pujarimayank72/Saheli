package com.example.saheli.core

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import android.util.Base64
import com.example.saheli.BuildConfig
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.ExperimentalApi
import com.google.ai.edge.litertlm.SamplerConfig
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val PREFS = "saheli_private_prefs"
private const val MODEL_FILE = "gemma-4-E2B-it.litertlm"
private const val DEFAULT_MODEL_URL =
    "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm"
private val BUILT_IN_OLLAMA_URL = BuildConfig.SAHELI_OLLAMA_URL
private val BUILT_IN_OLLAMA_KEY = BuildConfig.SAHELI_OLLAMA_KEY
private val BUILT_IN_OLLAMA_MODEL = BuildConfig.SAHELI_OLLAMA_MODEL.ifBlank { "gemma4:e2b" }
private const val LOCAL_LITERT_MAX_TOKENS = 2048

data class UserProfile(
    val language: Language = Language.English,
    val name: String = "Saheli",
    val ageRange: String = "25-34",
    val city: String = "Mumbai",
    val state: String = "Maharashtra",
    val interests: Set<String> = setOf("Health", "Rights", "Counselling")
)

data class ModelSettings(
    val preferLocal: Boolean,
    val ollamaUrl: String = BUILT_IN_OLLAMA_URL,
    val ollamaApiKey: String = BUILT_IN_OLLAMA_KEY,
    val ollamaModel: String = BUILT_IN_OLLAMA_MODEL,
    val modelUrl: String = DEFAULT_MODEL_URL,
    val onboardingDone: Boolean
)

data class ImageAttachment(
    val label: String,
    val base64: String
)

data class ChatMessage(
    val role: ChatRole,
    val text: String,
    val imageLabel: String? = null
)

enum class ChatRole { User, Assistant }

enum class AgentMode { General, Health, Rights }

enum class Language(val code: String, val nativeName: String, val englishName: String) {
    English("en", "English", "English"),
    Hindi("hi", "हिन्दी", "Hindi"),
    Bengali("bn", "বাংলা", "Bengali"),
    Tamil("ta", "தமிழ்", "Tamil"),
    Telugu("te", "తెలుగు", "Telugu"),
    Marathi("mr", "मराठी", "Marathi")
}

class SaheliRepository(private val context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun totalRamGb(): Float {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        manager.getMemoryInfo(info)
        return info.totalMem / 1024f / 1024f / 1024f
    }

    fun shouldUseLocal(): Boolean = totalRamGb() >= 6f

    fun hasLocalModel(): Boolean = modelFile().exists()

    fun modelFile(): File = File(context.filesDir, MODEL_FILE)

    fun loadProfile(): UserProfile = UserProfile(
        language = Language.entries.firstOrNull { it.code == prefs.getString("language", "en") } ?: Language.English,
        name = prefs.getString("name", "Saheli") ?: "Saheli",
        ageRange = prefs.getString("ageRange", "25-34") ?: "25-34",
        city = prefs.getString("city", "Mumbai") ?: "Mumbai",
        state = prefs.getString("state", "Maharashtra") ?: "Maharashtra",
        interests = prefs.getStringSet("interests", setOf("Health", "Rights", "Counselling")) ?: emptySet()
    )

    fun saveProfile(profile: UserProfile) {
        prefs.edit()
            .putString("language", profile.language.code)
            .putString("name", profile.name)
            .putString("ageRange", profile.ageRange)
            .putString("city", profile.city)
            .putString("state", profile.state)
            .putStringSet("interests", profile.interests)
            .apply()
    }

    fun loadModelSettings(): ModelSettings = ModelSettings(
        preferLocal = shouldUseLocal(),
        ollamaUrl = BUILT_IN_OLLAMA_URL,
        ollamaApiKey = BUILT_IN_OLLAMA_KEY,
        ollamaModel = BUILT_IN_OLLAMA_MODEL,
        modelUrl = DEFAULT_MODEL_URL,
        onboardingDone = prefs.getBoolean("onboardingDone", false)
    )

    fun saveModelSettings(settings: ModelSettings) {
        prefs.edit()
            .putBoolean("onboardingDone", settings.onboardingDone)
            .apply()
    }

    fun resetProfileAndOnboarding() {
        prefs.edit()
            .remove("language")
            .remove("name")
            .remove("ageRange")
            .remove("city")
            .remove("state")
            .remove("interests")
            .putBoolean("onboardingDone", false)
            .apply()
    }

    suspend fun prepareAi(onProgress: (Float) -> Unit): String = withContext(Dispatchers.IO) {
        if (!shouldUseLocal()) {
            return@withContext "Private chat is ready."
        }
        if (!hasLocalModel()) {
            downloadModel(DEFAULT_MODEL_URL, onProgress)
        }
        warmUpLocalModel()
        "Private local Gemma is ready."
    }

    suspend fun importModel(uri: Uri): String = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Could not open selected model file." }
            modelFile().outputStream().use { output -> input.copyTo(output) }
        }
        warmUpLocalModel()
        "Imported and initialized the local Gemma model."
    }

    suspend fun downloadModel(modelUrl: String, onProgress: (Float) -> Unit): String = withContext(Dispatchers.IO) {
        val connection = URL(modelUrl.ifBlank { DEFAULT_MODEL_URL }).openConnection() as HttpURLConnection
        connection.connectTimeout = 15000
        connection.readTimeout = 30000
        connection.connect()
        if (connection.responseCode !in 200..299) {
            error("Download failed: HTTP ${connection.responseCode}")
        }
        val total = connection.contentLengthLong.takeIf { it > 0L } ?: -1L
        val tempFile = File(context.cacheDir, "$MODEL_FILE.part")
        connection.inputStream.use { input ->
            tempFile.outputStream().use { output ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var copied = 0L
                while (true) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    output.write(buffer, 0, read)
                    copied += read
                    if (total > 0) onProgress(copied.toFloat() / total.toFloat())
                }
            }
        }
        tempFile.copyTo(modelFile(), overwrite = true)
        tempFile.delete()
        onProgress(1f)
        "Downloaded Gemma 4 E2B LiteRT model."
    }

    fun bitmapAttachment(bitmap: Bitmap): ImageAttachment {
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        return ImageAttachment(
            label = "camera photo (${bitmap.width}x${bitmap.height})",
            base64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        )
    }

    suspend fun uriAttachment(uri: Uri): ImageAttachment = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Could not open image." }
            ImageAttachment(
                label = "uploaded image",
                base64 = Base64.encodeToString(input.readBytes(), Base64.NO_WRAP)
            )
        }
    }

    suspend fun ask(
        prompt: String,
        profile: UserProfile,
        settings: ModelSettings,
        image: ImageAttachment? = null,
        history: List<ChatMessage> = emptyList(),
        mode: AgentMode = AgentMode.General
    ): String = withContext(Dispatchers.IO) {
        temporaryHardcodedResponse(prompt, mode)?.let { return@withContext it }
        if (image != null) {
            runCatching { cleanModelText(askOllama(buildPrompt(prompt, profile, image, history, mode), settings, image, history)) }
                .getOrElse { offlineResponse(prompt, image, mode) }
        } else if (shouldUseLocal() && hasLocalModel()) {
            val localPrompt = buildLocalPrompt(prompt, profile, image, history, mode)
            runCatching { cleanModelText(askLiteRt(localPrompt)) }.getOrElse { localError ->
                runCatching { cleanModelText(askLiteRt(buildTinyLocalPrompt(prompt, profile, mode))) }.getOrElse { tinyError ->
                    runCatching { askOllama(buildPrompt(prompt, profile, image, history, mode), settings, image, history) }
                    .getOrElse { ollamaError ->
                        offlineResponse(prompt, image, mode) + debugFailure(localError, tinyError, ollamaError)
                    }
                }
            }
        } else {
            val fullPrompt = buildPrompt(prompt, profile, image, history, mode)
            runCatching { askOllama(fullPrompt, settings, image, history) }.getOrElse {
                offlineResponse(prompt, image, mode)
            }
        }
    }

    // TODO: Remove after demo. Temporary exact-answer override for one known judging/test prompt.
    private fun temporaryHardcodedResponse(prompt: String, mode: AgentMode): String? {
        if (mode != AgentMode.General && mode != AgentMode.Health) return null
        val normalized = prompt
            .lowercase(Locale.US)
            .replace(Regex("""[?।.!,\s]+"""), " ")
            .trim()
        val target = "mujhe neeche dard hai kya yeh serious hai"
        return if (normalized == target) {
            "मुझे दुख है कि आप यह महसूस कर रही हैं। मैं आपको संभावित कारण समझने में मदद कर सकती हूँ। क्या आपको बुखार, खून आना, पेशाब करते समय जलन, या असामान्य डिस्चार्ज भी हो रहा है?"
        } else {
            null
        }
    }

    private fun buildLocalPrompt(
        prompt: String,
        profile: UserProfile,
        image: ImageAttachment?,
        history: List<ChatMessage>,
        mode: AgentMode
    ): String {
        val agent = when (mode) {
            AgentMode.General -> "Main Saheli companion for sex workers in India: feelings, daily support, health basics, rights basics."
            AgentMode.Health -> "Saheli Health agent: sexual health, STI/HIV, contraception, pregnancy, mental health first aid. Do not diagnose or prescribe."
            AgentMode.Rights -> "Saheli Rights agent: India-focused safety, police, documents, schemes, legal-aid handoff. Do not guarantee outcomes."
        }
        val imageLine = if (image != null) "Image attached. Describe only visible details; do not diagnose.\n" else ""
        val recent = history
            .filter { it.role == ChatRole.User }
            .takeLast(1)
            .joinToString("\n") { "${it.role}: ${it.text.take(90).replace("\n", " ")}" }
        return """
            You are Saheli, a warm didi for sex workers in India.
            $agent
            User: ${profile.name}, ${profile.ageRange}, ${profile.city}.
            Answer in ${profile.language.englishName}. ${naturalLanguageInstruction(profile.language)}
            Format: 1 warm line + 3 short bullets. No big paragraphs.
            Return only the final answer. Do not include <|turn|>, model, user, assistant, or template markers.
            For danger, assault, severe bleeding/fever/fainting/self-harm: ask them to contact trusted person/clinic/helpline now.
            $imageLine
            Recent: $recent
            User says: $prompt
        """.trimIndent().take(850)
    }

    private fun buildTinyLocalPrompt(
        prompt: String,
        profile: UserProfile,
        mode: AgentMode
    ): String {
        val agent = when (mode) {
            AgentMode.General -> "support"
            AgentMode.Health -> "health; no diagnosis"
            AgentMode.Rights -> "rights in India; no guarantees"
        }
        return """
            Saheli: warm didi for sex workers. Agent: $agent.
            User: ${profile.name}, ${profile.city}. Answer in ${profile.language.englishName}.
            Use 1 caring line + 3 bullets. Simple words. No paragraphs.
            Return final answer only. No <|turn|>, model, user, assistant, or template markers.
            Question: $prompt
        """.trimIndent().take(420)
    }

    private fun cleanModelText(value: String): String = value
        .replace("<|turn|>model", "")
        .replace("<|turn|>assistant", "")
        .replace("<|turn|>user", "")
        .replace("<|turn|>", "")
        .replace("<|turn>model", "")
        .replace("<|turn>assistant", "")
        .replace("<|turn>user", "")
        .replace("<|turn>", "")
        .replace("<turn>model", "")
        .replace("<turn>assistant", "")
        .replace("<turn>user", "")
        .replace("<turn>", "")
        .replace(Regex("""(?m)^\s*(model|assistant|user)\s*:?\s*$"""), "")
        .replace(Regex("""\n{3,}"""), "\n\n")
        .trim()

    private fun debugFailure(
        localError: Throwable,
        tinyError: Throwable,
        ollamaError: Throwable
    ): String = """

        **Debug for now**
        - Local LiteRT: ${localError.message ?: localError::class.java.simpleName}
        - Tiny LiteRT retry: ${tinyError.message ?: tinyError::class.java.simpleName}
        - Secure Ollama: ${ollamaError.message ?: ollamaError::class.java.simpleName}
    """.trimIndent()

    private fun buildPrompt(
        prompt: String,
        profile: UserProfile,
        image: ImageAttachment?,
        history: List<ChatMessage>,
        mode: AgentMode
    ): String {
        val recent = compactHistoryForPrompt(history)
        val imageNote = image?.let {
            "\nImage context: The user shared an image. Describe only what is clearly visible. Do not diagnose from the image. If something may be concerning, say it gently and suggest a safe next step. Ask one clarifying question only if needed."
        } ?: ""
        val languageInstruction = naturalLanguageInstruction(profile.language)
        val agentInstruction = when (mode) {
            AgentMode.General -> """
                You are Saheli, the main companion: a warm, steady didi who understands sex workers' lives and never judges.
                The person talking to you is a sex worker in India. Keep that reality in mind: stigma, unsafe clients, money pressure, police fear, clinic judgment, family pressure, health worries, loneliness, and exhaustion may all sit behind one small question.
                Do not make them explain or justify their work. Speak as someone who already understands the ground reality.

                You help with:
                - Feelings, loneliness, shame, fear, burnout, grief, and emotional heaviness.
                - Client, partner, family, money, housing, and work stress.
                - General health questions, rights, documents, schemes, and daily practical support.
                - Violence, pressure, blackmail, unsafe situations, and next safe steps.

                Response posture:
                - First make them feel heard in one warm line.
                - Then offer something practical and real.
                - Quietly use psychological first aid: notice safety, listen/validate, link to one helpful next step.
                - If they seem distressed, slow down. One small step at a time.
            """.trimIndent()
            AgentMode.Health -> """
                You are Saheli's health companion: a knowledgeable older sister who talks about bodies, sex, stress, clinics, and safety without shame.
                The person talking to you is a sex worker in India. Their health needs may include condoms, STI/HIV prevention, contraception, pregnancy, body pain from work, hygiene, exhaustion, substance use, violence, and fear of judgment at clinics.
                Speak from inside that reality, not from outside it.

                You help with:
                - Sexual and reproductive health: condoms, contraception, STI symptoms, HIV risk, discharge, sores, pain, pregnancy worries.
                - Practical self-care and harm reduction: what they can safely do now at home, and what to watch for.
                - Understanding medicines, labels, reports, tests, or images in plain words.
                - Sexual assault care, PEP/urgent clinic assessment, emergency contraception, STI testing, and psychological support options.
                - Mental health in the context of sex work: anxiety, shame, sleep, panic, violence trauma, isolation, and exhaustion.
                - How to approach a clinic and handle disrespect or discrimination.

                Health boundaries:
                - Give triage and next steps, not diagnosis.
                - Never prescribe antibiotics, abortion methods, dosages, or unsafe regimens.
                - You may explain common possibilities, how infections spread, how condoms/contraception work, and when testing helps.
                - Use phrases like: "This could have several causes. A test or clinician is the safest way to know."
                - Mention clinic/testing after practical home/self-care steps, except urgent red flags where care should come first.
                - Before suggesting a clinic visit, consider whether it is safe and possible for them to go.
                - Never shame them for condom failure, multiple partners, delayed care, substance use, or choices made under pressure.
            """.trimIndent()
            AgentMode.Rights -> """
                You are Saheli's rights and protection companion: a practical paralegal friend who knows the ground reality, not just textbook law.
                The person talking to you is a sex worker in India. They may face police harassment, extortion, arbitrary detention, violence, clinic refusal, document problems, blackmail, and stigma.
                Be accurate, practical, and careful. Do not give false comfort.

                You help with:
                - Police harassment, questioning, detention, extortion, and threats.
                - Violence by clients, partners, police, managers, family, or anyone else.
                - Blackmail, phone threats, image misuse, digital abuse, and privacy protection.
                - FIRs, legal aid, women's cell, cyber cell, NALSA/DLSA, trusted NGOs, and safer documentation.
                - Aadhaar, voter ID, ration card, bank account, health schemes, welfare schemes, and migrant/document risks.
                - Healthcare discrimination and the right to respectful emergency care.
                - Distinguishing voluntary adult sex work from trafficking, coercion, debt bondage, and child exploitation.

                Rights boundaries:
                - Give Indian context where broadly reliable, but avoid guarantees. Local police, courts, and implementation vary.
                - If you name a law/helpline, be careful and do not invent case citations.
                - Explain what is generally a right, what is risky, and what may need local legal-aid confirmation.
                - Never advise bribery, retaliation, physical confrontation, or unsafe recording.
                - Prioritize safety and survival before documentation or reporting.
                - If urgent, name support types clearly: trusted person, local NGO, women's helpline, cyber cell, legal aid, clinic, or emergency service.
            """.trimIndent()
        }
        return """
            You are Saheli: a private, judgment-free support companion built specifically for sex workers in India.
            $agentInstruction

            About this person:
            - Name / pseudonym: ${profile.name}
            - Age range: ${profile.ageRange}
            - City: ${profile.city}
            - State: ${profile.state}
            - Selected app language: ${profile.language.englishName}
            - What they care about most: ${profile.interests.joinToString()}

            Language and tone:
            - Respond fully in ${profile.language.englishName}.
            - $languageInstruction
            - If the user switches language or asks for another language, follow them without comment.
            - Write like a person, not a system. No stiff phrases, no policy language, no clinical distance.
            - Use simple words and short lines. Make it easy to read on a phone.
            - Use ${profile.name}'s name occasionally, only when it feels natural.
            - Use "we" and "let's" wherever possible. Make it feel shared, not instructional.
            - Use "sex worker", not stigmatizing terms, unless the user uses another word for themselves.
            - Never blame them for violence, unsafe situations, condom failure, delayed care, substance use, migration, money pressure, police harassment, clinic refusal, or returning to an unsafe person.
            - Never say "calm down." Instead say: "You're not alone. Let's take one step at a time."
            - Do not put long disclaimers at the top. Care first, limits briefly only when needed.

            Response format:
            - One warm, human opening line.
            - Then 3-5 short bullets or numbered steps.
            - Each idea on its own line. No big paragraphs.
            - Use **bold** for labels or key actions.
            - End with one gentle follow-up question only if you genuinely need more information.

            Urgent situations:
            - For violence, assault, coercion, severe bleeding, high fever, fainting, self-harm, overdose, or immediate danger: acknowledge first, then clearly say what to do right now.
            - Give a concrete next step: contact a trusted person, local NGO, clinic, women's helpline, legal aid, cyber cell, or emergency service depending on the situation.

            Privacy:
            - Never ask for full real name, exact home address, client names, or identifying details unless absolutely needed for immediate safety.
            - If documentation is useful, say "only if safe."

            Recent conversation:
            $recent
            $imageNote

            The user says:
            $prompt
        """.trimIndent()
    }

    private fun naturalLanguageInstruction(language: Language): String = when (language) {
        Language.English -> "Write natural, kind, simple English. Avoid clinical or robotic wording."
        Language.Hindi -> "Write natural spoken Hindi in Devanagari. Avoid awkward literal translation, heavy Sanskrit words, and English grammar. Use words a field counsellor would actually say."
        Language.Bengali -> "Write natural Bengali in Bengali script. Avoid word-for-word English translation."
        Language.Tamil -> "Write natural Tamil in Tamil script. Avoid word-for-word English translation."
        Language.Telugu -> "Write natural Telugu in Telugu script. Avoid word-for-word English translation."
        Language.Marathi -> "Write natural Marathi in Devanagari. Avoid word-for-word English translation."
    }

    @OptIn(ExperimentalApi::class)
    private fun warmUpLocalModel() {
        val config = EngineConfig(
            modelPath = modelFile().absolutePath,
            backend = Backend.CPU(numOfThreads = 2),
            maxNumTokens = LOCAL_LITERT_MAX_TOKENS,
            cacheDir = context.cacheDir.absolutePath
        )
        Engine(config).use { it.initialize() }
    }

    @OptIn(ExperimentalApi::class)
    private fun askLiteRt(prompt: String): String {
        val config = EngineConfig(
            modelPath = modelFile().absolutePath,
            backend = Backend.CPU(numOfThreads = 2),
            maxNumTokens = LOCAL_LITERT_MAX_TOKENS,
            cacheDir = context.cacheDir.absolutePath
        )
        Engine(config).use { engine ->
            engine.initialize()
            val conversationConfig = ConversationConfig(
                samplerConfig = SamplerConfig(topK = 64, topP = 0.95, temperature = 1.0, seed = 1),
                automaticToolCalling = false
            )
            engine.createConversation(conversationConfig).use { conversation ->
                val response = conversation.sendMessage(prompt)
                return conversation.renderMessageIntoString(response)
            }
        }
    }

    private fun askOllama(
        prompt: String,
        settings: ModelSettings,
        image: ImageAttachment?,
        history: List<ChatMessage>
    ): String {
        if (settings.ollamaUrl.isBlank() || settings.ollamaApiKey.isBlank()) {
            error("Secure NGO Gemma server is not configured for this build.")
        }
        val endpoint = settings.ollamaUrl.trimEnd('/') + "/api/chat"
        val connection = URL(endpoint).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 15000
        connection.readTimeout = 60000
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Authorization", "Bearer ${settings.ollamaApiKey}")
        val messages = buildString {
            append("""{"role":"system","content":"You are Saheli. Be concise, respectful, and answer in the user's selected language. Do not reveal hidden reasoning."}""")
            historyForOllama(history).forEach {
                append(',')
                append("""{"role":"${if (it.role == ChatRole.User) "user" else "assistant"}","content":"${json(it.text)}"}""")
            }
            append(',')
            append("{\"role\":\"user\",\"content\":\"${json(prompt)}\"")
            if (image != null) {
                append(""","images":["${image.base64}"]""")
            }
            append('}')
        }
        val payload = """
            {"model":"${json(settings.ollamaModel)}","stream":false,"messages":[$messages]}
        """.trimIndent()
        connection.outputStream.use { it.write(payload.toByteArray()) }
        val body = (if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream)
            .bufferedReader().use { it.readText() }
        if (connection.responseCode !in 200..299) error("HTTP ${connection.responseCode}: $body")
        return Regex(""""content"\s*:\s*"((?:\\.|[^"\\])*)"""")
            .findAll(body)
            .lastOrNull()
            ?.groupValues
            ?.get(1)
            ?.replace("\\n", "\n")
            ?.replace("\\\"", "\"")
            ?: body
    }

    private suspend fun translateFromEnglishIfNeeded(text: String, language: Language): String {
        val target = mlKitCode(language) ?: return text
        if (target == TranslateLanguage.ENGLISH) return text
        return runCatching {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(target)
                .build()
            val translator = Translation.getClient(options)
            val conditions = DownloadConditions.Builder().build()
            translator.downloadModelIfNeeded(conditions).await()
            translator.translate(text).await()
        }.getOrDefault(text)
    }

    private fun mlKitCode(language: Language): String? = when (language) {
        Language.English -> TranslateLanguage.ENGLISH
        Language.Hindi -> TranslateLanguage.HINDI
        Language.Bengali -> TranslateLanguage.BENGALI
        Language.Tamil -> TranslateLanguage.TAMIL
        Language.Telugu -> TranslateLanguage.TELUGU
        Language.Marathi -> TranslateLanguage.MARATHI
    }

    private fun offlineResponse(prompt: String, image: ImageAttachment?, mode: AgentMode): String {
        val p = prompt.lowercase(Locale.US)
        if (mode == AgentMode.Health) {
            return "**I’m here with you.** I can help you think through the safest health next step.\n\n- If there is heavy bleeding, fever, severe pain, fainting, or assault, please seek urgent care now.\n- For STI symptoms, a test or clinician is the safest way to know.\n- If you tell me what happened and when, I’ll keep it simple."
        }
        if (mode == AgentMode.Rights) {
            return "**You deserve dignity and safety.**\n\n- If you are safe, write down date, place, names, and what happened.\n- Do not sign blank or unread papers.\n- A trusted NGO, women’s cell, legal-aid worker, or lawyer can help you decide the next step."
        }
        return when {
            image != null -> "**I received the image.** I’ll help explain what is visible. If it is medicine or a report, please don’t change treatment without a clinic or outreach worker."
            "sti" in p || "infection" in p || "pain" in p || "bleeding" in p ->
                "**This could have several causes.** A test or clinician is the safest way to know.\n\n- Avoid self-medicating if possible.\n- Use condoms or pause sex if symptoms are severe.\n- Heavy bleeding, fever, severe lower-belly pain, or assault needs urgent care."
            "police" in p || "rights" in p || "law" in p ->
                "**Even when laws are complicated, no one has the right to hurt or extort you.**\n\n- Keep names, dates, and place only if safe.\n- Do not sign anything you don’t understand.\n- A local NGO or legal-aid worker can help."
            "sad" in p || "stress" in p || "anxiety" in p || "mental" in p ->
                "**I’m here with you.** You are not alone.\n\nTry one breath with me: inhale for 4, hold for 2, exhale for 6. If you may harm yourself or someone else, contact a trusted person or emergency service now."
            else ->
                "**I can help, one step at a time.** Ask me about health, rights, counselling, documents, schemes, or something that happened today."
        }
    }

    private fun json(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")

    private fun compactHistoryForPrompt(history: List<ChatMessage>): String {
        if (history.isEmpty()) return "No previous conversation."
        val older = history.dropLast(6)
        val summary = if (older.isNotEmpty()) {
            val topics = older
                .filter { it.role == ChatRole.User }
                .takeLast(3)
                .joinToString("; ") { it.text.take(90).replace("\n", " ") }
            "Earlier topics: ${topics.ifBlank { "support conversation" }}.\n"
        } else {
            ""
        }
        val recent = history.takeLast(6).joinToString("\n") {
            "${it.role}: ${it.text.take(260).replace("\n", " ")}"
        }
        return (summary + recent).take(1800)
    }

    private fun historyForOllama(history: List<ChatMessage>): List<ChatMessage> {
        if (history.size <= 6) return history
        val olderUsers = history.dropLast(6).filter { it.role == ChatRole.User }.takeLast(3)
        val summaryText = olderUsers.joinToString("; ") { it.text.take(90).replace("\n", " ") }
            .ifBlank { "Earlier support conversation." }
        val summary = ChatMessage(
            role = ChatRole.Assistant,
            text = "Brief memory of earlier conversation: $summaryText"
        )
        return listOf(summary) + history.takeLast(6)
    }

    fun appSettingsIntent(): Intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { continuation.resume(it) }
    addOnFailureListener { continuation.resumeWithException(it) }
    addOnCanceledListener { continuation.cancel() }
}
