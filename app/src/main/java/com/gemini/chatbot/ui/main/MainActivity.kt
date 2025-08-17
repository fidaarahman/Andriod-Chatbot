package com.gemini.chatbot.ui.main

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
import com.gemini.chatbot.R
import com.gemini.chatbot.adapter.GeminiAdapter
import com.gemini.chatbot.databinding.ActivityMainBinding
import com.gemini.chatbot.model.DataResponse
import com.gemini.chatbot.roomHelper.AppDB
import com.gemini.chatbot.roomHelper.NoteRepository
import com.gemini.chatbot.ui.chat.ChatFragment
import com.gemini.chatbot.viewmodel.ChatViewModel
import com.gemini.chatbot.viewmodel.ChatViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var bitmap: Bitmap? = null
    private lateinit var imageUri: String
    private val REQUEST_PERMISSION_MICROPHONE = 1
    private lateinit var adapter: GeminiAdapter

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var recognizerIntent: Intent

    private lateinit var viewModel: ChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = GeminiAdapter(this, arrayListOf())
        binding.recyclerViewId.adapter = adapter

        val db = AppDB.getDatabase(applicationContext, CoroutineScope(SupervisorJob()))
        val noteRepository = NoteRepository(db.noteDao())
        val chatRepository = com.gemini.chatbot.data.repository.ChatRepository(applicationContext, noteRepository)
        val factory = ChatViewModelFactory(chatRepository)
        viewModel = androidx.lifecycle.ViewModelProvider(this, factory)[ChatViewModel::class.java]

        viewModel.messages.observe(this) { list ->
            adapter.submitItems(list)
        }

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_chats -> {
                    openFragment(ChatFragment())
                    binding.drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show()
                    binding.drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }
        }

        checkAndRequestPermissions()
        initSpeechRecognizer()

        binding.drawerIcon.setOnClickListener {
            binding.drawerLayout.openDrawer(binding.navigationView)
        }

        binding.ivVoice.setOnClickListener {
            Toast.makeText(this, "speak to record text", Toast.LENGTH_SHORT).show()
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

        binding.askButton.setOnClickListener {
            val prompt = binding.askEditText.text.toString()
            if (prompt.isNotEmpty()) {
                binding.askEditText.setText("")
                viewModel.sendPrompt(prompt, bitmap, imageUri)
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
            .addToBackStack(null)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .commit()
    }
}