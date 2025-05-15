package me.pjq.chatcat.platform

import platform.UIKit.UIPasteboard

/**
 * iOS implementation of copyToClipboard.
 * Uses UIPasteboard to copy text to the clipboard.
 */
actual fun copyToClipboard(text: String) {
    UIPasteboard.generalPasteboard.string = text
}
