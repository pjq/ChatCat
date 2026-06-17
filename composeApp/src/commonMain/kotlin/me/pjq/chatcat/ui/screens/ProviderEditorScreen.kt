package me.pjq.chatcat.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.pjq.chatcat.model.DefaultModelProviders
import me.pjq.chatcat.model.ModelProvider
import me.pjq.chatcat.model.ProviderPreset
import me.pjq.chatcat.model.ProviderType
import me.pjq.chatcat.platform.randomUUID
import me.pjq.chatcat.viewmodel.SettingsViewModel

private enum class ProbeState { Idle, Loading, Success, Error }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderEditorScreen(
    viewModel: SettingsViewModel,
    initial: ModelProvider?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNew = initial == null
    val seed = initial ?: ModelProvider(
        id = "provider_${randomUUID().take(8)}",
        name = "",
        baseUrl = "",
        providerType = ProviderType.OPENAI_COMPATIBLE,
        selectedModel = ""
    )

    var name by remember { mutableStateOf(seed.name) }
    var baseUrl by remember { mutableStateOf(seed.baseUrl) }
    var apiKey by remember { mutableStateOf(seed.apiKey) }
    var providerType by remember { mutableStateOf(seed.providerType) }
    var selectedModel by remember { mutableStateOf(seed.selectedModel) }
    var availableModels by remember { mutableStateOf(seed.availableModels) }
    var visionModels by remember { mutableStateOf(seed.visionModels) }
    var imageGenModels by remember { mutableStateOf(seed.imageGenModels) }

    var keyVisible by remember { mutableStateOf(false) }
    var probeState by remember { mutableStateOf(ProbeState.Idle) }
    var probeMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun applyPreset(p: ProviderPreset) {
        if (name.isBlank() || isNew) name = p.name
        baseUrl = p.baseUrl
        providerType = p.providerType
        if (selectedModel.isBlank()) selectedModel = p.suggestedModels.firstOrNull().orEmpty()
        if (availableModels.isEmpty()) availableModels = p.suggestedModels
        if (visionModels.isEmpty()) visionModels = p.visionModels
        if (imageGenModels.isEmpty()) imageGenModels = p.imageGenModels
        probeState = ProbeState.Idle
        probeMessage = null
    }

    fun probe() {
        if (baseUrl.isBlank()) {
            probeState = ProbeState.Error
            probeMessage = "Enter a base URL first"
            return
        }
        scope.launch {
            probeState = ProbeState.Loading
            probeMessage = null
            val result = viewModel.probeProvider(baseUrl, apiKey)
            result.fold(
                onSuccess = { models ->
                    if (models.isEmpty()) {
                        probeState = ProbeState.Error
                        probeMessage = "Connected, but the server returned no models."
                    } else {
                        availableModels = models
                        if (selectedModel.isBlank() || selectedModel !in models) {
                            selectedModel = models.first()
                        }
                        probeState = ProbeState.Success
                        probeMessage = "Connected · ${models.size} models discovered"
                    }
                },
                onFailure = {
                    probeState = ProbeState.Error
                    probeMessage = it.message ?: "Connection failed"
                }
            )
        }
    }

    val canSave = name.isNotBlank() && baseUrl.isNotBlank()

    // Auto-fetch models when editing an existing provider with credentials
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (!isNew && baseUrl.isNotBlank()) {
            probeState = ProbeState.Loading
            val result = viewModel.probeProvider(baseUrl, apiKey)
            result.fold(
                onSuccess = { models ->
                    if (models.isNotEmpty()) {
                        availableModels = models
                        probeState = ProbeState.Success
                        probeMessage = "${models.size} models available"
                    } else {
                        probeState = ProbeState.Idle
                    }
                },
                onFailure = { probeState = ProbeState.Idle }
            )
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Add provider" else "Edit provider") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        enabled = canSave,
                        onClick = {
                            viewModel.saveProvider(
                                seed.copy(
                                    name = name,
                                    baseUrl = baseUrl,
                                    apiKey = apiKey,
                                    providerType = providerType,
                                    selectedModel = selectedModel,
                                    availableModels = availableModels,
                                    visionModels = visionModels,
                                    imageGenModels = imageGenModels
                                )
                            )
                            onClose()
                        }
                    ) { Text(if (isNew) "Add" else "Save") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (isNew) {
                Section(title = "Quick start") {
                    Text(
                        "Tap a preset to autofill the form, then add your API key.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    PresetGrid(onPick = ::applyPreset)
                }
            }

            Section(title = "Identity") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display name") },
                    placeholder = { Text("e.g. OpenAI · Personal") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text("Provider type", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.padding(top = 4.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ProviderType.values().forEach { type ->
                        FilterChip(
                            selected = type == providerType,
                            onClick = { providerType = type },
                            label = { Text(prettyType(type)) }
                        )
                    }
                }
            }

            Section(title = "Connection") {
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it; probeState = ProbeState.Idle },
                    label = { Text("Base URL") },
                    placeholder = { Text("https://api.example.com/v1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it; probeState = ProbeState.Idle },
                    label = { Text("API key") },
                    placeholder = { Text("sk-…") },
                    singleLine = true,
                    visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        Row {
                            IconButton(onClick = { keyVisible = !keyVisible }) {
                                Icon(
                                    imageVector = if (keyVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (keyVisible) "Hide key" else "Show key"
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { probe() },
                        enabled = probeState != ProbeState.Loading
                    ) {
                        if (probeState == ProbeState.Loading) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Testing…")
                        } else {
                            Text("Test connection")
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    AnimatedVisibility(visible = probeMessage != null && probeState != ProbeState.Loading) {
                        StatusPill(state = probeState, message = probeMessage.orEmpty())
                    }
                }
            }

            Section(title = "Default model") {
                if (availableModels.isEmpty()) {
                    Text(
                        "No models loaded yet. Test the connection above, or type a model name below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = selectedModel,
                        onValueChange = { selectedModel = it },
                        label = { Text("Model") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ModelDropdown(
                        models = availableModels,
                        selected = selectedModel,
                        onSelect = { selectedModel = it }
                    )
                }
            }

            if (!isNew) {
                Section(title = "Danger zone") {
                    OutlinedButton(
                        onClick = {
                            viewModel.deleteProvider(seed.id)
                            onClose()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Delete provider", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) { content() }
        }
    }
}

@Composable
private fun PresetGrid(onPick: (ProviderPreset) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DefaultModelProviders.presets.forEach { preset ->
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPick(preset) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(preset.emoji, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(preset.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        if (preset.description.isNotBlank()) {
                            Text(
                                preset.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelDropdown(
    models: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf(selected) }
    val filtered = remember(models, query) {
        if (query.isBlank()) models else models.filter { it.contains(query, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                onSelect(it)
                expanded = true
            },
            label = { Text("Model") },
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(type = androidx.compose.material3.MenuAnchorType.PrimaryEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            filtered.take(40).forEach { model ->
                DropdownMenuItem(
                    text = { Text(model) },
                    onClick = {
                        onSelect(model)
                        query = model
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun StatusPill(state: ProbeState, message: String) {
    val (bg, fg, icon) = when (state) {
        ProbeState.Success -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Filled.Check
        )
        ProbeState.Error -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            Icons.Outlined.ErrorOutline
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Outlined.ContentPaste
        )
    }
    Surface(color = bg, shape = RoundedCornerShape(20.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(message, style = MaterialTheme.typography.labelMedium, color = fg)
        }
    }
}

private fun prettyType(t: ProviderType): String = when (t) {
    ProviderType.OPENAI -> "OpenAI"
    ProviderType.OPENAI_COMPATIBLE -> "Compatible"
    ProviderType.ANTHROPIC -> "Claude"
    ProviderType.CUSTOM -> "Custom"
}
