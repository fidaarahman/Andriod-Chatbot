package com.gemini.chatbot

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gemini.chatbot.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private var isEmailFocused = false
    private var isPasswordFocused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Disable the sign-up button initially
        binding.btnSignup.isEnabled = false
        binding.btnSignup.setBackgroundColor(Color.parseColor("#B0B0B0")) // Gray color initially

        // Focus change listeners to track user interaction
        binding.etEmail.setOnFocusChangeListener { _, hasFocus ->
            isEmailFocused = hasFocus
        }

        binding.etPassword.setOnFocusChangeListener { _, hasFocus ->
            isPasswordFocused = hasFocus
        }

        // TextWatcher to handle dynamic validations and button state
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = binding.etEmail.text.toString()
                val password = binding.etPassword.text.toString()

                // Handle email validation
                if (isEmailFocused) {
                    if (email.isEmpty()) {
                        binding.etEmail.error = "Required"
                    } else if (!isEmailValid(email)) {
                        binding.etEmail.error = "Invalid email address"
                    } else {
                        binding.etEmail.error = null // Clear error when valid
                    }
                }

                // Handle password validation
                if (isPasswordFocused) {
                    if (password.isEmpty()) {
                        binding.etPassword.error = "Required"
                    } else if (!isPasswordValid(password)) {
                        binding.etPassword.error = "Password must be 6-12 characters"
                    } else {
                        binding.etPassword.error = null // Clear error when valid
                    }
                }

                // Enable or disable the sign-up button
                binding.btnSignup.isEnabled = isEmailValid(email) && isPasswordValid(password)

                // Change button background color based on its state
                if (binding.btnSignup.isEnabled) {
                    binding.btnSignup.setBackgroundColor(Color.parseColor("#000000")) // Black color
                } else {
                    binding.btnSignup.setBackgroundColor(Color.parseColor("#B0B0B0")) // Gray color
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }


        // Attach the TextWatcher to email and password fields
        binding.etEmail.addTextChangedListener(textWatcher)
        binding.etPassword.addTextChangedListener(textWatcher)

        // Handle Sign-Up Button click
        binding.btnSignup.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            // Ensure no errors before signing up
            if (binding.etEmail.error != null || binding.etPassword.error != null) {
                Toast.makeText(this, "Please fix errors before signing up", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create new user with Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                        // Navigate to Login Activity
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Sign-up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
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
