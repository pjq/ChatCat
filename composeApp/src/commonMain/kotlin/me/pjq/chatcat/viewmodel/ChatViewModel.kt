package me.pjq.chatcat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.pjq.chatcat.di.AppModule
import me.pjq.chatcat.model.Conversation
import me.pjq.chatcat.model.Message
import me.pjq.chatcat.model.Role
import me.pjq.chatcat.model.UserPreferences
import me.pjq.chatcat.repository.ConversationRepository
import me.pjq.chatcat.repository.PreferencesRepository
import me.pjq.chatcat.service.ChatService
import java.util.UUID

class ChatViewModel : ViewModel() {
    private val conversationRepository: ConversationRepository = AppModule.conversationRepository
    private val chatService: ChatService = AppModule.chatService
    private val preferencesRepository: PreferencesRepository = AppModule.preferencesRepository
    
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
        val conversation = conversationRepository.createConversation(generateTitleFromContent("New Chat"))
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
        
        sendMessageInternal(content)
    }
    
    /**
     * Resends a previously sent message
     */
    fun resendMessage(message: Message) {
        if (message.role != Role.USER) return
        
        sendMessageInternal(message.content)
    }
    
    /**
     * Deletes a message from the current conversation
     */
    fun deleteMessage(messageId: String) {
        val conversationId = currentConversationId ?: return
        
        viewModelScope.launch {
            conversationRepository.deleteMessage(conversationId, messageId)
        }
    }
    
    /**
     * Internal implementation of sending a message
     */
    private fun sendMessageInternal(content: String) {
        
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
            var conversation = conversationRepository.getConversation(conversationId)
            if (conversation != null) {
                // Get user preferences to find the active provider and selected model
                val userPreferences = preferencesRepository.getUserPreferencesSync()
                val activeProvider = userPreferences.modelProviders.find { it.id == userPreferences.activeProviderId }
                
                activeProvider?.selectedModel?.let { selectedModel ->
                    // Create a new modelConfig with the selected model
                    val newModelConfig = conversation.modelConfig.copy(model = selectedModel)
                    
                    // Only update the conversation if the configs are different
                    if (conversation.modelConfig != newModelConfig) {
                        val updatedConversation = conversation.copy(modelConfig = newModelConfig)
                        conversationRepository.updateConversation(updatedConversation)

                        // Update the local reference to use the updated conversation
                        val updatedConversationWithConfig = conversationRepository.getConversation(conversationId)
                        if (updatedConversationWithConfig != null) {
                            _uiState.update { it.copy(currentConversation = updatedConversationWithConfig) }
                            // Use the updated conversation from here on
                            conversation = updatedConversationWithConfig
                        }
                    }
                }
                
                val isStreamingEnabled = conversation.modelConfig.stream
                
                if (isStreamingEnabled) {
                    // Handle streaming response
                    _uiState.update { it.copy(isStreaming = true) }
                    
                    var firstMessage = true
                    var messageId: String? = null
                    
                    try {
                        // Use collect instead of collectLatest for streaming
                        chatService.sendMessage(conversation.messages, conversation.modelConfig)
                            .collect { result ->
                                result.fold(
                                    onSuccess = { assistantMessage ->
                                        if (firstMessage) {
                                            // First message, add it to the conversation
                                            messageId = assistantMessage.id
                                            conversationRepository.addMessage(conversationId, assistantMessage)
                                            firstMessage = false
                                        } else {
                                            // Update existing message with new content
                                            messageId?.let { id ->
                                                conversationRepository.updateMessage(conversationId, assistantMessage)
                                            }
                                        }
                                    },
                                    onFailure = { error ->
                                        val errorMessage = Message(
                                            id = UUID.randomUUID().toString(),
                                            content = "Error: ${error.message ?: "Unknown error"}",
                                            role = Role.ASSISTANT,
                                            isError = true
                                        )
                                        conversationRepository.addMessage(conversationId, errorMessage)
                                        _uiState.update { it.copy(isLoading = false, isStreaming = false, error = error.message) }
                                    }
                                )
                            }
                    } catch (e: Exception) {
                        // Handle any exceptions during streaming
                        val errorMessage = Message(
                            id = UUID.randomUUID().toString(),
                            content = "Error: ${e.message ?: "Unknown error"}",
                            role = Role.ASSISTANT,
                            isError = true
                        )
                        conversationRepository.addMessage(conversationId, errorMessage)
                        _uiState.update { it.copy(isLoading = false, isStreaming = false, error = e.message) }
                    } finally {
                        // After collection is complete, update UI state
                        _uiState.update { it.copy(isLoading = false, isStreaming = false, error = null) }
                    }
                } else {
                    // Handle non-streaming response
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
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Conversation not found") }
            }
        }
    }
    
    fun cancelRequest() {
        chatService.cancelRequest()
        _uiState.update { it.copy(isLoading = false, isStreaming = false) }
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
    val isStreaming: Boolean = false,
    val error: String? = null
)
