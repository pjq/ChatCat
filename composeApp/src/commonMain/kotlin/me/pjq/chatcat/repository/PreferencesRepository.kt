package me.pjq.chatcat.repository

import kotlinx.coroutines.flow.Flow
import me.pjq.chatcat.model.FontSize
import me.pjq.chatcat.model.ModelConfig
import me.pjq.chatcat.model.Theme
import me.pjq.chatcat.model.UserPreferences

interface PreferencesRepository {
    suspend fun getUserPreferences(): Flow<UserPreferences>
    suspend fun updateUserPreferences(preferences: UserPreferences)
    
    suspend fun getApiKey(): String
    suspend fun setApiKey(apiKey: String)
    
    suspend fun getApiBaseUrl(): String
    suspend fun setApiBaseUrl(baseUrl: String)
    
    suspend fun getTheme(): Theme
    suspend fun setTheme(theme: Theme)
    
    suspend fun getFontSize(): FontSize
    suspend fun setFontSize(fontSize: FontSize)
    
    suspend fun getDefaultModelConfig(): ModelConfig
    suspend fun setDefaultModelConfig(modelConfig: ModelConfig)
    
    suspend fun getSoundEffectsEnabled(): Boolean
    suspend fun setSoundEffectsEnabled(enabled: Boolean)
    
    suspend fun getMarkdownEnabled(): Boolean
    suspend fun setMarkdownEnabled(enabled: Boolean)
    
    suspend fun getActiveProviderId(): String
    suspend fun setActiveProviderId(providerId: String)
}
