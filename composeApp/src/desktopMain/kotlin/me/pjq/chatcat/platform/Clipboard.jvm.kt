package me.pjq.chatcat.platform

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

/**
 * Desktop (JVM) implementation of copyToClipboard.
 * Uses AWT Toolkit to copy text to the clipboard.
 */
actual fun copyToClipboard(text: String) {
    val selection = StringSelection(text)
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    clipboard.setContents(selection, selection)
}
