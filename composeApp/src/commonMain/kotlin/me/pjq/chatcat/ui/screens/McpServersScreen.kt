package me.pjq.chatcat.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.pjq.chatcat.model.McpServer
import me.pjq.chatcat.model.McpTransport
import me.pjq.chatcat.platform.randomUUID
import me.pjq.chatcat.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun McpServersScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var editing by remember { mutableStateOf<McpServer?>(null) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("MCP servers") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editing = McpServer(id = "mcp_${randomUUID().take(8)}", name = "New MCP server")
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        if (state.preferences.mcpServers.isEmpty()) {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "No MCP servers yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Connect to a Model Context Protocol server to give the model access to tools and resources.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.preferences.mcpServers, key = { it.id }) { server ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        server.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "${server.transport.name} · ${server.url.ifBlank { "(no URL)" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = server.isEnabled,
                                    onCheckedChange = { viewModel.saveMcpServer(server.copy(isEnabled = it)) }
                                )
                            }
                            if (server.description.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    server.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Row {
                                OutlinedButton(onClick = { viewModel.testMcpServer(server) }) {
                                    Text("Test connection")
                                }
                                Spacer(Modifier.weight(1f))
                                IconButton(onClick = { editing = server }) {
                                    Icon(Icons.Outlined.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { viewModel.deleteMcpServer(server.id) }) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
                state.mcpStatus?.let { status ->
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = status,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    editing?.let { server ->
        McpEditDialog(
            initial = server,
            onDismiss = { editing = null },
            onSave = {
                viewModel.saveMcpServer(it)
                editing = null
            }
        )
    }
}

@Composable
private fun McpEditDialog(
    initial: McpServer,
    onDismiss: () -> Unit,
    onSave: (McpServer) -> Unit
) {
    var name by remember { mutableStateOf(initial.name) }
    var url by remember { mutableStateOf(initial.url) }
    var description by remember { mutableStateOf(initial.description) }
    var headerKey by remember { mutableStateOf("") }
    var headerValue by remember { mutableStateOf("") }
    var headers by remember { mutableStateOf(initial.headers) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("MCP server") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(url, { url = it }, label = { Text("URL (HTTP JSON-RPC)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                Text("Custom headers", style = MaterialTheme.typography.labelLarge)
                headers.forEach { (k, _) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$k: ${headers[k]}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { headers = headers - k }) { Text("Remove") }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        headerKey, { headerKey = it },
                        label = { Text("Header") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.height(0.dp))
                    OutlinedTextField(
                        headerValue, { headerValue = it },
                        label = { Text("Value") },
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        if (headerKey.isNotBlank()) {
                            headers = headers + (headerKey to headerValue)
                            headerKey = ""
                            headerValue = ""
                        }
                    }) { Text("Add") }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    initial.copy(
                        name = name,
                        url = url,
                        description = description,
                        headers = headers,
                        transport = McpTransport.HTTP
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
