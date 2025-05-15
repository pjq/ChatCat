package me.pjq.chatcat

import androidx.compose.runtime.Composable
import me.pjq.chatcat.ui.navigation.AppNavigation
import moe.tlaster.precompose.PreComposeApp

@Composable
fun App() {
    PreComposeApp {
        AppNavigation()
    }
}
