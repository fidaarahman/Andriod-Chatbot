package com.gemini.chatbot

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
import com.gemini.chatbot.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        clearCachedErrors()


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Disable login button initially
        binding.btnLogin.isEnabled = false
        binding.btnLogin.setBackgroundColor(Color.parseColor("#B0B0B0"))

        // TextWatcher to enable/disable login button dynamically
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

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        showError("Login failed: ${task.exception?.localizedMessage}")
                    }
                }
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
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    "Reset link sent. Check your inbox.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                showError("Error: ${task.exception?.localizedMessage}")
                            }
                        }
                }
            }

            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            builder.show()
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
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                showError("Google Sign-In failed: ${e.localizedMessage}")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    showError("Google Authentication failed: ${task.exception?.localizedMessage}")
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

    // Password Validation
    private fun isPasswordValid(password: String): Boolean {
        return password.length in 6..12
    }

    // Error Message Display
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
