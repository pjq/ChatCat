package me.pjq.chatcat.di

import com.russhwolf.settings.ExperimentalSettingsApi
import me.pjq.chatcat.i18n.LanguageManager
import me.pjq.chatcat.repository.*
import me.pjq.chatcat.service.ChatService
import me.pjq.chatcat.service.OpenAIClientChatService

/**
 * Application dependency injection module
 */
object AppModule {
    // Repositories
    @OptIn(ExperimentalSettingsApi::class)
    val preferencesRepository: PreferencesRepository by lazy {
        // Use our persistent implementation with persistent settings
        PersistentPreferencesRepository(
            SettingsFactory.createFlowSettings("chatcat_preferences")
        )
    }
    
    val conversationRepository: ConversationRepository by lazy {
        // Use our persistent implementation with persistent settings
        PersistentConversationRepository(
            SettingsFactory.createSettings("chatcat_conversations"),
            preferencesRepository
        )
    }
    
    // Services
    val chatService: ChatService by lazy {
        OpenAIClientChatService(preferencesRepository)
    }
    
    // Model service for listing available models
    val modelService by lazy {
        chatService as OpenAIClientChatService
    }
    
    // Language manager for i18n
    val languageManager by lazy {
        LanguageManager.getInstance(preferencesRepository)
    }

    val settingsViewModel: me.pjq.chatcat.viewmodel.SettingsViewModel by lazy {
        me.pjq.chatcat.viewmodel.SettingsViewModel()
    }
}
