package me.pjq.chatcat.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import me.pjq.chatcat.AndroidContextProvider

/**
 * Android implementation of copyToClipboard.
 * Uses the Android ClipboardManager to copy text to the clipboard.
 */
actual fun copyToClipboard(text: String) {
    val context = AndroidContextProvider.getApplicationContext()
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("ChatCat Message", text)
    clipboard.setPrimaryClip(clip)
}
