package me.pjq.chatcat.platform

import androidx.compose.runtime.Composable
import me.pjq.chatcat.model.ContentPart

@Composable
expect fun rememberImagePicker(onPicked: (ContentPart.Image) -> Unit): ImagePickerHandle

interface ImagePickerHandle {
    fun launch()
    val isAvailable: Boolean
}
