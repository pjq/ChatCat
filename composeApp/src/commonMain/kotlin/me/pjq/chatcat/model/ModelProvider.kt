package me.pjq.chatcat.model

import kotlinx.serialization.Serializable

/**
 * Represents a model provider such as OpenAI or any OpenAI-compatible API
 */
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
    val selectedModel: String = "" // Store the selected model for this specific provider
)

/**
 * Enum representing the type of model provider
 */
@Serializable
enum class ProviderType {
    OPENAI,           // Official OpenAI API
    OPENAI_COMPATIBLE, // OpenAI-compatible APIs (e.g., LM Studio, LocalAI, etc.)
    CUSTOM            // Custom provider
}

/**
 * Default model providers
 */
object DefaultModelProviders {
    val OPENAI = ModelProvider(
        id = "openai",
        name = "OpenAI",
        baseUrl = "https://api.openai.com/v1",
        providerType = ProviderType.OPENAI,
        isDefault = true,
        selectedModel = "gpt-4o" // Default model for OpenAI
    )
    
    val LOCAL_AI = ModelProvider(
        id = "localai",
        name = "LocalAI",
        baseUrl = "http://localhost:8080/v1",
        providerType = ProviderType.OPENAI_COMPATIBLE,
        isEnabled = false,
        selectedModel = "gpt-4o" // Default model for LocalAI
    )
    
    fun getDefaultProviders(): List<ModelProvider> {
        return listOf(OPENAI, LOCAL_AI)
    }
}
