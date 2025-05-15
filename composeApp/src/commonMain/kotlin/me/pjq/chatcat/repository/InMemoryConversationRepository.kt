package me.pjq.chatcat.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import me.pjq.chatcat.model.Conversation
import me.pjq.chatcat.model.Message
import me.pjq.chatcat.repository.ConversationRepository
import java.util.UUID

class InMemoryConversationRepository : ConversationRepository {
    private val conversations = mutableListOf<Conversation>()
    private val conversationsFlow = MutableStateFlow<List<Conversation>>(emptyList())
    
    override suspend fun getConversations(): Flow<List<Conversation>> {
        return conversationsFlow.asStateFlow()
    }
    
    override suspend fun getConversation(id: String): Conversation? {
        return conversations.find { it.id == id }
    }
    
    override suspend fun createConversation(title: String): Conversation {
        val conversation = Conversation(
            id = UUID.randomUUID().toString(),
            title = title
        )
        conversations.add(conversation)
        updateFlow()
        return conversation
    }
    
    override suspend fun updateConversation(conversation: Conversation): Conversation {
        val index = conversations.indexOfFirst { it.id == conversation.id }
        if (index != -1) {
            val updatedConversation = conversation.copy(
                updatedAt = Clock.System.now()
            )
            conversations[index] = updatedConversation
            updateFlow()
            return updatedConversation
        }
        return conversation
    }
    
    override suspend fun deleteConversation(id: String): Boolean {
        val removed = conversations.removeIf { it.id == id }
        if (removed) {
            updateFlow()
        }
        return removed
    }
    
    override suspend fun addMessage(conversationId: String, message: Message): Conversation? {
        val index = conversations.indexOfFirst { it.id == conversationId }
        if (index != -1) {
            val conversation = conversations[index]
            val updatedMessages = conversation.messages + message
            val updatedConversation = conversation.copy(
                messages = updatedMessages,
                updatedAt = Clock.System.now()
            )
            conversations[index] = updatedConversation
            updateFlow()
            return updatedConversation
        }
        return null
    }
    
    override suspend fun clearConversations(): Boolean {
        conversations.clear()
        updateFlow()
        return true
    }
    
    private fun updateFlow() {
        conversationsFlow.value = conversations.toList()
    }
}
