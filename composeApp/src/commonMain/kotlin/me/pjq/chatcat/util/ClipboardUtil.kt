package me.pjq.chatcat.util

import me.pjq.chatcat.platform.copyToClipboard as platformCopyToClipboard

/**
 * Utility class for clipboard operations
 */
object ClipboardUtil {
    /**
     * Copies the given text to the clipboard
     * This is a wrapper around the platform-specific implementation
     * to avoid ambiguity errors
     */
    fun copyToClipboard(text: String) {
        platformCopyToClipboard(text)
    }
}
