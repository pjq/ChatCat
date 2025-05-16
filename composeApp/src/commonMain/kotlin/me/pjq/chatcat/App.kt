package me.pjq.chatcat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import me.pjq.chatcat.di.AppModule
import me.pjq.chatcat.ui.navigation.AppNavigation
import moe.tlaster.precompose.PreComposeApp

@Composable
fun App() {
    // Initialize language manager
    LaunchedEffect(Unit) {
        // Access the languageManager to trigger its initialization
        AppModule.languageManager
    }
    
    PreComposeApp {
        AppNavigation()
    }
}
