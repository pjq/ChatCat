package me.pjq.chatcat.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import me.pjq.chatcat.di.AppModule
import me.pjq.chatcat.ui.screens.ChatScreen
import me.pjq.chatcat.ui.screens.ModelProvidersScreen
import me.pjq.chatcat.ui.screens.SettingsScreen
import me.pjq.chatcat.ui.theme.ChatCatTheme
import me.pjq.chatcat.viewmodel.ChatViewModel
import me.pjq.chatcat.viewmodel.SettingsViewModel
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition

@Composable
fun AppNavigation() {
    val navigator = rememberNavigator()
    val chatViewModel = remember { ChatViewModel() }
    val settingsViewModel = remember { SettingsViewModel() }
    
    ChatCatTheme {
        NavHost(
            navigator = navigator,
            initialRoute = Route.CHAT,
            navTransition = NavTransition()
        ) {
            scene(
                route = Route.CHAT,
                content = {
                    ChatScreen(
                        viewModel = chatViewModel,
                            settingsViewModel = settingsViewModel,
                        onNavigateToSettings = {
                            navigator.navigate(Route.SETTINGS)
                        }
                    )
                }
            )
            
            scene(
                route = Route.SETTINGS,
                content = {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigateBack = {
                            navigator.goBack()
                        },
                        onNavigateToModelProviders = {
                            navigator.navigate(Route.MODEL_PROVIDERS)
                        }
                    )
                }
            )
            
            scene(
                route = Route.MODEL_PROVIDERS,
                content = {
                    ModelProvidersScreen(
                        viewModel = settingsViewModel,
                        onNavigateBack = {
                            navigator.goBack()
                        },
                        onNavigateToProviderSettings = { providerId ->
                            // Navigate back to settings with provider ID
                            navigator.goBack()
                        }
                    )
                }
            )
        }
    }
}

object Route {
    const val CHAT = "chat"
    const val SETTINGS = "settings"
    const val MODEL_PROVIDERS = "model_providers"
}
