package com.example.saheli.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.saheli.core.SaheliRepository
import com.example.saheli.ui.screens.DashboardScreen
import com.example.saheli.ui.screens.OnboardingScreen

@Composable
fun SaheliNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val repository = remember { SaheliRepository(context.applicationContext) }
    val start = if (repository.loadModelSettings().onboardingDone) "dashboard" else "onboarding"

    NavHost(navController = navController, startDestination = start) {
        composable("onboarding") {
            OnboardingScreen(repository = repository, onFinished = {
                navController.navigate("dashboard") {
                    popUpTo("onboarding") { inclusive = true }
                }
            })
        }
        composable("dashboard") {
            DashboardScreen(repository = repository)
        }
    }
}
