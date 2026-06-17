package me.pjq.chatcat.service

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import me.pjq.chatcat.model.ContentPart
import me.pjq.chatcat.model.ImageSource
import me.pjq.chatcat.repository.PreferencesRepository

class OpenAIImageGenerationService(
    private val preferencesRepository: PreferencesRepository,
    private val httpClient: HttpClient = OpenAIChatService.defaultHttpClient()
) : ImageGenerationService {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override suspend fun generate(
        prompt: String,
        model: String,
        size: String,
        n: Int
    ): Result<List<ContentPart.Image>> = runCatching {
        val prefs = preferencesRepository.getUserPreferencesSync()
        val provider = prefs.modelProviders.firstOrNull { it.id == prefs.activeProviderId }
            ?: error("No active provider")
        val apiKey = provider.apiKey.ifBlank { prefs.apiKey }
        require(apiKey.isNotBlank()) { "API key required for image generation" }

        val payload = buildJsonObject {
            put("model", model)
            put("prompt", prompt)
            put("size", size)
            put("n", n)
            put("response_format", "b64_json")
        }
        val response = httpClient.post("${provider.baseUrl.trimEnd('/')}/images/generations") {
            headers { append(HttpHeaders.Authorization, "Bearer $apiKey") }
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(JsonObject.serializer(), payload))
        }
        if (!response.status.isSuccess()) {
            error("Image generation failed: HTTP ${response.status.value}: ${response.body<String>().take(400)}")
        }
        val body = response.body<String>()
        val parsed = json.parseToJsonElement(body).jsonObject
        val data = parsed["data"]?.jsonArray ?: error("No data in response")
        data.mapNotNull { entry ->
            val obj = entry.jsonObject
            val b64 = obj["b64_json"]?.jsonPrimitive?.content
            val url = obj["url"]?.jsonPrimitive?.content
            when {
                b64 != null -> ContentPart.Image(
                    source = ImageSource.Base64(b64),
                    mimeType = "image/png",
                    caption = prompt
                )
                url != null -> ContentPart.Image(
                    source = ImageSource.Url(url),
                    mimeType = "image/png",
                    caption = prompt
                )
                else -> null
            }
        }
    }
}
