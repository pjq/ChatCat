package me.pjq.chatcat.model

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val apiKey: String = "",
    val apiBaseUrl: String = "https://api.openai.com/v1",
    val theme: Theme = Theme.SYSTEM,
    val fontSize: FontSize = FontSize.MEDIUM,
    val language: Language = Language.ENGLISH,
    val enableOfflineMode: Boolean = false,
    val defaultModelConfig: ModelConfig = ModelConfig(),
    val enableNotifications: Boolean = true,
    val enableSoundEffects: Boolean = true,
    val enableAutoSave: Boolean = true,
    val autoSaveInterval: Int = 5, // in minutes
    val enableMarkdown: Boolean = true, // Enable Markdown rendering by default
    val modelProviders: List<ModelProvider> = DefaultModelProviders.getDefaultProviders(),
    val activeProviderId: String = DefaultModelProviders.OPENAI.id
)

@Serializable
enum class Language(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    CHINESE("zh", "简体中文"),
    SPANISH("es", "Español"),
    JAPANESE("ja", "日本語"),
    GERMAN("de", "Deutsch"),
    FRENCH("fr", "Français");
    
    companion object {
        fun fromCode(code: String): Language {
            return values().find { it.code == code } ?: ENGLISH
        }
    }
}

@Serializable
enum class Theme {
    LIGHT, DARK, SYSTEM
}

@Serializable
enum class FontSize {
    SMALL, MEDIUM, LARGE, EXTRA_LARGE
}
