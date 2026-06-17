package me.pjq.chatcat.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.pjq.chatcat.model.ContentPart
import me.pjq.chatcat.model.ImageSource
import java.io.ByteArrayOutputStream

@Composable
actual fun rememberImagePicker(onPicked: (ContentPart.Image) -> Unit): ImagePickerHandle {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                val resolver = context.contentResolver
                val mime = resolver.getType(uri) ?: "image/jpeg"
                val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return@withContext null

                // Resize large images to max 1024px on longest side for reasonable payload size
                val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return@withContext null
                val maxDim = 1024
                val scaled = if (original.width > maxDim || original.height > maxDim) {
                    val scale = maxDim.toFloat() / maxOf(original.width, original.height)
                    val w = (original.width * scale).toInt()
                    val h = (original.height * scale).toInt()
                    Bitmap.createScaledBitmap(original, w, h, true).also {
                        if (it !== original) original.recycle()
                    }
                } else original

                val outputStream = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                scaled.recycle()
                val compressedBytes = outputStream.toByteArray()
                val b64 = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)
                ContentPart.Image(source = ImageSource.Base64(b64), mimeType = "image/jpeg")
            }
            if (result != null) onPicked(result)
        }
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
