package me.pjq.chatcat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import me.pjq.chatcat.ui.screens.ChatScreen
import me.pjq.chatcat.ui.screens.McpServersScreen
import me.pjq.chatcat.ui.screens.ProviderEditorScreen
import me.pjq.chatcat.ui.screens.SettingsScreen
import me.pjq.chatcat.ui.theme.ChatCatTheme
import me.pjq.chatcat.ui.theme.rememberDynamicColorScheme
import me.pjq.chatcat.viewmodel.ChatViewModel
import me.pjq.chatcat.viewmodel.SettingsViewModel
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.path
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition

@Composable
fun AppNavigation() {
    val navigator = rememberNavigator()
    val chatViewModel = remember { ChatViewModel() }
    val settingsViewModel = remember { SettingsViewModel() }
    val settingsState by settingsViewModel.uiState.collectAsState()
    val prefs = settingsState.preferences

    val isDark = when (prefs.theme) {
        me.pjq.chatcat.model.Theme.LIGHT -> false
        me.pjq.chatcat.model.Theme.DARK -> true
        me.pjq.chatcat.model.Theme.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    val dynamicScheme = rememberDynamicColorScheme(useDarkTheme = isDark, enabled = prefs.useDynamicColor)

    ChatCatTheme(
        themeMode = prefs.theme,
        accent = prefs.accent,
        fontSize = prefs.fontSize,
        density = prefs.density,
        dynamicColorScheme = dynamicScheme
    ) {
        NavHost(
            navigator = navigator,
            initialRoute = Route.CHAT,
            navTransition = NavTransition()
        ) {
            scene(route = Route.CHAT) {
                ChatScreen(
                    viewModel = chatViewModel,
                    onNavigateToSettings = { navigator.navigate(Route.SETTINGS) },
                    onNavigateToMcp = { navigator.navigate(Route.MCP) }
                )
            }
            scene(route = Route.SETTINGS) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { navigator.goBack() },
                    onNavigateToMcp = { navigator.navigate(Route.MCP) },
                    onAddProvider = { navigator.navigate(Route.providerEdit(null)) },
                    onEditProvider = { providerId -> navigator.navigate(Route.providerEdit(providerId)) }
                )
            }
            scene(route = Route.MCP) {
                McpServersScreen(
                    viewModel = settingsViewModel,
                    onNavigateBack = { navigator.goBack() }
                )
            }
            scene(route = "${Route.PROVIDER_EDIT}/{providerId}") { backStackEntry ->
                val providerId: String? = backStackEntry.path<String>("providerId")?.takeIf { it != "new" }
                val initial = providerId?.let { id ->
                    settingsState.preferences.modelProviders.firstOrNull { it.id == id }
                }
                ProviderEditorScreen(
                    viewModel = settingsViewModel,
                    initial = initial,
                    onClose = { navigator.goBack() }
                )
            }
        }
    }
}

object Route {
    const val CHAT = "chat"
    const val SETTINGS = "settings"
    const val MCP = "mcp"
    const val PROVIDER_EDIT = "provider_edit"

    fun providerEdit(providerId: String?): String =
        "$PROVIDER_EDIT/${providerId ?: "new"}"
}
