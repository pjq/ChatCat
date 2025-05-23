package me.pjq.chatcat.model.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.pjq.chatcat.model.ModelConfig

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double? = null,
    @SerialName("max_tokens") val maxTokens: Int? = null,
    @SerialName("top_p") val topP: Double? = null,
    @SerialName("frequency_penalty") val frequencyPenalty: Double? = null,
    @SerialName("presence_penalty") val presencePenalty: Double? = null,
    val stream: Boolean = false
) {
    companion object {
        /**
         * Create a ChatRequest from a ModelConfig and the active provider.
         * 
         * @param messages The list of chat messages
         * @param modelConfig The model configuration settings
         * @param activeProviderModel The model string from the active provider's selectedModel property
         * @param fallbackModel Optional fallback model to use if activeProviderModel is blank
         */
        fun fromModelConfig(
            messages: List<ChatMessage>, 
            modelConfig: ModelConfig,
            activeProviderModel: String,
            fallbackModel: String = "gpt-4o"
        ): ChatRequest {
            // Use the active provider's selected model, with fallback if it's blank
            val effectiveModel = if (activeProviderModel.isNotBlank()) {
                activeProviderModel
            } else {
                fallbackModel
            }
            
            return ChatRequest(
                model = effectiveModel,
                messages = messages,
                temperature = modelConfig.temperature,
                maxTokens = modelConfig.maxTokens,
                topP = modelConfig.topP,
                frequencyPenalty = modelConfig.frequencyPenalty,
                presencePenalty = modelConfig.presencePenalty,
                stream = modelConfig.stream
            )
        }
    }
}

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)
