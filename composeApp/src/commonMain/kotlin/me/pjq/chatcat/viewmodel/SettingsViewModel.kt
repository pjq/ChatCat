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
import me.pjq.chatcat.model.FontSize
import me.pjq.chatcat.model.ModelConfig
import me.pjq.chatcat.model.Theme
import me.pjq.chatcat.model.UserPreferences
import me.pjq.chatcat.repository.PreferencesRepository
import me.pjq.chatcat.service.ChatService
import me.pjq.chatcat.service.OpenAIClientChatService

class SettingsViewModel : ViewModel() {
    private val preferencesRepository: PreferencesRepository = AppModule.preferencesRepository
    private val chatService: ChatService = AppModule.chatService
    private val modelService: OpenAIClientChatService = AppModule.modelService
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadPreferences()
        checkApiAvailability()
    }
    
    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesRepository.getUserPreferences().collectLatest { preferences ->
                _uiState.update { it.copy(preferences = preferences) }
            }
        }
    }
    
    fun checkApiAvailability() {
        viewModelScope.launch {
            val isAvailable = chatService.isApiAvailable()
            _uiState.update { it.copy(isApiAvailable = isAvailable) }
        }
    }
    
    fun loadAvailableModels() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val models = modelService.listModels()
                // If no models are returned, use some default models for testing
                val modelList = if (models.isEmpty()) {
                    listOf("gpt-4o", "gpt-4", "gpt-3.5-turbo", "claude-3-opus", "claude-3-sonnet")
                } else {
                    models
                }
                _uiState.update { it.copy(availableModels = modelList, isLoading = false) }
            } catch (e: Exception) {
                // Use default models if there's an error
                val defaultModels = listOf("gpt-4o", "gpt-4", "gpt-3.5-turbo", "claude-3-opus", "claude-3-sonnet")
                _uiState.update { 
                    it.copy(
                        availableModels = defaultModels, 
                        isLoading = false,
                        error = e.message
                    ) 
                }
            }
        }
    }
    
    fun updateApiKey(apiKey: String) {
        viewModelScope.launch {
            preferencesRepository.setApiKey(apiKey)
            val preferences = _uiState.value.preferences.copy(apiKey = apiKey)
            preferencesRepository.updateUserPreferences(preferences)
            checkApiAvailability()
        }
    }
    
    fun updateApiBaseUrl(baseUrl: String) {
        viewModelScope.launch {
            preferencesRepository.setApiBaseUrl(baseUrl)
            val preferences = _uiState.value.preferences.copy(apiBaseUrl = baseUrl)
            preferencesRepository.updateUserPreferences(preferences)
            checkApiAvailability()
        }
    }
    
    fun updateTheme(theme: Theme) {
        viewModelScope.launch {
            preferencesRepository.setTheme(theme)
            val preferences = _uiState.value.preferences.copy(theme = theme)
            preferencesRepository.updateUserPreferences(preferences)
        }
    }
    
    fun updateFontSize(fontSize: FontSize) {
        viewModelScope.launch {
            preferencesRepository.setFontSize(fontSize)
            val preferences = _uiState.value.preferences.copy(fontSize = fontSize)
            preferencesRepository.updateUserPreferences(preferences)
        }
    }
    
    fun updateDefaultModelConfig(modelConfig: ModelConfig) {
        viewModelScope.launch {
            preferencesRepository.setDefaultModelConfig(modelConfig)
            val preferences = _uiState.value.preferences.copy(defaultModelConfig = modelConfig)
            preferencesRepository.updateUserPreferences(preferences)
        }
    }
    
    fun updateModel(model: String) {
        viewModelScope.launch {
            val currentModelConfig = _uiState.value.preferences.defaultModelConfig
            val updatedModelConfig = currentModelConfig.copy(model = model)
            updateDefaultModelConfig(updatedModelConfig)
        }
    }
    
    fun updateStreamMode(enabled: Boolean) {
        viewModelScope.launch {
            val currentModelConfig = _uiState.value.preferences.defaultModelConfig
            val updatedModelConfig = currentModelConfig.copy(stream = enabled)
            updateDefaultModelConfig(updatedModelConfig)
        }
    }
    
    fun updateSoundEffectsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSoundEffectsEnabled(enabled)
            val preferences = _uiState.value.preferences.copy(enableSoundEffects = enabled)
            preferencesRepository.updateUserPreferences(preferences)
        }
    }
    
    fun updateMarkdownEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setMarkdownEnabled(enabled)
            val preferences = _uiState.value.preferences.copy(enableMarkdown = enabled)
            preferencesRepository.updateUserPreferences(preferences)
        }
    }
}

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val isApiAvailable: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val availableModels: List<String> = emptyList()
)
