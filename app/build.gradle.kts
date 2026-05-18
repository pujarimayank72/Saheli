import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun localSecret(propertyName: String, environmentName: String, fallback: String = ""): String {
    val raw = localProperties.getProperty(propertyName)
        ?: System.getenv(environmentName)
        ?: fallback
    return raw.replace("\\", "\\\\").replace("\"", "\\\"")
}

android {
    namespace = "com.example.saheli"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.saheli"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "SAHELI_OLLAMA_URL",
            "\"${localSecret("saheli.ollama.url", "SAHELI_OLLAMA_URL")}\""
        )
        buildConfigField(
            "String",
            "SAHELI_OLLAMA_KEY",
            "\"${localSecret("saheli.ollama.key", "SAHELI_OLLAMA_KEY")}\""
        )
        buildConfigField(
            "String",
            "SAHELI_OLLAMA_MODEL",
            "\"${localSecret("saheli.ollama.model", "SAHELI_OLLAMA_MODEL", "gemma4:e2b")}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.litertlm.android)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.mlkit.translate)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
