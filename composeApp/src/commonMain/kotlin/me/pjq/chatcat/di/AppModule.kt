package me.pjq.chatcat.di

import me.pjq.chatcat.repository.ConversationRepository
import me.pjq.chatcat.repository.InMemoryConversationRepository
import me.pjq.chatcat.repository.PreferencesRepository
import me.pjq.chatcat.repository.PreferencesRepositoryImpl
import me.pjq.chatcat.service.ChatService
import me.pjq.chatcat.service.OpenAIChatService

/**
 * Application dependency injection module
 */
object AppModule {
    // Repositories
    private val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepositoryImpl()
    }
    
    private val conversationRepository: ConversationRepository by lazy {
        InMemoryConversationRepository()
    }
    
    // Services
    private val chatService: ChatService by lazy {
        OpenAIChatService(preferencesRepository)
    }
    
    // Public getters
    fun getPreferencesRepository(): PreferencesRepository = preferencesRepository
    fun getConversationRepository(): ConversationRepository = conversationRepository
    fun getChatService(): ChatService = chatService
}
