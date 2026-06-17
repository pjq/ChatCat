package me.pjq.chatcat.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.pjq.chatcat.model.ContentPart

@Composable
fun ChatInput(
    onSend: (String, List<ContentPart.Image>) -> Unit,
    onPickImage: (() -> Unit)?,
    onGenerateImage: ((String) -> Unit)?,
    onCancel: () -> Unit,
    pendingImages: List<ContentPart.Image>,
    onRemoveImage: (Int) -> Unit,
    isLoading: Boolean,
    canSendImages: Boolean,
    canGenerateImages: Boolean,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    var generateMode by remember { mutableStateOf(false) }

    val canSubmit = (text.isNotBlank() || pendingImages.isNotEmpty()) && !isLoading

    Surface(
        tonalElevation = 2.dp,
        shadowElevation = 0.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            if (pendingImages.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
                ) {
                    items(pendingImages.withIndex().toList()) { (index, image) ->
                        Box {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(64.dp)
                            ) {
                                val src = when (val s = image.source) {
                                    is me.pjq.chatcat.model.ImageSource.Url -> s.url to false
                                    is me.pjq.chatcat.model.ImageSource.Base64 -> s.data to true
                                    is me.pjq.chatcat.model.ImageSource.Local -> s.path to false
                                }
                                PlatformImage(data = src.first, isBase64 = src.second)
                            }
                            FilledIconButton(
                                onClick = { onRemoveImage(index) },
                                modifier = Modifier.size(20.dp).align(Alignment.TopEnd),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Icon(
                                    Icons.Outlined.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.size(8.dp))
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        if (onPickImage != null) {
                            IconButton(onClick = onPickImage, modifier = Modifier.size(40.dp)) {
                                Icon(
                                    Icons.Outlined.AddPhotoAlternate,
                                    contentDescription = "Attach image",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (canGenerateImages && onGenerateImage != null) {
                            IconButton(
                                onClick = { generateMode = !generateMode },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.AutoAwesome,
                                    contentDescription = "Generate image",
                                    tint = if (generateMode) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 40.dp, max = 160.dp)
                                .padding(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            BasicTextField(
                                value = text,
                                onValueChange = { text = it },
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = LocalContentColor.current
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Default
                                ),
                                keyboardActions = KeyboardActions.Default,
                                maxLines = 6
                            )
                            if (text.isEmpty()) {
                                Text(
                                    text = if (generateMode) "Describe an image to generate…"
                                    else "Message ChatCat…",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                if (isLoading) {
                    FilledIconButton(
                        onClick = onCancel,
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Icon(Icons.Outlined.Stop, contentDescription = "Stop")
                    }
                } else {
                    FilledIconButton(
                        onClick = {
                            if (!canSubmit) return@FilledIconButton
                            if (generateMode && onGenerateImage != null) {
                                onGenerateImage(text)
                            } else {
                                onSend(text, pendingImages)
                            }
                            text = ""
                        },
                        enabled = canSubmit,
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}
