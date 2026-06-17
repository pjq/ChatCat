package me.pjq.chatcat.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import me.pjq.chatcat.model.Accent
import me.pjq.chatcat.model.Density
import me.pjq.chatcat.model.FontSize
import me.pjq.chatcat.model.Theme

/** Curated accent palettes — each one yields its own light + dark color scheme. */
private data class Palette(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val tertiary: Color
)

private val palettes: Map<Accent, Palette> = mapOf(
    Accent.INDIGO to Palette(
        primary = Color(0xFF5B6CFF),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE0E3FF),
        onPrimaryContainer = Color(0xFF0E1B6B),
        secondary = Color(0xFF6B6F8A),
        tertiary = Color(0xFF845EC2)
    ),
    Accent.ROSE to Palette(
        primary = Color(0xFFE5446D),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFE0E8),
        onPrimaryContainer = Color(0xFF591127),
        secondary = Color(0xFF9C5C7C),
        tertiary = Color(0xFFFF8FA3)
    ),
    Accent.EMERALD to Palette(
        primary = Color(0xFF1F9E73),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD3F4E5),
        onPrimaryContainer = Color(0xFF013A26),
        secondary = Color(0xFF4F7868),
        tertiary = Color(0xFF45C4B0)
    ),
    Accent.AMBER to Palette(
        primary = Color(0xFFE08914),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFE9C8),
        onPrimaryContainer = Color(0xFF3F2200),
        secondary = Color(0xFF8A6A2C),
        tertiary = Color(0xFFCB6C19)
    ),
    Accent.OCEAN to Palette(
        primary = Color(0xFF0E7CB7),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD3ECFB),
        onPrimaryContainer = Color(0xFF002338),
        secondary = Color(0xFF4F6F84),
        tertiary = Color(0xFF35B6CC)
    )
)

private fun lightSchemeFor(accent: Accent): ColorScheme {
    val p = palettes.getValue(accent)
    return lightColorScheme(
        primary = p.primary,
        onPrimary = p.onPrimary,
        primaryContainer = p.primaryContainer,
        onPrimaryContainer = p.onPrimaryContainer,
        secondary = p.secondary,
        onSecondary = Color.White,
        tertiary = p.tertiary,
        onTertiary = Color.White,
        background = Color(0xFFFBFAFE),
        onBackground = Color(0xFF1A1B22),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1A1B22),
        surfaceVariant = Color(0xFFEEEFF7),
        onSurfaceVariant = Color(0xFF464956),
        surfaceTint = p.primary,
        outline = Color(0xFFB0B3C2),
        outlineVariant = Color(0xFFD8DAE4),
        error = Color(0xFFB42318),
        onError = Color.White,
        errorContainer = Color(0xFFFEE4E2),
        onErrorContainer = Color(0xFF7A271A)
    )
}

private fun darkSchemeFor(accent: Accent): ColorScheme {
    val p = palettes.getValue(accent)
    return darkColorScheme(
        primary = p.primary,
        onPrimary = p.onPrimary,
        primaryContainer = p.primary.copy(alpha = 0.18f),
        onPrimaryContainer = Color(0xFFE3E5FF),
        secondary = p.secondary,
        onSecondary = Color.Black,
        tertiary = p.tertiary,
        onTertiary = Color.Black,
        background = Color(0xFF0F1014),
        onBackground = Color(0xFFEDEEF5),
        surface = Color(0xFF15171D),
        onSurface = Color(0xFFEDEEF5),
        surfaceVariant = Color(0xFF1E2129),
        onSurfaceVariant = Color(0xFFB6B9C7),
        surfaceTint = p.primary,
        outline = Color(0xFF40434F),
        outlineVariant = Color(0xFF2B2E37),
        error = Color(0xFFF97066),
        onError = Color.Black,
        errorContainer = Color(0xFF7A271A),
        onErrorContainer = Color(0xFFFEE4E2)
    )
}

val LocalFontScale = compositionLocalOf { 1f }
val LocalDensitySetting = compositionLocalOf { Density.COMFORTABLE }

@Composable
fun ChatCatTheme(
    themeMode: Theme = Theme.SYSTEM,
    accent: Accent = Accent.INDIGO,
    fontSize: FontSize = FontSize.MEDIUM,
    density: Density = Density.COMFORTABLE,
    dynamicColorScheme: ColorScheme? = null,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        Theme.LIGHT -> false
        Theme.DARK -> true
        Theme.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = dynamicColorScheme ?: if (isDark) darkSchemeFor(accent) else lightSchemeFor(accent)
    val scale = when (fontSize) {
        FontSize.SMALL -> 0.9f
        FontSize.MEDIUM -> 1f
        FontSize.LARGE -> 1.12f
        FontSize.EXTRA_LARGE -> 1.24f
    }
    CompositionLocalProvider(LocalFontScale provides scale, LocalDensitySetting provides density) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = chatCatTypography(scale),
            shapes = chatCatShapes(),
            content = content
        )
    }
}

private fun chatCatTypography(scale: Float): Typography {
    val base = Typography()
    fun TextStyle.scaled() = copy(fontSize = (fontSize.value * scale).sp)
    return base.copy(
        displayLarge = base.displayLarge.scaled(),
        displayMedium = base.displayMedium.scaled(),
        headlineLarge = base.headlineLarge.scaled().copy(fontWeight = FontWeight.SemiBold),
        headlineMedium = base.headlineMedium.scaled().copy(fontWeight = FontWeight.SemiBold),
        headlineSmall = base.headlineSmall.scaled().copy(fontWeight = FontWeight.SemiBold),
        titleLarge = base.titleLarge.scaled().copy(fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.scaled().copy(fontWeight = FontWeight.Medium),
        titleSmall = base.titleSmall.scaled().copy(fontWeight = FontWeight.Medium),
        bodyLarge = base.bodyLarge.scaled(),
        bodyMedium = base.bodyMedium.scaled(),
        bodySmall = base.bodySmall.scaled(),
        labelLarge = base.labelLarge.scaled().copy(fontWeight = FontWeight.Medium),
        labelMedium = base.labelMedium.scaled(),
        labelSmall = base.labelSmall.scaled()
    )
}

private fun chatCatShapes(): Shapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
