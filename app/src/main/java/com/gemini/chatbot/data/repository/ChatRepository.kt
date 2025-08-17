package com.gemini.chatbot.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.gemini.chatbot.R
import com.gemini.chatbot.roomHelper.NoteData
import com.gemini.chatbot.roomHelper.NoteRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

class ChatRepository(appContext: Context, private val noteRepository: NoteRepository) {

    private val generativeModel: GenerativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = appContext.getString(R.string.api_key)
    )

    suspend fun generateResponse(prompt: String, bitmap: Bitmap?): String {
        return if (bitmap != null) {
            val inputContent = content {
                image(bitmap)
                text(prompt)
            }
            val response = generativeModel.generateContent(inputContent)
            response.text ?: ""
        } else {
            val response = generativeModel.generateContent(prompt)
            response.text ?: ""
        }
    }

    suspend fun insertUserMessage(message: String) {
        noteRepository.insertChatMessage(NoteData(userName = "You", message = message))
    }

    suspend fun insertBotMessage(message: String) {
        noteRepository.insertChatMessage(NoteData(userName = "Bot", message = message))
    }

    suspend fun clearMessages() {
        noteRepository.deleteAllChats()
    }
}