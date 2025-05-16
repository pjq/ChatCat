package me.pjq.chatcat.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.pjq.chatcat.model.FontSize
import me.pjq.chatcat.model.ModelConfig
import me.pjq.chatcat.model.Theme
import me.pjq.chatcat.model.UserPreferences

class PreferencesRepositoryImpl : PreferencesRepository {
    
    // In-memory preferences
    private var preferences = UserPreferences()
    
    override suspend fun getUserPreferences(): Flow<UserPreferences> {
        return flowOf(preferences)
    }
    
    override suspend fun updateUserPreferences(preferences: UserPreferences) {
        this.preferences = preferences
    }
    
    override suspend fun getApiKey(): String {
        return preferences.apiKey
    }
    
    override suspend fun setApiKey(apiKey: String) {
        preferences = preferences.copy(apiKey = apiKey)
    }
    
    override suspend fun getApiBaseUrl(): String {
        return preferences.apiBaseUrl
    }
    
    override suspend fun setApiBaseUrl(baseUrl: String) {
        preferences = preferences.copy(apiBaseUrl = baseUrl)
    }
    
    override suspend fun getTheme(): Theme {
        return preferences.theme
    }
    
    override suspend fun setTheme(theme: Theme) {
        preferences = preferences.copy(theme = theme)
    }
    
    override suspend fun getFontSize(): FontSize {
        return preferences.fontSize
    }
    
    override suspend fun setFontSize(fontSize: FontSize) {
        preferences = preferences.copy(fontSize = fontSize)
    }
    
    override suspend fun getDefaultModelConfig(): ModelConfig {
        return preferences.defaultModelConfig
    }
    
    override suspend fun setDefaultModelConfig(modelConfig: ModelConfig) {
        preferences = preferences.copy(defaultModelConfig = modelConfig)
    }
    
    override suspend fun getSoundEffectsEnabled(): Boolean {
        return preferences.enableSoundEffects
    }
    
    override suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        preferences = preferences.copy(enableSoundEffects = enabled)
    }
    
    override suspend fun getMarkdownEnabled(): Boolean {
        return preferences.enableMarkdown
    }
    
    override suspend fun setMarkdownEnabled(enabled: Boolean) {
        preferences = preferences.copy(enableMarkdown = enabled)
    }
    
    override suspend fun getActiveProviderId(): String {
        return preferences.activeProviderId
    }
    
    override suspend fun setActiveProviderId(providerId: String) {
        preferences = preferences.copy(activeProviderId = providerId)
        println("In-memory active provider ID updated: $providerId")
    }
}
