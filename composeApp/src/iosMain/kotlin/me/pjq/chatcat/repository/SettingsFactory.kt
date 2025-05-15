package me.pjq.chatcat.repository

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation of createPlatformSettings using NSUserDefaults
 */
actual fun createPlatformSettings(name: String): Settings {
    // Create NSUserDefaults-backed settings
    val userDefaults = NSUserDefaults(suiteName = name)
    return NSUserDefaultsSettings(userDefaults)
}
