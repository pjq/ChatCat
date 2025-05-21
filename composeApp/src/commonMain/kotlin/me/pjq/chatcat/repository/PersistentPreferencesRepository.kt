package me.pjq.chatcat.repository

import com.russhwolf.settings.coroutines.FlowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine // Make sure this import is present
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.pjq.chatcat.model.DefaultModelProviders
import me.pjq.chatcat.model.FontSize
import me.pjq.chatcat.model.ModelConfig
import me.pjq.chatcat.model.ModelProvider
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
        const val DEFAULT_MODEL_CONFIG = "default_model_config"
        const val SOUND_EFFECTS_ENABLED = "sound_effects_enabled"
        const val MARKDOWN_ENABLED = "markdown_enabled"
        const val MODEL_PROVIDERS = "model_providers"
        const val ACTIVE_PROVIDER_ID = "active_provider_id"
    }
    
    override suspend fun getUserPreferences(): Flow<UserPreferences> {
        // Combine flows for all relevant preference keys
        return combine(
            settings.getStringOrNullFlow(PreferenceKeys.API_KEY),
            settings.getStringOrNullFlow(PreferenceKeys.API_BASE_URL),
            settings.getStringOrNullFlow(PreferenceKeys.THEME),
            settings.getStringOrNullFlow(PreferenceKeys.FONT_SIZE),
            settings.getStringOrNullFlow(PreferenceKeys.DEFAULT_MODEL_CONFIG),
            settings.getBooleanFlow(PreferenceKeys.SOUND_EFFECTS_ENABLED, true), // Default value for boolean
            settings.getBooleanFlow(PreferenceKeys.MARKDOWN_ENABLED, true),     // Default value for boolean
            settings.getStringOrNullFlow(PreferenceKeys.MODEL_PROVIDERS),
            settings.getStringOrNullFlow(PreferenceKeys.ACTIVE_PROVIDER_ID)
        ) { values ->
            // The 'values' array contains the latest emissions from each combined flow
            // We can now reconstruct UserPreferences using these values, or simply by calling the individual getters
            // which will read the latest persisted state. Calling getters is simpler and less prone to error
            // if the order of combine arguments changes.

            UserPreferences(
                apiKey = getApiKey(), 
                apiBaseUrl = getApiBaseUrl(),
                theme = getTheme(),
                fontSize = getFontSize(),
                defaultModelConfig = getDefaultModelConfig(),
                enableSoundEffects = getSoundEffectsEnabled(),
                enableMarkdown = getMarkdownEnabled(),
                modelProviders = getModelProviders(),
                activeProviderId = getActiveProviderId()
            )
        }
    }
    
    override suspend fun updateUserPreferences(preferences: UserPreferences) {
        setApiKey(preferences.apiKey)
        setApiBaseUrl(preferences.apiBaseUrl)
        setTheme(preferences.theme)
        setFontSize(preferences.fontSize)
        setDefaultModelConfig(preferences.defaultModelConfig)
        setSoundEffectsEnabled(preferences.enableSoundEffects)
        setMarkdownEnabled(preferences.enableMarkdown)
        setModelProviders(preferences.modelProviders)
        setActiveProviderId(preferences.activeProviderId)
    }
    
    private suspend fun getModelProviders(): List<ModelProvider> {
        val providersJson = settings.getStringOrNull(PreferenceKeys.MODEL_PROVIDERS)
        return if (providersJson != null) {
            try {
                json.decodeFromString<List<ModelProvider>>(providersJson)
            } catch (e: Exception) {
                println("Error decoding ModelProviders: ${e.message}")
                DefaultModelProviders.getDefaultProviders()
            }
        } else {
            DefaultModelProviders.getDefaultProviders()
        }
    }
    
    private suspend fun setModelProviders(providers: List<ModelProvider>) {
        try {
            val providersJson = json.encodeToString(providers)
            settings.putString(PreferenceKeys.MODEL_PROVIDERS, providersJson)
            println("Saved ModelProviders: $providersJson")
        } catch (e: Exception) {
            println("Error encoding ModelProviders: ${e.message}")
        }
    }
    
    override suspend fun getActiveProviderId(): String {
        return settings.getStringOrNull(PreferenceKeys.ACTIVE_PROVIDER_ID) ?: DefaultModelProviders.OPENAI.id
    }
    
    override suspend fun setActiveProviderId(providerId: String) {
        settings.putString(PreferenceKeys.ACTIVE_PROVIDER_ID, providerId)
        println("Active provider ID saved: $providerId")
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
    
    override suspend fun getDefaultModelConfig(): ModelConfig {
        val modelConfigJson = settings.getStringOrNull(PreferenceKeys.DEFAULT_MODEL_CONFIG)
        return if (modelConfigJson != null) {
            try {
                val modelConfig = json.decodeFromString<ModelConfig>(modelConfigJson)
                // Ensure we're actually using the saved stream value
                modelConfig
            } catch (e: Exception) {
                // Log the error if possible
                println("Error decoding ModelConfig: ${e.message}")
                // Return default config if there's an error
                ModelConfig()
            }
        } else {
            ModelConfig()
        }
    }
    
    override suspend fun setDefaultModelConfig(modelConfig: ModelConfig) {
        try {
            val modelConfigJson = json.encodeToString(modelConfig)
            settings.putString(PreferenceKeys.DEFAULT_MODEL_CONFIG, modelConfigJson)
            // Verify the save worked by logging
            println("Saved ModelConfig: $modelConfigJson")
        } catch (e: Exception) {
            // Log the error if possible
            println("Error encoding ModelConfig: ${e.message}")
        }
    }
    
    override suspend fun getSoundEffectsEnabled(): Boolean {
        return settings.getBoolean(PreferenceKeys.SOUND_EFFECTS_ENABLED, true)
    }
    
    override suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        settings.putBoolean(PreferenceKeys.SOUND_EFFECTS_ENABLED, enabled)
    }
    
    override suspend fun getMarkdownEnabled(): Boolean {
        return settings.getBoolean(PreferenceKeys.MARKDOWN_ENABLED, true)
    }
    
    override suspend fun setMarkdownEnabled(enabled: Boolean) {
        settings.putBoolean(PreferenceKeys.MARKDOWN_ENABLED, enabled)
    }
}
