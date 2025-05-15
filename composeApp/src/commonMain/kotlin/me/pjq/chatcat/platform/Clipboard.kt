package me.pjq.chatcat.platform

/**
 * Copies the given text to the system clipboard.
 * Platform-specific implementations are provided.
 *
 * @param text The text to copy to the clipboard
 */
expect fun copyToClipboard(text: String)
