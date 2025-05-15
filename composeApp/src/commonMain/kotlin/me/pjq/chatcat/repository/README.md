# ChatCat Persistent Storage

This package contains the implementation of persistent storage for the ChatCat application.

## Overview

The persistent storage solution is built on top of the [Multiplatform Settings](https://github.com/russhwolf/multiplatform-settings) library, which provides a common API for storing key-value pairs across different platforms.

## Components

### SettingsFactory

`SettingsFactory` is a utility class that creates platform-agnostic `Settings` and `FlowSettings` instances. Currently, it uses an in-memory implementation for development purposes, but it can be extended to use platform-specific implementations in the future.

### InMemorySettings

`InMemorySettings` is a simple implementation of the `Settings` interface that stores data in memory. This is used as a fallback when platform-specific implementations are not available.

### PersistentPreferencesRepository

`PersistentPreferencesRepository` implements the `PreferencesRepository` interface and uses `FlowSettings` to store user preferences persistently.

### PersistentConversationRepository

`PersistentConversationRepository` implements the `ConversationRepository` interface and uses `Settings` to store conversations persistently.

## Future Enhancements

In the future, the storage solution can be enhanced with:

1. **Platform-specific implementations**: Replace the in-memory implementation with platform-specific ones:
   - Android: SharedPreferences
   - iOS: NSUserDefaults
   - Desktop: Java Preferences API
   - Web: LocalStorage

2. **Data migration**: Add support for migrating data between different versions of the app.

3. **Encryption**: Add support for encrypting sensitive data.

4. **Cloud synchronization**: Add support for synchronizing data across devices using a cloud service.
