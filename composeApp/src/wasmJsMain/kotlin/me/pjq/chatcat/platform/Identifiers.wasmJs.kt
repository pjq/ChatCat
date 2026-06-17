package me.pjq.chatcat.platform

import kotlin.random.Random

actual fun randomUUID(): String {
    val bytes = ByteArray(16) { Random.nextInt(0, 256).toByte() }
    bytes[6] = ((bytes[6].toInt() and 0x0f) or 0x40).toByte()
    bytes[8] = ((bytes[8].toInt() and 0x3f) or 0x80).toByte()
    val hex = bytes.joinToString("") { ((it.toInt() and 0xff) + 0x100).toString(16).substring(1) }
    return "${hex.substring(0, 8)}-${hex.substring(8, 12)}-${hex.substring(12, 16)}-${hex.substring(16, 20)}-${hex.substring(20)}"
}
