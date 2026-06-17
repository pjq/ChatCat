package me.pjq.chatcat.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import me.pjq.chatcat.model.ContentPart
import me.pjq.chatcat.model.ImageSource
import me.pjq.chatcat.model.McpTool
import me.pjq.chatcat.model.Message
import me.pjq.chatcat.model.ModelConfig
import me.pjq.chatcat.model.Role
import me.pjq.chatcat.platform.randomUUID
import me.pjq.chatcat.repository.PreferencesRepository

class OpenAIChatService(
    private val preferencesRepository: PreferencesRepository,
    private val httpClient: HttpClient = defaultHttpClient()
) : ChatService {

    private var currentJob: Job? = null

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
    }

    override suspend fun sendMessage(
        messages: List<Message>,
        modelConfig: ModelConfig,
        availableTools: List<McpTool>,
        systemPrompt: String?
    ): Flow<Result<Message>> = flow {
        val prefs = preferencesRepository.getUserPreferencesSync()
        val provider = prefs.modelProviders.firstOrNull { it.id == prefs.activeProviderId }
        if (provider == null) {
            emit(Result.failure(IllegalStateException("No active provider configured")))
            return@flow
        }
        val apiKey = provider.apiKey.ifBlank { prefs.apiKey }
        if (apiKey.isBlank()) {
            emit(Result.failure(IllegalStateException("API key is empty for provider ${provider.name}")))
            return@flow
        }
        val model = provider.selectedModel.ifBlank { provider.availableModels.firstOrNull() ?: "gpt-4o" }
        val baseUrl = provider.baseUrl.trimEnd('/')

        val payload = buildChatPayload(
            model = model,
            messages = messages,
            modelConfig = modelConfig,
            tools = availableTools,
            systemPrompt = systemPrompt,
            stream = modelConfig.stream
        )

        try {
            if (modelConfig.stream) {
                streamChat(baseUrl, apiKey, payload, model).collect { emit(it) }
            } else {
                emit(blockingChat(baseUrl, apiKey, payload, model))
            }
        } catch (t: Throwable) {
            emit(Result.failure(t))
        }
    }.onStart { currentJob = currentCoroutineContext()[Job] }

    private suspend fun blockingChat(
        baseUrl: String,
        apiKey: String,
        payload: JsonObject,
        model: String
    ): Result<Message> = runCatching {
        val response = httpClient.post("$baseUrl/chat/completions") {
            applyAuth(apiKey)
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(JsonObject.serializer(), payload))
        }
        if (!response.status.isSuccess()) {
            error("HTTP ${response.status.value}: ${response.body<String>().take(400)}")
        }
        val body = response.body<String>()
        val parsed = json.parseToJsonElement(body).jsonObject
        val text = parsed["choices"]?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("message")
            ?.jsonObject?.get("content")
            ?.jsonPrimitive?.contentOrNullSafe()
            ?: ""
        Message.text(id = randomUUID(), role = Role.ASSISTANT, text = text).copy(modelName = model)
    }

    private fun streamChat(
        baseUrl: String,
        apiKey: String,
        payload: JsonObject,
        model: String
    ): Flow<Result<Message>> = flow {
        val messageId = randomUUID()
        val buffer = StringBuilder()
        try {
            httpClient.preparePost("$baseUrl/chat/completions") {
                applyAuth(apiKey)
                contentType(ContentType.Application.Json)
                headers { append(HttpHeaders.Accept, "text/event-stream") }
                setBody(json.encodeToString(JsonObject.serializer(), payload))
            }.execute { response: HttpResponse ->
                if (!response.status.isSuccess()) {
                    val errBody = runCatching { response.body<String>() }.getOrDefault("")
                    throw RuntimeException("HTTP ${response.status.value}: ${errBody.take(400)}")
                }
                val channel = response.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: break
                    if (!line.startsWith("data:")) continue
                    val data = line.removePrefix("data:").trim()
                    if (data == "[DONE]") break
                    if (data.isEmpty()) continue
                    val element = runCatching { json.parseToJsonElement(data).jsonObject }.getOrNull() ?: continue
                    val delta = element["choices"]?.jsonArray?.firstOrNull()
                        ?.jsonObject?.get("delta")?.jsonObject
                    val chunk = delta?.get("content")?.jsonPrimitive?.contentOrNullSafe()
                    if (!chunk.isNullOrEmpty()) {
                        buffer.append(chunk)
                        emit(
                            Result.success(
                                Message.text(messageId, Role.ASSISTANT, buffer.toString())
                                    .copy(modelName = model)
                            )
                        )
                    }
                }
            }
            if (buffer.isEmpty()) emit(Result.failure(RuntimeException("Empty stream from $model")))
        } catch (t: Throwable) {
            emit(Result.failure(t))
        }
    }

    private fun HttpRequestBuilder.applyAuth(apiKey: String) {
        headers { append(HttpHeaders.Authorization, "Bearer $apiKey") }
    }

    private fun buildChatPayload(
        model: String,
        messages: List<Message>,
        modelConfig: ModelConfig,
        tools: List<McpTool>,
        systemPrompt: String?,
        stream: Boolean
    ): JsonObject = buildJsonObject {
        put("model", model)
        put("temperature", modelConfig.temperature)
        put("max_tokens", modelConfig.maxTokens)
        put("top_p", modelConfig.topP)
        put("stream", stream)
        putJsonArray("messages") {
            if (!systemPrompt.isNullOrBlank()) {
                add(buildJsonObject {
                    put("role", "system")
                    put("content", systemPrompt)
                })
            }
            for (msg in messages) add(toJsonMessage(msg))
        }
        if (tools.isNotEmpty()) {
            putJsonArray("tools") {
                for (t in tools) {
                    add(buildJsonObject {
                        put("type", "function")
                        putJsonObject("function") {
                            put("name", t.name)
                            put("description", t.description)
                            put("parameters", parseSchema(t.inputSchemaJson))
                        }
                    })
                }
            }
        }
    }

    private fun parseSchema(raw: String): JsonElement = runCatching { json.parseToJsonElement(raw) }
        .getOrDefault(buildJsonObject { put("type", "object") })

    private fun toJsonMessage(message: Message): JsonObject = buildJsonObject {
        put("role", when (message.role) {
            Role.USER -> "user"
            Role.ASSISTANT -> "assistant"
            Role.SYSTEM -> "system"
            Role.TOOL -> "tool"
        })
        val onlyText = message.parts.size == 1 && message.parts.first() is ContentPart.Text
        if (onlyText) {
            put("content", (message.parts.first() as ContentPart.Text).text)
        } else {
            putJsonArray("content") {
                for (part in message.parts) when (part) {
                    is ContentPart.Text -> add(buildJsonObject {
                        put("type", "text")
                        put("text", part.text)
                    })
                    is ContentPart.Image -> add(buildJsonObject {
                        put("type", "image_url")
                        putJsonObject("image_url") {
                            put("url", imageUrlOf(part))
                        }
                    })
                    is ContentPart.ToolCall -> {
                        // tool_calls are surfaced via the dedicated field on assistant messages;
                        // they don't appear inline as content parts in the wire format.
                    }
                }
            }
        }
    }

    private fun imageUrlOf(image: ContentPart.Image): String = when (val src = image.source) {
        is ImageSource.Url -> src.url
        is ImageSource.Base64 -> "data:${image.mimeType};base64,${src.data}"
        is ImageSource.Local -> "data:${image.mimeType};base64,"
    }

    override fun cancelRequest() {
        currentJob?.cancel()
        currentJob = null
    }

    override suspend fun isApiAvailable(): Boolean = runCatching {
        val prefs = preferencesRepository.getUserPreferencesSync()
        val provider = prefs.modelProviders.firstOrNull { it.id == prefs.activeProviderId } ?: return false
        val apiKey = provider.apiKey.ifBlank { prefs.apiKey }
        if (apiKey.isBlank()) return false
        val response = httpClient.get("${provider.baseUrl.trimEnd('/')}/models") { applyAuth(apiKey) }
        response.status.isSuccess()
    }.getOrDefault(false)

    /** Test an arbitrary baseUrl + apiKey and return discovered models. Used by the provider editor. */
    suspend fun probe(baseUrl: String, apiKey: String): Result<List<String>> = runCatching {
        val response = httpClient.get("${baseUrl.trimEnd('/')}/models") {
            if (apiKey.isNotBlank()) headers { append(HttpHeaders.Authorization, "Bearer $apiKey") }
        }
        if (!response.status.isSuccess()) {
            error("HTTP ${response.status.value}: ${response.body<String>().take(200)}")
        }
        val body = response.body<String>()
        val data = json.parseToJsonElement(body).jsonObject["data"]?.jsonArray ?: return@runCatching emptyList()
        data.mapNotNull { it.jsonObject["id"]?.jsonPrimitive?.contentOrNullSafe() }.sorted()
    }

    override suspend fun listModels(): List<String> = runCatching {
        val prefs = preferencesRepository.getUserPreferencesSync()
        val provider = prefs.modelProviders.firstOrNull { it.id == prefs.activeProviderId } ?: return emptyList()
        val apiKey = provider.apiKey.ifBlank { prefs.apiKey }
        if (apiKey.isBlank()) return provider.availableModels
        val response = httpClient.get("${provider.baseUrl.trimEnd('/')}/models") { applyAuth(apiKey) }
        if (!response.status.isSuccess()) return provider.availableModels
        val body = response.body<String>()
        val data = json.parseToJsonElement(body).jsonObject["data"]?.jsonArray ?: return provider.availableModels
        data.mapNotNull { it.jsonObject["id"]?.jsonPrimitive?.contentOrNullSafe() }
    }.getOrDefault(emptyList())

    companion object {
        fun defaultHttpClient(): HttpClient = HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = 120_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 120_000
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
        }
    }
}

private fun JsonPrimitive.contentOrNullSafe(): String? = if (isString) content else content.takeIf { it != "null" }
