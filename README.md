# ChatCat AI

ChatCat is a modern AI chat assistant built with Kotlin Multiplatform, offering an OpenAI-like experience across multiple platforms.

## Features

- **Intelligent Conversations** - Chat with an AI assistant that can answer questions, provide information, and help with various tasks
- **Cross-Platform Support** - Available on Android, iOS, Web, and Desktop
- **Elegant UI** - Clean, intuitive interface with a cat-themed design
- **Offline Mode** - Basic functionality available without an internet connection
- **Conversation History** - Save and review past conversations
- **Customizable Responses** - Adjust the AI's personality and response style
- **File Sharing** - Send and receive files through the chat interface
- **Dark/Light Mode** - Choose your preferred visual theme
- **Configurable API Settings** - Easily set up API keys, base URLs, and connection parameters
- **Preference Management** - Store and manage user settings across sessions and platforms

## Project Structure

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that's common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple's CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you're sharing your UI with Compose Multiplatform, 
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

## Getting Started

1. Clone the repository
2. Open the project in Android Studio or IntelliJ IDEA
3. Configure your API settings in the `local.properties` file:
   ```
   api.key=your_api_key_here
   api.base.url=https://your-api-endpoint.com
   ```
4. Run the desired platform configuration
   - For web: Run the `:composeApp:wasmJsBrowserDevelopmentRun` Gradle task
   - For Android: Use the Android run configuration
   - For desktop: Run the `:composeApp:run` Gradle task
   - For iOS: Open the Xcode project in the `/iosApp` directory

## Configuration

ChatCat supports various configuration options:

- **API Configuration**: Set your API key, base URL, and other connection parameters
- **Chat Settings**: Customize temperature, max tokens, and other model parameters
- **UI Preferences**: Set theme, font size, and other display options
- **Storage Settings**: Configure where and how conversations are saved

These settings can be accessed through the settings screen in the app interface.

## Technology

ChatCat is built using:
- Kotlin Multiplatform
- Compose Multiplatform for UI
- Kotlin/Wasm for web support
- Retrofit for API communication
- Kotlin Coroutines for asynchronous operations

## Additional Resources

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).