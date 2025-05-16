package me.pjq.chatcat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.pjq.chatcat.di.AppModule
import me.pjq.chatcat.model.Message
import me.pjq.chatcat.model.Role
import me.pjq.chatcat.ui.theme.ChatCatColors
import me.pjq.chatcat.ui.theme.getMessageBackgroundColor
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MessageBubble(
    message: Message,
    modifier: Modifier = Modifier,
    onCopyMessage: ((String) -> Unit)? = null,
    onResendMessage: ((Message) -> Unit)? = null,
    onDeleteMessage: ((String) -> Unit)? = null
) {
    val isUserMessage = message.role == Role.USER
    val alignment = if (isUserMessage) Alignment.End else Alignment.Start
    val backgroundColor = getMessageBackgroundColor(isUserMessage)
    val bubbleShape = RoundedCornerShape(
        topStart = if (isUserMessage) 16.dp else 0.dp,
        topEnd = if (isUserMessage) 0.dp else 16.dp,
        bottomStart = 16.dp,
        bottomEnd = 16.dp
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            if (!isUserMessage) {
                // Assistant avatar
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ChatCatColors.catPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    // Replace with actual cat icon when available
                    Text(
                        text = "🐱",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Column {
                Box(
                    modifier = Modifier
                        .clip(bubbleShape)
                        .background(backgroundColor)
                        .padding(12.dp)
                ) {
                    // Use MarkdownText to render message content
                    MarkdownText(
                        markdown = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        isError = message.isError
                    )
                }
                
                // Display attachments if any
                message.attachments.forEach { attachment ->
                    AttachmentItem(attachment = attachment)
                }
                
                // Action buttons (copy, resend, delete)
                if (onCopyMessage != null || onResendMessage != null || onDeleteMessage != null) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        // Only show copy button if callback is provided
                        onCopyMessage?.let {
                            IconButton(
                                onClick = { onCopyMessage(message.content) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text(
                                    text = "📋", // Copy icon
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        
                        // Only show resend button for user messages if callback is provided
                        if (message.role == Role.USER && onResendMessage != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { onResendMessage(message) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text(
                                    text = "↩️", // Resend icon
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        
                        // Delete message button
                        if (onDeleteMessage != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { onDeleteMessage(message.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text(
                                    text = "🗑️", // Delete icon
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
            
            if (isUserMessage) {
                Spacer(modifier = Modifier.width(8.dp))
                // User avatar
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "👤",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
