package me.pjq.chatcat.model

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val apiKey: String = "",
    val apiBaseUrl: String = "https://api.openai.com/v1",
    val theme: Theme = Theme.SYSTEM,
    val fontSize: FontSize = FontSize.MEDIUM,
    val enableOfflineMode: Boolean = false,
    val defaultModelConfig: ModelConfig = ModelConfig(),
    val enableNotifications: Boolean = true,
    val enableSoundEffects: Boolean = true,
    val enableAutoSave: Boolean = true,
    val autoSaveInterval: Int = 5 // in minutes
)

@Serializable
enum class Theme {
    LIGHT, DARK, SYSTEM
}

@Serializable
enum class FontSize {
    SMALL, MEDIUM, LARGE, EXTRA_LARGE
}
