package me.pjq.chatcat.repository

import kotlinx.coroutines.flow.Flow
import me.pjq.chatcat.model.Conversation
import me.pjq.chatcat.model.Message

interface ConversationRepository {
    suspend fun getConversations(): Flow<List<Conversation>>
    suspend fun getConversation(id: String): Conversation?
    suspend fun createConversation(title: String): Conversation
    suspend fun updateConversation(conversation: Conversation): Conversation
    suspend fun deleteConversation(id: String): Boolean
    suspend fun addMessage(conversationId: String, message: Message): Conversation?
    suspend fun updateMessage(conversationId: String, message: Message): Conversation?
    suspend fun deleteMessage(conversationId: String, messageId: String): Conversation?
    suspend fun clearConversations(): Boolean
}
