package me.pjq.chatcat.service

import me.pjq.chatcat.model.ContentPart

interface ImageGenerationService {
    suspend fun generate(
        prompt: String,
        model: String,
        size: String = "1024x1024",
        n: Int = 1
    ): Result<List<ContentPart.Image>>
}
