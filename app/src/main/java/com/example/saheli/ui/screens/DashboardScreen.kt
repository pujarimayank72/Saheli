@file:OptIn(ExperimentalLayoutApi::class)

package com.example.saheli.ui.screens

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saheli.R
import com.example.saheli.core.AgentMode
import com.example.saheli.core.ChatMessage
import com.example.saheli.core.ChatRole
import com.example.saheli.core.ImageAttachment
import com.example.saheli.core.LocalizedText
import com.example.saheli.core.ModelSettings
import com.example.saheli.core.SaheliRepository
import com.example.saheli.core.UserProfile
import kotlinx.coroutines.launch
import java.util.Locale

private val Teal = Color(0xFF137F70)
private val Mint = Color(0xFFE6F5EF)
private val Blue = Color(0xFF3454D1)
private val BlueSoft = Color(0xFFEFF3FF)
private val Purple = Color(0xFF7C3AED)
private val PurpleSoft = Color(0xFFF5F3FF)
private val Amber = Color(0xFFF59E0B)
private val AmberSoft = Color(0xFFFFF3D0)
private val Coral = Color(0xFFFF755F)
private val Warm = Color(0xFFFAF8F5)
private val Border = Color(0xFFEDE8E2)
private val Ink = Color(0xFF1C1C1E)
private val Muted = Color(0xFF667085)

enum class AppTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Default.Home),
    Health("Health", Icons.Default.HealthAndSafety),
    Ask("Ask", Icons.Default.Chat),
    Rights("Rights", Icons.Default.Gavel),
    Support("Help", Icons.Default.People),
    Settings("More", Icons.Default.Settings)
}

@Composable
fun DashboardScreen(repository: SaheliRepository) {
    var selectedTab by remember { mutableStateOf(AppTab.Ask) }
    var profile by remember { mutableStateOf(repository.loadProfile()) }
    var settings by remember { mutableStateOf(repository.loadModelSettings()) }
    var preparing by remember { mutableStateOf(true) }
    var prepareStatus by remember { mutableStateOf(LocalizedText.t(profile.language, "preparing")) }
    var prepareProgress by remember { mutableFloatStateOf(0f) }
    val messages = remember {
        mutableStateListOf(
            ChatMessage(ChatRole.Assistant, "I am Saheli. You can ask about health, rights, schemes, documents, feelings, or upload a photo of a pamphlet or medicine label.")
        )
    }
    val healthMessages = remember {
        mutableStateListOf(
            ChatMessage(ChatRole.Assistant, "Tell me what you are feeling or what health question you have. I can help you decide safe next steps and when to visit a clinic.")
        )
    }
    val rightsMessages = remember {
        mutableStateListOf(
            ChatMessage(ChatRole.Assistant, "Tell me what happened, your city, and what help you need. I can explain rights, documents, schemes, and NGO/legal aid next steps.")
        )
    }
    LaunchedEffect(Unit) {
        prepareStatus = runCatching { repository.prepareAi { prepareProgress = it } }
            .getOrElse { "AI setup will continue in the background: ${it.message}" }
        settings = repository.loadModelSettings()
        preparing = false
    }

    if (preparing) {
        PreparingScreen(profile, prepareStatus, prepareProgress)
        return
    }

    Scaffold(
        bottomBar = {
            CompactBottomBar(selectedTab, profile) { selectedTab = it }
        },
        containerColor = Warm
    ) { paddingValues ->
        Surface(Modifier.padding(paddingValues).fillMaxSize(), color = Warm) {
            when (selectedTab) {
                AppTab.Home -> HomeTab(profile, settings, repository) { selectedTab = it }
                AppTab.Health -> AgentChatTab(repository, profile, settings, AgentMode.Health, healthMessages)
                AppTab.Ask -> AskAiTab(repository, profile, settings, messages)
                AppTab.Rights -> AgentChatTab(repository, profile, settings, AgentMode.Rights, rightsMessages)
                AppTab.Support -> SupportTab(profile)
                AppTab.Settings -> SettingsTab(
                    repository = repository,
                    profile = profile,
                    settings = settings,
                    onProfile = {
                        profile = it
                        repository.saveProfile(it)
                    },
                    onSettings = {
                        settings = it
                        repository.saveModelSettings(it)
                    },
                    onResetProfile = {
                        repository.resetProfileAndOnboarding()
                        profile = repository.loadProfile()
                        selectedTab = AppTab.Ask
                    }
                )
            }
        }
    }
}

@Composable
private fun CompactBottomBar(selected: AppTab, profile: UserProfile, onSelect: (AppTab) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(1.dp, Border)
            .navigationBarsPadding()
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(AppTab.Home, AppTab.Health, AppTab.Ask, AppTab.Rights, AppTab.Support, AppTab.Settings).forEach { tab ->
            val active = selected == tab
            val label = when (tab) {
                AppTab.Support -> "Help"
                AppTab.Settings -> "More"
                else -> LocalizedText.t(profile.language, tab.label.lowercase(Locale.US))
            }
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (active) PurpleSoft else Color.Transparent)
                    .clickable { onSelect(tab) }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(tab.icon, contentDescription = label, tint = if (active) Ink else Color(0xFF4B4B55), modifier = Modifier.size(22.dp))
                Text(
                    label,
                    color = if (active) Ink else Color(0xFF4B4B55),
                    fontSize = 10.sp,
                    lineHeight = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
        }
    }
}

@Composable
private fun PreparingScreen(profile: UserProfile, status: String, progress: Float) {
    Column(
        Modifier.fillMaxSize().background(Warm).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.saheli_logo),
            contentDescription = "Saheli",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(120.dp).clip(CircleShape)
        )
        Spacer(Modifier.height(20.dp))
        Text(LocalizedText.t(profile.language, "preparing"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = Teal)
        Text(LocalizedText.t(profile.language, "autoSetup"), color = Muted, modifier = Modifier.padding(top = 8.dp))
        if (progress in 0.01f..0.99f) {
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(top = 18.dp))
        }
        Text(status, color = Muted, modifier = Modifier.padding(top = 14.dp))
    }
}

@Composable
private fun Header(title: String, subtitle: String, trailing: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = Ink, lineHeight = 28.sp)
            Text(subtitle, color = Muted, fontSize = 14.sp, lineHeight = 19.sp)
        }
        trailing?.invoke()
    }
}

@Composable
private fun HomeTab(profile: UserProfile, settings: ModelSettings, repository: SaheliRepository, go: (AppTab) -> Unit) {
    val t = { key: String -> LocalizedText.t(profile.language, key) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Header(
            title = "${t("hello")}, ${profile.name}",
            subtitle = if (repository.hasLocalModel() && repository.shouldUseLocal()) t("privateReady") else t("serverReady"),
            trailing = {
                Image(
                    painter = painterResource(R.drawable.saheli_logo),
                    contentDescription = "Saheli",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(50.dp).clip(CircleShape)
                )
            }
        )
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    HomeTile(t("health"), "STI, contraception, mental health", Icons.Default.Favorite, Mint, Teal, Modifier.weight(1f)) { go(AppTab.Health) }
                    HomeTile(t("askAnything"), "Text, voice, image chat", Icons.Default.Chat, BlueSoft, Blue, Modifier.weight(1f)) { go(AppTab.Ask) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    HomeTile(t("rights"), "Police, documents, schemes", Icons.Default.Gavel, PurpleSoft, Purple, Modifier.weight(1f)) { go(AppTab.Rights) }
                    HomeTile(t("support"), "NGO, peer and counselling", Icons.Default.People, AmberSoft, Amber, Modifier.weight(1f)) { go(AppTab.Support) }
                }
            }
            Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(8.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(t("quickCounselling"), fontWeight = FontWeight.SemiBold)
                    Text("A calm, private space for fear, stress, grief, violence, money pressure, or anything you do not want to say out loud.", color = Muted, fontSize = 14.sp, lineHeight = 20.sp)
                    Button(onClick = { go(AppTab.Ask) }, colors = ButtonDefaults.buttonColors(containerColor = Teal)) {
                        Icon(Icons.Default.Chat, null)
                        Spacer(Modifier.size(8.dp))
                        Text(t("talk"))
                    }
                }
            }
            Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(12.dp), modifier = Modifier.border(1.dp, Border, RoundedCornerShape(12.dp))) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Recent chats", fontWeight = FontWeight.SemiBold, color = Ink)
                    listOf("Health question", "Rights guidance", "Counselling check-in").forEach {
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Warm).clickable { go(AppTab.Ask) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Chat, null, tint = Teal, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(it, color = Ink, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTile(title: String, subtitle: String, icon: ImageVector, bg: Color, fg: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(bg),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.height(128.dp).clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.62f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
            }
            Text(title, color = fg, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 17.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = fg.copy(alpha = 0.82f), fontSize = 11.sp, lineHeight = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun HealthTab(profile: UserProfile, openAsk: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Header(LocalizedText.t(profile.language, "health"), "Simple private guidance, saved for offline use")
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val topics = listOf(
                Triple("STI symptoms", "Discharge, sores, pain, itching, fever", Teal),
                Triple("Contraception", "Condoms, emergency pills, pregnancy tests", Blue),
                Triple("Mental health", "Stress, panic, sleep, violence trauma", Purple),
                Triple("Nutrition", "Low-cost food, anemia warning signs", Amber)
            )
            topics.forEach { (title, subtitle, color) ->
                Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Article, null, tint = color)
                        Column(Modifier.weight(1f)) {
                            Text(title, fontWeight = FontWeight.SemiBold)
                            Text(subtitle, color = Muted)
                        }
                    }
                }
            }
            Card(colors = CardDefaults.cardColors(Mint), shape = RoundedCornerShape(8.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Photo help", fontWeight = FontWeight.SemiBold, color = Teal)
                    Text("Upload or capture a label, report, prescription, or pamphlet in Ask. Saheli keeps it temporary and explains what to do next.", color = Teal.copy(alpha = 0.8f))
                    Button(onClick = openAsk, colors = ButtonDefaults.buttonColors(containerColor = Teal)) {
                        Icon(Icons.Default.Image, null)
                        Spacer(Modifier.size(8.dp))
                        Text("Ask with photo")
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentChatTab(
    repository: SaheliRepository,
    profile: UserProfile,
    settings: ModelSettings,
    mode: AgentMode,
    messages: MutableList<ChatMessage>
) {
    val title = if (mode == AgentMode.Health) LocalizedText.t(profile.language, "health") else LocalizedText.t(profile.language, "rights")
    val subtitle = if (mode == AgentMode.Health) {
        "Health guidance with clinic-safe next steps"
    } else {
        "Guidance for ${profile.city}, ${profile.state}: documents, schemes, and legal aid"
    }
    val chips = if (mode == AgentMode.Health) {
        listOf("STI symptoms", "Contraception", "Mental health", "Medicine label")
    } else {
        listOf("Police stopped me", "Need ID help", "Scheme eligibility", "Clinic refused care")
    }
    ChatSurface(
        repository = repository,
        profile = profile,
        settings = settings,
        mode = mode,
        title = title,
        subtitle = subtitle,
        suggestions = chips,
        messages = messages
    )
}

@Composable
private fun AskAiTab(
    repository: SaheliRepository,
    profile: UserProfile,
    settings: ModelSettings,
    messages: MutableList<ChatMessage>
) {
    ChatSurface(
        repository = repository,
        profile = profile,
        settings = settings,
        mode = AgentMode.General,
        title = LocalizedText.t(profile.language, "askAnything"),
        subtitle = "Private counselling, health, rights, images, and voice",
        suggestions = listOf("I feel unsafe", "STI symptoms", "Police stopped me", "Explain a medicine", "I feel anxious"),
        messages = messages
    )
}

@Composable
private fun ChatSurface(
    repository: SaheliRepository,
    profile: UserProfile,
    settings: ModelSettings,
    mode: AgentMode,
    title: String,
    subtitle: String,
    suggestions: List<String>,
    messages: MutableList<ChatMessage>
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var attachedImage by remember { mutableStateOf<ImageAttachment?>(null) }
    var loading by remember { mutableStateOf(false) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(profile.language) {
        val engine = TextToSpeech(context) { }
        engine.language = Locale(profile.language.code)
        tts = engine
        onDispose { engine.shutdown() }
    }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) attachedImage = repository.bitmapAttachment(bitmap)
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) scope.launch { attachedImage = repository.uriAttachment(uri) }
    }
    val speech = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!text.isNullOrBlank()) input = text
    }

    fun send(text: String) {
        val prompt = text.trim()
        if (prompt.isBlank() && attachedImage == null) return
        val userText = prompt.ifBlank { "Please explain this image." }
        messages.add(ChatMessage(ChatRole.User, userText, attachedImage?.label))
        input = ""
        val imageNote = attachedImage
        attachedImage = null
        loading = true
        scope.launch {
            val answer = repository.ask(userText, profile, settings, imageNote, messages, mode)
            messages.add(ChatMessage(ChatRole.Assistant, answer))
            loading = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        Header(title, subtitle, trailing = { AiModePill(repository) })
        LazyColumn(Modifier.weight(1f).padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 10.dp)) {
                    suggestions.forEach {
                        FilterChip(selected = false, onClick = { input = it }, label = { Text(it) })
                    }
                }
            }
            items(messages) { message ->
                ChatBubble(message) {
                    if (message.role == ChatRole.Assistant) tts?.speak(markdownToSpeechText(message.text), TextToSpeech.QUEUE_FLUSH, null, "saheli")
                }
            }
            if (loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        }
        Column(
            Modifier
                .background(Color.White)
                .border(1.dp, Border)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            attachedImage?.let {
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(BlueSoft).padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Image, null, tint = Blue)
                    Spacer(Modifier.size(8.dp))
                    Text(it.label, color = Blue, modifier = Modifier.weight(1f))
                    Text("Remove", color = Blue, modifier = Modifier.clickable { attachedImage = null })
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Warm)
                    .border(1.dp, Border, RoundedCornerShape(26.dp))
                    .padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                    IconButton(onClick = { gallery.launch("image/*") }, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.Image, "Upload image", tint = Blue, modifier = Modifier.size(21.dp)) }
                    IconButton(onClick = {
                        cameraPermission.launch(Manifest.permission.CAMERA)
                        camera.launch(null)
                    }, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.AddAPhoto, "Capture image", tint = Amber, modifier = Modifier.size(21.dp)) }
                    IconButton(onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, profile.language.code)
                        }
                        speech.launch(intent)
                    }, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.Mic, "Voice input", tint = Coral, modifier = Modifier.size(21.dp)) }
                }
                OutlinedTextField(
                    input,
                    { input = it },
                    placeholder = { Text(LocalizedText.t(profile.language, "chatPlaceholder"), fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    minLines = 1,
                    maxLines = 3,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                IconButton(
                    onClick = { send(input) },
                    modifier = Modifier.size(42.dp).clip(CircleShape).background(Teal)
                ) { Icon(Icons.Default.Send, "Send", tint = Color.White, modifier = Modifier.size(21.dp)) }
            }
        }
    }
}

@Composable
private fun AiModePill(repository: SaheliRepository) {
    val local = repository.shouldUseLocal()
    Row(
        Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (local) Mint else BlueSoft)
            .border(1.dp, if (local) Teal.copy(alpha = 0.22f) else Blue.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(if (local) Icons.Default.Memory else Icons.Default.Cloud, null, tint = if (local) Teal else Blue, modifier = Modifier.size(18.dp))
        Column {
            Text(if (local) "Private AI" else "Secure NGO AI", color = if (local) Teal else Blue, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, lineHeight = 12.sp)
            Text(if (local) "On device" else "Encrypted server", color = Muted, fontSize = 9.sp, lineHeight = 10.sp)
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage, onSpeak: () -> Unit) {
    val isUser = message.role == ChatRole.User
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start) {
        Card(
            colors = CardDefaults.cardColors(if (isUser) Teal else Color.White),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            modifier = Modifier.fillMaxWidth(0.88f).border(1.dp, if (isUser) Teal else Border, RoundedCornerShape(18.dp))
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                message.imageLabel?.let {
                    Text("Image: $it", color = if (isUser) Color.White.copy(alpha = 0.8f) else Blue, style = MaterialTheme.typography.labelMedium)
                }
                MarkdownText(message.text, color = if (isUser) Color.White else Ink)
                if (!isUser) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onSpeak)) {
                        Icon(Icons.Default.VolumeUp, "Speak response", tint = Teal, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Listen", color = Teal, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun MarkdownText(text: String, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        text.lines()
            .joinToString("\n")
            .split("\n\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .forEach { block ->
                val lines = block.lines()
                if (lines.size > 1 && lines.all { it.trimStart().startsWith("- ") || Regex("""^\d+\.\s+""").containsMatchIn(it.trimStart()) }) {
                    lines.forEach { line ->
                        val clean = line.trimStart()
                            .removePrefix("- ")
                            .replace(Regex("""^\d+\.\s+"""), "")
                        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text("•", color = color, fontSize = 15.sp, lineHeight = 22.sp)
                            Text(markdownInline(clean), color = color, fontSize = 15.sp, lineHeight = 22.sp, modifier = Modifier.weight(1f))
                        }
                    }
                } else {
                    Text(markdownInline(block), color = color, fontSize = 15.sp, lineHeight = 22.sp)
                }
            }
    }
}

private fun markdownInline(raw: String) = buildAnnotatedString {
    val cleaned = raw
        .replace(Regex("""^#{1,6}\s*"""), "")
        .replace(Regex("""^\s*[-*+]\s+"""), "")
        .replace(Regex("""^\s*\d+\.\s+"""), "")
        .replace(Regex("""\[(.*?)\]\((.*?)\)"""), "$1")
    var i = 0
    while (i < cleaned.length) {
        val boldStart = cleaned.indexOf("**", i)
        if (boldStart == -1) {
            append(cleaned.substring(i).replace("`", "").replace("*", ""))
            break
        }
        append(cleaned.substring(i, boldStart).replace("`", "").replace("*", ""))
        val boldEnd = cleaned.indexOf("**", boldStart + 2)
        if (boldEnd == -1) {
            append(cleaned.substring(boldStart).replace("`", "").replace("*", ""))
            break
        }
        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
            append(cleaned.substring(boldStart + 2, boldEnd).replace("`", "").replace("*", ""))
        }
        i = boldEnd + 2
    }
}

private fun markdownToSpeechText(raw: String): String = raw
    .replace(Regex("""```[\s\S]*?```"""), " ")
    .replace(Regex("""`([^`]*)`"""), "$1")
    .replace(Regex("""\[(.*?)\]\((.*?)\)"""), "$1")
    .replace(Regex("""^#{1,6}\s*""", RegexOption.MULTILINE), "")
    .replace(Regex("""^\s*[-*+]\s+""", RegexOption.MULTILINE), "")
    .replace(Regex("""^\s*\d+\.\s+""", RegexOption.MULTILINE), "")
    .replace("**", "")
    .replace("__", "")
    .replace("*", "")
    .replace("#", "")
    .replace(Regex("""\n{3,}"""), "\n\n")
    .trim()

@Composable
private fun RightsTab(profile: UserProfile, openAsk: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Header(LocalizedText.t(profile.language, "rights"), "Plain-language guidance for ${profile.state}")
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(
                "Dignity and protection" to "No one has the right to beat, threaten, extort, or sexually assault you. Document incidents only when it is safe.",
                "Healthcare without judgement" to "Clinics should provide treatment, STI testing, contraception, and emergency care respectfully.",
                "Police interactions" to "Ask for names, station details, and a woman officer where applicable. Do not sign a blank paper.",
                "Documents and schemes" to "Aadhaar, voter ID, ration card, bank account, and Ayushman/health schemes can unlock benefits. NGO workers can help with paperwork."
            ).forEachIndexed { index, pair ->
                Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(8.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${index + 1}. ${pair.first}", fontWeight = FontWeight.SemiBold)
                        Text(pair.second, color = Muted)
                    }
                }
            }
            Button(onClick = openAsk, colors = ButtonDefaults.buttonColors(containerColor = Purple)) {
                Icon(Icons.Default.Gavel, null)
                Spacer(Modifier.size(8.dp))
                Text("Ask Saheli about my situation")
            }
        }
    }
}

@Composable
private fun SupportTab(profile: UserProfile) {
    val context = LocalContext.current
    var selectedSupport by remember { mutableStateOf<SupportSheet?>(null) }
    Column(Modifier.fillMaxSize()) {
        Header(LocalizedText.t(profile.language, "support"), "Counselling, NGO handoff, and peer help")
        val items = listOf(
            SupportSheet.Counsellors to Triple("Talk to a counsellor", LocalizedText.t(profile.language, "helpCounsellors"), Coral),
            SupportSheet.Clinics to Triple("Find a clinic", LocalizedText.t(profile.language, "helpClinics"), Teal),
            SupportSheet.Documents to Triple("Documents help", LocalizedText.t(profile.language, "helpDocuments"), Blue),
            SupportSheet.LegalAid to Triple(LocalizedText.t(profile.language, "helpLegal"), "Women helpline, emergency, legal support.", Purple)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(14.dp),
            state = rememberLazyGridState(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { (sheet, data) ->
                val (title, subtitle, color) = data
                Card(
                    colors = CardDefaults.cardColors(Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(170.dp)
                        .border(1.dp, Border, RoundedCornerShape(16.dp))
                        .clickable { selectedSupport = sheet }
                ) {
                    Column(
                        Modifier.fillMaxSize().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(Modifier.size(42.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Campaign, null, tint = color, modifier = Modifier.size(22.dp))
                        }
                        Text(title, fontWeight = FontWeight.SemiBold, color = Ink, fontSize = 15.sp, lineHeight = 18.sp)
                        Text(subtitle, color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
                    }
                }
            }
        }
    }
    selectedSupport?.let { sheet ->
        SupportDialog(
            sheet = sheet,
            profile = profile,
            onDismiss = { selectedSupport = null },
            onCall = { phone ->
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
            }
        )
    }
}

private enum class SupportSheet { Counsellors, Clinics, Documents, LegalAid }

@Composable
private fun SupportDialog(
    sheet: SupportSheet,
    profile: UserProfile,
    onDismiss: () -> Unit,
    onCall: (String) -> Unit
) {
    val title = when (sheet) {
        SupportSheet.Counsellors -> LocalizedText.t(profile.language, "helpCounsellors")
        SupportSheet.Clinics -> LocalizedText.t(profile.language, "helpClinics")
        SupportSheet.Documents -> LocalizedText.t(profile.language, "helpDocuments")
        SupportSheet.LegalAid -> LocalizedText.t(profile.language, "helpLegal")
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text(LocalizedText.t(profile.language, "close")) } },
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text(
                        LocalizedText.t(profile.language, "privacyHelp"),
                        color = Muted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
                when (sheet) {
                    SupportSheet.Counsellors -> items(counsellorItems(profile.language)) { item -> SupportContactRow(item, profile.language, onCall) }
                    SupportSheet.Clinics -> items(clinicItems(profile)) { item -> SupportInfoRow(item.title, item.subtitle, item.phone, profile.language, onCall) }
                    SupportSheet.Documents -> items(documentItems(profile.language)) { item -> SupportInfoRow(item.title, item.subtitle, null, profile.language, onCall) }
                    SupportSheet.LegalAid -> items(legalAidItems(profile.language)) { item -> SupportContactRow(item, profile.language, onCall) }
                }
            }
        }
    )
}

private data class SupportContact(val title: String, val subtitle: String, val phone: String)
private data class SupportInfo(val title: String, val subtitle: String, val phone: String? = null)

@Composable
private fun SupportContactRow(item: SupportContact, language: com.example.saheli.core.Language, onCall: (String) -> Unit) {
    SupportInfoRow(item.title, item.subtitle, item.phone, language, onCall)
}

@Composable
private fun SupportInfoRow(title: String, subtitle: String, phone: String?, language: com.example.saheli.core.Language, onCall: (String) -> Unit) {
    Card(colors = CardDefaults.cardColors(Warm), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = Ink, fontSize = 14.sp)
            Text(subtitle, color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
            phone?.let {
                TextButton(onClick = { onCall(it) }, modifier = Modifier.padding(0.dp)) {
                    Text("${LocalizedText.t(language, "call")} $it")
                }
            }
        }
    }
}

private fun counsellorItems(language: com.example.saheli.core.Language): List<SupportContact> = when (language) {
    com.example.saheli.core.Language.Hindi -> listOf(
        SupportContact("Tele-MANAS", "मुफ्त 24x7 सरकारी मानसिक स्वास्थ्य सहायता।", "14416"),
        SupportContact("Tele-MANAS दूसरा नंबर", "टोल-फ्री मानसिक स्वास्थ्य सहायता।", "18008914416"),
        SupportContact("KIRAN हेल्पलाइन", "भारतीय भाषाओं में मानसिक स्वास्थ्य सहायता।", "18005990019"),
        SupportContact("महिला हेल्पलाइन", "महिलाओं की सुरक्षा और सहायता के लिए।", "181")
    )
    else -> listOf(
        SupportContact("Tele-MANAS", "Free 24x7 government mental health support.", "14416"),
        SupportContact("Tele-MANAS alternate", "Toll-free mental health support.", "18008914416"),
        SupportContact("KIRAN mental health helpline", "Mental health support across Indian languages.", "18005990019"),
        SupportContact("Women helpline", "For women needing support or protection.", "181")
    )
}

private fun legalAidItems(language: com.example.saheli.core.Language): List<SupportContact> = when (language) {
    com.example.saheli.core.Language.Hindi -> listOf(
        SupportContact("आपातकालीन सहायता", "पुलिस, आग, एम्बुलेंस या तुरंत खतरा।", "112"),
        SupportContact("महिला हेल्पलाइन", "हिंसा, उत्पीड़न या सुरक्षा सहायता।", "181"),
        SupportContact("राष्ट्रीय AIDS हेल्पलाइन", "HIV/STI जानकारी और रेफरल सहायता।", "1097"),
        SupportContact("कानूनी सहायता", "जहाँ उपलब्ध हो, NALSA कानूनी मदद।", "15100")
    )
    else -> listOf(
        SupportContact("Emergency response", "Police, fire, ambulance and urgent danger.", "112"),
        SupportContact("Women helpline", "Violence, harassment, family or public safety support.", "181"),
        SupportContact("National AIDS helpline", "HIV/STI information and referral support.", "1097"),
        SupportContact("Legal aid helpline", "NALSA legal aid support where available.", "15100")
    )
}

private fun documentItems(language: com.example.saheli.core.Language): List<SupportInfo> = when (language) {
    com.example.saheli.core.Language.Hindi -> listOf(
        SupportInfo("Aadhaar", "आधार नंबर/कार्ड, लिंक फोन और पता अपडेट का सबूत रखें।"),
        SupportInfo("Voter ID", "पहचान और पते के लिए उपयोगी। नाम, उम्र और पता ठीक से जांचें।"),
        SupportInfo("Ration card", "खाद्य सहायता के लिए। सदस्य जोड़ने या सुधार के लिए NGO/CSC से पूछें।"),
        SupportInfo("Bank account / Jan Dhan", "फोटो ID, मोबाइल नंबर और पता प्रमाण मदद करते हैं।"),
        SupportInfo("Health cover", "Ayushman Bharat या राज्य स्वास्थ्य योजना के बारे में पूछें।"),
        SupportInfo("कॉपी सुरक्षित रखें", "फोटो/PDF लॉक फोल्डर में रखें। असुरक्षित लोगों से ID शेयर न करें।")
    )
    else -> listOf(
        SupportInfo("Aadhaar", "Keep Aadhaar number/card, linked phone if available, and address/update proof."),
        SupportInfo("Voter ID", "Useful for local identity and address. Check name, age, and address spelling."),
        SupportInfo("Ration card", "For food support. Ask local NGO/CSC about adding family members or correcting details."),
        SupportInfo("Bank account / Jan Dhan", "Photo ID, mobile number, and address proof help. Zero-balance account may be possible."),
        SupportInfo("Health cover", "Ask about Ayushman Bharat/state health schemes and required ID/address documents."),
        SupportInfo("Keep copies private", "Save photos/PDFs in a locked folder if possible. Do not share IDs with unsafe people.")
    )
}

private fun clinicItems(profile: UserProfile): List<SupportInfo> {
    val city = profile.city.lowercase(Locale.US)
    return when {
        "delhi" in city -> listOf(
            SupportInfo("Delhi government hospital / STI clinic", "Use nearest govt hospital dermatology/STI/ART/ICTC service. Ask for confidential STI/HIV testing."),
            SupportInfo("Delhi State AIDS Control Society linked ICTC/ART", "For HIV testing, counselling, ART referral and STI support."),
            SupportInfo("Mohalla Clinic / Urban PHC", "For first-line care, fever, pain, pregnancy test referral, and basic medicines."),
            SupportInfo("Trusted NGO outreach worker", "Best option if you fear clinic judgement. They can accompany you.")
        )
        "mumbai" in city || "maharashtra" in profile.state.lowercase(Locale.US) -> listOf(
            SupportInfo("BMC / government hospital STI clinic", "Ask for STI/HIV testing, contraception, pregnancy care, or ART/ICTC referral."),
            SupportInfo("Maharashtra State AIDS Control Society linked services", "For HIV/STI counselling, testing, ART and referral support."),
            SupportInfo("Urban Health Post / UPHC", "For basic care and referral to a larger hospital if needed."),
            SupportInfo("Trusted NGO outreach worker", "Useful if you want someone to go with you.")
        )
        else -> listOf(
            SupportInfo("Nearest government hospital", "Ask for STI/HIV testing, contraception, pregnancy care, or emergency care."),
            SupportInfo("ICTC / ART centre", "For HIV testing, counselling and treatment referral. Available through many government hospitals."),
            SupportInfo("Urban PHC / CHC", "For basic care and referral. Good first step if a hospital feels difficult."),
            SupportInfo("Sex worker-led NGO / outreach worker", "Best privacy-first route when available. They can help with clinic navigation.")
        )
    }
}

@Composable
private fun SettingsTab(
    repository: SaheliRepository,
    profile: UserProfile,
    settings: ModelSettings,
    onProfile: (UserProfile) -> Unit,
    onSettings: (ModelSettings) -> Unit,
    onResetProfile: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("") }
    var progress by remember { mutableFloatStateOf(0f) }
    var showResetConfirm by remember { mutableStateOf(false) }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            status = "Importing model..."
            status = repository.importModel(uri)
        }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Header(LocalizedText.t(profile.language, "settings"), "Language, profile, private chat, and reset")
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(8.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(LocalizedText.t(profile.language, "changeLanguage"), fontWeight = FontWeight.SemiBold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        com.example.saheli.core.Language.entries.forEach {
                            FilterChip(selected = profile.language == it, onClick = { onProfile(profile.copy(language = it)) }, label = { Text(it.englishName) })
                        }
                    }
                    OutlinedTextField(profile.name, { onProfile(profile.copy(name = it)) }, label = { Text("Name or pseudonym") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(profile.city, { onProfile(profile.copy(city = it)) }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
                }
            }
            Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(8.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Private chat", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = repository.shouldUseLocal(), onClick = {}, label = { Text("On-device") })
                        FilterChip(selected = !repository.shouldUseLocal(), onClick = {}, label = { Text("Secure server") })
                    }
                    Text("Saheli chooses the private option that works best on this phone. You do not need to set anything.", color = Muted, fontSize = 14.sp, lineHeight = 20.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = {
                            scope.launch {
                                status = LocalizedText.t(profile.language, "preparing")
                                runCatching { repository.prepareAi { progress = it } }
                                    .onSuccess { status = it }
                                    .onFailure { status = "Setup failed: ${it.message}" }
                            }
                        }, colors = ButtonDefaults.buttonColors(containerColor = Teal), modifier = Modifier.weight(1f)) {
                            Text(LocalizedText.t(profile.language, "preparing"))
                        }
                        Button(onClick = { importer.launch(arrayOf("*/*")) }, colors = ButtonDefaults.buttonColors(containerColor = Blue), modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.FolderOpen, null)
                            Spacer(Modifier.size(8.dp))
                            Text(LocalizedText.t(profile.language, "importOptional"))
                        }
                    }
                    if (progress in 0.01f..0.99f) LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    if (status.isNotBlank()) Text(status, color = if (status.startsWith("Setup failed")) Coral else Teal)
                }
            }
            Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(8.dp), modifier = Modifier.border(1.dp, Border, RoundedCornerShape(8.dp))) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.RestartAlt, null, tint = Coral)
                        Column(Modifier.weight(1f)) {
                            Text("Reset profile", fontWeight = FontWeight.SemiBold, color = Ink)
                            Text("Clears name, city, language and interests. Onboarding will open next time.", color = Muted, fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                    Button(
                        onClick = { showResetConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Coral),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset profile only")
                    }
                }
            }
        }
    }
    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset profile?") },
            text = { Text("This only resets your Saheli profile and onboarding. It does not delete the app or model file.") },
            confirmButton = {
                TextButton(onClick = {
                    showResetConfirm = false
                    onResetProfile()
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
