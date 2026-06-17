package me.pjq.chatcat.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
actual fun PlatformImage(
    data: String,
    isBase64: Boolean,
    modifier: Modifier,
    contentScale: ContentScale,
    contentDescription: String?
) {
    if (isBase64) {
        val bitmap = remember(data) {
            runCatching {
                val bytes = Base64.decode(data, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
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
    } else {
        AsyncImage(
            model = data,
            contentDescription = contentDescription,
            modifier = modifier.fillMaxSize(),
            contentScale = contentScale
        )
    }
}
