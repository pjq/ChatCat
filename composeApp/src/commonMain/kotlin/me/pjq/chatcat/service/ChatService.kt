package me.pjq.chatcat.service

import kotlinx.coroutines.flow.Flow
import me.pjq.chatcat.model.McpTool
import me.pjq.chatcat.model.Message
import me.pjq.chatcat.model.ModelConfig

interface ChatService {
    /** Streams partial assistant messages as the model produces them. */
    suspend fun sendMessage(
        messages: List<Message>,
        modelConfig: ModelConfig,
        availableTools: List<McpTool> = emptyList(),
        systemPrompt: String? = null
    ): Flow<Result<Message>>

    fun cancelRequest()

    suspend fun isApiAvailable(): Boolean

    suspend fun listModels(): List<String>
}
