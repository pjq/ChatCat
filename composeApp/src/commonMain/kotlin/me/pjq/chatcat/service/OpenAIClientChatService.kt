package me.pjq.chatcat.service

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import me.pjq.chatcat.model.Message
import me.pjq.chatcat.model.ModelConfig
import me.pjq.chatcat.model.Role
import me.pjq.chatcat.repository.PreferencesRepository
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

class OpenAIClientChatService(
    private val preferencesRepository: PreferencesRepository
) : ChatService {
    
    private var currentJob: Job? = null
    private var openAI: OpenAI? = null
    private var lastApiKey: String = ""
    //baseUrl
    private var lastBaseUrl: String = ""
    
    private suspend fun getOrCreateOpenAI(): OpenAI {
        val apiKey = preferencesRepository.getApiKey()
        val baseUrl = preferencesRepository.getApiBaseUrl()
        // We're not using baseUrl since we're using the default OpenAI API endpoint
        val normalizedBaseUrl = if (!baseUrl.endsWith("/")) "$baseUrl/" else baseUrl
        
        if (apiKey.isBlank()) {
            throw IllegalStateException("API key is not set")
        }
        
        // Create a new OpenAI client if needed or if the API key has changed
        if (openAI == null || apiKey != lastApiKey || normalizedBaseUrl != lastBaseUrl) {
            val config = OpenAIConfig(
                token = apiKey,
                host = OpenAIHost(normalizedBaseUrl),
                timeout = Timeout(socket = 60.seconds)
            )
            openAI = OpenAI(config)
            lastApiKey = apiKey
            lastBaseUrl = normalizedBaseUrl
        }
        
        return openAI!!
    }
    
    override suspend fun sendMessage(
        messages: List<Message>,
        modelConfig: ModelConfig
    ): Flow<Result<Message>> = flow {
        try {
            val openAI = getOrCreateOpenAI()
            
            val chatMessages = messages.map { message ->
                ChatMessage(
                    role = when (message.role) {
                        Role.USER -> ChatRole.User
                        Role.ASSISTANT -> ChatRole.Assistant
                        Role.SYSTEM -> ChatRole.System
                    },
                    content = message.content
                )
            }
            
            val request = ChatCompletionRequest(
                model = ModelId(modelConfig.model),
                messages = chatMessages,
                temperature = modelConfig.temperature,
                maxTokens = modelConfig.maxTokens,
                topP = modelConfig.topP,
                frequencyPenalty = modelConfig.frequencyPenalty,
                presencePenalty = modelConfig.presencePenalty
            )
            
            if (modelConfig.stream) {
                // Handle streaming response
                val messageId = UUID.randomUUID().toString()
                val contentBuilder = StringBuilder()
                
                openAI.chatCompletions(request)
                    .collect { chunk ->
                        val content = chunk.choices.firstOrNull()?.delta?.content
                        if (content != null) {
                            contentBuilder.append(content)
                            val message = Message(
                                id = messageId,
                                content = contentBuilder.toString(),
                                role = Role.ASSISTANT
                            )
                            emit(Result.success(message))
                        }
                    }
                
                // Emit final message if no content was emitted
                if (contentBuilder.isEmpty()) {
                    emit(Result.failure(Exception("No response from assistant")))
                }
            } else {
                // Handle non-streaming response
//                val completion = openAI.chatCompletion(request)
                val completion: ChatCompletion = openAI.chatCompletion(request)

                val content = completion.choices.firstOrNull()?.message?.content
                
                if (content != null) {
                    val message = Message(
                        id = UUID.randomUUID().toString(),
                        content = content,
                        role = Role.ASSISTANT
                    )
                    emit(Result.success(message))
                } else {
                    emit(Result.failure(Exception("No response from assistant")))
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
            val openAI = getOrCreateOpenAI()
            
            val chatMessages = messages.map { message ->
                ChatMessage(
                    role = when (message.role) {
                        Role.USER -> ChatRole.User
                        Role.ASSISTANT -> ChatRole.Assistant
                        Role.SYSTEM -> ChatRole.System
                    },
                    content = message.content
                )
            }
            
            val request = ChatCompletionRequest(
                model = ModelId(modelConfig.model),
                messages = chatMessages,
                temperature = modelConfig.temperature,
                maxTokens = modelConfig.maxTokens,
                topP = modelConfig.topP,
                frequencyPenalty = modelConfig.frequencyPenalty,
                presencePenalty = modelConfig.presencePenalty
            )
            
            val completion = openAI.chatCompletion(request)
            val content = completion.choices.firstOrNull()?.message?.content
            
            if (content != null) {
                val message = Message(
                    id = UUID.randomUUID().toString(),
                    content = content,
                    role = Role.ASSISTANT
                )
                Result.success(message)
            } else {
                Result.failure(Exception("No response from assistant"))
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
            val openAI = getOrCreateOpenAI()
            // Try to list models as a way to check if the API is available
            openAI.models()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Lists all available models from the OpenAI API
     */
    suspend fun listModels(): List<String> {
        return try {
            val openAI = getOrCreateOpenAI()
            openAI.models().map { it.id.id }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
