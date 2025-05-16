package me.pjq.chatcat.i18n

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.pjq.chatcat.model.Language
import me.pjq.chatcat.repository.PreferencesRepository

/**
 * Manages language settings and provides localized strings for the app.
 */
class LanguageManager(
    private val preferencesRepository: PreferencesRepository
) {
    private val _currentLanguage = MutableStateFlow(Language.ENGLISH)
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()
    private val scope = CoroutineScope(Dispatchers.Main)
    
    init {
        // Initialize with the language from preferences
        scope.launch {
            val preferences = preferencesRepository.getUserPreferences().first()
            _currentLanguage.value = preferences.language
        }
    }
    
    /**
     * Changes the app language and saves it to preferences.
     */
    suspend fun setLanguage(language: Language) {
        val currentPreferences = preferencesRepository.getUserPreferences().first()
        preferencesRepository.updateUserPreferences(currentPreferences.copy(language = language))
        _currentLanguage.value = language
    }
    
    /**
     * Gets the current locale code for the selected language.
     */
    fun getCurrentLocale(): String {
        return _currentLanguage.value.code
    }
    
    /**
     * Gets a localized string by key.
     */
    fun getString(key: String): String {
        return StringResources.getString(key, _currentLanguage.value)
    }
    
    /**
     * Gets a localized string with format arguments.
     */
    fun getString(key: String, vararg args: Any): String {
        val template = StringResources.getString(key, _currentLanguage.value)
        return if (args.isEmpty()) {
            template
        } else {
//            String.format(template, *args)
//            ""
            template
        }
    }
    
    /**
     * Gets the locale tag for the current language.
     */
    private fun getLocaleTag(): String {
        return when (_currentLanguage.value) {
            Language.ENGLISH -> "en"
            Language.CHINESE -> "zh"
            Language.SPANISH -> "es"
            Language.JAPANESE -> "ja"
            Language.GERMAN -> "de"
            Language.FRENCH -> "fr"
        }
    }
    
    companion object {
        // Singleton instance
        private var instance: LanguageManager? = null
        
        fun getInstance(preferencesRepository: PreferencesRepository): LanguageManager {
            return instance ?: LanguageManager(preferencesRepository).also { instance = it }
        }
    }
}
