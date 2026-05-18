package com.example.saheli

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.saheli.ui.navigation.SaheliNavigation
import com.example.saheli.ui.theme.SaheliTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SaheliTheme(darkTheme = isSystemInDarkTheme()) {
                SaheliNavigation()
            }
        }
    }
}
