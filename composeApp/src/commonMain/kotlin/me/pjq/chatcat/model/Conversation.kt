package me.pjq.chatcat.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Conversation(
    val id: String,
    val title: String,
    val messages: List<Message> = emptyList(),
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
    val modelConfig: ModelConfig = ModelConfig()
)

@Serializable
data class ModelConfig(
    val model: String = "gpt-4o",
    val temperature: Double = 0.7,
    val maxTokens: Int = 2000,
    val topP: Double = 1.0,
    val frequencyPenalty: Double = 0.0,
    val presencePenalty: Double = 0.0,
    val stream: Boolean = true
)
