package com.gemini.chatbot.roomHelper

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gemini.chatbot.roomHelper.NoteData
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDAO {

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChats(): Flow<List<NoteData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(chatMessage: NoteData)

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllChats()
}