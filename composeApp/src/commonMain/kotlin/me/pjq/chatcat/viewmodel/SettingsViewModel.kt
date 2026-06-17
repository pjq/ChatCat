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
import me.pjq.chatcat.i18n.LanguageManager
import me.pjq.chatcat.model.Accent
import me.pjq.chatcat.model.DefaultModelProviders
import me.pjq.chatcat.model.Density
import me.pjq.chatcat.model.FontSize
import me.pjq.chatcat.model.Language
import me.pjq.chatcat.model.McpServer
import me.pjq.chatcat.model.ModelConfig
import me.pjq.chatcat.model.ModelProvider
import me.pjq.chatcat.model.ProviderType
import me.pjq.chatcat.model.Theme
import me.pjq.chatcat.model.UserPreferences
import me.pjq.chatcat.platform.randomUUID
import me.pjq.chatcat.repository.PreferencesRepository
import me.pjq.chatcat.repository.ConversationRepository
import me.pjq.chatcat.service.ChatService
import me.pjq.chatcat.service.McpClientService
import me.pjq.chatcat.service.OpenAIChatService

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository = AppModule.preferencesRepository,
    private val chatService: ChatService = AppModule.chatService,
    private val mcpClient: McpClientService = AppModule.mcpClient,
    private val conversationRepository: ConversationRepository = AppModule.conversationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            var lastProviderId: String? = null
            preferencesRepository.getUserPreferences().collectLatest { prefs ->
                val active = prefs.modelProviders.firstOrNull { it.id == prefs.activeProviderId }
                    ?: DefaultModelProviders.OPENAI
                _uiState.update {
                    it.copy(
                        preferences = prefs,
                        activeProvider = active,
                        availableModels = active.availableModels
                    )
                }
                if (lastProviderId != active.id) {
                    lastProviderId = active.id
                    refreshAvailableModels()
                }
            }
        }
    }

    fun refreshAvailableModels() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingModels = true) }
            val models = chatService.listModels()
            val current = _uiState.value
            val merged = if (models.isNotEmpty()) models else current.activeProvider.availableModels
            _uiState.update { it.copy(availableModels = merged, isLoadingModels = false) }

            // Persist into provider's availableModels
            val prefs = current.preferences
            val updated = prefs.copy(
                modelProviders = prefs.modelProviders.map { provider ->
                    if (provider.id == prefs.activeProviderId) provider.copy(availableModels = merged)
                    else provider
                }
            )
            preferencesRepository.updateUserPreferences(updated)
        }
    }

    fun updateActiveProvider(providerId: String) {
        viewModelScope.launch {
            preferencesRepository.setActiveProviderId(providerId)
        }
    }

    fun saveProvider(provider: ModelProvider) {
        viewModelScope.launch {
            val prefs = preferencesRepository.getUserPreferencesSync()
            val updatedProviders = if (prefs.modelProviders.any { it.id == provider.id }) {
                prefs.modelProviders.map { if (it.id == provider.id) provider else it }
            } else {
                prefs.modelProviders + provider
            }
            preferencesRepository.updateUserPreferences(prefs.copy(modelProviders = updatedProviders))
        }
    }

    fun deleteProvider(providerId: String) {
        viewModelScope.launch {
            val prefs = preferencesRepository.getUserPreferencesSync()
            val remaining = prefs.modelProviders.filter { it.id != providerId }
            val newActive = if (prefs.activeProviderId == providerId)
                remaining.firstOrNull()?.id ?: DefaultModelProviders.OPENAI.id
            else prefs.activeProviderId
            preferencesRepository.updateUserPreferences(prefs.copy(modelProviders = remaining, activeProviderId = newActive))
        }
    }

    fun newProviderTemplate(): ModelProvider = ModelProvider(
        id = "provider_${randomUUID().take(8)}",
        name = "New Provider",
        baseUrl = "https://api.openai.com/v1",
        providerType = ProviderType.OPENAI_COMPATIBLE,
        selectedModel = "gpt-4o-mini"
    )

    /** Returns Result with the list of discovered models, or failure with a human-readable message. */
    suspend fun probeProvider(baseUrl: String, apiKey: String): Result<List<String>> {
        val service = chatService as? OpenAIChatService ?: return Result.failure(
            IllegalStateException("Probe not supported for this service")
        )
        return service.probe(baseUrl, apiKey)
    }

    fun clearAllConversations(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            conversationRepository.clearConversations()
            onComplete()
        }
    }

    fun resetPreferences(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            preferencesRepository.updateUserPreferences(UserPreferences())
            onComplete()
        }
    }

    fun updateTheme(theme: Theme) {
        viewModelScope.launch { preferencesRepository.setTheme(theme) }
    }

    fun updateAccent(accent: Accent) {
        viewModelScope.launch {
            val prefs = preferencesRepository.getUserPreferencesSync()
            preferencesRepository.updateUserPreferences(prefs.copy(accent = accent))
        }
    }

    fun updateDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            val prefs = preferencesRepository.getUserPreferencesSync()
            preferencesRepository.updateUserPreferences(prefs.copy(useDynamicColor = enabled))
        }
    }

    fun updateFontSize(size: FontSize) {
        viewModelScope.launch { preferencesRepository.setFontSize(size) }
    }

    fun updateDensity(density: Density) {
        viewModelScope.launch {
            val prefs = preferencesRepository.getUserPreferencesSync()
            preferencesRepository.updateUserPreferences(prefs.copy(density = density))
        }
    }

    fun updateLanguage(language: Language) {
        viewModelScope.launch {
            val prefs = preferencesRepository.getUserPreferencesSync()
            preferencesRepository.updateUserPreferences(prefs.copy(language = language))
            LanguageManager.getInstance(preferencesRepository).setLanguage(language)
        }
    }

    fun updateModelConfig(config: ModelConfig) {
        viewModelScope.launch { preferencesRepository.setDefaultModelConfig(config) }
    }

    fun updateSystemPrompt(prompt: String) {
        viewModelScope.launch {
            val prefs = preferencesRepository.getUserPreferencesSync()
            preferencesRepository.updateUserPreferences(prefs.copy(systemPrompt = prompt))
        }
    }

    fun saveMcpServer(server: McpServer) {
        viewModelScope.launch {
            val prefs = preferencesRepository.getUserPreferencesSync()
            val updated = if (prefs.mcpServers.any { it.id == server.id }) {
                prefs.mcpServers.map { if (it.id == server.id) server else it }
            } else {
                prefs.mcpServers + server
            }
            preferencesRepository.updateUserPreferences(prefs.copy(mcpServers = updated))
        }
    }

    fun deleteMcpServer(serverId: String) {
        viewModelScope.launch {
            val prefs = preferencesRepository.getUserPreferencesSync()
            preferencesRepository.updateUserPreferences(
                prefs.copy(mcpServers = prefs.mcpServers.filter { it.id != serverId })
            )
        }
    }

    fun testMcpServer(server: McpServer) {
        viewModelScope.launch {
            _uiState.update { it.copy(mcpStatus = "Testing ${server.name}…") }
            val ping = mcpClient.ping(server)
            val tools = if (ping.isSuccess) mcpClient.listTools(server) else Result.success(emptyList())
            val msg = ping.fold(
                onSuccess = {
                    val toolList = tools.getOrDefault(emptyList())
                    "Connected. ${toolList.size} tools available."
                },
                onFailure = { "Failed: ${it.message}" }
            )
            _uiState.update { it.copy(mcpStatus = msg) }
        }
    }
}

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val activeProvider: ModelProvider = DefaultModelProviders.OPENAI,
    val availableModels: List<String> = emptyList(),
    val isLoadingModels: Boolean = false,
    val mcpStatus: String? = null
)
