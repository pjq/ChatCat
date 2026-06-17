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
    val modelConfig: ModelConfig = ModelConfig(),
    val pinned: Boolean = false,
    val systemPrompt: String? = null,
    val enabledMcpServerIds: List<String> = emptyList()
)

@Serializable
data class ModelConfig(
    val temperature: Double = 1.0,
    val maxTokens: Int = 4096,
    val topP: Double = 1.0,
    val frequencyPenalty: Double = 0.0,
    val presencePenalty: Double = 0.0,
    val stream: Boolean = true
)
