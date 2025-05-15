package me.pjq.chatcat.repository

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings

/**
 * Web implementation of createPlatformSettings using localStorage
 */
actual fun createPlatformSettings(name: String): Settings {
    // Create localStorage-backed settings
    return StorageSettings(name)
}
