package com.gemini.chatbot.data.repository

import android.content.Context
import com.gemini.chatbot.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthRepository(private val firebaseAuth: FirebaseAuth) {

    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    fun signInWithEmailAndPassword(email: String, password: String): Task<AuthResult> =
        firebaseAuth.signInWithEmailAndPassword(email, password)

    fun createUserWithEmailAndPassword(email: String, password: String): Task<AuthResult> =
        firebaseAuth.createUserWithEmailAndPassword(email, password)

    fun signInWithCredential(credential: AuthCredential): Task<AuthResult> =
        firebaseAuth.signInWithCredential(credential)

    fun sendPasswordResetEmail(email: String): Task<Void> =
        firebaseAuth.sendPasswordResetEmail(email)

    fun getGoogleSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }
}