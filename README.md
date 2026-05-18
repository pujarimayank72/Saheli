# Saheli

Saheli is a privacy-first Android Kotlin MVP for the Gemma 4 Good hackathon. It supports sex workers with private AI counselling, health guidance, rights information, NGO support workflows, voice input, text-to-speech, and image upload/capture for multimodal questions.

## Project Metadata

- App name: Saheli
- Android package: `com.example.saheli`
- Platform: Android Kotlin with Jetpack Compose
- AI runtime: Gemma LiteRT-LM on supported devices, with optional NGO-hosted Ollama fallback
- Current scope: MVP prototype for hackathon evaluation
- Privacy posture: profile and onboarding data stay in app-private storage; server credentials are not committed; private docs, PDFs, PRD files, and generated build outputs are ignored by git.

## MVP Features

- Jetpack Compose Android app with warm Saheli UI and the provided brand asset.
- Onboarding with English, Hindi, Bengali, Tamil, Telugu, and Marathi.
- Profile setup for pseudonym, age range, city/state, and support priorities.
- Model setup after onboarding and again in Settings.
- RAM detection: phones with 6 GB or more default to local LiteRT; lower-memory phones can use NGO Ollama.
- Local Gemma 4 LiteRT model import or download into private app storage.
- LiteRT-LM inference configured with CPU backend, 2 threads, 512 max tokens, sampler settings, and tool-calling disabled.
- Secure Ollama fallback settings: API URL, API key, and model name.
- Ask Anything chat with text, Android speech recognition, text-to-speech, gallery image upload, and camera capture.
- Offline health, rights, counselling, support, documents, and schemes guidance.
- Safety tab intentionally omitted per current MVP scope.

## Build

Copy `local.properties.example` to `local.properties` and update the SDK path for your machine. If you want to enable the NGO Ollama fallback in a local build, add the private endpoint values there or set these environment variables:

```text
SAHELI_OLLAMA_URL
SAHELI_OLLAMA_KEY
SAHELI_OLLAMA_MODEL
```

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug --no-daemon
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Model Setup

Use Settings or the onboarding Gemma step to either:

- Download a `.litertlm` Gemma 4 E2B model URL into private storage.
- Import an existing `.litertlm` model file from the device.
- Configure a secure NGO-hosted Ollama endpoint for lower-memory phones or cloud fallback.

When no model or server is configured, Saheli still provides offline static health, rights, and counselling guidance. Do not commit private server URLs, bearer tokens, PDFs, PRDs, or exported APKs.
