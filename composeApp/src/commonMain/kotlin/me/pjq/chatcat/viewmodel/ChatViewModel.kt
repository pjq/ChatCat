package me.pjq.chatcat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.pjq.chatcat.di.AppModule
import me.pjq.chatcat.model.Conversation
import me.pjq.chatcat.model.Message
import me.pjq.chatcat.model.Role
import me.pjq.chatcat.repository.ConversationRepository
import me.pjq.chatcat.service.ChatService
import java.util.UUID

class ChatViewModel : ViewModel() {
    private val conversationRepository: ConversationRepository = AppModule.getConversationRepository()
    private val chatService: ChatService = AppModule.getChatService()
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private var currentConversationId: String? = null
    
    init {
        loadConversations()
    }
    
    private fun loadConversations() {
        viewModelScope.launch {
            conversationRepository.getConversations().collectLatest { conversations ->
                _uiState.update { it.copy(conversations = conversations) }
                
                // Update current conversation if it's in the list
                currentConversationId?.let { id ->
                    val currentConversation = conversations.find { it.id == id }
                    if (currentConversation != null) {
                        _uiState.update { it.copy(currentConversation = currentConversation) }
                    }
                }
            }
        }
    }
    
    fun selectConversation(conversationId: String) {
        viewModelScope.launch {
            val conversation = conversationRepository.getConversation(conversationId)
            if (conversation != null) {
                currentConversationId = conversationId
                _uiState.update { it.copy(currentConversation = conversation) }
            }
        }
    }
    
    fun createNewConversation() {
        viewModelScope.launch {
            val conversation = conversationRepository.createConversation("New Chat")
            currentConversationId = conversation.id
            _uiState.update { it.copy(currentConversation = conversation) }
        }
    }
    
    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            val success = conversationRepository.deleteConversation(conversationId)
            if (success && conversationId == currentConversationId) {
                currentConversationId = null
                _uiState.update { it.copy(currentConversation = null) }
            }
        }
    }
    
    fun updateConversationTitle(conversationId: String, newTitle: String) {
        viewModelScope.launch {
            val conversation = conversationRepository.getConversation(conversationId)
            if (conversation != null) {
                val updatedConversation = conversation.copy(title = newTitle)
                conversationRepository.updateConversation(updatedConversation)
            }
        }
    }
    
    fun sendMessage(content: String) {
        if (content.isBlank()) return
        
        viewModelScope.launch {
            val conversationId = currentConversationId ?: run {
                val newConversation = conversationRepository.createConversation(generateTitleFromContent(content))
                currentConversationId = newConversation.id
                newConversation.id
            }
            
            // Create and add user message
            val userMessage = Message(
                id = UUID.randomUUID().toString(),
                content = content,
                role = Role.USER
            )
            
            conversationRepository.addMessage(conversationId, userMessage)
            
            // Show loading state
            _uiState.update { it.copy(isLoading = true) }
            
            // Get current conversation with the new user message
            val conversation = conversationRepository.getConversation(conversationId)
            if (conversation != null) {
                // Send message to AI service
                chatService.sendMessage(conversation.messages, conversation.modelConfig)
                    .collectLatest { result ->
                        result.fold(
                            onSuccess = { assistantMessage ->
                                conversationRepository.addMessage(conversationId, assistantMessage)
                                _uiState.update { it.copy(isLoading = false, error = null) }
                            },
                            onFailure = { error ->
                                val errorMessage = Message(
                                    id = UUID.randomUUID().toString(),
                                    content = "Error: ${error.message ?: "Unknown error"}",
                                    role = Role.ASSISTANT,
                                    isError = true
                                )
                                conversationRepository.addMessage(conversationId, errorMessage)
                                _uiState.update { it.copy(isLoading = false, error = error.message) }
                            }
                        )
                    }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Conversation not found") }
            }
        }
    }
    
    fun cancelRequest() {
        chatService.cancelRequest()
        _uiState.update { it.copy(isLoading = false) }
    }
    
    private fun generateTitleFromContent(content: String): String {
        return if (content.length > 30) {
            content.take(30) + "..."
        } else {
            content
        }
    }
}

data class ChatUiState(
    val conversations: List<Conversation> = emptyList(),
    val currentConversation: Conversation? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
