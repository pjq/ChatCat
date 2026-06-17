# ChatCat

Kotlin Multiplatform Compose chat app — multimodal AI client with MCP support.

## Build & Run

```bash
# Android debug APK
./gradlew :composeApp:assembleDebug

# Desktop
./gradlew :composeApp:desktopRun -PmainClass=me.pjq.chatcat.MainKt

# Web (Wasm)
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

## Architecture

- **KMP** with Compose Multiplatform (Android-first, Desktop/iOS/Wasm shared)
- **Ktor** HTTP client — direct OpenAI-compatible API calls (no wrapper libraries)
- **SSE streaming** for real-time chat responses
- **MCP client** — JSON-RPC 2.0 over HTTP to connect external tool servers
- **PreCompose** for navigation, ViewModels
- **multiplatform-settings** for persistence (single JSON blob pattern)
- **kotlinx.serialization** with sealed class polymorphism

## Key Directories

```
composeApp/src/commonMain/kotlin/me/pjq/chatcat/
├── model/          # Data classes: Message, Conversation, ModelProvider, McpServer, UserPreferences
├── repository/     # PersistentConversationRepository, PersistentPreferencesRepository
├── service/        # OpenAIChatService, McpClientService, OpenAIImageGenerationService
├── viewmodel/      # ChatViewModel, SettingsViewModel
├── ui/
│   ├── components/ # MessageBubble, ChatInput, SettingsRows, ConversationListItem
│   ├── screens/    # ChatScreen, SettingsScreen, ProviderEditorScreen, McpServersScreen
│   ├── navigation/ # AppNavigation (PreCompose NavHost)
│   └── theme/      # ChatCatTheme, accent palettes, dynamic color
├── platform/       # expect/actual: UUID, ImagePicker, PlatformImage
├── di/             # AppModule (manual DI)
└── i18n/           # LanguageManager, StringResources (6 languages)
```

## Conventions

- Kotlin only (no Java)
- Material 3 design system with 5 accent palettes + Android 12+ dynamic color
- All API communication via Ktor (no aallam/openai-client)
- Provider-agnostic: supports OpenAI, Anthropic, Groq, Together, Ollama, LM Studio, any OpenAI-compatible endpoint
- Settings stored as single JSON blob in multiplatform-settings
- Legacy v1 conversations auto-migrated on first load
- Images: vision input via platform photo picker + image generation via DALL-E/gpt-image-1

## Model Defaults

- Default provider: OpenAI (gpt-5)
- Temperature: 1.0
- Max tokens: 4096
- Stream: enabled

## Platform-Specific Code (expect/actual)

- `platform/Identifiers.kt` — `randomUUID()`
- `platform/ImagePicker.kt` — system photo picker
- `ui/components/PlatformImage.kt` — image rendering (Coil on Android, Skia on Desktop)
- `ui/theme/ChatCatTheme.kt` — `rememberDynamicColorScheme()` (Android 12+ only)
