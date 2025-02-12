package com.gemini.chatbot.roomHelper

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [
        NoteData::class
    ],
    version = 2
)
abstract class AppDB : RoomDatabase() {

    abstract fun noteDao(): NoteDAO

    companion object {
        @Volatile
        var INSTANCE: AppDB? = null

        fun getDatabase(context: Context, applicationScope: CoroutineScope): AppDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDB::class.java,
                    "bot_db.db"
                )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    //.addCallback(PreInsertData(context, applicationScope))
                    .build()
                INSTANCE = instance

                instance
            }

        }

    }

    class PreInsertData(private val context: Context, private val scope: CoroutineScope):Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
//                    populateNote(database.noteDao())

                }
            }
        }


//        private suspend fun populateNote(noteDAO: NoteDAO) {
//            val tempList:ArrayList<NoteData> = ArrayList()
//            tempList.add(NoteData(0, "Ehsan", "Desc 1"))
//            tempList.add(NoteData(0, "Ehsan 1", "Desc 1"))
//            tempList.add(NoteData(0, "Ehsan 2", "Desc 2"))
//            noteDAO.insertDua(tempList)
//        }

    }



}

