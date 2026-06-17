package me.pjq.chatcat.platform

import platform.Foundation.NSUUID

actual fun randomUUID(): String = NSUUID().UUIDString()
