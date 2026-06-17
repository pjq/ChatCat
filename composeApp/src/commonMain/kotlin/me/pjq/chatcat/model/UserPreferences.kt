package me.pjq.chatcat.model

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val apiKey: String = "",
    val apiBaseUrl: String = "https://api.openai.com/v1",
    val theme: Theme = Theme.SYSTEM,
    val accent: Accent = Accent.INDIGO,
    val useDynamicColor: Boolean = true,
    val fontSize: FontSize = FontSize.MEDIUM,
    val density: Density = Density.COMFORTABLE,
    val language: Language = Language.ENGLISH,
    val enableOfflineMode: Boolean = false,
    val defaultModelConfig: ModelConfig = ModelConfig(),
    val enableNotifications: Boolean = true,
    val enableSoundEffects: Boolean = false,
    val enableAutoSave: Boolean = true,
    val autoSaveInterval: Int = 5,
    val enableMarkdown: Boolean = true,
    val sendOnEnter: Boolean = false,
    val modelProviders: List<ModelProvider> = DefaultModelProviders.getDefaultProviders(),
    val activeProviderId: String = DefaultModelProviders.OPENAI.id,
    val mcpServers: List<McpServer> = emptyList(),
    val systemPrompt: String = ""
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
        fun fromCode(code: String): Language = values().find { it.code == code } ?: ENGLISH
    }
}

@Serializable
enum class Theme { LIGHT, DARK, SYSTEM }

@Serializable
enum class Accent { INDIGO, ROSE, EMERALD, AMBER, OCEAN }

@Serializable
enum class FontSize { SMALL, MEDIUM, LARGE, EXTRA_LARGE }

@Serializable
enum class Density { COMPACT, COMFORTABLE, SPACIOUS }
