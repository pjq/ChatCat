package me.pjq.chatcat.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.skia.Image
import java.util.Base64

@Composable
actual fun PlatformImage(
    data: String,
    isBase64: Boolean,
    modifier: Modifier,
    contentScale: ContentScale,
    contentDescription: String?
) {
    val bitmap = remember(data, isBase64) {
        runCatching {
            if (isBase64) {
                val bytes = Base64.getDecoder().decode(data)
                Image.makeFromEncoded(bytes).toComposeImageBitmap()
            } else null
        }.getOrNull()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = modifier.fillMaxSize(),
            contentScale = contentScale
        )
    }
}
