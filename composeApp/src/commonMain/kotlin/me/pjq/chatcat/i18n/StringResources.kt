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
    const val NAV_CHAT = "nav_chat"
    const val NAV_HISTORY = "nav_history"
    const val NAV_SETTINGS = "nav_settings"
    const val NAV_ABOUT = "nav_about"
    const val SETTINGS_TITLE = "settings_title"
    const val SETTINGS_LANGUAGE = "settings_language"
    const val SETTINGS_THEME = "settings_theme"
    const val SETTINGS_THEME_LIGHT = "settings_theme_light"
    const val SETTINGS_THEME_DARK = "settings_theme_dark"
    const val SETTINGS_THEME_SYSTEM = "settings_theme_system"
    const val SETTINGS_APPEARANCE_SECTION = "settings_appearance_section"
    const val SETTINGS_MODEL_SECTION = "settings_model_section"
    const val SETTINGS_OTHER_SECTION = "settings_other_section"
    const val RECENT_CONVERSATIONS = "recent_conversations"
    const val API_CONNECTED = "api_connected"
    const val ACTIVE_PROVIDER = "active_provider"
    const val TEMPERATURE = "temperature"
    const val MAX_TOKENS = "max_tokens"
    const val STREAM_MODE = "stream_mode"
    const val FONT_SIZE = "font_size"
    const val SMALL = "small"
    const val MEDIUM = "medium"
    const val LARGE = "large"
    const val EXTRA_LARGE = "extra_large"
    const val SOUND_EFFECTS = "sound_effects"
    const val COPY_CONFIRMATION = "copy_confirmation"
    const val OPEN_MENU = "open_menu"
    const val NEW_CHAT = "new_chat"
    const val ERROR = "error"
    const val CAT_EMOJI = "cat_emoji"
    const val ERROR_DESCRIPTION = "error_description"
    const val SELECT_AND_MANAGE_PROVIDERS = "select_and_manage_providers"
    const val OPENAI_PROVIDERS = "openai_providers"
    const val COMPATIBLE_PROVIDERS = "compatible_providers"
    const val CUSTOM_PROVIDERS = "custom_providers"
    const val BASE_URL = "base_url"
    const val API_SETTINGS = "api_settings"
    const val API_BASE_URL = "api_base_url"
    const val API_KEY = "api_key"
    const val SHOW_EDIT_API_KEY = "show_edit_api_key"
    const val MODEL_SETTINGS = "model_settings"
    const val MODEL = "model"
    const val SELECTED_MODEL = "selected_model"
    const val SELECT = "select"
    const val LOADING_MODELS = "loading_models"
    const val CONNECT_TO_API = "connect_to_api"
    const val REFRESH_MODELS = "refresh_models"
    const val EDIT_ICON = "edit_icon"
    const val DELETE_ICON = "delete_icon"
    const val MANAGE_MODEL_PROVIDERS = "manage_model_providers"
    
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
        SETTINGS_OTHER_SECTION to "其他设置",
        RECENT_CONVERSATIONS to "最近对话",
        API_CONNECTED to "✓ API已连接",
        ACTIVE_PROVIDER to "当前提供商",
        TEMPERATURE to "温度",
        MAX_TOKENS to "最大令牌数",
        STREAM_MODE to "流式模式",
        FONT_SIZE to "字体大小",
        SMALL to "小",
        MEDIUM to "中",
        LARGE to "大",
        EXTRA_LARGE to "特大",
        SOUND_EFFECTS to "声音效果",
        COPY_CONFIRMATION to "▼",
        OPEN_MENU to "打开菜单",
        NEW_CHAT to "新对话",
        ERROR to "错误",
        SELECT_AND_MANAGE_PROVIDERS to "选择和管理AI模型提供商",
        OPENAI_PROVIDERS to "OpenAI提供商",
        COMPATIBLE_PROVIDERS to "兼容提供商",
        CUSTOM_PROVIDERS to "自定义提供商",
        BASE_URL to "基础URL",
        API_SETTINGS to "API设置",
        API_BASE_URL to "API基础URL",
        API_KEY to "API密钥",
        SHOW_EDIT_API_KEY to "点击眼睛图标显示/编辑API密钥",
        MODEL_SETTINGS to "模型设置",
        MODEL to "模型",
        SELECTED_MODEL to "已选择模型",
        SELECT to "选择",
        LOADING_MODELS to "加载模型中...",
        CONNECT_TO_API to "连接到API以加载可用模型",
        REFRESH_MODELS to "刷新模型",
        EDIT_ICON to "✏️",
        DELETE_ICON to "🗑️",
        MANAGE_MODEL_PROVIDERS to "管理模型提供商"
    )
    
    // Spanish strings
    private val spanishStrings = mapOf(
        APP_NAME to "ChatCat",
        OK to "Aceptar",
        CANCEL to "Cancelar",
        SAVE to "Guardar",
        DELETE to "Eliminar",
        EDIT to "Editar",
        BACK to "Atrás",
        NEXT to "Siguiente",
        DONE to "Hecho",
        SEARCH to "Buscar",
        SEND to "Enviar",
        COPY to "Copiar",
        SHARE to "Compartir",
        RETRY to "Reintentar",
        WRITING to "Escribiendo...",
        THINKING to "Pensando...",
        NAV_CHAT to "Chat",
        NAV_HISTORY to "Historial",
        NAV_SETTINGS to "Ajustes",
        NAV_ABOUT to "Acerca de",
        SETTINGS_TITLE to "Configuración",
        SETTINGS_LANGUAGE to "Idioma",
        SETTINGS_THEME to "Tema",
        SETTINGS_THEME_LIGHT to "Claro",
        SETTINGS_THEME_DARK to "Oscuro",
        SETTINGS_THEME_SYSTEM to "Predeterminado del sistema",
        SETTINGS_APPEARANCE_SECTION to "Apariencia",
        SETTINGS_MODEL_SECTION to "Configuración de modelo",
        SETTINGS_OTHER_SECTION to "Otros ajustes",
        RECENT_CONVERSATIONS to "Conversaciones recientes",
        API_CONNECTED to "✓ API Conectada",
        ACTIVE_PROVIDER to "Proveedor activo",
        TEMPERATURE to "Temperatura",
        MAX_TOKENS to "Tokens máximos",
        STREAM_MODE to "Modo flujo",
        FONT_SIZE to "Tamaño de fuente",
        SMALL to "Pequeño",
        MEDIUM to "Mediano",
        LARGE to "Grande",
        EXTRA_LARGE to "Extra grande",
        SOUND_EFFECTS to "Efectos de sonido",
        COPY_CONFIRMATION to "▼",
        OPEN_MENU to "Abrir menú",
        NEW_CHAT to "Nuevo chat",
        ERROR to "Error",
        CAT_EMOJI to "🐱",
        ERROR_DESCRIPTION to "Error",
        SELECT_AND_MANAGE_PROVIDERS to "Seleccionar y gestionar proveedores de modelos IA",
        OPENAI_PROVIDERS to "Proveedores OpenAI",
        COMPATIBLE_PROVIDERS to "Proveedores compatibles",
        CUSTOM_PROVIDERS to "Proveedores personalizados",
        BASE_URL to "URL base",
        API_SETTINGS to "Configuración de API",
        API_BASE_URL to "URL base de API",
        API_KEY to "Clave API",
        SHOW_EDIT_API_KEY to "Haga clic en el icono del ojo para mostrar/editar la clave API",
        MODEL_SETTINGS to "Configuración del modelo",
        MODEL to "Modelo",
        SELECTED_MODEL to "Modelo seleccionado",
        SELECT to "Seleccionar",
        LOADING_MODELS to "Cargando modelos...",
        CONNECT_TO_API to "Conecte a la API para cargar los modelos disponibles",
        REFRESH_MODELS to "Actualizar modelos",
        EDIT_ICON to "✏️",
        DELETE_ICON to "🗑️",
        MANAGE_MODEL_PROVIDERS to "Gestionar proveedores de modelos"
    )
    
    // Japanese strings
    private val japaneseStrings = mapOf(
        APP_NAME to "ChatCat",
        OK to "OK",
        CANCEL to "キャンセル",
        SAVE to "保存",
        DELETE to "削除",
        EDIT to "編集",
        BACK to "戻る",
        NEXT to "次へ",
        DONE to "完了",
        SEARCH to "検索",
        SEND to "送信",
        COPY to "コピー",
        SHARE to "共有",
        RETRY to "再試行",
        WRITING to "作成中...",
        THINKING to "思考中...",
        NAV_CHAT to "チャット",
        NAV_HISTORY to "履歴",
        NAV_SETTINGS to "設定",
        NAV_ABOUT to "情報",
        SETTINGS_TITLE to "設定",
        SETTINGS_LANGUAGE to "言語",
        SETTINGS_THEME to "テーマ",
        SETTINGS_THEME_LIGHT to "ライト",
        SETTINGS_THEME_DARK to "ダーク",
        SETTINGS_THEME_SYSTEM to "システムデフォルト",
        SETTINGS_APPEARANCE_SECTION to "外観",
        SETTINGS_MODEL_SECTION to "モデル設定",
        SETTINGS_OTHER_SECTION to "その他の設定",
        RECENT_CONVERSATIONS to "最近の会話",
        API_CONNECTED to "✓ API接続済み",
        ACTIVE_PROVIDER to "アクティブなプロバイダー",
        TEMPERATURE to "温度",
        MAX_TOKENS to "最大トークン数",
        STREAM_MODE to "ストリームモード",
        FONT_SIZE to "フォントサイズ",
        SMALL to "小",
        MEDIUM to "中",
        LARGE to "大",
        EXTRA_LARGE to "特大",
        SOUND_EFFECTS to "効果音",
        COPY_CONFIRMATION to "▼",
        OPEN_MENU to "メニューを開く",
        NEW_CHAT to "新規チャット",
        ERROR to "エラー",
        SELECT_AND_MANAGE_PROVIDERS to "AIモデルプロバイダーの選択と管理",
        OPENAI_PROVIDERS to "OpenAIプロバイダー",
        COMPATIBLE_PROVIDERS to "互換性のあるプロバイダー",
        CUSTOM_PROVIDERS to "カスタムプロバイダー",
        BASE_URL to "ベースURL",
        API_SETTINGS to "API設定",
        API_BASE_URL to "APIベースURL",
        API_KEY to "APIキー",
        SHOW_EDIT_API_KEY to "目のアイコンをクリックしてAPIキーを表示/編集",
        MODEL_SETTINGS to "モデル設定",
        MODEL to "モデル",
        SELECTED_MODEL to "選択されたモデル",
        SELECT to "選択",
        LOADING_MODELS to "モデルを読み込み中...",
        CONNECT_TO_API to "利用可能なモデルを読み込むためにAPIに接続",
        REFRESH_MODELS to "モデルの更新",
        EDIT_ICON to "✏️",
        DELETE_ICON to "🗑️",
        MANAGE_MODEL_PROVIDERS to "モデルプロバイダーの管理"
    )
    
    // German strings
    private val germanStrings = mapOf(
        APP_NAME to "ChatCat",
        OK to "OK",
        CANCEL to "Abbrechen",
        SAVE to "Speichern",
        DELETE to "Löschen",
        EDIT to "Bearbeiten",
        BACK to "Zurück",
        NEXT to "Weiter",
        DONE to "Fertig",
        SEARCH to "Suche",
        SEND to "Senden",
        COPY to "Kopieren",
        SHARE to "Teilen",
        RETRY to "Wiederholen",
        WRITING to "Schreiben...",
        THINKING to "Denken...",
        NAV_CHAT to "Chat",
        NAV_HISTORY to "Verlauf",
        NAV_SETTINGS to "Einstellungen",
        NAV_ABOUT to "Über",
        SETTINGS_TITLE to "Einstellungen",
        SETTINGS_LANGUAGE to "Sprache",
        SETTINGS_THEME to "Thema",
        SETTINGS_THEME_LIGHT to "Hell",
        SETTINGS_THEME_DARK to "Dunkel",
        SETTINGS_THEME_SYSTEM to "Systemstandard",
        SETTINGS_APPEARANCE_SECTION to "Aussehen",
        SETTINGS_MODEL_SECTION to "Modellkonfiguration",
        SETTINGS_OTHER_SECTION to "Weitere Einstellungen",
        RECENT_CONVERSATIONS to "Letzte Gespräche",
        API_CONNECTED to "✓ API verbunden",
        ACTIVE_PROVIDER to "Aktiver Anbieter",
        TEMPERATURE to "Temperatur",
        MAX_TOKENS to "Max. Tokens",
        STREAM_MODE to "Stream-Modus",
        FONT_SIZE to "Schriftgröße",
        SMALL to "Klein",
        MEDIUM to "Mittel",
        LARGE to "Groß",
        EXTRA_LARGE to "Sehr groß",
        SOUND_EFFECTS to "Soundeffekte",
        COPY_CONFIRMATION to "▼",
        OPEN_MENU to "Menü öffnen",
        NEW_CHAT to "Neuer Chat",
        ERROR to "Fehler",
        SELECT_AND_MANAGE_PROVIDERS to "KI-Modellanbieter auswählen und verwalten",
        OPENAI_PROVIDERS to "OpenAI-Anbieter",
        COMPATIBLE_PROVIDERS to "Kompatible Anbieter",
        CUSTOM_PROVIDERS to "Benutzerdefinierte Anbieter",
        BASE_URL to "Basis-URL",
        API_SETTINGS to "API-Einstellungen",
        API_BASE_URL to "API-Basis-URL",
        API_KEY to "API-Schlüssel",
        SHOW_EDIT_API_KEY to "Klicken Sie auf das Augensymbol, um den API-Schlüssel anzuzeigen/zu bearbeiten",
        MODEL_SETTINGS to "Modelleinstellungen",
        MODEL to "Modell",
        SELECTED_MODEL to "Ausgewähltes Modell",
        SELECT to "Auswählen",
        LOADING_MODELS to "Modelle werden geladen...",
        CONNECT_TO_API to "Mit API verbinden, um verfügbare Modelle zu laden",
        REFRESH_MODELS to "Modelle aktualisieren",
        EDIT_ICON to "✏️",
        DELETE_ICON to "🗑️",
        MANAGE_MODEL_PROVIDERS to "Modellanbieter verwalten"
    )
    
    // French strings
    private val frenchStrings = mapOf(
        APP_NAME to "ChatCat",
        OK to "OK",
        CANCEL to "Annuler",
        SAVE to "Enregistrer",
        DELETE to "Supprimer",
        EDIT to "Modifier",
        BACK to "Retour",
        NEXT to "Suivant",
        DONE to "Terminé",
        SEARCH to "Rechercher",
        SEND to "Envoyer",
        COPY to "Copier",
        SHARE to "Partager",
        RETRY to "Réessayer",
        WRITING to "Rédaction...",
        THINKING to "Réflexion...",
        NAV_CHAT to "Discussion",
        NAV_HISTORY to "Historique",
        NAV_SETTINGS to "Paramètres",
        NAV_ABOUT to "À propos",
        SETTINGS_TITLE to "Paramètres",
        SETTINGS_LANGUAGE to "Langue",
        SETTINGS_THEME to "Thème",
        SETTINGS_THEME_LIGHT to "Clair",
        SETTINGS_THEME_DARK to "Sombre",
        SETTINGS_THEME_SYSTEM to "Par défaut du système",
        SETTINGS_APPEARANCE_SECTION to "Apparence",
        SETTINGS_MODEL_SECTION to "Configuration du modèle",
        SETTINGS_OTHER_SECTION to "Autres paramètres",
        RECENT_CONVERSATIONS to "Conversations récentes",
        API_CONNECTED to "✓ API connectée",
        ACTIVE_PROVIDER to "Fournisseur actif",
        TEMPERATURE to "Température",
        MAX_TOKENS to "Tokens maximum",
        STREAM_MODE to "Mode flux",
        FONT_SIZE to "Taille de police",
        SMALL to "Petit",
        MEDIUM to "Moyen",
        LARGE to "Grand",
        EXTRA_LARGE to "Très grand",
        SOUND_EFFECTS to "Effets sonores",
        COPY_CONFIRMATION to "▼",
        OPEN_MENU to "Ouvrir le menu",
        NEW_CHAT to "Nouvelle discussion",
        ERROR to "Erreur",
        SELECT_AND_MANAGE_PROVIDERS to "Sélectionner et gérer les fournisseurs de modèles IA",
        OPENAI_PROVIDERS to "Fournisseurs OpenAI",
        COMPATIBLE_PROVIDERS to "Fournisseurs compatibles",
        CUSTOM_PROVIDERS to "Fournisseurs personnalisés",
        BASE_URL to "URL de base",
        API_SETTINGS to "Paramètres API",
        API_BASE_URL to "URL de base de l'API",
        API_KEY to "Clé API",
        SHOW_EDIT_API_KEY to "Cliquez sur l'icône œil pour afficher/modifier la clé API",
        MODEL_SETTINGS to "Paramètres du modèle",
        MODEL to "Modèle",
        SELECTED_MODEL to "Modèle sélectionné",
        SELECT to "Sélectionner",
        LOADING_MODELS to "Chargement des modèles...",
        CONNECT_TO_API to "Connectez-vous à l'API pour charger les modèles disponibles",
        REFRESH_MODELS to "Actualiser les modèles",
        EDIT_ICON to "✏️",
        DELETE_ICON to "🗑️",
        MANAGE_MODEL_PROVIDERS to "Gérer les fournisseurs de modèles"
    )
    
    // English strings
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
        SETTINGS_OTHER_SECTION to "Other Settings",
        RECENT_CONVERSATIONS to "Recent Conversations",
        API_CONNECTED to "✓ API Connected",
        ACTIVE_PROVIDER to "Active Provider",
        TEMPERATURE to "Temperature",
        MAX_TOKENS to "Max Tokens",
        STREAM_MODE to "Stream Mode",
        FONT_SIZE to "Font Size",
        SMALL to "Small",
        MEDIUM to "Medium",
        LARGE to "Large",
        EXTRA_LARGE to "Extra Large",
        SOUND_EFFECTS to "Sound Effects",
        COPY_CONFIRMATION to "▼",
        OPEN_MENU to "Open Menu",
        NEW_CHAT to "New Chat",
        ERROR to "Error",
        SELECT_AND_MANAGE_PROVIDERS to "Select and manage your AI model providers",
        OPENAI_PROVIDERS to "OpenAI Providers",
        COMPATIBLE_PROVIDERS to "Compatible Providers",
        CUSTOM_PROVIDERS to "Custom Providers",
        BASE_URL to "Base URL",
        API_SETTINGS to "API Settings",
        API_BASE_URL to "API Base URL",
        API_KEY to "API Key",
        SHOW_EDIT_API_KEY to "Click the eye icon to show/edit the API key",
        MODEL_SETTINGS to "Model Settings",
        MODEL to "Model",
        SELECTED_MODEL to "Selected Model",
        SELECT to "Select",
        LOADING_MODELS to "Loading models...",
        CONNECT_TO_API to "Connect to API to load available models",
        REFRESH_MODELS to "Refresh Models",
        EDIT_ICON to "✏️",
        DELETE_ICON to "🗑️",
        MANAGE_MODEL_PROVIDERS to "Manage Model Providers",
        CAT_EMOJI to "🐱",
    )
}
