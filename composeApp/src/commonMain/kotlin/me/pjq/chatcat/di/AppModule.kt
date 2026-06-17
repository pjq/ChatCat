package me.pjq.chatcat.di

import com.russhwolf.settings.ExperimentalSettingsApi
import me.pjq.chatcat.i18n.LanguageManager
import me.pjq.chatcat.repository.ConversationRepository
import me.pjq.chatcat.repository.PersistentConversationRepository
import me.pjq.chatcat.repository.PersistentPreferencesRepository
import me.pjq.chatcat.repository.PreferencesRepository
import me.pjq.chatcat.repository.SettingsFactory
import me.pjq.chatcat.service.ChatService
import me.pjq.chatcat.service.ImageGenerationService
import me.pjq.chatcat.service.McpClientService
import me.pjq.chatcat.service.OpenAIChatService
import me.pjq.chatcat.service.OpenAIImageGenerationService

@OptIn(ExperimentalSettingsApi::class)
object AppModule {
    val preferencesRepository: PreferencesRepository by lazy {
        PersistentPreferencesRepository(SettingsFactory.createFlowSettings("chatcat_preferences_v2"))
    }

    val conversationRepository: ConversationRepository by lazy {
        PersistentConversationRepository(
            SettingsFactory.createSettings("chatcat_conversations_v2"),
            preferencesRepository
        )
    }

    val chatService: ChatService by lazy { OpenAIChatService(preferencesRepository) }

    val imageGenerationService: ImageGenerationService by lazy {
        OpenAIImageGenerationService(preferencesRepository)
    }

    val mcpClient: McpClientService by lazy { McpClientService() }

    val languageManager: LanguageManager by lazy { LanguageManager.getInstance(preferencesRepository) }
}
