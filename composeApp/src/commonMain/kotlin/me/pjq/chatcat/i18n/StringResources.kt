package me.pjq.chatcat.i18n

import me.pjq.chatcat.model.Language

/**
 * String resources for the app.
 * This class provides access to localized strings.
 * 
 * Note: In a real implementation with MOKO Resources, this would use the generated MR class
 * to access string resources from XML files. For now, we're using a simplified approach
 * with hardcoded strings for demonstration purposes.
 */
object StringResources {
    // Get a localized string based on the language
    fun getString(key: String, language: Language): String {
        return when (language) {
            Language.ENGLISH -> englishStrings[key] ?: key
            Language.CHINESE -> chineseStrings[key] ?: englishStrings[key] ?: key
            Language.SPANISH -> spanishStrings[key] ?: englishStrings[key] ?: key
            Language.JAPANESE -> japaneseStrings[key] ?: englishStrings[key] ?: key
            Language.GERMAN -> germanStrings[key] ?: englishStrings[key] ?: key
            Language.FRENCH -> frenchStrings[key] ?: englishStrings[key] ?: key
        }
    }
    
    // String keys
    const val APP_NAME = "app_name"
    
    // Common UI elements
    const val OK = "ok"
    const val CANCEL = "cancel"
    const val SAVE = "save"
    const val DELETE = "delete"
    const val EDIT = "edit"
    const val BACK = "back"
    const val NEXT = "next"
    const val DONE = "done"
    const val SEARCH = "search"
    const val SEND = "send"
    const val COPY = "copy"
    const val SHARE = "share"
    const val RETRY = "retry"
    const val WRITING = "writing"
    const val THINKING = "thinking"
    
    // Navigation
    const val NAV_CHAT = "nav_chat"
    const val NAV_HISTORY = "nav_history"
    const val NAV_SETTINGS = "nav_settings"
    const val NAV_ABOUT = "nav_about"
    
    // Settings
    const val SETTINGS_TITLE = "settings_title"
    const val SETTINGS_LANGUAGE = "settings_language"
    const val SETTINGS_THEME = "settings_theme"
    const val SETTINGS_THEME_LIGHT = "settings_theme_light"
    const val SETTINGS_THEME_DARK = "settings_theme_dark"
    const val SETTINGS_THEME_SYSTEM = "settings_theme_system"
    const val SETTINGS_APPEARANCE_SECTION = "settings_appearance_section"
    const val SETTINGS_MODEL_SECTION = "settings_model_section"
    const val SETTINGS_OTHER_SECTION = "settings_other_section"
    
    // English strings (default)
    private val englishStrings = mapOf(
        APP_NAME to "ChatCat",
        OK to "OK",
        CANCEL to "Cancel",
        SAVE to "Save",
        DELETE to "Delete",
        EDIT to "Edit",
        BACK to "Back",
        NEXT to "Next",
        DONE to "Done",
        SEARCH to "Search",
        SEND to "Send",
        COPY to "Copy",
        SHARE to "Share",
        RETRY to "Retry",
        WRITING to "Writing...",
        THINKING to "Thinking...",
        NAV_CHAT to "Chat",
        NAV_HISTORY to "History",
        NAV_SETTINGS to "Settings",
        NAV_ABOUT to "About",
        SETTINGS_TITLE to "Settings",
        SETTINGS_LANGUAGE to "Language",
        SETTINGS_THEME to "Theme",
        SETTINGS_THEME_LIGHT to "Light",
        SETTINGS_THEME_DARK to "Dark",
        SETTINGS_THEME_SYSTEM to "System Default",
        SETTINGS_APPEARANCE_SECTION to "Appearance",
        SETTINGS_MODEL_SECTION to "Model Configuration",
        SETTINGS_OTHER_SECTION to "Other Settings"
    )
    
    // Chinese strings
    private val chineseStrings = mapOf(
        APP_NAME to "ChatCat",
        OK to "确定",
        CANCEL to "取消",
        SAVE to "保存",
        DELETE to "删除",
        EDIT to "编辑",
        BACK to "返回",
        NEXT to "下一步",
        DONE to "完成",
        SEARCH to "搜索",
        SEND to "发送",
        COPY to "复制",
        SHARE to "分享",
        RETRY to "重试",
        WRITING to "正在写作...",
        THINKING to "正在思考...",
        NAV_CHAT to "聊天",
        NAV_HISTORY to "历史",
        NAV_SETTINGS to "设置",
        NAV_ABOUT to "关于",
        SETTINGS_TITLE to "设置",
        SETTINGS_LANGUAGE to "语言",
        SETTINGS_THEME to "主题",
        SETTINGS_THEME_LIGHT to "浅色",
        SETTINGS_THEME_DARK to "深色",
        SETTINGS_THEME_SYSTEM to "系统默认",
        SETTINGS_APPEARANCE_SECTION to "外观",
        SETTINGS_MODEL_SECTION to "模型配置",
        SETTINGS_OTHER_SECTION to "其他设置"
    )
    
    // Spanish strings (minimal example)
    private val spanishStrings = mapOf(
        APP_NAME to "ChatCat",
        OK to "Aceptar",
        CANCEL to "Cancelar",
        SAVE to "Guardar",
        SETTINGS_TITLE to "Configuración",
        SETTINGS_LANGUAGE to "Idioma",
        SETTINGS_THEME to "Tema"
    )
    
    // Japanese strings (minimal example)
    private val japaneseStrings = mapOf(
        OK to "OK",
        CANCEL to "キャンセル",
        SETTINGS_TITLE to "設定",
        SETTINGS_LANGUAGE to "言語"
    )
    
    // German strings (minimal example)
    private val germanStrings = mapOf(
        OK to "OK",
        CANCEL to "Abbrechen",
        SETTINGS_TITLE to "Einstellungen",
        SETTINGS_LANGUAGE to "Sprache"
    )
    
    // French strings (minimal example)
    private val frenchStrings = mapOf(
        OK to "OK",
        CANCEL to "Annuler",
        SETTINGS_TITLE to "Paramètres",
        SETTINGS_LANGUAGE to "Langue"
    )
}
