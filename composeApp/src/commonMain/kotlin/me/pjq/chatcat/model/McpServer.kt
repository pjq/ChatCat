package me.pjq.chatcat.model

import kotlinx.serialization.Serializable

@Serializable
data class McpServer(
    val id: String,
    val name: String,
    val transport: McpTransport = McpTransport.HTTP,
    val url: String = "",
    val headers: Map<String, String> = emptyMap(),
    val command: String = "",
    val args: List<String> = emptyList(),
    val isEnabled: Boolean = true,
    val description: String = "",
    val knownTools: List<McpTool> = emptyList()
)

@Serializable
enum class McpTransport { HTTP, SSE, STDIO }

@Serializable
data class McpTool(
    val name: String,
    val description: String = "",
    val inputSchemaJson: String = "{}",
    val serverId: String = ""
)

object DefaultMcpServers {
    fun examples(): List<McpServer> = listOf(
        McpServer(
            id = "example-fetch",
            name = "Fetch (example)",
            transport = McpTransport.HTTP,
            url = "https://example-mcp.workers.dev/mcp",
            isEnabled = false,
            description = "Example HTTP MCP server. Edit the URL to point at your own."
        )
    )
}
