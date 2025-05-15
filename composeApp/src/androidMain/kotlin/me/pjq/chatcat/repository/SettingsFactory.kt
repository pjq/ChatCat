package me.pjq.chatcat.repository

import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import me.pjq.chatcat.AndroidContextProvider

/**
 * Android implementation of createPlatformSettings using DataStore
 */
actual fun createPlatformSettings(name: String): Settings {
    // Get the application context
    val context = AndroidContextProvider.getApplicationContext()
    
    // Create DataStore-backed settings
    return SharedPreferencesSettings(context.getSharedPreferences(name, 0) , commit = true)
}
