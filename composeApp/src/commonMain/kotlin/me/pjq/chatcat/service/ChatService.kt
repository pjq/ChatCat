package me.pjq.chatcat.service

import kotlinx.coroutines.flow.Flow
import me.pjq.chatcat.model.Message
import me.pjq.chatcat.model.ModelConfig

interface ChatService {
    /**
     * Sends a message to the AI and returns the response as a flow of message chunks
     */
    suspend fun sendMessage(
        messages: List<Message>,
        modelConfig: ModelConfig
    ): Flow<Result<Message>>
    
    /**
     * Sends a message to the AI and returns the complete response
     */
    suspend fun sendMessageSync(
        messages: List<Message>,
        modelConfig: ModelConfig
    ): Result<Message>
    
    /**
     * Cancels any ongoing request
     */
    fun cancelRequest()
    
    /**
     * Checks if the API is available
     */
    suspend fun isApiAvailable(): Boolean
}
