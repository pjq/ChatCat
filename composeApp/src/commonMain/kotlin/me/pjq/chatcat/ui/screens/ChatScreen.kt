package me.pjq.chatcat.ui.screens

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import me.pjq.chatcat.di.AppModule
import me.pjq.chatcat.i18n.StringResources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.pjq.chatcat.model.Message
import me.pjq.chatcat.util.ClipboardUtil
import me.pjq.chatcat.ui.components.ChatInput
import me.pjq.chatcat.ui.components.ConversationListItem
import me.pjq.chatcat.ui.components.MessageBubble
import me.pjq.chatcat.viewmodel.ChatViewModel
import me.pjq.chatcat.viewmodel.SettingsViewModel
import moe.tlaster.precompose.navigation.BackHandler
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settingsUiState by settingsViewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // State for copy confirmation toast
    var showCopyConfirmation by remember { mutableStateOf(false) }
    var copyConfirmationMessage by remember { mutableStateOf("") }
    
    // Auto-hide toast after delay
    if (showCopyConfirmation) {
        LaunchedEffect(showCopyConfirmation) {
            kotlinx.coroutines.delay(2000)
            showCopyConfirmation = false
        }
    }
    
    // Get the language manager
    val languageManager = AppModule.languageManager
    
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp) // Set a maximum width for the drawer
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight()
                ) {
                    // Elegant drawer header
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp, horizontal = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // App logo
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = languageManager.getString(StringResources.CAT_EMOJI),
                                        style = MaterialTheme.typography.headlineMedium,
                                        modifier = Modifier.padding(8.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    text = languageManager.getString(StringResources.APP_NAME),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                    
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Conversation list header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = languageManager.getString(StringResources.RECENT_CONVERSATIONS),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Text(
                            text = "${uiState.conversations.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    
                    // Conversation list
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(uiState.conversations) { conversation ->
                            ConversationListItem(
                                conversation = conversation,
                                isSelected = uiState.currentConversation?.id == conversation.id,
                                onClick = {
                                    viewModel.selectConversation(conversation.id)
                                    scope.launch {
                                        drawerState.close()
                                    }
                                },
                                onDelete = {
                                    viewModel.deleteConversation(conversation.id)
                                }
                            )
                        }
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = 0.dp))
                    
                    // Settings button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(horizontal = 0.dp, vertical = 0.dp)
                            .clickable { onNavigateToSettings() }
                    ) {
                    Spacer(modifier = Modifier.height(14.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = languageManager.getString(StringResources.SETTINGS_TITLE),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = languageManager.getString(StringResources.NAV_SETTINGS),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        },
        modifier = modifier
    ) {
        // Use a different Scaffold configuration to ensure the title bar stays visible
        Scaffold(
            topBar = {
                // Make the TopAppBar fixed and not scrollable
                TopAppBar(
                    title = { 
                        Text(uiState.currentConversation?.title ?: languageManager.getString(StringResources.NAV_CHAT)) 
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = languageManager.getString(StringResources.OPEN_MENU),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    // Add "New Chat" button to the right side of the title bar
                    actions = {
                        // Model Selection Dropdown
                        ModelSelectionDropdown(
                            availableModels = settingsUiState.availableModels,
                            selectedModel = settingsUiState.activeProvider.selectedModel,
                            onModelSelected = { model ->
                                viewModel.selectModel(model)
                            }
                        )

                        IconButton(
                            onClick = {
                                viewModel.createNewConversation()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = languageManager.getString(StringResources.NEW_CHAT),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            },
            // Add a snackbar host for copy confirmation
            snackbarHost = {
                if (showCopyConfirmation) {
                    Snackbar {
                        Text(copyConfirmationMessage)
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Messages list
                val messages = uiState.currentConversation?.messages ?: emptyList()
                val listState = rememberLazyListState()
                
                // Chat messages area - give it more space and make it more elegant
                Box(
                    modifier = Modifier
                        .weight(1f) // Use more space for the chat area
                        .fillMaxWidth()
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp) // Add padding at top and bottom
                    ) {
                        items(messages) { message ->
                            MessageBubble(
                                message = message,
                                onCopyMessage = { content ->
                                    // Use the ClipboardUtil to copy text to clipboard
                                    ClipboardUtil.copyToClipboard(content)
                                    showCopyConfirmation = true
                                    copyConfirmationMessage = languageManager.getString(StringResources.COPY_CONFIRMATION)
                                },
                                onResendMessage = { msg ->
                                    viewModel.resendMessage(msg)
                                },
                                onDeleteMessage = { messageId ->
                                    viewModel.deleteMessage(messageId)
                                },
                                isStreaming = uiState.isStreaming
                            )
                        }
                        
                        // Show loading or streaming indicator if waiting for response
                        if (uiState.isLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        // Add a subtle loading animation
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        Text(
                                            text = if (uiState.isStreaming) 
                                                languageManager.getString(StringResources.WRITING) 
                                            else 
                                                languageManager.getString(StringResources.THINKING),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Error message if any - make it more elegant
                uiState.error?.let { error ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = languageManager.getString(StringResources.ERROR_DESCRIPTION),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Input area
                ChatInput(
                    onSendMessage = { message ->
                        viewModel.sendMessage(message)
                    },
                    onAttachFile = { /* TODO: Implement file attachment */ },
                    isLoading = uiState.isLoading
                )
            }
        }
    }
}

@Composable
fun ModelSelectionDropdown(
    availableModels: List<String>,
    selectedModel: String,
    onModelSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(selectedModel.take(12) + if (selectedModel.length > 12) "..." else "") // Truncate if too long
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Model"
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableModels.forEach { model ->
                DropdownMenuItem(
                    text = { Text(model) },
                    onClick = {
                        onModelSelected(model)
                        expanded = false
                    }
                )
            }
        }
    }
}
