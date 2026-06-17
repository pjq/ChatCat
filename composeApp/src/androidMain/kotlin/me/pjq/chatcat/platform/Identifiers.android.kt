package me.pjq.chatcat.platform

import java.util.UUID

actual fun randomUUID(): String = UUID.randomUUID().toString()
