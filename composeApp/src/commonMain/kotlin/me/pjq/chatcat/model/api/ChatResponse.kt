package me.pjq.chatcat.model.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
    val id: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage? = null
)

@Serializable
data class Choice(
    val index: Int,
    val message: ChatMessage,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class Usage(
    @SerialName("prompt_tokens") val promptTokens: Int,
    @SerialName("completion_tokens") val completionTokens: Int,
    @SerialName("total_tokens") val totalTokens: Int
)

@Serializable
data class ChatStreamResponse(
    val id: String? = null,
    val created: Long? = null,
    val model: String? = null,
    val choices: List<StreamChoice>? = null
)

@Serializable
data class StreamChoice(
    val index: Int? = null,
    val delta: DeltaMessage? = null,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class DeltaMessage(
    val role: String? = null,
    val content: String? = null
)
