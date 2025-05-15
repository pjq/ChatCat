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
        fun fromModelConfig(messages: List<ChatMessage>, modelConfig: ModelConfig): ChatRequest {
            return ChatRequest(
                model = modelConfig.model,
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
