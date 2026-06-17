package me.pjq.chatcat.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import me.pjq.chatcat.model.ContentPart

@Composable
actual fun rememberImagePicker(onPicked: (ContentPart.Image) -> Unit): ImagePickerHandle {
    return remember {
        object : ImagePickerHandle {
            override val isAvailable: Boolean = false
            override fun launch() = Unit
        }
    }
}
