package me.pjq.chatcat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Snackbar
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
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.pjq.chatcat.model.Message
import me.pjq.chatcat.util.ClipboardUtil
import me.pjq.chatcat.ui.components.ChatInput
import me.pjq.chatcat.ui.components.ConversationListItem
import me.pjq.chatcat.ui.components.MessageBubble
import me.pjq.chatcat.viewmodel.ChatViewModel
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
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
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier.fillMaxHeight()
                ) {
                    // Drawer header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🐱",
                                style = MaterialTheme.typography.headlineLarge
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = languageManager.getString(StringResources.APP_NAME),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    Divider()
                    
                    // Conversation list
                    LazyColumn(
                        modifier = Modifier.weight(1f)
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
                    
                    Divider()
                    
                    // Settings button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { onNavigateToSettings() }
                        ) {
                            Text(
                                text = "⚙️",
                                style = MaterialTheme.typography.titleLarge
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Text(
                                text = languageManager.getString(StringResources.NAV_SETTINGS),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
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
                            Text(
                                text = "☰",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    },
                    // Add "New Chat" button to the right side of the title bar
                    actions = {
                        IconButton(
                            onClick = { 
                                viewModel.createNewConversation()
                            }
                        ) {
                            Text(
                                text = "➕",
                                style = MaterialTheme.typography.titleMedium
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
                
                // Auto-scroll has been removed to allow manual scrolling
                
                // Use a Box with a small fixed height to contain the LazyColumn
                // This prevents the keyboard from pushing the title bar off-screen
                Box(
                    modifier = Modifier
                        .weight(0.3f) // Use even smaller weight to prevent expanding too much
                        .fillMaxWidth()
                        .height(200.dp) // Use a smaller fixed height
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize() // Fill the entire Box with a single modifier
                    ) {
                        items(messages) { message ->
                            MessageBubble(
                                message = message,
                                onCopyMessage = { content ->
                                    // Use the ClipboardUtil to copy text to clipboard
                                    ClipboardUtil.copyToClipboard(content)
                                    showCopyConfirmation = true
                                    copyConfirmationMessage = languageManager.getString(StringResources.COPY) + " - " + 
                                                             languageManager.getString(StringResources.DONE)
                                },
                                onResendMessage = { msg ->
                                    viewModel.resendMessage(msg)
                                },
                                onDeleteMessage = { messageId ->
                                    viewModel.deleteMessage(messageId)
                                }
                            )
                        }
                        
                        // Show loading or streaming indicator if waiting for response
                        if (uiState.isLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (uiState.isStreaming) "✏️ Writing..." else "⏳ Thinking...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Error message if any
                uiState.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
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
