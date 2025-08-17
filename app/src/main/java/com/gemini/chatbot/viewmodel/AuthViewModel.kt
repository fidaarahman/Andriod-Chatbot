package com.gemini.chatbot.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gemini.chatbot.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser?) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    private val _passwordResetState = MutableLiveData<AuthState>(AuthState.Idle)
    val passwordResetState: LiveData<AuthState> = _passwordResetState

    fun signInWithEmailAndPassword(email: String, password: String) {
        _authState.value = AuthState.Loading
        repository.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success(repository.getCurrentUser())
                } else {
                    _authState.value = AuthState.Error(task.exception?.localizedMessage ?: "Login failed")
                }
            }
    }

    fun createUserWithEmailAndPassword(email: String, password: String) {
        _authState.value = AuthState.Loading
        repository.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success(repository.getCurrentUser())
                } else {
                    _authState.value = AuthState.Error(task.exception?.localizedMessage ?: "Sign up failed")
                }
            }
    }

    fun signInWithGoogleIdToken(idToken: String) {
        _authState.value = AuthState.Loading
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        repository.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success(repository.getCurrentUser())
                } else {
                    _authState.value = AuthState.Error(task.exception?.localizedMessage ?: "Google sign-in failed")
                }
            }
    }

    fun sendPasswordResetEmail(email: String) {
        _passwordResetState.value = AuthState.Loading
        repository.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _passwordResetState.value = AuthState.Success(null)
                } else {
                    _passwordResetState.value = AuthState.Error(task.exception?.localizedMessage ?: "Failed to send reset email")
                }
            }
    }

    fun getGoogleSignInClient(context: Context) = repository.getGoogleSignInClient(context)
}

class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}