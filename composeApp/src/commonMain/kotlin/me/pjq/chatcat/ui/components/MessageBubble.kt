package me.pjq.chatcat.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.pjq.chatcat.model.ContentPart
import me.pjq.chatcat.model.ImageSource
import me.pjq.chatcat.model.Message
import me.pjq.chatcat.model.Role

@Composable
fun MessageBubble(
    message: Message,
    isStreaming: Boolean,
    onCopy: (String) -> Unit,
    onResend: (Message) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == Role.USER
    val arrangement = if (isUser) Arrangement.End else Arrangement.Start

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = arrangement,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) AssistantAvatar()
        Column(
            modifier = Modifier.widthIn(max = 480.dp).padding(horizontal = 8.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            BubbleSurface(isUser = isUser, isError = message.isError) {
                Column {
                    val text = message.text.trim()
                    if (text.isNotEmpty()) {
                        if (isUser) {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            MarkdownText(
                                markdown = text,
                                style = MaterialTheme.typography.bodyLarge,
                                isError = message.isError,
                                isStreaming = isStreaming && message.role == Role.ASSISTANT
                            )
                        }
                    }
                    val images = message.images
                    if (images.isNotEmpty()) {
                        Spacer(Modifier.height(if (text.isNotEmpty()) 8.dp else 0.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (img in images) ImageAttachment(img)
                        }
                    }
                    if (isStreaming && message.role == Role.ASSISTANT && text.isNotEmpty()) {
                        StreamingCaret()
                    }
                }
            }
            if (message.modelName != null && !isUser) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = message.modelName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            ActionRow(
                isUser = isUser,
                onCopy = { onCopy(message.text) },
                onResend = if (isUser) ({ onResend(message) }) else null,
                onDelete = { onDelete(message.id) }
            )
        }
        if (isUser) UserAvatar()
    }
}

@Composable
private fun AssistantAvatar() {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text("🐱", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun UserAvatar() {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.tertiaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Y",
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun BubbleSurface(
    isUser: Boolean,
    isError: Boolean,
    content: @Composable () -> Unit
) {
    val (container, _) = when {
        isError -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        isUser -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    val shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomStart = if (isUser) 18.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 18.dp
    )
    Surface(
        color = container,
        shape = shape,
        tonalElevation = 0.dp
    ) {
        Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) { content() }
    }
}

@Composable
private fun ImageAttachment(image: ContentPart.Image) {
    val (data, isBase64) = when (val src = image.source) {
        is ImageSource.Url -> src.url to false
        is ImageSource.Base64 -> src.data to true
        is ImageSource.Local -> src.path to false
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.heightIn(max = 280.dp).widthIn(max = 360.dp)
    ) {
        PlatformImage(data = data, isBase64 = isBase64, contentDescription = image.caption)
    }
}

@Composable
private fun StreamingCaret() {
    val alpha by animateFloatAsState(
        targetValue = 0.3f,
        animationSpec = tween(durationMillis = 600),
        label = "caret"
    )
    Spacer(Modifier.height(2.dp))
    Box(
        modifier = Modifier
            .size(width = 8.dp, height = 14.dp)
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                shape = RoundedCornerShape(2.dp)
            )
    )
}

@Composable
private fun ActionRow(
    isUser: Boolean,
    onCopy: () -> Unit,
    onResend: (() -> Unit)?,
    onDelete: () -> Unit
) {
    Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        SmallIconButton(Icons.Outlined.ContentCopy, "Copy", onCopy)
        if (onResend != null) SmallIconButton(Icons.Outlined.Refresh, "Resend", onResend)
        SmallIconButton(Icons.Outlined.DeleteOutline, "Delete", onDelete)
    }
}

@Composable
private fun SmallIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(28.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

