package me.pjq.chatcat.platform

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLTextAreaElement

/**
 * Web (Wasm) implementation of copyToClipboard.
 * Uses the Clipboard API if available, falls back to document.execCommand for older browsers.
 */
actual fun copyToClipboard(text: String) {
    // Try to use the modern Clipboard API
    try {
        js("navigator.clipboard.writeText(text)")
        return
    } catch (e: Throwable) {
        // Fall back to the older execCommand method
        val textArea = document.createElement("textarea") as HTMLTextAreaElement
        textArea.value = text
        
        // Make the textarea out of viewport
        textArea.style.position = "fixed"
        textArea.style.left = "-999999px"
        textArea.style.top = "-999999px"
        
        document.body?.appendChild(textArea)
        textArea.focus()
        textArea.select()
        
        try {
            val successful = js("document.execCommand('copy')")
            if (successful as Boolean) {
                console.log("Text copied to clipboard")
            } else {
                console.error("Failed to copy text")
            }
        } catch (e: Throwable) {
            console.error("Failed to copy text: ${e.message}")
        }
        
        document.body?.removeChild(textArea)
    }
}
