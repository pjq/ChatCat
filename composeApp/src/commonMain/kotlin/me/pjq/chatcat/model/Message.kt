package me.pjq.chatcat.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val content: String,
    val role: Role,
    val timestamp: Instant = Clock.System.now(),
    val attachments: List<Attachment> = emptyList(),
    val isError: Boolean = false
)

@Serializable
enum class Role {
    USER, ASSISTANT, SYSTEM
}

@Serializable
data class Attachment(
    val id: String,
    val name: String,
    val type: String,
    val url: String? = null,
    val localPath: String? = null,
    val size: Long? = null
)
