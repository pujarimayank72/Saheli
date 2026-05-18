@file:OptIn(ExperimentalLayoutApi::class)

package com.example.saheli.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.saheli.R
import com.example.saheli.core.Language
import com.example.saheli.core.LocalizedText
import com.example.saheli.core.ModelSettings
import com.example.saheli.core.SaheliRepository
import com.example.saheli.core.UserProfile
import kotlinx.coroutines.launch

private val Teal = Color(0xFF137F70)
private val Mint = Color(0xFFE6F5EF)
private val Coral = Color(0xFFFF755F)
private val Warm = Color(0xFFFAF8F5)

@Composable
fun OnboardingScreen(repository: SaheliRepository, onFinished: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var profile by remember { mutableStateOf(repository.loadProfile()) }
    var settings by remember { mutableStateOf(repository.loadModelSettings()) }
    var status by remember { mutableStateOf("") }
    var progress by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    Surface(Modifier.fillMaxSize(), color = Warm) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(
                    painter = painterResource(R.drawable.saheli_logo),
                    contentDescription = "Saheli logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                )
                Column {
                    Text("Saheli", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, color = Teal)
                    Text("Your privacy. Your voice. Your strength.", color = Color(0xFF45615E))
                }
            }

            StepDots(step)

            when (step) {
                0 -> LanguageStep(profile.language) { profile = profile.copy(language = it) }
                1 -> ProfileStep(profile) { profile = it }
                else -> ModelStep(
                    repository = repository,
                    profile = profile,
                    status = status,
                    progress = progress,
                    onPrepare = {
                        scope.launch {
                            status = LocalizedText.t(profile.language, "preparing")
                            runCatching {
                                repository.prepareAi { progress = it }
                            }.onSuccess { status = it }
                                .onFailure { status = "Setup failed: ${it.message}" }
                        }
                    }
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (step > 0) {
                    Button(
                        onClick = { step-- },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Teal),
                        modifier = Modifier.weight(1f)
                    ) { Text("Back") }
                }
                Button(
                    onClick = {
                        if (step < 2) {
                            step++
                        } else {
                            repository.saveProfile(profile)
                            repository.saveModelSettings(settings.copy(onboardingDone = true))
                            onFinished()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal),
                    modifier = Modifier.weight(1f)
                ) { Text(if (step < 2) "Continue" else "Open Saheli") }
            }
        }
    }
}

@Composable
private fun StepDots(step: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Language", "Profile", "Gemma").forEachIndexed { index, label ->
            Surface(
                color = if (index == step) Teal else Color.White,
                contentColor = if (index == step) Color.White else Color(0xFF667085),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.border(1.dp, if (index == step) Teal else Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
            ) {
                Text(label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun LanguageStep(selected: Language, onSelect: (Language) -> Unit) {
    Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(Icons.Default.Language, contentDescription = null, tint = Teal)
            Text("Choose your language", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("English is included, and you can switch languages later in Settings.", color = Color(0xFF667085))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Language.entries.forEach { language ->
                    val picked = language == selected
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (picked) Mint else Warm)
                            .border(1.dp, if (picked) Teal else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .clickable { onSelect(language) }
                            .padding(14.dp)
                    ) {
                        Text(language.englishName, color = if (picked) Teal else Color(0xFF667085), style = MaterialTheme.typography.labelMedium)
                        Text(language.nativeName, color = if (picked) Teal else Color(0xFF1C1C1E), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Coral)
                Text("Voice prompts and text-to-speech are available in chat.", color = Color(0xFF667085))
            }
        }
    }
}

@Composable
private fun ProfileStep(profile: UserProfile, onChange: (UserProfile) -> Unit) {
    Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Teal)
            Text("Tell Saheli what helps you", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Teal, modifier = Modifier.size(18.dp))
                Text("This stays on your phone.", color = Color(0xFF667085))
            }
            OutlinedTextField(profile.name, { onChange(profile.copy(name = it)) }, label = { Text("Name or pseudonym") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(profile.ageRange, { onChange(profile.copy(ageRange = it)) }, label = { Text("Age range") }, modifier = Modifier.weight(1f))
                OutlinedTextField(profile.city, { onChange(profile.copy(city = it)) }, label = { Text("City") }, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(profile.state, { onChange(profile.copy(state = it)) }, label = { Text("State") }, modifier = Modifier.fillMaxWidth())
            Text("What should Saheli prioritize?", fontWeight = FontWeight.Medium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Health", "Rights", "Counselling", "Schemes", "Documents", "Peer support").forEach { item ->
                    val selected = item in profile.interests
                    FilterChip(
                        selected = selected,
                        onClick = {
                            val next = if (selected) profile.interests - item else profile.interests + item
                            onChange(profile.copy(interests = next))
                        },
                        label = { Text(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelStep(
    repository: SaheliRepository,
    profile: UserProfile,
    status: String,
    progress: Float,
    onPrepare: () -> Unit
) {
    val ram = repository.totalRamGb()
    LaunchedEffect(Unit) { onPrepare() }
    Card(colors = CardDefaults.cardColors(Color.White), shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(Icons.Default.Memory, contentDescription = null, tint = Teal)
            Text(LocalizedText.t(profile.language, "preparing"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("RAM: ${"%.1f".format(ram)} GB. ${LocalizedText.t(profile.language, "modelAutomatic")}", color = Color(0xFF667085))
            if (progress in 0.01f..0.99f) LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            Text(status.ifBlank { LocalizedText.t(profile.language, "autoSetup") }, color = if (repository.hasLocalModel() || !repository.shouldUseLocal()) Teal else Color(0xFF667085))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Favorite, null, tint = Coral)
                Text(LocalizedText.t(profile.language, "modelAutomatic"), color = Color(0xFF667085))
            }
        }
    }
}
