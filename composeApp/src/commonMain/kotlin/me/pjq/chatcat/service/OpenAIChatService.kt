package me.pjq.chatcat.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import me.pjq.chatcat.model.Message
import me.pjq.chatcat.model.ModelConfig
import me.pjq.chatcat.model.Role
import me.pjq.chatcat.model.api.ChatMessage
import me.pjq.chatcat.model.api.ChatRequest
import me.pjq.chatcat.model.api.ChatResponse
import me.pjq.chatcat.model.api.ChatStreamResponse
import me.pjq.chatcat.repository.PreferencesRepository
import java.util.*

class OpenAIChatService(
    private val preferencesRepository: PreferencesRepository
) : ChatService {
    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(jsonConfig)
        }
    }
    
    private var currentJob: Job? = null
    
    override suspend fun sendMessage(
        messages: List<Message>,
        modelConfig: ModelConfig
    ): Flow<Result<Message>> = flow {
        try {
            val apiKey = preferencesRepository.getApiKey()
            val baseUrl = preferencesRepository.getApiBaseUrl()
            
            if (apiKey.isBlank()) {
                emit(Result.failure(Exception("API key is not set")))
                return@flow
            }
            
            val chatMessages = messages.map {
                ChatMessage(
                    role = it.role.name.lowercase(),
                    content = it.content
                )
            }
            
            val request = ChatRequest.fromModelConfig(chatMessages, modelConfig)
            
            if (modelConfig.stream) {
                // Handle streaming response
                val messageId = UUID.randomUUID().toString()
                var contentBuilder = StringBuilder()
                
                val response = client.preparePost("$baseUrl/chat/completions") {
                    contentType(ContentType.Application.Json)
                    header("Authorization", "Bearer $apiKey")
                    header("Accept", "text/event-stream")
                    setBody(request)
                }.execute()
                
                if (response.status.isSuccess()) {
                    val channel = response.bodyAsChannel()
                    val buffer = ByteArray(1024)
                    
                    while (!channel.isClosedForRead) {
                        val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
                        if (bytesRead < 0) break
                        
                        val chunk = buffer.decodeToString(0, bytesRead)
                        val lines = chunk.split("\n")
                        
                        for (line in lines) {
                            if (line.startsWith("data: ")) {
                                val jsonData = line.substring(6).trim()
                                
                                if (jsonData == "[DONE]") {
                                    // Stream is complete
                                    continue
                                }
                                
                                try {
                                    val streamResponse = jsonConfig.decodeFromString<ChatStreamResponse>(jsonData)
                                    val deltaContent = streamResponse.choices?.firstOrNull()?.delta?.content
                                    
                                    if (deltaContent != null) {
                                        contentBuilder.append(deltaContent)
                                        val message = Message(
                                            id = messageId,
                                            content = contentBuilder.toString(),
                                            role = Role.ASSISTANT
                                        )
                                        emit(Result.success(message))
                                    }
                                } catch (e: Exception) {
                                    // Skip malformed JSON
                                }
                            }
                        }
                    }
                    
                    // Emit final message if no content was emitted
                    if (contentBuilder.isEmpty()) {
                        emit(Result.failure(Exception("No response from assistant")))
                    }
                } else {
                    emit(Result.failure(Exception("API request failed with status: ${response.status}")))
                }
            } else {
                // Handle non-streaming response
                val response = client.post("$baseUrl/chat/completions") {
                    contentType(ContentType.Application.Json)
                    header("Authorization", "Bearer $apiKey")
                    header("Accept", "application/json")
                    setBody(request)
                }
                
                if (response.status.isSuccess()) {
                    val responseText = response.bodyAsText()
                    val chatResponse = jsonConfig.decodeFromString<ChatResponse>(responseText)
                    val assistantMessage = chatResponse.choices.firstOrNull()?.message
                    
                    if (assistantMessage != null) {
                        val message = Message(
                            id = UUID.randomUUID().toString(),
                            content = assistantMessage.content,
                            role = Role.ASSISTANT
                        )
                        emit(Result.success(message))
                    } else {
                        emit(Result.failure(Exception("No response from assistant")))
                    }
                } else {
                    emit(Result.failure(Exception("API request failed with status: ${response.status}")))
                }
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.onStart {
        currentJob = currentCoroutineContext()[Job]
    }
    
    override suspend fun sendMessageSync(
        messages: List<Message>,
        modelConfig: ModelConfig
    ): Result<Message> {
        return try {
            val apiKey = preferencesRepository.getApiKey()
            val baseUrl = preferencesRepository.getApiBaseUrl()

            if (apiKey.isBlank()) {
                return Result.failure(Exception("API key is not set"))
            }

            val chatMessages = messages.map {
                ChatMessage(
                    role = it.role.name.lowercase(),
                    content = it.content
                )
            }

            val request = ChatRequest.fromModelConfig(chatMessages, modelConfig)

            val response = client.post("$baseUrl/chat/completions") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $apiKey")
                header("Accept", "application/json")
                setBody(request)
            }

            if (response.status.isSuccess()) {
                val responseText = response.bodyAsText()
                val chatResponse = jsonConfig.decodeFromString<ChatResponse>(responseText)
                val assistantMessage = chatResponse.choices.firstOrNull()?.message

                if (assistantMessage != null) {
                    val message = Message(
                        id = UUID.randomUUID().toString(),
                        content = assistantMessage.content,
                        role = Role.ASSISTANT
                    )
                    Result.success(message)
                } else {
                    Result.failure(Exception("No response from assistant"))
                }
            } else {
                Result.failure(Exception("API request failed with status: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun cancelRequest() {
        currentJob?.cancel()
        currentJob = null
    }
    
    override suspend fun isApiAvailable(): Boolean {
        return try {
            val apiKey = preferencesRepository.getApiKey()
            val baseUrl = preferencesRepository.getApiBaseUrl()

            if (apiKey.isBlank()) {
                return false
            }

            val response = client.get("$baseUrl/models") {
                header("Authorization", "Bearer $apiKey")
                header("Accept", "application/json")
            }

            response.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }
}
