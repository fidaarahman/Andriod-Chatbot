package com.gemini.chatbot.ui.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.gemini.chatbot.databinding.ActivitySignUpBinding
import com.gemini.chatbot.viewmodel.AuthState
import com.gemini.chatbot.viewmodel.AuthViewModel
import com.gemini.chatbot.viewmodel.AuthViewModelFactory
import com.gemini.chatbot.data.repository.AuthRepository
import com.gemini.chatbot.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var viewModel: AuthViewModel
    private var isEmailFocused = false
    private var isPasswordFocused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = AuthViewModelFactory(AuthRepository(FirebaseAuth.getInstance()))
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        binding.btnSignup.isEnabled = false
        binding.btnSignup.setBackgroundColor(Color.parseColor("#B0B0B0"))

        binding.etEmail.setOnFocusChangeListener { _, hasFocus ->
            isEmailFocused = hasFocus
        }

        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            isPasswordFocused = hasFocus
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = binding.etEmail.text.toString()
                val password = binding.etPassword.text.toString()

                if (isEmailFocused) {
                    if (email.isEmpty()) {
                        binding.etEmail.error = "Required"
                    } else if (!isEmailValid(email)) {
                        binding.etEmail.error = "Invalid email address"
                    } else {
                        binding.etEmail.error = null
                    }
                }

                if (isPasswordFocused) {
                    if (password.isEmpty()) {
                        binding.etPassword.error = "Required"
                    } else if (!isPasswordValid(password)) {
                        binding.etPassword.error = "Password must be 6-12 characters"
                    } else {
                        binding.etPassword.error = null
                    }
                }

                binding.btnSignup.isEnabled = isEmailValid(email) && isPasswordValid(password)
                if (binding.btnSignup.isEnabled) {
                    binding.btnSignup.setBackgroundColor(Color.parseColor("#000000"))
                } else {
                    binding.btnSignup.setBackgroundColor(Color.parseColor("#B0B0B0"))
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        binding.etEmail.addTextChangedListener(textWatcher)
        binding.etPassword.addTextChangedListener(textWatcher)

        binding.btnSignup.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (binding.etEmail.error != null || binding.etPassword.error != null) {
                Toast.makeText(this, "Please fix errors before signing up", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.createUserWithEmailAndPassword(email, password)
        }

        viewModel.authState.observe(this) { state ->
            when (state) {
                is com.gemini.chatbot.viewmodel.AuthState.Success -> {
                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is com.gemini.chatbot.viewmodel.AuthState.Error -> {
                    Toast.makeText(this, "Sign-up failed: ${state.message}", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length in 6..12
    }
}