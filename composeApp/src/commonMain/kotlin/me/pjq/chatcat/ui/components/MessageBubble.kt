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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    modifier: Modifier = Modifier
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
                    if (message.isError) {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // Display attachments if any
                message.attachments.forEach { attachment ->
                    AttachmentItem(attachment = attachment)
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
