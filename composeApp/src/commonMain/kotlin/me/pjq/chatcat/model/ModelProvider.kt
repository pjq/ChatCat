package me.pjq.chatcat.model

import kotlinx.serialization.Serializable

@Serializable
data class ModelProvider(
    val id: String,
    val name: String,
    val baseUrl: String,
    val apiKey: String = "",
    val isEnabled: Boolean = true,
    val providerType: ProviderType = ProviderType.OPENAI,
    val availableModels: List<String> = emptyList(),
    val isDefault: Boolean = false,
    val selectedModel: String = "",
    val visionModels: List<String> = emptyList(),
    val imageGenModels: List<String> = emptyList(),
    val supportsTools: Boolean = true
) {
    fun supportsVision(model: String = selectedModel): Boolean =
        model in visionModels || model.contains("vision") || model.startsWith("gpt-4o") ||
            model.startsWith("gpt-4.1") || model.startsWith("claude-3") || model.contains("gemini")

    fun supportsImageGeneration(): Boolean = imageGenModels.isNotEmpty()
}

@Serializable
enum class ProviderType {
    OPENAI,
    OPENAI_COMPATIBLE,
    ANTHROPIC,
    CUSTOM
}

data class ProviderPreset(
    val id: String,
    val name: String,
    val emoji: String,
    val baseUrl: String,
    val providerType: ProviderType,
    val suggestedModels: List<String>,
    val visionModels: List<String> = emptyList(),
    val imageGenModels: List<String> = emptyList(),
    val docsUrl: String? = null,
    val description: String = ""
) {
    fun toProvider(idSuffix: String): ModelProvider = ModelProvider(
        id = "${id}_$idSuffix",
        name = name,
        baseUrl = baseUrl,
        providerType = providerType,
        availableModels = suggestedModels,
        visionModels = visionModels,
        imageGenModels = imageGenModels,
        selectedModel = suggestedModels.firstOrNull().orEmpty()
    )
}

object DefaultModelProviders {
    val OPENAI = ModelProvider(
        id = "openai",
        name = "OpenAI",
        baseUrl = "https://api.openai.com/v1",
        providerType = ProviderType.OPENAI,
        isDefault = true,
        selectedModel = "gpt-5",
        availableModels = listOf("gpt-5.5", "gpt-5.4", "gpt-5", "gpt-5.3-codex", "gpt-4o", "gpt-4o-mini"),
        visionModels = listOf("gpt-5.5", "gpt-5.4", "gpt-5", "gpt-4o", "gpt-4o-mini"),
        imageGenModels = listOf("gpt-image-1", "dall-e-3")
    )

    val LOCAL_AI = ModelProvider(
        id = "localai",
        name = "Local (LM Studio / Ollama)",
        baseUrl = "http://localhost:1234/v1",
        providerType = ProviderType.OPENAI_COMPATIBLE,
        isEnabled = false,
        selectedModel = ""
    )

    fun getDefaultProviders(): List<ModelProvider> = listOf(OPENAI, LOCAL_AI)

    val presets: List<ProviderPreset> = listOf(
        ProviderPreset(
            id = "openai",
            name = "OpenAI",
            emoji = "🟢",
            baseUrl = "https://api.openai.com/v1",
            providerType = ProviderType.OPENAI,
            suggestedModels = listOf("gpt-5.5", "gpt-5.4", "gpt-5", "gpt-5.3-codex", "gpt-4o", "gpt-4o-mini"),
            visionModels = listOf("gpt-5.5", "gpt-5.4", "gpt-5", "gpt-4o", "gpt-4o-mini"),
            imageGenModels = listOf("gpt-image-1", "dall-e-3"),
            docsUrl = "https://platform.openai.com/api-keys",
            description = "GPT-5 series with vision + image generation."
        ),
        ProviderPreset(
            id = "anthropic",
            name = "Anthropic (Claude)",
            emoji = "🟣",
            baseUrl = "https://api.anthropic.com/v1",
            providerType = ProviderType.ANTHROPIC,
            suggestedModels = listOf("anthropic--claude-4.7-opus", "anthropic--claude-4.6-opus", "anthropic--claude-4.6-sonnet", "anthropic--claude-4.5-sonnet", "anthropic--claude-4.5-opus"),
            visionModels = listOf("anthropic--claude-4.7-opus", "anthropic--claude-4.6-opus", "anthropic--claude-4.6-sonnet", "anthropic--claude-4.5-sonnet", "anthropic--claude-4.5-opus"),
            docsUrl = "https://console.anthropic.com/settings/keys",
            description = "Claude 4.x models. Vision supported."
        ),
        ProviderPreset(
            id = "gemini",
            name = "Google Gemini",
            emoji = "💎",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta/openai",
            providerType = ProviderType.OPENAI_COMPATIBLE,
            suggestedModels = listOf("gemini-2.5-pro"),
            visionModels = listOf("gemini-2.5-pro"),
            docsUrl = "https://aistudio.google.com/apikey",
            description = "Gemini 2.5 Pro with vision support."
        ),
        ProviderPreset(
            id = "groq",
            name = "Groq",
            emoji = "⚡",
            baseUrl = "https://api.groq.com/openai/v1",
            providerType = ProviderType.OPENAI_COMPATIBLE,
            suggestedModels = listOf("llama-3.3-70b-versatile", "llama-3.1-70b-versatile", "mixtral-8x7b-32768"),
            docsUrl = "https://console.groq.com/keys",
            description = "Ultra-fast inference for open models."
        ),
        ProviderPreset(
            id = "together",
            name = "Together AI",
            emoji = "🤝",
            baseUrl = "https://api.together.xyz/v1",
            providerType = ProviderType.OPENAI_COMPATIBLE,
            suggestedModels = listOf("meta-llama/Llama-3.3-70B-Instruct-Turbo", "Qwen/Qwen2.5-72B-Instruct-Turbo"),
            docsUrl = "https://api.together.xyz/settings/api-keys",
            description = "Hosted open-weight models."
        ),
        ProviderPreset(
            id = "ollama",
            name = "Ollama (local)",
            emoji = "🦙",
            baseUrl = "http://localhost:11434/v1",
            providerType = ProviderType.OPENAI_COMPATIBLE,
            suggestedModels = listOf("llama3.2", "qwen2.5", "mistral"),
            docsUrl = "https://ollama.com/download",
            description = "Run models locally. No API key needed."
        ),
        ProviderPreset(
            id = "lmstudio",
            name = "LM Studio (local)",
            emoji = "🧪",
            baseUrl = "http://localhost:1234/v1",
            providerType = ProviderType.OPENAI_COMPATIBLE,
            suggestedModels = emptyList(),
            description = "Local server exposing the OpenAI API. Start the server in LM Studio first."
        ),
        ProviderPreset(
            id = "custom",
            name = "Custom",
            emoji = "🛠",
            baseUrl = "https://",
            providerType = ProviderType.OPENAI_COMPATIBLE,
            suggestedModels = emptyList(),
            description = "Any OpenAI-compatible endpoint."
        )
    )
}
