package me.pjq.chatcat.repository

import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.FlowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Creates a platform-specific Settings instance
 * This is implemented differently on each platform
 */
expect fun createPlatformSettings(name: String): Settings

/**
 * Utility class for creating Settings instances
 * Uses expect/actual pattern to provide platform-specific implementations
 */
object SettingsFactory {
    // Store settings instances by name
    private val settingsMap = mutableMapOf<String, Settings>()
    private val flowSettingsMap = mutableMapOf<String, FlowSettings>()
    
    /**
     * Creates a Settings instance
     * @param name The name of the settings store
     * @return A Settings instance
     */
    fun createSettings(name: String = "chatcat_settings"): Settings {
        return settingsMap.getOrPut(name) {
            // Use platform-specific implementation
            createPlatformSettings(name)
        }
    }
    
    /**
     * Creates a FlowSettings instance
     * @param name The name of the settings store
     * @return A FlowSettings instance
     */
    fun createFlowSettings(name: String = "chatcat_settings"): FlowSettings {
        return flowSettingsMap.getOrPut(name) {
            // Create a reactive wrapper around our settings
            ReactiveFlowSettings(createSettings(name))
        }
    }
    
    /**
     * A reactive implementation of FlowSettings that properly emits updates
     */
    private class ReactiveFlowSettings(private val settings: Settings) : FlowSettings {
        // Store flows for each key
        private val flowMap = mutableMapOf<String, MutableStateFlow<Any?>>()
        
        // Helper to get or create a flow for a key
        private fun <T> getOrCreateFlow(key: String, initialValue: T?): MutableStateFlow<T?> {
            @Suppress("UNCHECKED_CAST")
            return flowMap.getOrPut(key) { 
                MutableStateFlow(initialValue)
            } as MutableStateFlow<T?>
        }
        
        override suspend fun keys(): Set<String> = settings.keys
        override suspend fun size(): Int = settings.size
        override suspend fun clear() {
            settings.clear()
            flowMap.forEach { (_, flow) -> flow.value = null }
        }
        
        override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean = 
            settings.getBoolean(key, defaultValue)
        override suspend fun getBooleanOrNull(key: String): Boolean? = 
            settings.getBooleanOrNull(key)
        
        override suspend fun getDouble(key: String, defaultValue: Double): Double = 
            settings.getDouble(key, defaultValue)
        override suspend fun getDoubleOrNull(key: String): Double? = 
            settings.getDoubleOrNull(key)
        
        override suspend fun getFloat(key: String, defaultValue: Float): Float = 
            settings.getFloat(key, defaultValue)
        override suspend fun getFloatOrNull(key: String): Float? = 
            settings.getFloatOrNull(key)
        
        override suspend fun getInt(key: String, defaultValue: Int): Int = 
            settings.getInt(key, defaultValue)
        override suspend fun getIntOrNull(key: String): Int? = 
            settings.getIntOrNull(key)
        
        override suspend fun getLong(key: String, defaultValue: Long): Long = 
            settings.getLong(key, defaultValue)
        override suspend fun getLongOrNull(key: String): Long? = 
            settings.getLongOrNull(key)
        
        override suspend fun getString(key: String, defaultValue: String): String = 
            settings.getString(key, defaultValue)
        override suspend fun getStringOrNull(key: String): String? = 
            settings.getStringOrNull(key)
        
        override suspend fun hasKey(key: String): Boolean = 
            settings.hasKey(key)
        
        // Override put methods to update flows
        override suspend fun putBoolean(key: String, value: Boolean) {
            settings.putBoolean(key, value)
            getOrCreateFlow<Boolean>(key, value).value = value
        }
        
        override suspend fun putDouble(key: String, value: Double) {
            settings.putDouble(key, value)
            getOrCreateFlow<Double>(key, value).value = value
        }
        
        override suspend fun putFloat(key: String, value: Float) {
            settings.putFloat(key, value)
            getOrCreateFlow<Float>(key, value).value = value
        }
        
        override suspend fun putInt(key: String, value: Int) {
            settings.putInt(key, value)
            getOrCreateFlow<Int>(key, value).value = value
        }
        
        override suspend fun putLong(key: String, value: Long) {
            settings.putLong(key, value)
            getOrCreateFlow<Long>(key, value).value = value
        }
        
        override suspend fun putString(key: String, value: String) {
            settings.putString(key, value)
            getOrCreateFlow<String>(key, value).value = value
        }
        
        override suspend fun remove(key: String) {
            settings.remove(key)
            flowMap[key]?.value = null
        }
        
        // Flow methods
        override fun getStringOrNullFlow(key: String): Flow<String?> {
            val initialValue = settings.getStringOrNull(key)
            return getOrCreateFlow(key, initialValue)
        }
        
        override fun getBooleanOrNullFlow(key: String): Flow<Boolean?> {
            val initialValue = settings.getBooleanOrNull(key)
            return getOrCreateFlow(key, initialValue)
        }
        
        override fun getIntOrNullFlow(key: String): Flow<Int?> {
            val initialValue = settings.getIntOrNull(key)
            return getOrCreateFlow(key, initialValue)
        }
        
        override fun getLongOrNullFlow(key: String): Flow<Long?> {
            val initialValue = settings.getLongOrNull(key)
            return getOrCreateFlow(key, initialValue)
        }
        
        override fun getFloatOrNullFlow(key: String): Flow<Float?> {
            val initialValue = settings.getFloatOrNull(key)
            return getOrCreateFlow(key, initialValue)
        }
        
        override fun getDoubleOrNullFlow(key: String): Flow<Double?> {
            val initialValue = settings.getDoubleOrNull(key)
            return getOrCreateFlow(key, initialValue)
        }
        
        // Flow methods with default values
        override fun getStringFlow(key: String, defaultValue: String): Flow<String> {
            val initialValue = settings.getStringOrNull(key) ?: defaultValue
            val flow = getOrCreateFlow(key, initialValue)
            return flow.map { it as? String ?: defaultValue }
        }
        
        override fun getBooleanFlow(key: String, defaultValue: Boolean): Flow<Boolean> {
            val initialValue = settings.getBooleanOrNull(key) ?: defaultValue
            val flow = getOrCreateFlow(key, initialValue)
            return flow.map { it as? Boolean ?: defaultValue }
        }
        
        override fun getIntFlow(key: String, defaultValue: Int): Flow<Int> {
            val initialValue = settings.getIntOrNull(key) ?: defaultValue
            val flow = getOrCreateFlow(key, initialValue)
            return flow.map { it as? Int ?: defaultValue }
        }
        
        override fun getLongFlow(key: String, defaultValue: Long): Flow<Long> {
            val initialValue = settings.getLongOrNull(key) ?: defaultValue
            val flow = getOrCreateFlow(key, initialValue)
            return flow.map { it as? Long ?: defaultValue }
        }
        
        override fun getFloatFlow(key: String, defaultValue: Float): Flow<Float> {
            val initialValue = settings.getFloatOrNull(key) ?: defaultValue
            val flow = getOrCreateFlow(key, initialValue)
            return flow.map { it as? Float ?: defaultValue }
        }
        
        override fun getDoubleFlow(key: String, defaultValue: Double): Flow<Double> {
            val initialValue = settings.getDoubleOrNull(key) ?: defaultValue
            val flow = getOrCreateFlow(key, initialValue)
            return flow.map { it as? Double ?: defaultValue }
        }
    }
}

/**
 * In-memory implementation of Settings for fallback or testing
 */
class InMemorySettings(private val name: String = "in_memory_settings") : Settings {
    // Use a companion object to store data statically
    companion object {
        private val storageMap = mutableMapOf<String, MutableMap<String, Any>>()
    }
    
    // Get or create storage for this instance
    private val storage: MutableMap<String, Any>
        get() = storageMap.getOrPut(name) { mutableMapOf() }
    
    override val keys: Set<String>
        get() = storage.keys
    
    override val size: Int
        get() = storage.size
    
    override fun clear() {
        storage.clear()
    }
    
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return storage[key] as? Boolean ?: defaultValue
    }
    
    override fun getBooleanOrNull(key: String): Boolean? {
        return storage[key] as? Boolean
    }
    
    override fun getDouble(key: String, defaultValue: Double): Double {
        return storage[key] as? Double ?: defaultValue
    }
    
    override fun getDoubleOrNull(key: String): Double? {
        return storage[key] as? Double
    }
    
    override fun getFloat(key: String, defaultValue: Float): Float {
        return storage[key] as? Float ?: defaultValue
    }
    
    override fun getFloatOrNull(key: String): Float? {
        return storage[key] as? Float
    }
    
    override fun getInt(key: String, defaultValue: Int): Int {
        return storage[key] as? Int ?: defaultValue
    }
    
    override fun getIntOrNull(key: String): Int? {
        return storage[key] as? Int
    }
    
    override fun getLong(key: String, defaultValue: Long): Long {
        return storage[key] as? Long ?: defaultValue
    }
    
    override fun getLongOrNull(key: String): Long? {
        return storage[key] as? Long
    }
    
    override fun getString(key: String, defaultValue: String): String {
        return storage[key] as? String ?: defaultValue
    }
    
    override fun getStringOrNull(key: String): String? {
        return storage[key] as? String
    }
    
    override fun hasKey(key: String): Boolean {
        return storage.containsKey(key)
    }
    
    override fun putBoolean(key: String, value: Boolean) {
        storage[key] = value
    }
    
    override fun putDouble(key: String, value: Double) {
        storage[key] = value
    }
    
    override fun putFloat(key: String, value: Float) {
        storage[key] = value
    }
    
    override fun putInt(key: String, value: Int) {
        storage[key] = value
    }
    
    override fun putLong(key: String, value: Long) {
        storage[key] = value
    }
    
    override fun putString(key: String, value: String) {
        storage[key] = value
    }
    
    override fun remove(key: String) {
        storage.remove(key)
    }
}
