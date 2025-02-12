package com.gemini.chatbot.roomHelper

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.threedevtech.pdf.roomHelper.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    val allChats: LiveData<List<NoteData>> = repository.allChats.asLiveData()

    fun insertChatMessage(chatMessage: NoteData) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertChatMessage(chatMessage)
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllChats()
        }
    }
}

class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
