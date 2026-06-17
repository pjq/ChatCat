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
import me.pjq.chatcat.model.ContentPart
import me.pjq.chatcat.model.Conversation
import me.pjq.chatcat.model.McpTool
import me.pjq.chatcat.model.Message
import me.pjq.chatcat.model.Role
import me.pjq.chatcat.platform.randomUUID
import me.pjq.chatcat.repository.ConversationRepository
import me.pjq.chatcat.repository.PreferencesRepository
import me.pjq.chatcat.service.ChatService
import me.pjq.chatcat.service.ImageGenerationService
import me.pjq.chatcat.service.McpClientService

class ChatViewModel(
    private val conversationRepository: ConversationRepository = AppModule.conversationRepository,
    private val chatService: ChatService = AppModule.chatService,
    private val imageGenerationService: ImageGenerationService = AppModule.imageGenerationService,
    private val mcpClient: McpClientService = AppModule.mcpClient,
    private val preferencesRepository: PreferencesRepository = AppModule.preferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentConversationId: String? = null

    init {
        observeConversations()
        observePreferences()
    }

    private fun observeConversations() {
        viewModelScope.launch {
            conversationRepository.getConversations().collectLatest { conversations ->
                _uiState.update { state ->
                    val current = currentConversationId?.let { id -> conversations.find { it.id == id } }
                        ?: state.currentConversation
                    state.copy(conversations = conversations, currentConversation = current)
                }
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.getUserPreferences().collectLatest { prefs ->
                val active = prefs.modelProviders.firstOrNull { it.id == prefs.activeProviderId }
                _uiState.update {
                    it.copy(
                        availableModels = active?.availableModels ?: emptyList(),
                        selectedModel = active?.selectedModel ?: "",
                        canSendImages = active?.supportsVision() == true,
                        canGenerateImages = active?.supportsImageGeneration() == true,
                        imageGenModels = active?.imageGenModels ?: emptyList(),
                        mcpEnabled = prefs.mcpServers.any { it.isEnabled }
                    )
                }
            }
        }
    }

    fun selectConversation(id: String) {
        viewModelScope.launch {
            val conversation = conversationRepository.getConversation(id) ?: return@launch
            currentConversationId = id
            _uiState.update { it.copy(currentConversation = conversation) }
        }
    }

    fun createNewConversation() {
        viewModelScope.launch {
            val conversation = conversationRepository.createConversation("New Chat")
            currentConversationId = conversation.id
            _uiState.update { it.copy(currentConversation = conversation) }
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            val ok = conversationRepository.deleteConversation(id)
            if (ok && id == currentConversationId) {
                currentConversationId = null
                _uiState.update { it.copy(currentConversation = null) }
            }
        }
    }

    fun selectModel(model: String) {
        viewModelScope.launch {
            val prefs = preferencesRepository.getUserPreferencesSync()
            val updated = prefs.copy(
                modelProviders = prefs.modelProviders.map { provider ->
                    if (provider.id == prefs.activeProviderId) provider.copy(selectedModel = model)
                    else provider
                }
            )
            preferencesRepository.updateUserPreferences(updated)
        }
    }

    fun toggleStreamMode() {
        viewModelScope.launch {
            val prefs = preferencesRepository.getUserPreferencesSync()
            val cfg = prefs.defaultModelConfig
            preferencesRepository.setDefaultModelConfig(cfg.copy(stream = !cfg.stream))
        }
    }

    fun sendMessage(text: String, attachments: List<ContentPart.Image> = emptyList()) {
        if (text.isBlank() && attachments.isEmpty()) return
        viewModelScope.launch {
            val conversationId = currentConversationId ?: conversationRepository.createConversation(
                deriveTitle(text)
            ).also { currentConversationId = it.id }.id

            val parts = buildList<ContentPart> {
                if (text.isNotBlank()) add(ContentPart.Text(text))
                addAll(attachments)
            }
            val userMessage = Message(id = randomUUID(), role = Role.USER, parts = parts)
            conversationRepository.addMessage(conversationId, userMessage)

            // Re-load conversation with the new message and trigger model call
            val convo = conversationRepository.getConversation(conversationId) ?: return@launch
            runChatCompletion(convo)

            val first = convo.messages.firstOrNull { it.role == Role.USER }
            if (first != null && convo.title == "New Chat" && first.text.isNotBlank()) {
                conversationRepository.updateConversation(convo.copy(title = deriveTitle(first.text)))
            }
        }
    }

    fun resendMessage(message: Message) {
        if (message.role != Role.USER) return
        sendMessage(text = message.text, attachments = message.images)
    }

    fun deleteMessage(messageId: String) {
        val conversationId = currentConversationId ?: return
        viewModelScope.launch { conversationRepository.deleteMessage(conversationId, messageId) }
    }

    fun cancel() {
        chatService.cancelRequest()
        _uiState.update { it.copy(isLoading = false, isStreaming = false) }
    }

    fun generateImage(prompt: String) {
        viewModelScope.launch {
            val prefs = preferencesRepository.getUserPreferencesSync()
            val provider = prefs.modelProviders.firstOrNull { it.id == prefs.activeProviderId } ?: return@launch
            val model = provider.imageGenModels.firstOrNull() ?: return@launch
            val conversationId = currentConversationId ?: conversationRepository.createConversation(
                deriveTitle(prompt)
            ).also { currentConversationId = it.id }.id

            val userMessage = Message(
                id = randomUUID(),
                role = Role.USER,
                parts = listOf(ContentPart.Text("🎨 Generate: $prompt"))
            )
            conversationRepository.addMessage(conversationId, userMessage)
            _uiState.update { it.copy(isLoading = true) }

            val result = imageGenerationService.generate(prompt = prompt, model = model)
            result.fold(
                onSuccess = { images ->
                    val assistant = Message(
                        id = randomUUID(),
                        role = Role.ASSISTANT,
                        parts = images,
                        modelName = model
                    )
                    conversationRepository.addMessage(conversationId, assistant)
                },
                onFailure = { err ->
                    val errorMessage = Message.text(
                        id = randomUUID(),
                        role = Role.ASSISTANT,
                        text = "Image generation failed: ${err.message}",
                        isError = true
                    )
                    conversationRepository.addMessage(conversationId, errorMessage)
                    _uiState.update { it.copy(error = err.message) }
                }
            )
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun runChatCompletion(conversation: Conversation) {
        _uiState.update { it.copy(isLoading = true, isStreaming = conversation.modelConfig.stream, error = null) }
        val tools = collectMcpTools(conversation)

        var assistantId: String? = null
        try {
            chatService.sendMessage(
                messages = conversation.messages,
                modelConfig = conversation.modelConfig,
                availableTools = tools,
                systemPrompt = conversation.systemPrompt
                    ?: preferencesRepository.getUserPreferencesSync().systemPrompt.takeIf { it.isNotBlank() }
            ).collect { result ->
                result.fold(
                    onSuccess = { partial ->
                        if (assistantId == null) {
                            assistantId = partial.id
                            conversationRepository.addMessage(conversation.id, partial)
                        } else {
                            conversationRepository.updateMessage(conversation.id, partial)
                        }
                    },
                    onFailure = { err ->
                        val errMsg = Message.text(
                            id = randomUUID(),
                            role = Role.ASSISTANT,
                            text = "Error: ${err.message ?: "unknown"}",
                            isError = true
                        )
                        conversationRepository.addMessage(conversation.id, errMsg)
                        _uiState.update { it.copy(error = err.message) }
                    }
                )
            }
        } finally {
            _uiState.update { it.copy(isLoading = false, isStreaming = false) }
        }
    }

    private suspend fun collectMcpTools(conversation: Conversation): List<McpTool> {
        val prefs = preferencesRepository.getUserPreferencesSync()
        val enabledServers = prefs.mcpServers.filter {
            it.isEnabled && (conversation.enabledMcpServerIds.isEmpty() || it.id in conversation.enabledMcpServerIds)
        }
        if (enabledServers.isEmpty()) return emptyList()
        return enabledServers.flatMap { server ->
            mcpClient.listTools(server).getOrDefault(server.knownTools)
        }
    }

    private fun deriveTitle(content: String): String {
        if (content.isBlank()) return "New Chat"
        val firstLine = content.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
        return if (firstLine.length > 32) firstLine.take(32) + "…" else firstLine
    }
}

data class ChatUiState(
    val conversations: List<Conversation> = emptyList(),
    val currentConversation: Conversation? = null,
    val isLoading: Boolean = false,
    val isStreaming: Boolean = false,
    val error: String? = null,
    val availableModels: List<String> = emptyList(),
    val selectedModel: String = "",
    val canSendImages: Boolean = false,
    val canGenerateImages: Boolean = false,
    val imageGenModels: List<String> = emptyList(),
    val mcpEnabled: Boolean = false
)
