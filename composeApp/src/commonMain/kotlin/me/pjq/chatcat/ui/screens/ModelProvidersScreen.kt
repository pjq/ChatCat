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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.OutlinedTextField
import me.pjq.chatcat.di.AppModule
import me.pjq.chatcat.i18n.StringResources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.pjq.chatcat.model.DefaultModelProviders
import me.pjq.chatcat.model.ModelProvider
import me.pjq.chatcat.model.ProviderType
import me.pjq.chatcat.ui.components.SwitchSetting
import me.pjq.chatcat.ui.components.ProviderEditDialog
import me.pjq.chatcat.ui.components.getProviderTypeDescription
import me.pjq.chatcat.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelProvidersScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToProviderSettings: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // State to track if we're currently switching providers
    var isSwitchingProvider by remember { mutableStateOf(false) }
    var justActivatedProviderId by remember { mutableStateOf<String?>(null) }
    val uiState by viewModel.uiState.collectAsState()
    val providers = uiState.preferences.modelProviders
    val activeProviderId = uiState.preferences.activeProviderId
    
    // Get the language manager
    val languageManager = AppModule.languageManager
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(languageManager.getString(StringResources.SETTINGS_MODEL_SECTION)) },
                navigationIcon = {
                    Text(
                        text = languageManager.getString(StringResources.BACK),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(8.dp)
                            .clickable { onNavigateBack() }
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.createNewProvider() }
            ) {
                Text(
                    text = "➕",
                    style = MaterialTheme.typography.titleMedium
                )
            }
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
            Text(
                text = "Select and manage your AI model providers",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Active provider section
            Text(
                text = "Active Provider",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Show active provider at the top
            val activeProvider = providers.find { it.id == activeProviderId } ?: providers.firstOrNull()
            if (activeProvider != null) {
                ProviderCard(
                    provider = activeProvider,
                    isActive = true,
                    onSelect = { },
                    onEdit = { viewModel.startEditingProvider(activeProvider) },
                    onDelete = { viewModel.deleteProvider(activeProvider.id) },
                    onNavigateToSettings = { onNavigateToProviderSettings(activeProvider.id) },
                    viewModel = viewModel,
                    languageManager = languageManager
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Group providers by type
            val providersByType = providers
                .filter { it.id != activeProviderId }
                .groupBy { it.providerType }
            
            // Display providers by type
            // OpenAI providers section
            if (providersByType.containsKey(ProviderType.OPENAI)) {
                Text(
                    text = "OpenAI Providers",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // OpenAI providers
                providersByType[ProviderType.OPENAI]?.forEach { provider ->
                    var isActivating by remember { mutableStateOf(false) }
                    ProviderCard(
                        provider = provider,
                        isActive = false,
                        isActivating = isActivating,
                        onSelect = { 
                            isActivating = true
                            viewModel.setActiveProvider(provider.id)
                            // After setting active provider, load available models
                            viewModel.loadAvailableModels()
                            // Simulate navigation back to top to see the active provider
                            onNavigateBack()
                        },
                        onEdit = { viewModel.startEditingProvider(provider) },
                        onDelete = { viewModel.deleteProvider(provider.id) },
                        onNavigateToSettings = { onNavigateToProviderSettings(provider.id) },
                        viewModel = viewModel,
                        languageManager = languageManager
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Compatible providers section
            if (providersByType.containsKey(ProviderType.OPENAI_COMPATIBLE)) {
                Text(
                    text = "Compatible Providers",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                providersByType[ProviderType.OPENAI_COMPATIBLE]?.forEach { provider ->
                    var isActivating by remember { mutableStateOf(false) }
                    ProviderCard(
                        provider = provider,
                        isActive = false,
                        isActivating = isActivating,
                        onSelect = { 
                            isActivating = true
                            viewModel.setActiveProvider(provider.id)
                            // After setting active provider, load available models
                            viewModel.loadAvailableModels()
                            onNavigateBack()
                        },
                        onEdit = { viewModel.startEditingProvider(provider) },
                        onDelete = { viewModel.deleteProvider(provider.id) },
                        onNavigateToSettings = { onNavigateToProviderSettings(provider.id) },
                        viewModel = viewModel,
                        languageManager = languageManager
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Custom providers section
            if (providersByType.containsKey(ProviderType.CUSTOM)) {
                Text(
                    text = "Custom Providers",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                providersByType[ProviderType.CUSTOM]?.forEach { provider ->
                    var isActivating by remember { mutableStateOf(false) }
                    ProviderCard(
                        provider = provider,
                        isActive = false,
                        isActivating = isActivating,
                        onSelect = { 
                            isActivating = true
                            viewModel.setActiveProvider(provider.id)
                            onNavigateBack()
                        },
                        onEdit = { viewModel.startEditingProvider(provider) },
                        onDelete = { viewModel.deleteProvider(provider.id) },
                        onNavigateToSettings = { onNavigateToProviderSettings(provider.id) },
                        viewModel = viewModel,
                        languageManager = languageManager
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            // Provider editing dialog
            val editingProvider = uiState.editingProvider
            if (uiState.isEditingProvider && editingProvider != null) {
                ProviderEditDialog(
                    provider = editingProvider,
                    onSave = { viewModel.saveProvider(it) },
                    onCancel = { viewModel.cancelEditingProvider() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderCard(
    provider: ModelProvider,
    isActive: Boolean,
    isActivating: Boolean = false,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: SettingsViewModel,
    languageManager: me.pjq.chatcat.i18n.LanguageManager
) {
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = provider.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Text(
                        text = getProviderTypeDescription(provider.providerType),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Active status indicator
                if (isActive) {
                    Card(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = languageManager.getString(StringResources.SETTINGS_MODEL_SECTION),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Provider details
            Text(
                text = "Base URL: ${provider.baseUrl}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Always show API Settings first
            Spacer(modifier = Modifier.height(16.dp))
            
            // API Settings
            Text(
                text = "API Settings",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Base URL
            var baseUrlText by remember(provider.baseUrl) { mutableStateOf(provider.baseUrl) }
            OutlinedTextField(
                value = baseUrlText,
                onValueChange = { baseUrlText = it },
                label = { Text("API Base URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // API Key with toggle for visibility
            var apiKeyText by remember(provider.apiKey) { mutableStateOf(provider.apiKey) }
            var showApiKey by remember { mutableStateOf(false) }
            
            OutlinedTextField(
                value = if (showApiKey) apiKeyText else if (apiKeyText.isNotEmpty()) "••••••••••••••••" else "",
                onValueChange = { 
                    // Only update if visible or empty (new input)
                    if (showApiKey || apiKeyText.isEmpty()) {
                        apiKeyText = it
                    }
                },
                label = { Text("API Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = true, // Always enabled
                trailingIcon = {
                    IconButton(onClick = { showApiKey = !showApiKey }) {
                        Text(
                            text = if (showApiKey) "🙈" else "👁️",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            )
            
            // Helper text for API key
            if (!showApiKey && apiKeyText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Click the eye icon to show/edit the API key",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Save button
            androidx.compose.material3.Button(
                onClick = {
                    // Create an updated provider with the new API settings
                    val updatedProvider = provider.copy(
                        baseUrl = baseUrlText,
                        apiKey = apiKeyText
                    )
                    
                    // Save the updated provider
                    viewModel.saveProvider(updatedProvider)
                    
                    // If this is the active provider, update the API settings
                    if (isActive) {
                        viewModel.updateApiBaseUrl(baseUrlText)
                        viewModel.updateApiKey(apiKeyText)
                        // Reload models with the new settings
                        viewModel.loadAvailableModels()
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(languageManager.getString(StringResources.SAVE))
            }
            
            // Model Settings section for active provider (now below API Settings)
            if (isActive) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Model Settings header
                Text(
                    text = "Model Settings",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val uiState by viewModel.uiState.collectAsState()
                val preferences = uiState.preferences
                
                // Use remember with key to update when preferences change
                var temperature by remember(preferences.defaultModelConfig.temperature) { 
                    mutableStateOf(preferences.defaultModelConfig.temperature) 
                }
                // Use remember with key to update when preferences change
                var maxTokens by remember(preferences.defaultModelConfig.maxTokens) { 
                    mutableStateOf(preferences.defaultModelConfig.maxTokens.toFloat()) 
                }
                // Use remember with key to update when model changes
                var modelExpanded by remember(preferences.defaultModelConfig.model) { mutableStateOf(false) }
                
                // Auto-load models when provider is active and models are empty
                if (uiState.availableModels.isEmpty() && !uiState.isLoading && uiState.isApiAvailable) {
                    androidx.compose.runtime.LaunchedEffect(provider.id) {
                        viewModel.loadAvailableModels()
                    }
                }
                
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
                                    modelExpanded = true 
                                },
                            singleLine = true,
                            enabled = false,
                            readOnly = true,
                            trailingIcon = {
                                if (uiState.availableModels.isNotEmpty()) {
                                    Text(
                                        text = "▼",
                                        modifier = Modifier.clickable { modelExpanded = true }
                                    )
                                } else if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.padding(end = 8.dp).width(20.dp).height(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        androidx.compose.material3.Button(
                            onClick = { modelExpanded = true },
                            enabled = uiState.availableModels.isNotEmpty()
                        ) {
                            Text("Select")
                        }
                        
                        DropdownMenu(
                            expanded = modelExpanded,
                            onDismissRequest = { modelExpanded = false }
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
                                            modelExpanded = false
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
                
                // Load models button
                if (!uiState.isLoading && uiState.isApiAvailable) {
                    androidx.compose.material3.OutlinedButton(
                        onClick = { viewModel.loadAvailableModels() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Refresh Models")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons - simplified and more elegant
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Edit and Delete buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Edit button
                    IconButton(
                        onClick = onEdit
                    ) {
                        Text(
                            text = "✏️",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    // Delete button (only for non-default providers)
                    if (provider.id != DefaultModelProviders.OPENAI.id) {
                        IconButton(
                            onClick = onDelete
                        ) {
                            Text(
                                text = "🗑️",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
                
                // Right side: Active switch or loading indicator
                if (isActivating) {
                    // Show loading indicator when activating
                    CircularProgressIndicator(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                } else {
                    // Show switch when not activating
                    androidx.compose.material3.Switch(
                        checked = isActive,
                        onCheckedChange = { checked -> 
                            // Only call onSelect when activating a non-active provider
                            if (checked && !isActive) onSelect() 
                        },
                        enabled = !isActive, // Only enable for non-active providers
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            checkedBorderColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    }
}
