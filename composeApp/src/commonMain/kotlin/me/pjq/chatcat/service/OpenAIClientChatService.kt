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
import me.pjq.chatcat.model.ProviderType
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
    private var lastProviderId: String = ""
    
    private suspend fun getOrCreateOpenAI(): OpenAI {
        // Get the current user preferences to access active provider information
        val userPreferences = preferencesRepository.getUserPreferencesSync()
        val activeProviderId = userPreferences.activeProviderId
        
        // Get the active provider
        val activeProvider = userPreferences.modelProviders.find { it.id == activeProviderId }
            ?: throw IllegalStateException("Active provider not found")
            
        val apiKey = activeProvider.apiKey.ifBlank { userPreferences.apiKey }
        val baseUrl = activeProvider.baseUrl
        val normalizedBaseUrl = if (!baseUrl.endsWith("/")) "$baseUrl/" else baseUrl
        
        if (apiKey.isBlank()) {
            throw IllegalStateException("API key is not set for provider: ${activeProvider.name}")
        }
        
        // Create a new OpenAI client if needed or if provider settings have changed
        if (openAI == null || 
            apiKey != lastApiKey || 
            normalizedBaseUrl != lastBaseUrl || 
            activeProviderId != lastProviderId) {
            
            println("Creating new OpenAI client instance for provider: ${activeProvider.name}")
            println("Provider ID: $activeProviderId")
            println("API Base URL: $normalizedBaseUrl")
            println("API Key (obscured): ${if (apiKey.length > 8) apiKey.take(4) + "..." + apiKey.takeLast(4) else "****"}")
            
            val config = OpenAIConfig(
                token = apiKey,
                host = OpenAIHost(normalizedBaseUrl),
                timeout = Timeout(socket = 60.seconds)
            )
            openAI = OpenAI(config)
            lastApiKey = apiKey
            lastBaseUrl = normalizedBaseUrl
            lastProviderId = activeProviderId
            println("OpenAI client initialized successfully for provider: ${activeProvider.name}")
        } else {
            println("Reusing existing OpenAI client instance")
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
            
            // Get the current user preferences to access the active provider
            val currentPrefs = preferencesRepository.getUserPreferencesSync()
            val activeProvider = currentPrefs.modelProviders.find { it.id == currentPrefs.activeProviderId }
                ?: throw IllegalStateException("Active provider not found")
            
            // Get the model from the active provider's selectedModel
            val effectiveModel = if (activeProvider.selectedModel.isNotBlank()) {
                activeProvider.selectedModel
            } else {
                // Fallback to a default model if no model is selected
                when (activeProvider.providerType) {
                    ProviderType.OPENAI -> "gpt-3.5-turbo"
                    ProviderType.OPENAI_COMPATIBLE -> "gpt-3.5-turbo"
                    ProviderType.CUSTOM -> "model1"
                }
            }
            
            println("Using model '${effectiveModel}' from provider '${activeProvider.name}'")
            
            val request = ChatCompletionRequest(
                model = ModelId(effectiveModel),
                messages = chatMessages,
                temperature = modelConfig.temperature,
                maxTokens = modelConfig.maxTokens,
                topP = modelConfig.topP,
                frequencyPenalty = modelConfig.frequencyPenalty,
                presencePenalty = modelConfig.presencePenalty,
//                stream = modelConfig.stream  // Make sure stream mode is passed to the API request
            )
            
            // Log the request payload with stream mode explicitly mentioned
            println("OpenAI API Request Payload:")
            println("Model: ${effectiveModel} (from provider: ${activeProvider.name})")
            println("Stream Mode: ${modelConfig.stream}")
            println("Temperature: ${modelConfig.temperature}")
            println("MaxTokens: ${modelConfig.maxTokens}")
            println("Messages: ${chatMessages.size} messages")
            chatMessages.forEachIndexed { index, message ->
                println("- Message ${index + 1}: Role=${message.role}, Content length=${message.content?.length}")
//                if (message.content.length < 100) {
//                    println("  Content: ${message.content}")
//                }
            }
            
            if (modelConfig.stream) {
                // Handle streaming response
                val messageId = UUID.randomUUID().toString()
                val contentBuilder = StringBuilder()
                var streamChunks = 0
                
                println("Starting streaming request...")
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
                            streamChunks++
                        }
                    }
                
                // Log the streaming response summary
                println("Streaming complete. Received $streamChunks chunks, total length: ${contentBuilder.length}")
                
                // Emit final message if no content was emitted
                if (contentBuilder.isEmpty()) {
                    println("ERROR: No content received in streaming response")
                    emit(Result.failure(Exception("No response from assistant")))
                }
            } else {
                // Handle non-streaming response
//                val completion = openAI.chatCompletion(request)
                println("Sending non-streaming request...")
                val completion: ChatCompletion = openAI.chatCompletion(request)

                val content = completion.choices.firstOrNull()?.message?.content
                
                println("Response received, model: ${completion.model?.id}, choices: ${completion.choices.size}")
                println("Response usage: ${completion.usage?.promptTokens} prompt tokens, " +
                       "${completion.usage?.completionTokens} completion tokens, " +
                       "${completion.usage?.totalTokens} total tokens")
                
                if (content != null) {
                    println("Response content length: ${content.length}")
                    val message = Message(
                        id = UUID.randomUUID().toString(),
                        content = content,
                        role = Role.ASSISTANT
                    )
                    emit(Result.success(message))
                } else {
                    println("ERROR: No content received in non-streaming response")
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
            
            // Get the current user preferences to access the active provider
            val currentPrefs = preferencesRepository.getUserPreferencesSync()
            val activeProvider = currentPrefs.modelProviders.find { it.id == currentPrefs.activeProviderId }
                ?: throw IllegalStateException("Active provider not found")
            
            // Get the model from the active provider's selectedModel
            val effectiveModel = if (activeProvider.selectedModel.isNotBlank()) {
                activeProvider.selectedModel
            } else {
                // Fallback to a default model if no model is selected
                when (activeProvider.providerType) {
                    ProviderType.OPENAI -> "gpt-3.5-turbo"
                    ProviderType.OPENAI_COMPATIBLE -> "gpt-3.5-turbo"
                    ProviderType.CUSTOM -> "model1"
                }
            }
            
            println("Using model '${effectiveModel}' from provider '${activeProvider.name}' (Sync method)")
            
            val request = ChatCompletionRequest(
                model = ModelId(effectiveModel),
                messages = chatMessages,
                temperature = modelConfig.temperature,
                maxTokens = modelConfig.maxTokens,
                topP = modelConfig.topP,
                frequencyPenalty = modelConfig.frequencyPenalty,
                presencePenalty = modelConfig.presencePenalty,
//                stream = modelConfig.stream  // Make sure stream mode is passed to the API request
            )
            
            // Log the request payload for sync requests with stream mode explicitly mentioned
            println("OpenAI API Request Payload (Sync Method):")
            println("Model: ${effectiveModel} (from provider: ${activeProvider.name})")
            println("Stream Mode: ${modelConfig.stream}")
            println("Temperature: ${modelConfig.temperature}")
            println("MaxTokens: ${modelConfig.maxTokens}")
            println("Messages: ${chatMessages.size} messages")
            chatMessages.forEachIndexed { index, message ->
                println("- Message ${index + 1}: Role=${message.role}, Content length=${message.content?.length}")
//                if (message.content?.length < 100) {
//                    println("  Content: ${message.content}")
//                }
            }
            
            println("Sending synchronous request...")
            val completion = openAI.chatCompletion(request)
            val content = completion.choices.firstOrNull()?.message?.content
            
            println("Sync response received, model: ${completion.model?.id}, choices: ${completion.choices.size}")
            println("Sync response usage: ${completion.usage?.promptTokens} prompt tokens, " +
                   "${completion.usage?.completionTokens} completion tokens, " +
                   "${completion.usage?.totalTokens} total tokens")
            
            if (content != null) {
                println("Sync response content length: ${content.length}")
                val message = Message(
                    id = UUID.randomUUID().toString(),
                    content = content,
                    role = Role.ASSISTANT
                )
                Result.success(message)
            } else {
                println("ERROR: No content received in sync response")
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
            println("Checking API availability...")
            val openAI = getOrCreateOpenAI()
            // Try to list models as a way to check if the API is available
            val models = openAI.models()
            println("API is available. Found ${models.size} models")
            true
        } catch (e: Exception) {
            println("API is unavailable. Error: ${e.message}")
            false
        }
    }
    
    /**
     * Lists all available models from the OpenAI API
     */
    suspend fun listModels(): List<String> {
        return try {
            // Get the current user preferences to access active provider information
            val userPreferences = preferencesRepository.getUserPreferencesSync()
            val activeProvider = userPreferences.modelProviders.find { it.id == userPreferences.activeProviderId }
                ?: throw IllegalStateException("Active provider not found")
            
            println("Fetching available models from provider: ${activeProvider.name}...")
            val openAI = getOrCreateOpenAI()
            val models = openAI.models().map { it.id.id }
            
            println("Models API responded with ${models.size} models from ${activeProvider.name}")
            println("Available models: ${models.joinToString(", ")}")
            
            // Filter models based on provider type if needed
            val filteredModels = when (activeProvider.providerType) {
                ProviderType.OPENAI -> models.filter { it.startsWith("gpt-") }
                else -> models
            }
            
            // If we have specific available models for this provider, use them
            val providerModels = activeProvider.availableModels
            val finalModels = if (providerModels.isNotEmpty()) {
                println("Using provider-specific model list: ${providerModels.joinToString(", ")}")
                providerModels
            } else if (filteredModels.isNotEmpty()) {
                filteredModels
            } else {
                models
            }
            
            finalModels
        } catch (e: Exception) {
            println("ERROR fetching models for active provider: ${e.message}")
            e.printStackTrace()
            
            // Fallback to default models based on the provider type
            try {
                val userPreferences = preferencesRepository.getUserPreferencesSync()
                val activeProvider = userPreferences.modelProviders.find { it.id == userPreferences.activeProviderId }
                
                val defaultModels = when (activeProvider?.providerType) {
                    ProviderType.OPENAI -> listOf("gpt-4o", "gpt-4", "gpt-3.5-turbo")
                    ProviderType.OPENAI_COMPATIBLE -> listOf("gpt-3.5-turbo", "gpt-4", "llama2")
                    ProviderType.CUSTOM -> listOf("model1", "model2")
                    null -> listOf("gpt-3.5-turbo")
                }
                
                println("Using fallback model list: ${defaultModels.joinToString(", ")}")
                defaultModels
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
