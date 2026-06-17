package me.pjq.chatcat.repository

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import me.pjq.chatcat.model.Conversation
import me.pjq.chatcat.model.Message
import me.pjq.chatcat.model.Role
import me.pjq.chatcat.platform.randomUUID

class PersistentConversationRepository(
    private val settings: Settings,
    private val preferencesRepository: PreferencesRepository
) : ConversationRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
        classDiscriminator = "kind"
    }

    private val conversations = mutableListOf<Conversation>()
    private val conversationsFlow = MutableStateFlow<List<Conversation>>(emptyList())

    private object StorageKeys {
        const val CONVERSATION_IDS = "conversation_ids"
        const val CONVERSATION_PREFIX = "conversation_"
    }

    init {
        loadConversations()
    }

    private fun loadConversations() {
        val conversationIdsString = settings.getString(StorageKeys.CONVERSATION_IDS, "")
        val conversationIds = conversationIdsString.split(",").filter { it.isNotEmpty() }

        conversations.clear()
        var migratedCount = 0
        for (id in conversationIds) {
            val key = "${StorageKeys.CONVERSATION_PREFIX}$id"
            if (!settings.hasKey(key)) continue
            val raw = settings.getString(key, "")
            val (conv, wasMigrated) = decodeOrMigrate(raw)
            if (conv != null) {
                conversations.add(conv)
                if (wasMigrated) {
                    saveConversation(conv)
                    migratedCount++
                }
            } else {
                println("ChatCat: dropping unreadable conversation $id")
                settings.remove(key)
            }
        }
        if (migratedCount > 0) println("ChatCat: migrated $migratedCount legacy conversations")
        saveConversationIds()
        updateFlow()
    }

    private fun decodeOrMigrate(raw: String): Pair<Conversation?, Boolean> {
        if (raw.isEmpty()) return null to false
        val element = try {
            json.parseToJsonElement(raw)
        } catch (e: Exception) {
            println("ChatCat: parse error ${e.message}")
            return null to false
        }
        val obj = element as? kotlinx.serialization.json.JsonObject ?: return null to false
        val isLegacy = isLegacyConversation(obj)
        return if (isLegacy) {
            try {
                migrateLegacyConversation(obj) to true
            } catch (e: Exception) {
                println("ChatCat: legacy migration failed: ${e.message}")
                null to false
            }
        } else {
            try {
                json.decodeFromJsonElement(Conversation.serializer(), element) to false
            } catch (e: Exception) {
                println("ChatCat: v2 decode failed: ${e.message}")
                try {
                    migrateLegacyConversation(obj) to true
                } catch (inner: Exception) {
                    println("ChatCat: fallback migration failed: ${inner.message}")
                    null to false
                }
            }
        }
    }

    private fun isLegacyConversation(obj: kotlinx.serialization.json.JsonObject): Boolean {
        val messages = obj["messages"] as? kotlinx.serialization.json.JsonArray ?: return false
        if (messages.isEmpty()) return false
        val first = messages.first() as? kotlinx.serialization.json.JsonObject ?: return false
        return first.containsKey("content") && !first.containsKey("parts")
    }

    private fun migrateLegacyConversation(obj: kotlinx.serialization.json.JsonObject): Conversation? {
        val id = (obj["id"] as? kotlinx.serialization.json.JsonPrimitive)?.content ?: return null
        val title = (obj["title"] as? kotlinx.serialization.json.JsonPrimitive)?.content ?: "Conversation"
        val messagesJson = obj["messages"] as? kotlinx.serialization.json.JsonArray
        val migratedMessages = messagesJson?.mapNotNull { msgEl ->
            val mObj = msgEl as? kotlinx.serialization.json.JsonObject ?: return@mapNotNull null
            val mid = (mObj["id"] as? kotlinx.serialization.json.JsonPrimitive)?.content ?: return@mapNotNull null
            val roleStr = (mObj["role"] as? kotlinx.serialization.json.JsonPrimitive)?.content ?: "USER"
            val content = (mObj["content"] as? kotlinx.serialization.json.JsonPrimitive)?.content ?: ""
            val isError = (mObj["isError"] as? kotlinx.serialization.json.JsonPrimitive)?.content?.toBoolean() ?: false
            Message.text(
                id = mid,
                role = runCatching { Role.valueOf(roleStr) }.getOrDefault(Role.USER),
                text = content,
                isError = isError
            )
        } ?: emptyList()
        return Conversation(id = id, title = title, messages = migratedMessages)
    }

    private fun saveConversationIds() {
        val ids = conversations.map { it.id }.joinToString(",")
        settings.putString(StorageKeys.CONVERSATION_IDS, ids)
    }

    private fun saveConversation(conversation: Conversation) {
        val conversationJson = json.encodeToString(conversation)
        settings.putString("${StorageKeys.CONVERSATION_PREFIX}${conversation.id}", conversationJson)
    }

    override suspend fun getConversations(): Flow<List<Conversation>> = conversationsFlow.asStateFlow()

    override suspend fun getConversation(id: String): Conversation? = conversations.find { it.id == id }

    override suspend fun createConversation(title: String): Conversation {
        val defaultModelConfig = preferencesRepository.getUserPreferencesSync().defaultModelConfig
        val conversation = Conversation(
            id = randomUUID(),
            title = title,
            modelConfig = defaultModelConfig
        )
        conversations.add(conversation)
        saveConversation(conversation)
        saveConversationIds()
        updateFlow()
        return conversation
    }

    override suspend fun updateConversation(conversation: Conversation): Conversation {
        val index = conversations.indexOfFirst { it.id == conversation.id }
        if (index == -1) return conversation
        val updated = conversation.copy(updatedAt = Clock.System.now())
        conversations[index] = updated
        saveConversation(updated)
        updateFlow()
        return updated
    }

    override suspend fun deleteConversation(id: String): Boolean {
        val removed = conversations.removeAll { it.id == id }
        if (removed) {
            settings.remove("${StorageKeys.CONVERSATION_PREFIX}$id")
            saveConversationIds()
            updateFlow()
        }
        return removed
    }

    override suspend fun addMessage(conversationId: String, message: Message): Conversation? {
        val index = conversations.indexOfFirst { it.id == conversationId }
        if (index == -1) return null
        val conversation = conversations[index]
        val updated = conversation.copy(
            messages = conversation.messages + message,
            updatedAt = Clock.System.now()
        )
        conversations[index] = updated
        saveConversation(updated)
        updateFlow()
        return updated
    }

    override suspend fun updateMessage(conversationId: String, message: Message): Conversation? {
        val index = conversations.indexOfFirst { it.id == conversationId }
        if (index == -1) return null
        val conversation = conversations[index]
        val messageIndex = conversation.messages.indexOfFirst { it.id == message.id }
        if (messageIndex == -1) return null
        val updatedMessages = conversation.messages.toMutableList().also { it[messageIndex] = message }
        val updated = conversation.copy(messages = updatedMessages, updatedAt = Clock.System.now())
        conversations[index] = updated
        saveConversation(updated)
        updateFlow()
        return updated
    }

    override suspend fun deleteMessage(conversationId: String, messageId: String): Conversation? {
        val index = conversations.indexOfFirst { it.id == conversationId }
        if (index == -1) return null
        val conversation = conversations[index]
        val filtered = conversation.messages.filter { it.id != messageId }
        if (filtered.size == conversation.messages.size) return null
        val updated = conversation.copy(messages = filtered, updatedAt = Clock.System.now())
        conversations[index] = updated
        saveConversation(updated)
        updateFlow()
        return updated
    }

    override suspend fun clearConversations(): Boolean {
        conversations.forEach { settings.remove("${StorageKeys.CONVERSATION_PREFIX}${it.id}") }
        conversations.clear()
        saveConversationIds()
        updateFlow()
        return true
    }

    private fun updateFlow() {
        conversationsFlow.value = conversations.sortedWith(
            compareByDescending<Conversation> { it.pinned }.thenByDescending { it.updatedAt }
        )
    }
}
