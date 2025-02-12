package com.gemini.chatbot.roomHelper

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "chat_messages")
data class NoteData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Auto-generated ID

    @ColumnInfo("user_name")
    var userName: String, // Name of the user or bot

    @ColumnInfo("message")
    var message: String, // Chat message content

    @ColumnInfo("timestamp")
    val timestamp: Long = System.currentTimeMillis() // Date and time
)