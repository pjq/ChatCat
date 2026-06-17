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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import me.pjq.chatcat.model.McpServer
import me.pjq.chatcat.model.McpTool
import me.pjq.chatcat.model.McpTransport
import me.pjq.chatcat.platform.randomUUID

/**
 * Minimal MCP client. Speaks JSON-RPC 2.0 over plain HTTP POST against the server URL.
 * Surfaces tools/list and tools/call. STDIO and SSE are not implemented yet — listing
 * tools for those transports returns an empty list rather than failing the chat loop.
 */
class McpClientService(
    private val httpClient: HttpClient = OpenAIChatService.defaultHttpClient()
) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun listTools(server: McpServer): Result<List<McpTool>> = runCatching {
        if (!server.isEnabled) return@runCatching emptyList()
        if (server.transport != McpTransport.HTTP || server.url.isBlank()) {
            return@runCatching server.knownTools
        }
        val response = jsonRpc(server, method = "tools/list", params = buildJsonObject { })
        val result = response["result"]?.jsonObject ?: return@runCatching emptyList()
        val tools = result["tools"]?.jsonArray ?: return@runCatching emptyList()
        tools.mapNotNull { el ->
            val obj = el.jsonObject
            val name = obj["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val desc = obj["description"]?.jsonPrimitive?.content ?: ""
            val schema = obj["inputSchema"]?.toString() ?: "{}"
            McpTool(name = name, description = desc, inputSchemaJson = schema, serverId = server.id)
        }
    }

    suspend fun callTool(server: McpServer, toolName: String, argumentsJson: String): Result<String> = runCatching {
        require(server.isEnabled) { "MCP server ${server.name} is disabled" }
        require(server.transport == McpTransport.HTTP) { "Only HTTP transport is implemented" }

        val args = runCatching { json.parseToJsonElement(argumentsJson) }.getOrNull() ?: buildJsonObject { }
        val params = buildJsonObject {
            put("name", toolName)
            put("arguments", args)
        }
        val response = jsonRpc(server, method = "tools/call", params = params)
        val result = response["result"] ?: return@runCatching response.toString()
        result.toString()
    }

    suspend fun ping(server: McpServer): Result<Unit> = runCatching {
        if (server.transport != McpTransport.HTTP) return@runCatching
        jsonRpc(
            server,
            method = "initialize",
            params = buildJsonObject {
                put("protocolVersion", "2024-11-05")
                putJsonObject("capabilities") { }
                putJsonObject("clientInfo") {
                    put("name", "ChatCat")
                    put("version", "2.0")
                }
            }
        )
    }

    private suspend fun jsonRpc(server: McpServer, method: String, params: JsonElement): JsonObject {
        val body = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", randomUUID())
            put("method", method)
            put("params", params)
        }
        val response = httpClient.post(server.url) {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Accept, "application/json")
                for ((k, v) in server.headers) append(k, v)
            }
            setBody(json.encodeToString(JsonObject.serializer(), body))
        }
        if (!response.status.isSuccess()) {
            error("MCP HTTP ${response.status.value} from ${server.name}: ${response.body<String>().take(300)}")
        }
        val text = response.body<String>()
        return json.parseToJsonElement(text).jsonObject
    }
}
