package com.gemini.chatbot.ui.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.gemini.chatbot.R
import com.gemini.chatbot.databinding.ActivityLoginBinding
import com.gemini.chatbot.ui.main.MainActivity
import com.gemini.chatbot.viewmodel.AuthState
import com.gemini.chatbot.viewmodel.AuthViewModel
import com.gemini.chatbot.viewmodel.AuthViewModelFactory
import com.gemini.chatbot.data.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val authRepository = AuthRepository(FirebaseAuth.getInstance())
        val factory = AuthViewModelFactory(authRepository)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        if (authRepository.getCurrentUser() != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        clearCachedErrors()

        googleSignInClient = viewModel.getGoogleSignInClient(this)

        binding.btnLogin.isEnabled = false
        binding.btnLogin.setBackgroundColor(Color.parseColor("#B0B0B0"))

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = binding.etEmail.text.toString()
                val password = binding.etPassword.text.toString()

                binding.btnLogin.isEnabled = isEmailValid(email) && isPasswordValid(password)
                binding.btnLogin.setBackgroundColor(
                    if (binding.btnLogin.isEnabled) Color.parseColor("#000000")
                    else Color.parseColor("#B0B0B0")
                )
            }
        }

        binding.etEmail.addTextChangedListener(textWatcher)
        binding.etPassword.addTextChangedListener(textWatcher)

        binding.tvSignIn.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.btnGoogleSignin.setOnClickListener {
            signInWithGoogle()
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            viewModel.signInWithEmailAndPassword(email, password)
        }

        binding.tvForgotPassword.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Reset Password")

            val input = EditText(this)
            input.hint = "Enter your email"
            input.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            builder.setView(input)

            builder.setPositiveButton("Send Reset Link") { _, _ ->
                val email = input.text.toString().trim()
                if (email.isEmpty()) {
                    Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
                } else if (!isEmailValid(email)) {
                    Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.sendPasswordResetEmail(email)
                }
            }

            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            builder.show()
        }

        viewModel.authState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    // Optionally show progress
                }
                is AuthState.Success -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is AuthState.Error -> {
                    showError(state.message)
                }
                else -> {}
            }
        }

        viewModel.passwordResetState.observe(this) { state ->
            when (state) {
                is AuthState.Success -> Toast.makeText(this, "Reset link sent. Check your inbox.", Toast.LENGTH_SHORT).show()
                is AuthState.Error -> showError(state.message)
                else -> {}
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                viewModel.signInWithGoogleIdToken(account.idToken!!)
            } catch (e: ApiException) {
                showError("Google Sign-In failed: ${e.localizedMessage}")
            }
        }
    }

    private fun clearCachedErrors() {
        binding.etEmail.error = null
        binding.etPassword.error = null
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length in 6..12
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}