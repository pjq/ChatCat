package me.pjq.chatcat.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.util.Base64
import me.pjq.chatcat.model.ContentPart
import me.pjq.chatcat.model.ImageSource

@Composable
actual fun rememberImagePicker(onPicked: (ContentPart.Image) -> Unit): ImagePickerHandle {
    return remember {
        object : ImagePickerHandle {
            override val isAvailable: Boolean = true
            override fun launch() {
                val dialog = FileDialog(null as Frame?, "Select image", FileDialog.LOAD)
                dialog.file = "*.png;*.jpg;*.jpeg;*.webp"
                dialog.isVisible = true
                val file = dialog.file ?: return
                val full = File(dialog.directory, file)
                if (!full.exists()) return
                val bytes = full.readBytes()
                val mime = when (full.extension.lowercase()) {
                    "png" -> "image/png"
                    "webp" -> "image/webp"
                    else -> "image/jpeg"
                }
                onPicked(
                    ContentPart.Image(
                        source = ImageSource.Base64(Base64.getEncoder().encodeToString(bytes)),
                        mimeType = mime
                    )
                )
            }
        }
    }
}
