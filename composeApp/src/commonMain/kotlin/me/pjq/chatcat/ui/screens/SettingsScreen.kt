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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import me.pjq.chatcat.model.FontSize
import me.pjq.chatcat.model.ModelConfig
import me.pjq.chatcat.model.Theme
import me.pjq.chatcat.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val preferences = uiState.preferences
    
    // Load available models when API is available
    if (uiState.isApiAvailable && uiState.availableModels.isEmpty() && !uiState.isLoading) {
        viewModel.loadAvailableModels()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    Text(
                        text = "←",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(8.dp)
                            .clickable { onNavigateBack() }
                    )
                },
                actions = {
                    if (uiState.isApiAvailable) {
                        Text(
                            text = "✓ API Connected",
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
            // API Settings
            SettingsSection(title = "API Settings") {
                // Use remember with key to update when preferences change
                var apiKeyText by remember(preferences.apiKey) { mutableStateOf(preferences.apiKey) }
                OutlinedTextField(
                    value = apiKeyText,
                    onValueChange = { 
                        apiKeyText = it
                        viewModel.updateApiKey(it) 
                    },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Use remember with key to update when preferences change
                var apiBaseUrlText by remember(preferences.apiBaseUrl) { mutableStateOf(preferences.apiBaseUrl) }
                OutlinedTextField(
                    value = apiBaseUrlText,
                    onValueChange = { 
                        apiBaseUrlText = it
                        viewModel.updateApiBaseUrl(it) 
                    },
                    label = { Text("API Base URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Save button for API settings
                androidx.compose.material3.Button(
                    onClick = {
                        // Explicitly save the settings
                        viewModel.updateApiKey(apiKeyText)
                        viewModel.updateApiBaseUrl(apiBaseUrlText)
                        viewModel.checkApiAvailability()
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save API Settings")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Model Settings - MOVED HERE FROM BELOW
            SettingsSection(title = "Model Settings") {
                // Use remember with key to update when preferences change
                var temperature by remember(preferences.defaultModelConfig.temperature) { 
                    mutableStateOf(preferences.defaultModelConfig.temperature) 
                }
                // Use remember with key to update when preferences change
                var maxTokens by remember(preferences.defaultModelConfig.maxTokens) { 
                    mutableStateOf(preferences.defaultModelConfig.maxTokens.toFloat()) 
                }
                // Use remember with key to update when model changes
                var expanded by remember(preferences.defaultModelConfig.model) { mutableStateOf(false) }
                
                // Model dropdown
                Column {
                    Text(
                        text = "Model",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Make the text field clickable to open the dropdown
                        OutlinedTextField(
                            value = preferences.defaultModelConfig.model,
                            onValueChange = { },
                            label = { Text("Selected Model") },
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = uiState.availableModels.isNotEmpty()) { 
                                    expanded = true 
                                },
                            singleLine = true,
                            enabled = false,
                            readOnly = true,
                            trailingIcon = {
                                if (uiState.availableModels.isNotEmpty()) {
                                    Text(
                                        text = "▼",
                                        modifier = Modifier.clickable { expanded = true }
                                    )
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        androidx.compose.material3.Button(
                            onClick = { expanded = true },
                            enabled = uiState.availableModels.isNotEmpty()
                        ) {
                            Text("Select")
                        }
                        
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            if (uiState.isLoading) {
                                DropdownMenuItem(
                                    text = { 
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                                            Text("Loading models...") 
                                        }
                                    },
                                    onClick = { }
                                )
                            } else {
                                uiState.availableModels.forEach { model ->
                                DropdownMenuItem(
                                    text = { Text(model) },
                                    onClick = {
                                        viewModel.updateModel(model)
                                        expanded = false
                                    }
                                )
                                }
                            }
                        }
                    }
                    
                    if (uiState.availableModels.isEmpty() && !uiState.isLoading) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Connect to API to load available models",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Temperature: ${temperature}",
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
                
                Text(
                    text = "Max Tokens: ${maxTokens.toInt()}",
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
                    title = "Stream Mode",
                    checked = streamMode,
                    onCheckedChange = { 
                        streamMode = it
                        viewModel.updateStreamMode(it) 
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Appearance Settings
            SettingsSection(title = "Appearance") {
                Text(
                    text = "Theme",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Column(Modifier.selectableGroup()) {
                    ThemeOption(
                        title = "Light",
                        selected = preferences.theme == Theme.LIGHT,
                        onClick = { viewModel.updateTheme(Theme.LIGHT) }
                    )
                    
                    ThemeOption(
                        title = "Dark",
                        selected = preferences.theme == Theme.DARK,
                        onClick = { viewModel.updateTheme(Theme.DARK) }
                    )
                    
                    ThemeOption(
                        title = "System Default",
                        selected = preferences.theme == Theme.SYSTEM,
                        onClick = { viewModel.updateTheme(Theme.SYSTEM) }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Font Size",
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
            SettingsSection(title = "Other Settings") {
                SwitchSetting(
                    title = "Sound Effects",
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
fun SwitchSetting(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
    
    Divider()
}
