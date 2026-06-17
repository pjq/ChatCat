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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.pjq.chatcat.di.AppModule
import me.pjq.chatcat.model.ContentPart
import me.pjq.chatcat.platform.rememberImagePicker
import me.pjq.chatcat.ui.components.ChatInput
import me.pjq.chatcat.ui.components.ConversationListItem
import me.pjq.chatcat.ui.components.MessageBubble
import me.pjq.chatcat.util.ClipboardUtil
import me.pjq.chatcat.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToMcp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val pendingImages = remember { mutableStateListOf<ContentPart.Image>() }

    val picker = rememberImagePicker { image ->
        pendingImages.add(image)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatDrawer(
                conversations = uiState.conversations,
                currentId = uiState.currentConversation?.id,
                onSelect = {
                    viewModel.selectConversation(it)
                    scope.launch { drawerState.close() }
                },
                onDelete = viewModel::deleteConversation,
                onNew = {
                    viewModel.createNewConversation()
                    scope.launch { drawerState.close() }
                },
                onSettings = {
                    scope.launch { drawerState.close() }
                    onNavigateToSettings()
                },
                onMcp = {
                    scope.launch { drawerState.close() }
                    onNavigateToMcp()
                }
            )
        },
        modifier = modifier
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = uiState.currentConversation?.title ?: "ChatCat",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (uiState.mcpEnabled) {
                                Text(
                                    "🛠 MCP active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Outlined.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        ModelPickerChip(
                            availableModels = uiState.availableModels,
                            selected = uiState.selectedModel,
                            onSelect = viewModel::selectModel
                        )
                        IconButton(onClick = viewModel::createNewConversation) {
                            Icon(Icons.Filled.Add, contentDescription = "New chat")
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                val messages = uiState.currentConversation?.messages ?: emptyList()
                val listState = rememberLazyListState()

                LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length) {
                    if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
                }

                if (messages.isEmpty()) {
                    EmptyState(modifier = Modifier.weight(1f))
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            MessageBubble(
                                message = msg,
                                isStreaming = uiState.isStreaming && msg == messages.last(),
                                onCopy = { ClipboardUtil.copyToClipboard(it) },
                                onResend = viewModel::resendMessage,
                                onDelete = viewModel::deleteMessage
                            )
                        }
                    }
                }

                AnimatedVisibility(visible = uiState.error != null) {
                    val message = uiState.error ?: ""
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                ChatInput(
                    onSend = { text, attachments ->
                        viewModel.sendMessage(text, attachments)
                        pendingImages.clear()
                    },
                    onPickImage = if (uiState.canSendImages && picker.isAvailable) ({ picker.launch() }) else null,
                    onGenerateImage = if (uiState.canGenerateImages) viewModel::generateImage else null,
                    onCancel = viewModel::cancel,
                    pendingImages = pendingImages.toList(),
                    onRemoveImage = { idx -> pendingImages.removeAt(idx) },
                    isLoading = uiState.isLoading,
                    canSendImages = uiState.canSendImages,
                    canGenerateImages = uiState.canGenerateImages
                )
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text("🐱", style = MaterialTheme.typography.displaySmall)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "How can I help today?",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Send a message, attach an image, or describe one to generate.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
private fun ModelPickerChip(
    availableModels: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val display = if (selected.isBlank()) "Pick model" else selected
    Box {
        AssistChip(
            onClick = { expanded = true },
            label = { Text(display.take(18) + if (display.length > 18) "…" else "") },
            leadingIcon = {
                Icon(Icons.Outlined.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            trailingIcon = {
                Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                trailingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (availableModels.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No models — set up provider in Settings") },
                    onClick = { expanded = false }
                )
            } else {
                availableModels.forEach { model ->
                    DropdownMenuItem(
                        text = { Text(model) },
                        onClick = {
                            onSelect(model)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatDrawer(
    conversations: List<me.pjq.chatcat.model.Conversation>,
    currentId: String?,
    onSelect: (String) -> Unit,
    onDelete: (String) -> Unit,
    onNew: () -> Unit,
    onSettings: () -> Unit,
    onMcp: () -> Unit
) {
    ModalDrawerSheet(modifier = Modifier.width(320.dp)) {
        Column(modifier = Modifier.fillMaxHeight()) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "ChatCat",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Multimodal AI chat",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            DrawerActionRow(
                icon = Icons.Filled.Add,
                label = "New conversation",
                onClick = onNew
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                text = "Conversations",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
            )
            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 4.dp)) {
                items(conversations, key = { it.id }) { conversation ->
                    ConversationListItem(
                        conversation = conversation,
                        isSelected = conversation.id == currentId,
                        onClick = { onSelect(conversation.id) },
                        onDelete = { onDelete(conversation.id) }
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            DrawerActionRow(icon = Icons.Outlined.Hub, label = "MCP servers", onClick = onMcp)
            DrawerActionRow(icon = Icons.Outlined.Settings, label = "Settings", onClick = onSettings)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun DrawerActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
