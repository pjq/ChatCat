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
import me.pjq.chatcat.model.DefaultModelProviders
import me.pjq.chatcat.model.FontSize
import me.pjq.chatcat.model.Language
import me.pjq.chatcat.model.ModelConfig
import me.pjq.chatcat.model.ModelProvider
import me.pjq.chatcat.model.ProviderType
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
        loadActiveProvider()
    }
    
    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesRepository.getUserPreferences().collectLatest { preferences ->
                _uiState.update { it.copy(preferences = preferences) }
                
                // Update active provider when preferences change
                val activeProviderId = preferences.activeProviderId
                val activeProvider = preferences.modelProviders.find { it.id == activeProviderId }
                    ?: DefaultModelProviders.OPENAI
                
                _uiState.update { it.copy(activeProvider = activeProvider) }
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
                // If no models are returned, use some default OpenAI models for testing
                val modelList = if (models.isEmpty()) {
                    listOf("gpt-4o", "gpt-4", "gpt-3.5-turbo", "gpt-4-turbo")
                } else {
                    models
                }
                _uiState.update { it.copy(availableModels = modelList, isLoading = false) }
            } catch (e: Exception) {
                // Use default OpenAI models if there's an error
                val defaultModels = listOf("gpt-4o", "gpt-4", "gpt-3.5-turbo", "gpt-4-turbo")
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
    
    fun updateLanguage(language: Language) {
        viewModelScope.launch {
            // Update the language in preferences
            val preferences = _uiState.value.preferences.copy(language = language)
            preferencesRepository.updateUserPreferences(preferences)
            
            // Update the language in the LanguageManager
            val languageManager = LanguageManager.getInstance(preferencesRepository)
            languageManager.setLanguage(language)
            
            // Log the language change
            println("Language changed to: ${language.displayName} (${language.code})")
        }
    }
    
    // Model Provider methods
    
    fun loadActiveProvider() {
        viewModelScope.launch {
            val preferences = _uiState.value.preferences
            val activeProviderId = preferences.activeProviderId
            val activeProvider = preferences.modelProviders.find { it.id == activeProviderId }
                ?: DefaultModelProviders.OPENAI
            
            _uiState.update { it.copy(activeProvider = activeProvider) }
        }
    }
    
    fun setActiveProvider(providerId: String) {
        viewModelScope.launch {
            // Get the current preferences
            val currentPreferences = _uiState.value.preferences
            
            // Find the provider in the list
            val provider = currentPreferences.modelProviders.find { it.id == providerId }
                ?: DefaultModelProviders.OPENAI
            
            // Create updated preferences with the new active provider ID
            val updatedPreferences = currentPreferences.copy(activeProviderId = providerId)
            
            // Persist the updated preferences
            preferencesRepository.updateUserPreferences(updatedPreferences)
            
            // Update the active provider ID in the repository directly
            preferencesRepository.setActiveProviderId(providerId)
            
            // Update API settings based on the selected provider
            preferencesRepository.setApiBaseUrl(provider.baseUrl)
            preferencesRepository.setApiKey(provider.apiKey)
            
            // Update UI state with the new preferences and active provider
            _uiState.update { 
                it.copy(
                    preferences = updatedPreferences,
                    activeProvider = provider
                ) 
            }
            
            // Log the change for debugging
            println("Active provider set to: ${provider.name}, ID: ${provider.id}")
            
            // Check API availability with the new settings
            checkApiAvailability()
        }
    }
    
    fun startEditingProvider(provider: ModelProvider) {
        _uiState.update { it.copy(isEditingProvider = true, editingProvider = provider) }
    }
    
    fun cancelEditingProvider() {
        _uiState.update { it.copy(isEditingProvider = false, editingProvider = null) }
    }
    
    fun saveProvider(provider: ModelProvider) {
        viewModelScope.launch {
            val currentProviders = _uiState.value.preferences.modelProviders
            
            // Find if provider already exists
            val updatedProviders = if (currentProviders.any { it.id == provider.id }) {
                // Update existing provider
                currentProviders.map { 
                    if (it.id == provider.id) provider else it 
                }
            } else {
                // Add new provider
                currentProviders + provider
            }
            
            // Update preferences with the new provider list
            val updatedPreferences = _uiState.value.preferences.copy(modelProviders = updatedProviders)
            
            // Persist the updated preferences
            preferencesRepository.updateUserPreferences(updatedPreferences)
            
            // Update the UI state with the new preferences
            _uiState.update { it.copy(preferences = updatedPreferences) }
            
            // If this is the active provider, update API settings and active provider in UI state
            if (provider.id == updatedPreferences.activeProviderId) {
                // Update API settings in the repository
                preferencesRepository.setApiBaseUrl(provider.baseUrl)
                preferencesRepository.setApiKey(provider.apiKey)
                
                // Update the active provider in UI state
                _uiState.update { it.copy(activeProvider = provider) }
                
                // Check API availability with the new settings
                checkApiAvailability()
            }
            
            // Log the update for debugging
            println("Provider saved: ${provider.name}, ID: ${provider.id}, Base URL: ${provider.baseUrl}")
            println("Updated providers list size: ${updatedProviders.size}")
            
            // Exit editing mode
            _uiState.update { it.copy(isEditingProvider = false, editingProvider = null) }
        }
    }
    
    fun deleteProvider(providerId: String) {
        viewModelScope.launch {
            val currentProviders = _uiState.value.preferences.modelProviders
            
            // Don't allow deleting the default provider
            if (providerId == DefaultModelProviders.OPENAI.id) {
                return@launch
            }
            
            // Remove the provider
            val updatedProviders = currentProviders.filter { it.id != providerId }
            
            // Update preferences
            val preferences = _uiState.value.preferences.copy(modelProviders = updatedProviders)
            
            // If the active provider was deleted, switch to the default provider
            if (providerId == preferences.activeProviderId) {
                val updatedPreferences = preferences.copy(activeProviderId = DefaultModelProviders.OPENAI.id)
                preferencesRepository.updateUserPreferences(updatedPreferences)
                updateApiBaseUrl(DefaultModelProviders.OPENAI.baseUrl)
                updateApiKey(DefaultModelProviders.OPENAI.apiKey)
                _uiState.update { it.copy(activeProvider = DefaultModelProviders.OPENAI) }
            } else {
                preferencesRepository.updateUserPreferences(preferences)
            }
        }
    }
    
    fun createNewProvider() {
        val newProvider = ModelProvider(
            id = "provider_${System.currentTimeMillis()}",
            name = "New Provider",
            baseUrl = "https://api.example.com",
            apiKey = "",
            isEnabled = true,
            providerType = ProviderType.OPENAI_COMPATIBLE
        )
        startEditingProvider(newProvider)
    }
}

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val isApiAvailable: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val availableModels: List<String> = emptyList(),
    val activeProvider: ModelProvider = DefaultModelProviders.OPENAI,
    val isEditingProvider: Boolean = false,
    val editingProvider: ModelProvider? = null
)
