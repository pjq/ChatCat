package me.pjq.chatcat.repository

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import me.pjq.chatcat.model.FontSize
import me.pjq.chatcat.model.ModelConfig
import me.pjq.chatcat.model.Theme
import me.pjq.chatcat.model.UserPreferences

/**
 * Persists [UserPreferences] as a single JSON blob. Simpler than per-field keys
 * and survives schema additions because [Json.ignoreUnknownKeys] is enabled.
 */
@OptIn(ExperimentalSettingsApi::class)
class PersistentPreferencesRepository(
    private val settings: FlowSettings
) : PreferencesRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        classDiscriminator = "kind"
    }

    private object Keys {
        const val PREFS = "user_preferences_v2"
    }

    private fun decode(raw: String?): UserPreferences {
        if (raw.isNullOrBlank()) return UserPreferences()
        return try {
            json.decodeFromString<UserPreferences>(raw)
        } catch (e: Exception) {
            println("Preferences decode failed (${e.message}); resetting to defaults.")
            UserPreferences()
        }
    }

    private suspend fun persist(prefs: UserPreferences) {
        settings.putString(Keys.PREFS, json.encodeToString(prefs))
    }

    override suspend fun getUserPreferences(): Flow<UserPreferences> =
        settings.getStringOrNullFlow(Keys.PREFS).map { decode(it) }

    override fun getUserPreferencesSync(): UserPreferences = runBlocking {
        decode(settings.getStringOrNull(Keys.PREFS))
    }

    override suspend fun updateUserPreferences(preferences: UserPreferences) {
        persist(preferences)
    }

    override suspend fun getApiKey(): String = getUserPreferencesSync().apiKey

    override suspend fun setApiKey(apiKey: String) {
        persist(getUserPreferencesSync().copy(apiKey = apiKey))
    }

    override suspend fun getApiBaseUrl(): String = getUserPreferencesSync().apiBaseUrl

    override suspend fun setApiBaseUrl(baseUrl: String) {
        persist(getUserPreferencesSync().copy(apiBaseUrl = baseUrl))
    }

    override suspend fun getTheme(): Theme = getUserPreferencesSync().theme

    override suspend fun setTheme(theme: Theme) {
        persist(getUserPreferencesSync().copy(theme = theme))
    }

    override suspend fun getFontSize(): FontSize = getUserPreferencesSync().fontSize

    override suspend fun setFontSize(fontSize: FontSize) {
        persist(getUserPreferencesSync().copy(fontSize = fontSize))
    }

    override suspend fun getDefaultModelConfig(): ModelConfig = getUserPreferencesSync().defaultModelConfig

    override suspend fun setDefaultModelConfig(modelConfig: ModelConfig) {
        persist(getUserPreferencesSync().copy(defaultModelConfig = modelConfig))
    }

    override suspend fun getSoundEffectsEnabled(): Boolean = getUserPreferencesSync().enableSoundEffects

    override suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        persist(getUserPreferencesSync().copy(enableSoundEffects = enabled))
    }

    override suspend fun getMarkdownEnabled(): Boolean = getUserPreferencesSync().enableMarkdown

    override suspend fun setMarkdownEnabled(enabled: Boolean) {
        persist(getUserPreferencesSync().copy(enableMarkdown = enabled))
    }

    override suspend fun getActiveProviderId(): String = getUserPreferencesSync().activeProviderId

    override suspend fun setActiveProviderId(providerId: String) {
        persist(getUserPreferencesSync().copy(activeProviderId = providerId))
    }
}
