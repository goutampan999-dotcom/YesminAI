package com.yesmin.ai.ai

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService(private val apiKey: String) {

    private val systemInstruction = """
        You are YESMIN AI, a modern, futuristic personal AI companion with a friendly female personality.
        You communicate naturally in Hindi, Hinglish, and English (default: Hindi/Hinglish).
        Your personality is caring, intelligent, playful, respectful, and helpful.
        When asked for device actions (like opening apps, playing music), keep your answers concise.
        For coding questions, provide accurate and well-explained code snippets.
    """.trimIndent()

    private val generativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey,
            systemInstruction = com.google.ai.client.generativeai.type.content {
                text(systemInstruction)
            }
        )
    }

    suspend fun generateResponse(prompt: String): String = withContext(Dispatchers.IO) {
        try {
            val response = generativeModel.generateContent(prompt)
            response.text ?: "Main samajh nahi paayi, kripya dobara bolein."
        } catch (e: Exception) {
            "Error: ${e.localizedMessage ?: "Connection error"}"
        }
    }
}
