package com.gemini.chatbot.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gemini.chatbot.data.repository.ChatRepository
import com.gemini.chatbot.model.DataResponse
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    private val _messages = MutableLiveData<List<DataResponse>>(emptyList())
    val messages: LiveData<List<DataResponse>> = _messages

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun sendPrompt(prompt: String, bitmap: Bitmap? = null, imageUri: String = "") {
        val updated = _messages.value.orEmpty().toMutableList()
        updated.add(DataResponse(isUser = 0, prompt = prompt, imageUri = imageUri))
        _messages.value = updated

        _loading.value = true
        viewModelScope.launch {
            try {
                val response = repository.generateResponse(prompt, bitmap)
                val withResponse = _messages.value.orEmpty().toMutableList()
                withResponse.add(DataResponse(isUser = 1, prompt = response, imageUri = ""))
                _messages.postValue(withResponse)

                repository.insertUserMessage(prompt)
                repository.insertBotMessage(response)
            } catch (t: Throwable) {
                _error.postValue(t.localizedMessage ?: "Unknown error")
            } finally {
                _loading.postValue(false)
            }
        }
    }
}

class ChatViewModelFactory(private val repository: ChatRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}