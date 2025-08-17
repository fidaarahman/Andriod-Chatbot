package com.gemini.chatbot.roomHelper

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDAO: NoteDAO) {

    val allChats: Flow<List<NoteData>> = noteDAO.getAllChats()

    suspend fun insertChatMessage(chatMessage: NoteData) {
        noteDAO.insertChatMessage(chatMessage)
    }

    suspend fun deleteAllChats() {
        noteDAO.deleteAllChats()
    }
}