package me.pjq.chatcat.repository

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.pjq.chatcat.model.Conversation
import me.pjq.chatcat.model.Message
import java.util.UUID

/**
 * Implementation of ConversationRepository that persists data using Multiplatform Settings
 */
class PersistentConversationRepository(
    private val settings: Settings
) : ConversationRepository {
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
        prettyPrint = false
    }
    
    private val conversations = mutableListOf<Conversation>()
    private val conversationsFlow = MutableStateFlow<List<Conversation>>(emptyList())
    
    // Keys for storing conversations
    private object StorageKeys {
        const val CONVERSATION_IDS = "conversation_ids"
        const val CONVERSATION_PREFIX = "conversation_"
    }
    
    init {
        loadConversations()
    }
    
    private fun loadConversations() {
        val conversationIdsString = if (settings.hasKey(StorageKeys.CONVERSATION_IDS)) {
            settings.getString(StorageKeys.CONVERSATION_IDS, "")
        } else {
            ""
        }
        
        val conversationIds = conversationIdsString.split(",").filter { it.isNotEmpty() }
        
        conversations.clear()
        conversationIds.forEach { id ->
            val key = "${StorageKeys.CONVERSATION_PREFIX}$id"
            if (settings.hasKey(key)) {
                try {
                    val conversationJson = settings.getString(key, "")
                    val conversation = json.decodeFromString<Conversation>(conversationJson)
                    conversations.add(conversation)
                } catch (e: Exception) {
                    // Log error or handle corrupted data
                    println("Error loading conversation $id: ${e.message}")
                }
            }
        }
        
        updateFlow()
    }
    
    private fun saveConversationIds() {
        val ids = conversations.map { it.id }.joinToString(",")
        settings.putString(StorageKeys.CONVERSATION_IDS, ids)
    }
    
    private fun saveConversation(conversation: Conversation) {
        val conversationJson = json.encodeToString(conversation)
        settings.putString("${StorageKeys.CONVERSATION_PREFIX}${conversation.id}", conversationJson)
    }
    
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
        saveConversation(conversation)
        saveConversationIds()
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
            saveConversation(updatedConversation)
            updateFlow()
            return updatedConversation
        }
        return conversation
    }
    
    override suspend fun deleteConversation(id: String): Boolean {
        val removed = conversations.removeIf { it.id == id }
        if (removed) {
            settings.remove("${StorageKeys.CONVERSATION_PREFIX}$id")
            saveConversationIds()
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
            saveConversation(updatedConversation)
            updateFlow()
            return updatedConversation
        }
        return null
    }
    
    override suspend fun clearConversations(): Boolean {
        // Remove all conversation entries
        conversations.forEach { conversation ->
            settings.remove("${StorageKeys.CONVERSATION_PREFIX}${conversation.id}")
        }
        
        // Clear the list and save empty conversation IDs
        conversations.clear()
        saveConversationIds()
        updateFlow()
        return true
    }
    
    private fun updateFlow() {
        conversationsFlow.value = conversations.toList()
    }
}
