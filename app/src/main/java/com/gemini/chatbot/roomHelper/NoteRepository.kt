package com.threedevtech.pdf.roomHelper

import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.database.getStringOrNull
import androidx.lifecycle.MutableLiveData
import com.gemini.chatbot.roomHelper.NoteDAO
import com.gemini.chatbot.roomHelper.NoteData
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