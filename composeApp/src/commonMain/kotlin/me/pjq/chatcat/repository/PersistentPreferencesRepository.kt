package me.pjq.chatcat.repository

import com.russhwolf.settings.coroutines.FlowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.pjq.chatcat.model.FontSize
import me.pjq.chatcat.model.ModelConfig
import me.pjq.chatcat.model.Theme
import me.pjq.chatcat.model.UserPreferences

/**
 * Implementation of PreferencesRepository that persists data using Multiplatform Settings
 */
class PersistentPreferencesRepository(
    private val settings: FlowSettings
) : PreferencesRepository {
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
    
    // Keys for storing preferences
    private object PreferenceKeys {
        const val API_KEY = "api_key"
        const val API_BASE_URL = "api_base_url"
        const val THEME = "theme"
        const val FONT_SIZE = "font_size"
        const val OFFLINE_MODE = "offline_mode"
        const val DEFAULT_MODEL_CONFIG = "default_model_config"
        const val NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val SOUND_EFFECTS_ENABLED = "sound_effects_enabled"
        const val AUTO_SAVE_ENABLED = "auto_save_enabled"
        const val AUTO_SAVE_INTERVAL = "auto_save_interval"
    }
    
    override suspend fun getUserPreferences(): Flow<UserPreferences> {
        return settings.getStringOrNullFlow(PreferenceKeys.API_KEY).map { _ ->
            UserPreferences(
                apiKey = getApiKey(),
                apiBaseUrl = getApiBaseUrl(),
                theme = getTheme(),
                fontSize = getFontSize(),
                enableOfflineMode = getOfflineModeEnabled(),
                defaultModelConfig = getDefaultModelConfig(),
                enableNotifications = getNotificationsEnabled(),
                enableSoundEffects = getSoundEffectsEnabled(),
                enableAutoSave = getAutoSaveEnabled(),
                autoSaveInterval = getAutoSaveInterval()
            )
        }
    }
    
    override suspend fun updateUserPreferences(preferences: UserPreferences) {
        setApiKey(preferences.apiKey)
        setApiBaseUrl(preferences.apiBaseUrl)
        setTheme(preferences.theme)
        setFontSize(preferences.fontSize)
        setOfflineModeEnabled(preferences.enableOfflineMode)
        setDefaultModelConfig(preferences.defaultModelConfig)
        setNotificationsEnabled(preferences.enableNotifications)
        setSoundEffectsEnabled(preferences.enableSoundEffects)
        setAutoSaveEnabled(preferences.enableAutoSave)
        setAutoSaveInterval(preferences.autoSaveInterval)
    }
    
    override suspend fun getApiKey(): String {
        return settings.getStringOrNull(PreferenceKeys.API_KEY) ?: ""
    }
    
    override suspend fun setApiKey(apiKey: String) {
        settings.putString(PreferenceKeys.API_KEY, apiKey)
    }
    
    override suspend fun getApiBaseUrl(): String {
        return settings.getStringOrNull(PreferenceKeys.API_BASE_URL) ?: "https://api.openai.com/v1"
    }
    
    override suspend fun setApiBaseUrl(baseUrl: String) {
        settings.putString(PreferenceKeys.API_BASE_URL, baseUrl)
    }
    
    override suspend fun getTheme(): Theme {
        val themeName = settings.getStringOrNull(PreferenceKeys.THEME) ?: Theme.SYSTEM.name
        return try {
            Theme.valueOf(themeName)
        } catch (e: IllegalArgumentException) {
            Theme.SYSTEM
        }
    }
    
    override suspend fun setTheme(theme: Theme) {
        settings.putString(PreferenceKeys.THEME, theme.name)
    }
    
    override suspend fun getFontSize(): FontSize {
        val fontSizeName = settings.getStringOrNull(PreferenceKeys.FONT_SIZE) ?: FontSize.MEDIUM.name
        return try {
            FontSize.valueOf(fontSizeName)
        } catch (e: IllegalArgumentException) {
            FontSize.MEDIUM
        }
    }
    
    override suspend fun setFontSize(fontSize: FontSize) {
        settings.putString(PreferenceKeys.FONT_SIZE, fontSize.name)
    }
    
    override suspend fun getOfflineModeEnabled(): Boolean {
        return settings.getBoolean(PreferenceKeys.OFFLINE_MODE, false)
    }
    
    override suspend fun setOfflineModeEnabled(enabled: Boolean) {
        settings.putBoolean(PreferenceKeys.OFFLINE_MODE, enabled)
    }
    
    override suspend fun getDefaultModelConfig(): ModelConfig {
        val modelConfigJson = settings.getStringOrNull(PreferenceKeys.DEFAULT_MODEL_CONFIG)
        return if (modelConfigJson != null) {
            try {
                json.decodeFromString<ModelConfig>(modelConfigJson)
            } catch (e: Exception) {
                ModelConfig()
            }
        } else {
            ModelConfig()
        }
    }
    
    override suspend fun setDefaultModelConfig(modelConfig: ModelConfig) {
        val modelConfigJson = json.encodeToString(modelConfig)
        settings.putString(PreferenceKeys.DEFAULT_MODEL_CONFIG, modelConfigJson)
    }
    
    override suspend fun getNotificationsEnabled(): Boolean {
        return settings.getBoolean(PreferenceKeys.NOTIFICATIONS_ENABLED, true)
    }
    
    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        settings.putBoolean(PreferenceKeys.NOTIFICATIONS_ENABLED, enabled)
    }
    
    override suspend fun getSoundEffectsEnabled(): Boolean {
        return settings.getBoolean(PreferenceKeys.SOUND_EFFECTS_ENABLED, true)
    }
    
    override suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        settings.putBoolean(PreferenceKeys.SOUND_EFFECTS_ENABLED, enabled)
    }
    
    override suspend fun getAutoSaveEnabled(): Boolean {
        return settings.getBoolean(PreferenceKeys.AUTO_SAVE_ENABLED, true)
    }
    
    override suspend fun setAutoSaveEnabled(enabled: Boolean) {
        settings.putBoolean(PreferenceKeys.AUTO_SAVE_ENABLED, enabled)
    }
    
    override suspend fun getAutoSaveInterval(): Int {
        return settings.getInt(PreferenceKeys.AUTO_SAVE_INTERVAL, 5)
    }
    
    override suspend fun setAutoSaveInterval(minutes: Int) {
        settings.putInt(PreferenceKeys.AUTO_SAVE_INTERVAL, minutes)
    }
}
