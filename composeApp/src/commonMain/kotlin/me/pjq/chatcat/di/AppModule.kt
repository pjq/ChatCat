package me.pjq.chatcat.di

import com.russhwolf.settings.ExperimentalSettingsApi
import me.pjq.chatcat.repository.*
import me.pjq.chatcat.service.ChatService
import me.pjq.chatcat.service.OpenAIChatService

/**
 * Application dependency injection module
 */
object AppModule {
    // Repositories
    @OptIn(ExperimentalSettingsApi::class)
    val preferencesRepository: PreferencesRepository by lazy {
        // Use our persistent implementation with the in-memory settings
        PersistentPreferencesRepository(
            SettingsFactory.createFlowSettings("chatcat_preferences")
        )
    }
    
    val conversationRepository: ConversationRepository by lazy {
        // Use our persistent implementation with the in-memory settings
        PersistentConversationRepository(
            SettingsFactory.createSettings("chatcat_conversations")
        )
    }
    
    // Services
    val chatService: ChatService by lazy {
        OpenAIChatService(preferencesRepository)
    }
}
