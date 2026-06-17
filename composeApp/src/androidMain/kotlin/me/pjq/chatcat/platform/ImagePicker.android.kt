package me.pjq.chatcat.platform

import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import me.pjq.chatcat.model.ContentPart
import me.pjq.chatcat.model.ImageSource

@Composable
actual fun rememberImagePicker(onPicked: (ContentPart.Image) -> Unit): ImagePickerHandle {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val resolver = context.contentResolver
        val mime = resolver.getType(uri) ?: "image/jpeg"
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return@rememberLauncherForActivityResult
        val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        onPicked(
            ContentPart.Image(
                source = ImageSource.Base64(b64),
                mimeType = mime
            )
        )
    }
    return remember {
        object : ImagePickerHandle {
            override val isAvailable: Boolean = true
            override fun launch() {
                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }
}
