package me.pjq.chatcat.repository

import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.FlowSettings

/**
 * Simple in-memory implementation of Settings for development purposes
 */
class InMemorySettings : Settings {
    private val map = mutableMapOf<String, Any>()
    
    override val keys: Set<String>
        get() = map.keys
    
    override val size: Int
        get() = map.size
    
    override fun clear() {
        map.clear()
    }
    
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return map[key] as? Boolean ?: defaultValue
    }
    
    override fun getBooleanOrNull(key: String): Boolean? {
        return map[key] as? Boolean
    }
    
    override fun getDouble(key: String, defaultValue: Double): Double {
        return map[key] as? Double ?: defaultValue
    }
    
    override fun getDoubleOrNull(key: String): Double? {
        return map[key] as? Double
    }
    
    override fun getFloat(key: String, defaultValue: Float): Float {
        return map[key] as? Float ?: defaultValue
    }
    
    override fun getFloatOrNull(key: String): Float? {
        return map[key] as? Float
    }
    
    override fun getInt(key: String, defaultValue: Int): Int {
        return map[key] as? Int ?: defaultValue
    }
    
    override fun getIntOrNull(key: String): Int? {
        return map[key] as? Int
    }
    
    override fun getLong(key: String, defaultValue: Long): Long {
        return map[key] as? Long ?: defaultValue
    }
    
    override fun getLongOrNull(key: String): Long? {
        return map[key] as? Long
    }
    
    override fun getString(key: String, defaultValue: String): String {
        return map[key] as? String ?: defaultValue
    }
    
    override fun getStringOrNull(key: String): String? {
        return map[key] as? String
    }
    
    override fun hasKey(key: String): Boolean {
        return map.containsKey(key)
    }
    
    override fun putBoolean(key: String, value: Boolean) {
        map[key] = value
    }
    
    override fun putDouble(key: String, value: Double) {
        map[key] = value
    }
    
    override fun putFloat(key: String, value: Float) {
        map[key] = value
    }
    
    override fun putInt(key: String, value: Int) {
        map[key] = value
    }
    
    override fun putLong(key: String, value: Long) {
        map[key] = value
    }
    
    override fun putString(key: String, value: String) {
        map[key] = value
    }
    
    override fun remove(key: String) {
        map.remove(key)
    }
}

/**
 * Utility class for creating Settings instances
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
            // For now, we'll use in-memory settings for all platforms
            // In a real app, you'd use platform-specific implementations
            InMemorySettings()
        }
    }
    
    /**
     * Creates a FlowSettings instance
     * @param name The name of the settings store
     * @return A FlowSettings instance
     */
    fun createFlowSettings(name: String = "chatcat_settings"): FlowSettings {
        return flowSettingsMap.getOrPut(name) {
            // For now, we'll create a simple wrapper around our in-memory settings
            object : FlowSettings {
                private val settings = createSettings(name)
                
                override suspend fun keys(): Set<String> = settings.keys
                override suspend fun size(): Int = settings.size
                override suspend fun clear() = settings.clear()
                
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
                
                override suspend fun putBoolean(key: String, value: Boolean) = 
                    settings.putBoolean(key, value)
                override suspend fun putDouble(key: String, value: Double) = 
                    settings.putDouble(key, value)
                override suspend fun putFloat(key: String, value: Float) = 
                    settings.putFloat(key, value)
                override suspend fun putInt(key: String, value: Int) = 
                    settings.putInt(key, value)
                override suspend fun putLong(key: String, value: Long) = 
                    settings.putLong(key, value)
                override suspend fun putString(key: String, value: String) = 
                    settings.putString(key, value)
                
                override suspend fun remove(key: String) = 
                    settings.remove(key)
                
                override fun getStringOrNullFlow(key: String) = 
                    kotlinx.coroutines.flow.flow { emit(getStringOrNull(key)) }
                override fun getBooleanOrNullFlow(key: String) = 
                    kotlinx.coroutines.flow.flow { emit(getBooleanOrNull(key)) }
                override fun getIntOrNullFlow(key: String) = 
                    kotlinx.coroutines.flow.flow { emit(getIntOrNull(key)) }
                override fun getLongOrNullFlow(key: String) = 
                    kotlinx.coroutines.flow.flow { emit(getLongOrNull(key)) }
                override fun getFloatOrNullFlow(key: String) = 
                    kotlinx.coroutines.flow.flow { emit(getFloatOrNull(key)) }
                override fun getDoubleOrNullFlow(key: String) = 
                    kotlinx.coroutines.flow.flow { emit(getDoubleOrNull(key)) }
                
                // Additional flow methods
                override fun getStringFlow(key: String, defaultValue: String) = 
                    kotlinx.coroutines.flow.flow { emit(getString(key, defaultValue)) }
                override fun getBooleanFlow(key: String, defaultValue: Boolean) = 
                    kotlinx.coroutines.flow.flow { emit(getBoolean(key, defaultValue)) }
                override fun getIntFlow(key: String, defaultValue: Int) = 
                    kotlinx.coroutines.flow.flow { emit(getInt(key, defaultValue)) }
                override fun getLongFlow(key: String, defaultValue: Long) = 
                    kotlinx.coroutines.flow.flow { emit(getLong(key, defaultValue)) }
                override fun getFloatFlow(key: String, defaultValue: Float) = 
                    kotlinx.coroutines.flow.flow { emit(getFloat(key, defaultValue)) }
                override fun getDoubleFlow(key: String, defaultValue: Double) = 
                    kotlinx.coroutines.flow.flow { emit(getDouble(key, defaultValue)) }
            }
        }
    }
}
