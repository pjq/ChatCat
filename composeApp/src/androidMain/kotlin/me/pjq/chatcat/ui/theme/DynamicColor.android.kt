package me.pjq.chatcat.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberDynamicColorScheme(useDarkTheme: Boolean, enabled: Boolean): ColorScheme? {
    if (!enabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    val context = LocalContext.current
    return if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
}
