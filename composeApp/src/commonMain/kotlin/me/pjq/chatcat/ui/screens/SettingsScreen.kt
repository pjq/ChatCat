package me.pjq.chatcat.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.pjq.chatcat.di.AppModule
import me.pjq.chatcat.i18n.LanguageManager
import me.pjq.chatcat.i18n.StringResources
import me.pjq.chatcat.model.FontSize
import me.pjq.chatcat.model.Language
import me.pjq.chatcat.model.ModelConfig
import me.pjq.chatcat.model.ProviderType
import me.pjq.chatcat.model.Theme
import me.pjq.chatcat.ui.components.getProviderTypeDescription
import me.pjq.chatcat.ui.components.SwitchSetting
import me.pjq.chatcat.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToModelProviders: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val preferences = uiState.preferences
    
    // Load available models when API is available
    if (uiState.isApiAvailable && uiState.availableModels.isEmpty() && !uiState.isLoading) {
        viewModel.loadAvailableModels()
    }
    
    // Get the language manager
    val languageManager = AppModule.languageManager
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(languageManager.getString(StringResources.SETTINGS_TITLE)) },
                navigationIcon = {
                    Text(
                        text = languageManager.getString(StringResources.BACK),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(8.dp)
                            .clickable { onNavigateBack() }
                    )
                },
                actions = {
                    if (uiState.isApiAvailable) {
                        Text(
                            text = languageManager.getString(StringResources.API_CONNECTED),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Model Providers Settings
            SettingsSection(title = languageManager.getString(StringResources.SELECT_AND_MANAGE_PROVIDERS)) {
                val activeProvider = uiState.activeProvider
                
                // Show active provider info and selected model in a more visual way
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Provider info
                        Text(
                            text = languageManager.getString(StringResources.ACTIVE_PROVIDER),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = activeProvider.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = getProviderTypeDescription(activeProvider.providerType),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Display API URL (truncated if too long)
                        val displayUrl = if (activeProvider.baseUrl.length > 30) 
                            activeProvider.baseUrl.take(27) + "..." 
                        else 
                            activeProvider.baseUrl
                        
                        Text(
                            text = displayUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Model info with a subtle divider
                        Divider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Display the currently selected model
                        Text(
                            text = "Current Model",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Show the provider's selected model if available, otherwise the default model
                        val modelToShow = if (activeProvider.selectedModel.isNotBlank()) {
                            activeProvider.selectedModel
                        } else {
                            // Fallback to a default model based on provider type
                            when (activeProvider.providerType) {
                                ProviderType.OPENAI -> "gpt-3.5-turbo"
                                ProviderType.OPENAI_COMPATIBLE -> "gpt-3.5-turbo"
                                ProviderType.CUSTOM -> "model1"
                            }
                        }
                        
                        Text(
                            text = modelToShow,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Button to navigate to Model Providers screen
                androidx.compose.material3.Button(
                    onClick = onNavigateToModelProviders,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(languageManager.getString(StringResources.MANAGE_MODEL_PROVIDERS))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Model Configuration Settings
            SettingsSection(title = languageManager.getString(StringResources.SETTINGS_MODEL_SECTION)) {
                val preferences = uiState.preferences
                
                // Use remember with key to update when preferences change
                var temperature by remember(preferences.defaultModelConfig.temperature) { 
                    mutableStateOf(preferences.defaultModelConfig.temperature) 
                }
                // Use remember with key to update when preferences change
                var maxTokens by remember(preferences.defaultModelConfig.maxTokens) { 
                    mutableStateOf(preferences.defaultModelConfig.maxTokens.toFloat()) 
                }
                
                // Temperature slider
                Text(
                    text = languageManager.getString(StringResources.TEMPERATURE) + ": ${temperature}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Slider(
                    value = temperature.toFloat(),
                    onValueChange = { 
                        temperature = it.toDouble()
                        viewModel.updateDefaultModelConfig(
                            preferences.defaultModelConfig.copy(temperature = it.toDouble())
                        )
                    },
                    valueRange = 0f..1f,
                    steps = 10,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Max Tokens slider
                Text(
                    text = languageManager.getString(StringResources.MAX_TOKENS) + ": ${maxTokens.toInt()}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Slider(
                    value = maxTokens,
                    onValueChange = { 
                        maxTokens = it
                        viewModel.updateDefaultModelConfig(
                            preferences.defaultModelConfig.copy(maxTokens = it.toInt())
                        )
                    },
                    valueRange = 100f..4000f,
                    steps = 39,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Add a state variable to properly track the stream mode toggle
                var streamMode by remember(preferences.defaultModelConfig.stream) { 
                    mutableStateOf(preferences.defaultModelConfig.stream) 
                }
                
                SwitchSetting(
                    title = languageManager.getString(StringResources.STREAM_MODE),
                    checked = streamMode,
                    onCheckedChange = { 
                        streamMode = it
                        viewModel.updateStreamMode(it) 
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Appearance Settings
            SettingsSection(title = languageManager.getString(StringResources.SETTINGS_APPEARANCE_SECTION)) {
                // Language setting (moved to top)
                Text(
                    text = languageManager.getString(StringResources.SETTINGS_LANGUAGE),
                    style = MaterialTheme.typography.bodyLarge
                )
                
                // Language dropdown
                LanguageSelector(
                    selectedLanguage = preferences.language,
                    onLanguageSelected = { viewModel.updateLanguage(it) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Theme options
                Column(Modifier.selectableGroup()) {
                    ThemeOption(
                        title = languageManager.getString(StringResources.SETTINGS_THEME_LIGHT),
                        selected = preferences.theme == Theme.LIGHT,
                        onClick = { viewModel.updateTheme(Theme.LIGHT) }
                    )
                    
                    ThemeOption(
                        title = languageManager.getString(StringResources.SETTINGS_THEME_DARK),
                        selected = preferences.theme == Theme.DARK,
                        onClick = { viewModel.updateTheme(Theme.DARK) }
                    )
                    
                    ThemeOption(
                        title = languageManager.getString(StringResources.SETTINGS_THEME_SYSTEM),
                        selected = preferences.theme == Theme.SYSTEM,
                        onClick = { viewModel.updateTheme(Theme.SYSTEM) }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = languageManager.getString(StringResources.FONT_SIZE),
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Column(Modifier.selectableGroup()) {
                    FontSizeOption(
                        title = "Small",
                        selected = preferences.fontSize == FontSize.SMALL,
                        onClick = { viewModel.updateFontSize(FontSize.SMALL) }
                    )
                    
                    FontSizeOption(
                        title = "Medium",
                        selected = preferences.fontSize == FontSize.MEDIUM,
                        onClick = { viewModel.updateFontSize(FontSize.MEDIUM) }
                    )
                    
                    FontSizeOption(
                        title = "Large",
                        selected = preferences.fontSize == FontSize.LARGE,
                        onClick = { viewModel.updateFontSize(FontSize.LARGE) }
                    )
                    
                    FontSizeOption(
                        title = "Extra Large",
                        selected = preferences.fontSize == FontSize.EXTRA_LARGE,
                        onClick = { viewModel.updateFontSize(FontSize.EXTRA_LARGE) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Other Settings
            SettingsSection(title = languageManager.getString(StringResources.SETTINGS_OTHER_SECTION)) {
                SwitchSetting(
                    title = languageManager.getString(StringResources.SOUND_EFFECTS),
                    checked = preferences.enableSoundEffects,
                    onCheckedChange = { viewModel.updateSoundEffectsEnabled(it) }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            content()
        }
    }
}

@Composable
fun ThemeOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick
            )
            .padding(vertical = 8.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun FontSizeOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick
            )
            .padding(vertical = 8.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun LanguageSelector(
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit
) {
    // Use remember with selectedLanguage as key to update when language changes
    var expanded by remember { mutableStateOf(false) }
    // Remember the current selected language to display
    var currentLanguage by remember(selectedLanguage) { mutableStateOf(selectedLanguage) }
    
    // Create a card with a clickable area to open the dropdown
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = currentLanguage.displayName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = "▼",
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Dropdown menu
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                // Add an item for each language
                Language.values().forEach { language ->
                    DropdownMenuItem(
                        text = { 
                            Text(language.displayName) 
                        },
                        onClick = {
                            currentLanguage = language
                            onLanguageSelected(language)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
