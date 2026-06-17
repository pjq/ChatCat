package me.pjq.chatcat.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale

@Composable
actual fun PlatformImage(
    data: String,
    isBase64: Boolean,
    modifier: Modifier,
    contentScale: ContentScale,
    contentDescription: String?
) {
    Box(modifier = modifier)
}
