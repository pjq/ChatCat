package me.pjq.chatcat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Stream
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.ViewCompact
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.pjq.chatcat.model.Accent
import me.pjq.chatcat.model.Density
import me.pjq.chatcat.model.FontSize
import me.pjq.chatcat.model.Language
import me.pjq.chatcat.model.Theme
import me.pjq.chatcat.ui.components.SettingsRowDivider
import me.pjq.chatcat.ui.components.SettingsRow
import me.pjq.chatcat.ui.components.SettingsSection
import me.pjq.chatcat.ui.components.SettingsSwitchRow
import me.pjq.chatcat.ui.components.SettingsValueRow
import me.pjq.chatcat.viewmodel.SettingsViewModel

private enum class SheetType { THEME, ACCENT, FONT_SIZE, DENSITY, LANGUAGE, SYSTEM_PROMPT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMcp: () -> Unit,
    onAddProvider: () -> Unit,
    onEditProvider: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val prefs = state.preferences
    var activeSheet by remember { mutableStateOf<SheetType?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ─── MODEL (TOP — most critical) ───────────────────────────────
            SettingsSection(title = "Model") {
                SettingsValueRow(
                    icon = Icons.Outlined.Cloud,
                    title = "Provider",
                    value = state.activeProvider.name,
                    onClick = { onEditProvider(state.activeProvider.id) }
                )
                SettingsRowDivider()
                SettingsValueRow(
                    icon = Icons.Outlined.Psychology,
                    title = "Model",
                    value = state.activeProvider.selectedModel.ifBlank { "Not set" },
                    onClick = { onEditProvider(state.activeProvider.id) }
                )
                SettingsRowDivider()
                SettingsValueRow(
                    icon = Icons.Outlined.Code,
                    title = "System prompt",
                    value = if (prefs.systemPrompt.isBlank()) "None" else prefs.systemPrompt.take(30) + "…",
                    onClick = { activeSheet = SheetType.SYSTEM_PROMPT }
                )
                SettingsRowDivider()
                SettingsSwitchRow(
                    icon = Icons.Outlined.Stream,
                    title = "Stream responses",
                    checked = prefs.defaultModelConfig.stream,
                    onChange = {
                        viewModel.updateModelConfig(prefs.defaultModelConfig.copy(stream = it))
                    }
                )
            }

            // ─── PROVIDERS ─────────────────────────────────────────────────
            SettingsSection(title = "Providers") {
                prefs.modelProviders.forEachIndexed { index, provider ->
                    SettingsRow(
                        icon = Icons.Outlined.Cloud,
                        iconTint = if (provider.id == prefs.activeProviderId)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        title = provider.name,
                        subtitle = provider.selectedModel.ifBlank { "No model" },
                        trailing = {
                            if (provider.id == prefs.activeProviderId) {
                                Icon(
                                    Icons.Outlined.Check,
                                    contentDescription = "Active",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        onClick = {
                            viewModel.updateActiveProvider(provider.id)
                            onEditProvider(provider.id)
                        }
                    )
                    if (index < prefs.modelProviders.lastIndex) SettingsRowDivider()
                }
                SettingsRowDivider()
                SettingsRow(
                    icon = null,
                    title = "Add provider…",
                    onClick = onAddProvider
                )
            }

            // ─── APPEARANCE ────────────────────────────────────────────────
            SettingsSection(title = "Appearance") {
                SettingsValueRow(
                    icon = Icons.Outlined.DarkMode,
                    title = "Theme",
                    value = prefs.theme.name.lowercase().replaceFirstChar(Char::uppercase),
                    onClick = { activeSheet = SheetType.THEME }
                )
                SettingsRowDivider()
                SettingsValueRow(
                    icon = Icons.Outlined.Palette,
                    title = "Accent color",
                    value = prefs.accent.name.lowercase().replaceFirstChar(Char::uppercase),
                    iconTint = accentColor(prefs.accent),
                    onClick = { activeSheet = SheetType.ACCENT }
                )
                SettingsRowDivider()
                SettingsSwitchRow(
                    icon = Icons.Outlined.Tune,
                    title = "Dynamic color",
                    subtitle = "Android 12+ system colors",
                    checked = prefs.useDynamicColor,
                    onChange = viewModel::updateDynamicColor
                )
                SettingsRowDivider()
                SettingsValueRow(
                    icon = Icons.Outlined.FormatSize,
                    title = "Font size",
                    value = prefs.fontSize.name.lowercase().replace('_', ' ').replaceFirstChar(Char::uppercase),
                    onClick = { activeSheet = SheetType.FONT_SIZE }
                )
                SettingsRowDivider()
                SettingsValueRow(
                    icon = Icons.Outlined.ViewCompact,
                    title = "Density",
                    value = prefs.density.name.lowercase().replaceFirstChar(Char::uppercase),
                    onClick = { activeSheet = SheetType.DENSITY }
                )
                SettingsRowDivider()
                SettingsValueRow(
                    icon = Icons.Outlined.Language,
                    title = "Language",
                    value = prefs.language.displayName,
                    onClick = { activeSheet = SheetType.LANGUAGE }
                )
            }

            // ─── INTEGRATIONS ──────────────────────────────────────────────
            SettingsSection(title = "Integrations") {
                SettingsValueRow(
                    icon = Icons.Outlined.Hub,
                    title = "MCP servers",
                    value = "${prefs.mcpServers.count { it.isEnabled }}/${prefs.mcpServers.size}",
                    onClick = onNavigateToMcp
                )
            }

            // ─── DANGER ZONE ───────────────────────────────────────────────
            SettingsSection(
                title = "Data",
                footer = "These actions cannot be undone."
            ) {
                SettingsRow(
                    icon = Icons.Outlined.Delete,
                    iconTint = MaterialTheme.colorScheme.error,
                    title = "Clear all conversations",
                    onClick = { showClearDialog = true }
                )
                SettingsRowDivider()
                SettingsRow(
                    icon = Icons.Outlined.RestartAlt,
                    iconTint = MaterialTheme.colorScheme.error,
                    title = "Reset to defaults",
                    onClick = { showResetDialog = true }
                )
            }

            // Footer
            Spacer(Modifier.height(16.dp))
            Text(
                "ChatCat 2.0 · Multimodal · MCP",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )
        }
    }

    // ─── BOTTOM SHEETS ─────────────────────────────────────────────────────
    activeSheet?.let { sheet ->
        ModalBottomSheet(
            onDismissRequest = { activeSheet = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            when (sheet) {
                SheetType.THEME -> PickerSheet(
                    title = "Theme",
                    options = Theme.values().toList(),
                    selected = prefs.theme,
                    labelOf = { it.name.lowercase().replaceFirstChar(Char::uppercase) },
                    onPick = { viewModel.updateTheme(it); activeSheet = null }
                )
                SheetType.ACCENT -> AccentPickerSheet(
                    selected = prefs.accent,
                    onPick = { viewModel.updateAccent(it); activeSheet = null }
                )
                SheetType.FONT_SIZE -> PickerSheet(
                    title = "Font size",
                    options = FontSize.values().toList(),
                    selected = prefs.fontSize,
                    labelOf = { it.name.lowercase().replace('_', ' ').replaceFirstChar(Char::uppercase) },
                    onPick = { viewModel.updateFontSize(it); activeSheet = null }
                )
                SheetType.DENSITY -> PickerSheet(
                    title = "Density",
                    options = Density.values().toList(),
                    selected = prefs.density,
                    labelOf = { it.name.lowercase().replaceFirstChar(Char::uppercase) },
                    onPick = { viewModel.updateDensity(it); activeSheet = null }
                )
                SheetType.LANGUAGE -> PickerSheet(
                    title = "Language",
                    options = Language.values().toList(),
                    selected = prefs.language,
                    labelOf = { it.displayName },
                    onPick = { viewModel.updateLanguage(it); activeSheet = null }
                )
                SheetType.SYSTEM_PROMPT -> SystemPromptSheet(
                    current = prefs.systemPrompt,
                    onSave = { viewModel.updateSystemPrompt(it); activeSheet = null }
                )
            }
        }
    }

    // ─── CONFIRMATION DIALOGS ──────────────────────────────────────────────
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear all conversations?") },
            text = { Text("This will permanently delete all chat history. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllConversations()
                    showClearDialog = false
                }) { Text("Clear", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Cancel") } }
        )
    }
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset all settings?") },
            text = { Text("All providers, appearance, and model settings will be reset to their defaults. Conversations will not be deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetPreferences()
                    showResetDialog = false
                }) { Text("Reset", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("Cancel") } }
        )
    }
}

// ─── BOTTOM SHEET CONTENT ──────────────────────────────────────────────────

@Composable
private fun <T> PickerSheet(
    title: String,
    options: List<T>,
    selected: T,
    labelOf: (T) -> String,
    onPick: (T) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 32.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        )
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPick(option) }
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = option == selected, onClick = { onPick(option) })
                Spacer(Modifier.width(12.dp))
                Text(labelOf(option), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun AccentPickerSheet(selected: Accent, onPick: (Accent) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 32.dp)) {
        Text(
            "Accent color",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        )
        Accent.values().forEach { accent ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPick(accent) }
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(accentColor(accent)),
                    contentAlignment = Alignment.Center
                ) {
                    if (accent == selected) {
                        Icon(
                            Icons.Outlined.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                Text(
                    accent.name.lowercase().replaceFirstChar(Char::uppercase),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}


@Composable
private fun SystemPromptSheet(current: String, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(current) }
    Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
        Text("System prompt", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Optional. Sent before every conversation.") },
            modifier = Modifier.fillMaxWidth().height(160.dp),
            minLines = 4,
            maxLines = 8
        )
        Spacer(Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { onSave(text) }) { Text("Save") }
        }
    }
}

private fun accentColor(accent: Accent): Color = when (accent) {
    Accent.INDIGO -> Color(0xFF5B6CFF)
    Accent.ROSE -> Color(0xFFE5446D)
    Accent.EMERALD -> Color(0xFF1F9E73)
    Accent.AMBER -> Color(0xFFE08914)
    Accent.OCEAN -> Color(0xFF0E7CB7)
}
