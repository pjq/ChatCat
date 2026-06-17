package me.pjq.chatcat.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("kind")
sealed class ContentPart {
    @Serializable
    @SerialName("text")
    data class Text(val text: String) : ContentPart()

    @Serializable
    @SerialName("image")
    data class Image(
        val source: ImageSource,
        val mimeType: String = "image/png",
        val caption: String? = null
    ) : ContentPart()

    @Serializable
    @SerialName("tool_call")
    data class ToolCall(
        val id: String,
        val name: String,
        val argumentsJson: String,
        val server: String? = null,
        val status: ToolCallStatus = ToolCallStatus.Pending,
        val resultPreview: String? = null
    ) : ContentPart()
}

@Serializable
sealed class ImageSource {
    @Serializable
    @SerialName("url")
    data class Url(val url: String) : ImageSource()

    @Serializable
    @SerialName("base64")
    data class Base64(val data: String) : ImageSource()

    @Serializable
    @SerialName("local")
    data class Local(val path: String) : ImageSource()
}

@Serializable
enum class ToolCallStatus { Pending, Running, Success, Error }

@Serializable
data class Message(
    val id: String,
    val role: Role,
    val parts: List<ContentPart> = emptyList(),
    val timestamp: Instant = Clock.System.now(),
    val isError: Boolean = false,
    val modelName: String? = null
) {
    val text: String
        get() = parts.filterIsInstance<ContentPart.Text>().joinToString("") { it.text }

    val images: List<ContentPart.Image>
        get() = parts.filterIsInstance<ContentPart.Image>()

    val toolCalls: List<ContentPart.ToolCall>
        get() = parts.filterIsInstance<ContentPart.ToolCall>()

    companion object {
        fun text(id: String, role: Role, text: String, isError: Boolean = false): Message =
            Message(id = id, role = role, parts = listOf(ContentPart.Text(text)), isError = isError)
    }
}

@Serializable
enum class Role { USER, ASSISTANT, SYSTEM, TOOL }
