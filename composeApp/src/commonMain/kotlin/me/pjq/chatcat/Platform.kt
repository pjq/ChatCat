package me.pjq.chatcat

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform