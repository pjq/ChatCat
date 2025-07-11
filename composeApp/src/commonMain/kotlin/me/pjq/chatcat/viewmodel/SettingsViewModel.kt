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
                
                _uiState.update { it.copy(activeProvider = activeProvider, selectedModel = activeProvider.selectedModel) }
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
                // Get the most up-to-date active provider from preferences
                val preferences = preferencesRepository.getUserPreferencesSync()
                val activeProviderId = preferences.activeProviderId
                val provider = preferences.modelProviders.find { it.id == activeProviderId } 
                    ?: DefaultModelProviders.OPENAI
                
                // Update activeProvider in uiState to ensure consistency
                _uiState.update { it.copy(activeProvider = provider) }
                
                // Apply the provider's settings to ensure we're using the right API URL and key
                preferencesRepository.setApiBaseUrl(provider.baseUrl)
                preferencesRepository.setApiKey(provider.apiKey)
                
                // Create a fresh OpenAI client with current provider settings
                val tempModelService = OpenAIClientChatService(preferencesRepository)
                
                println("Loading models for provider: ${provider.name} (${provider.id})")
                println("Base URL: ${provider.baseUrl}")
                val models = tempModelService.listModels()
                
                // If no models are returned, use some default models based on provider type
                val modelList = if (models.isEmpty()) {
                    when (provider.providerType) {
                        ProviderType.OPENAI -> listOf("gpt-4o", "gpt-4", "gpt-3.5-turbo", "gpt-4-turbo")
                        ProviderType.OPENAI_COMPATIBLE, ProviderType.CUSTOM -> listOf("gpt-3.5-turbo", "gpt-4")
                    }
                } else {
                    models
                }
                
                println("Available models: ${modelList.joinToString(", ")}")
                _uiState.update { it.copy(availableModels = modelList, isLoading = false) }
            } catch (e: Exception) {
                // Use default models based on provider type if there's an error
                val currentProvider = _uiState.value.activeProvider
                val defaultModels = when (currentProvider.providerType) {
                    ProviderType.OPENAI -> listOf("gpt-4o", "gpt-4", "gpt-3.5-turbo", "gpt-4-turbo")
                    ProviderType.OPENAI_COMPATIBLE, ProviderType.CUSTOM -> listOf("gpt-3.5-turbo", "gpt-4")
                }
                
                println("Error loading models: ${e.message}")
                e.printStackTrace()
                
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
            // Only update the selected model for the active provider
            // No longer storing model in ModelConfig since that's been removed
            val currentPrefs = _uiState.value.preferences
            val activeProviderId = currentPrefs.activeProviderId
            
            // Find and update the active provider
            val updatedProviders = currentPrefs.modelProviders.map { provider ->
                if (provider.id == activeProviderId) {
                    // Update this provider's selected model
                    provider.copy(selectedModel = model)
                } else {
                    provider
                }
            }
            
            // Save the updated providers list
            val updatedPrefs = currentPrefs.copy(modelProviders = updatedProviders)
            preferencesRepository.updateUserPreferences(updatedPrefs)
            
            // Log the change
            println("Updated selected model for provider '$activeProviderId' to: $model")
            
            // Update UI state
            _uiState.update { it.copy(preferences = updatedPrefs, selectedModel = model) }
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
            var activeProvider = currentPreferences.modelProviders.find { it.id == providerId }
                ?: DefaultModelProviders.OPENAI
            
            // Check if we need to set a default model for this provider
            if (activeProvider.selectedModel.isBlank()) {
                // Find a suitable default model based on provider type
                val defaultModel = when (activeProvider.providerType) {
                    ProviderType.OPENAI -> "gpt-3.5-turbo"
                    ProviderType.OPENAI_COMPATIBLE -> "gpt-3.5-turbo"
                    ProviderType.CUSTOM -> "model1"
                }
                
                // Update the provider with a selected model
                val updatedProvider = activeProvider.copy(selectedModel = defaultModel)
                val updatedProviders = currentPreferences.modelProviders.map {
                    if (it.id == providerId) updatedProvider else it
                }
                
                // Update the activeProvider reference for later use
                activeProvider = updatedProvider
                
                // Create updated preferences with the updated providers
                val updatedPreferences = currentPreferences.copy(
                    activeProviderId = providerId,
                    modelProviders = updatedProviders
                )
                
                preferencesRepository.updateUserPreferences(updatedPreferences)
            } else {
                // Create updated preferences with the new active provider ID
                val updatedPreferences = currentPreferences.copy(activeProviderId = providerId)
                preferencesRepository.updateUserPreferences(updatedPreferences)
            }
            
            println("Setting active provider to: ${activeProvider.name}, with model: ${activeProvider.selectedModel}")
            
            // Update the active provider ID in the repository directly
            preferencesRepository.setActiveProviderId(providerId)
            
            // Update API settings based on the selected provider
            preferencesRepository.setApiBaseUrl(activeProvider.baseUrl)
            preferencesRepository.setApiKey(activeProvider.apiKey)
            
            // Update UI state with the new preferences and active provider
            _uiState.update { 
                it.copy(
                    preferences = preferencesRepository.getUserPreferencesSync(),
                    activeProvider = activeProvider,
                    availableModels = emptyList() // Clear models when switching providers
                ) 
            }
            
            // Log the change for debugging
            println("Active provider set to: ${activeProvider.name}, ID: ${activeProvider.id}, Model: ${activeProvider.selectedModel}")
            
            // Check API availability with the new settings
            checkApiAvailability()
            
            // Load available models for the new provider
            loadAvailableModels()
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
                // Update existing provider but preserve its selected model
                currentProviders.map { existingProvider -> 
                    if (existingProvider.id == provider.id) {
                        // Preserve the existing selected model if the new one is blank
                        val modelToUse = if (provider.selectedModel.isBlank() && existingProvider.selectedModel.isNotBlank()) {
                            existingProvider.selectedModel
                        } else {
                            provider.selectedModel
                        }
                        provider.copy(selectedModel = modelToUse)
                    } else {
                        existingProvider 
                    }
                }
            } else {
                // For new providers, ensure they have a selected model
                val newProvider = if (provider.selectedModel.isBlank()) {
                    // Use an appropriate default model based on provider type
                    val defaultModel = when (provider.providerType) {
                        ProviderType.OPENAI -> "gpt-3.5-turbo"
                        ProviderType.OPENAI_COMPATIBLE -> "gpt-3.5-turbo"
                        ProviderType.CUSTOM -> "model1"
                    }
                    provider.copy(selectedModel = defaultModel)
                } else {
                    provider
                }
                currentProviders + newProvider
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
        // Use a default model based on provider type
        val defaultModel = "gpt-4o" // Default for OPENAI_COMPATIBLE
        
        val newProvider = ModelProvider(
            id = "provider_${System.currentTimeMillis()}",
            name = "New Provider",
            baseUrl = "https://api.openai.com/v1",
            apiKey = "",
            isEnabled = true,
            providerType = ProviderType.OPENAI_COMPATIBLE,
            selectedModel = defaultModel // Initialize with a default model
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
    val editingProvider: ModelProvider? = null,
    val selectedModel: String = ""
)
