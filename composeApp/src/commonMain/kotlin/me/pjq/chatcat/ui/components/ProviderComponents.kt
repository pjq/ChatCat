package me.pjq.chatcat.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.pjq.chatcat.model.ModelProvider
import me.pjq.chatcat.model.ProviderType

/**
 * Returns a user-friendly description of the provider type
 */
@Composable
fun getProviderTypeDescription(providerType: ProviderType): String {
    return when (providerType) {
        ProviderType.OPENAI -> "Official OpenAI API"
        ProviderType.OPENAI_COMPATIBLE -> "OpenAI-Compatible API"
        ProviderType.CUSTOM -> "Custom Provider"
    }
}

/**
 * Dialog for editing a model provider
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderEditDialog(
    provider: ModelProvider,
    onSave: (ModelProvider) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember(provider.name) { mutableStateOf(provider.name) }
    var baseUrl by remember(provider.baseUrl) { mutableStateOf(provider.baseUrl) }
    var apiKey by remember(provider.apiKey) { mutableStateOf(provider.apiKey) }
    var providerType by remember(provider.providerType) { mutableStateOf(provider.providerType) }
    var isEnabled by remember(provider.isEnabled) { mutableStateOf(provider.isEnabled) }
    
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(if (provider.id.startsWith("provider_")) "Add Provider" else "Edit Provider") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Provider Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text("API Base URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Provider Type",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Column(Modifier.selectableGroup()) {
                    ProviderTypeOption(
                        title = "OpenAI",
                        description = "Official OpenAI API",
                        selected = providerType == ProviderType.OPENAI,
                        onClick = { providerType = ProviderType.OPENAI }
                    )
                    
                    ProviderTypeOption(
                        title = "OpenAI-Compatible",
                        description = "Third-party API compatible with OpenAI",
                        selected = providerType == ProviderType.OPENAI_COMPATIBLE,
                        onClick = { providerType = ProviderType.OPENAI_COMPATIBLE }
                    )
                    
                    ProviderTypeOption(
                        title = "Custom",
                        description = "Custom provider implementation",
                        selected = providerType == ProviderType.CUSTOM,
                        onClick = { providerType = ProviderType.CUSTOM }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                SwitchSetting(
                    title = "Enabled",
                    checked = isEnabled,
                    onCheckedChange = { isEnabled = it }
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = {
                    val updatedProvider = provider.copy(
                        name = name,
                        baseUrl = baseUrl,
                        apiKey = apiKey,
                        providerType = providerType,
                        isEnabled = isEnabled
                    )
                    onSave(updatedProvider)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = onCancel
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Option for selecting a provider type
 */
@Composable
fun ProviderTypeOption(
    title: String,
    description: String,
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
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
