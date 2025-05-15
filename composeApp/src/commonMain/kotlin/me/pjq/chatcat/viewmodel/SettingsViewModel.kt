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

class SettingsViewModel : ViewModel() {
    private val preferencesRepository: PreferencesRepository = AppModule.getPreferencesRepository()
    private val chatService: ChatService = AppModule.getChatService()
    
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
    
    private fun checkApiAvailability() {
        viewModelScope.launch {
            val isAvailable = chatService.isApiAvailable()
            _uiState.update { it.copy(isApiAvailable = isAvailable) }
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
    
    fun updateOfflineMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setOfflineModeEnabled(enabled)
            val preferences = _uiState.value.preferences.copy(enableOfflineMode = enabled)
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
    
    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNotificationsEnabled(enabled)
            val preferences = _uiState.value.preferences.copy(enableNotifications = enabled)
            preferencesRepository.updateUserPreferences(preferences)
        }
    }
    
    fun updateSoundEffectsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSoundEffectsEnabled(enabled)
            val preferences = _uiState.value.preferences.copy(enableSoundEffects = enabled)
            preferencesRepository.updateUserPreferences(preferences)
        }
    }
    
    fun updateAutoSaveEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoSaveEnabled(enabled)
            val preferences = _uiState.value.preferences.copy(enableAutoSave = enabled)
            preferencesRepository.updateUserPreferences(preferences)
        }
    }
    
    fun updateAutoSaveInterval(minutes: Int) {
        viewModelScope.launch {
            preferencesRepository.setAutoSaveInterval(minutes)
            val preferences = _uiState.value.preferences.copy(autoSaveInterval = minutes)
            preferencesRepository.updateUserPreferences(preferences)
        }
    }
}

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val isApiAvailable: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
