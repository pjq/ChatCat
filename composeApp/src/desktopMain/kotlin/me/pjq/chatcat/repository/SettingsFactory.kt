package me.pjq.chatcat.repository

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

/**
 * Desktop implementation of createPlatformSettings using Java Preferences API
 */
actual fun createPlatformSettings(name: String): Settings {
    // Create Java Preferences-backed settings
    val preferences = Preferences.userRoot().node(name)
    return PreferencesSettings(preferences)
}
