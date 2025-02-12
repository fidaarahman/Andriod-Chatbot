package com.gemini.chatbot

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.gemini.chatbot.adapter.GeminiAdapter
import com.gemini.chatbot.databinding.ActivityMainBinding
import com.gemini.chatbot.model.DataResponse
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var bitmap: Bitmap? = null
    private lateinit var imageUri: String
    private val REQUEST_PERMISSION_MICROPHONE = 1
    private var responseData = arrayListOf<DataResponse>()
    private lateinit var adapter: GeminiAdapter

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var recognizerIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView and Adapter
        adapter = GeminiAdapter(this, responseData)
        binding.recyclerViewId.adapter = adapter
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_chats -> {
                    openFragment(ChatFragment()) // Open ChatFragment
                    binding.drawerLayout.closeDrawers() // Close the drawer
                    true
                }
                R.id.nav_settings -> {
                    // Handle settings navigation (if required)
                    Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show()
                    binding.drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }
        }
        // Check and request permissions
        checkAndRequestPermissions()

        // Initialize Speech Recognizer
        initSpeechRecognizer()


        // Drawer Icon Click Listener
        binding.drawerIcon.setOnClickListener {
            binding.drawerLayout.openDrawer(binding.navigationView) // Open drawer
        }

        // Mic Icon Click Listener
        binding.ivVoice.setOnClickListener {
            Toast.makeText(this,"speak to record text",Toast.LENGTH_SHORT).show()
            if (checkPermission()) {
                if (SpeechRecognizer.isRecognitionAvailable(this)) {
                    speechRecognizer.startListening(recognizerIntent)
                } else {
                    Toast.makeText(this, "Speech recognition not available!", Toast.LENGTH_SHORT).show()
                }
            } else {
                requestPermission()
            }
        }

        // Button Click Listener
        binding.askButton.setOnClickListener {
            val prompt = binding.askEditText.text.toString()
            if (prompt.isNotEmpty()) {
                binding.askEditText.setText("")
                processUserPrompt(prompt)
            }
        }
    }

    private fun processUserPrompt(prompt: String) {
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = getString(R.string.api_key)
        )

        if (bitmap != null) {
            responseData.add(DataResponse(0, prompt, imageUri))
            adapter.notifyDataSetChanged()

            val inputContent = content {
                image(bitmap!!)
                text(prompt)
            }

            GlobalScope.launch {
                val response = generativeModel.generateContent(inputContent)
                runOnUiThread {
                    responseData.add(DataResponse(1, response.text!!, ""))
                    adapter.notifyDataSetChanged()
                }
            }
        } else {
            responseData.add(DataResponse(0, prompt, ""))
            adapter.notifyDataSetChanged()

            GlobalScope.launch {
                bitmap = null
                imageUri = ""
                val response = generativeModel.generateContent(prompt)
                runOnUiThread {
                    responseData.add(DataResponse(1, response.text!!, ""))
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun initSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("SpeechRecognizer", "Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                Log.d("SpeechRecognizer", "Speech input started")
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                Log.d("SpeechRecognizer", "Speech input ended")
            }

            override fun onError(error: Int) {
                val errorMessage = getErrorText(error)
                Log.e("SpeechRecognizer", "Error occurred: $errorMessage")
                Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                restartSpeechRecognizer()
            }

            override fun onResults(results: Bundle?) {
                val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!data.isNullOrEmpty()) {
                    binding.askEditText.setText(data[0])
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun restartSpeechRecognizer() {
        speechRecognizer.destroy()
        initSpeechRecognizer()
    }

    private fun getErrorText(error: Int): String {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
            else -> "Unknown error: $error"
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_PERMISSION_MICROPHONE)
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION),
                REQUEST_PERMISSION_MICROPHONE
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_PERMISSION_MICROPHONE
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // Add to backstack for navigation
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
    }
}