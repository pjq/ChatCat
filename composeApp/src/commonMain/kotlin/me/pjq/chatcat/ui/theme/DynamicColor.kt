package me.pjq.chatcat.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
expect fun rememberDynamicColorScheme(useDarkTheme: Boolean, enabled: Boolean): ColorScheme?
