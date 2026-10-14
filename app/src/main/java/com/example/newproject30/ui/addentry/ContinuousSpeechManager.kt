package com.example.newproject30.ui.addentry

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Continuous SpeechRecognizer manager supporting live partial streaming, error auto-restart across pauses, and explicit manual controls.
 */
class ContinuousSpeechManager(
    private val context: Context,
    private val onPartialResult: (String) -> Unit,
    private val onFinalResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onRecordingStateChanged: (Boolean) -> Unit
) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isUserRecording: Boolean = false

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition is unavailable on this device")
            Toast.makeText(context, "Speech recognition is unavailable on this device", Toast.LENGTH_SHORT).show()
            return
        }

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@ContinuousSpeechManager)
            }
        }

        isUserRecording = true
        onRecordingStateChanged(true)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            stopListening()
            onError(e.localizedMessage ?: "Failed to start speech recognition")
        }
    }

    fun stopListening() {
        isUserRecording = false
        onRecordingStateChanged(false)
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) { }
    }

    fun cancelListening() {
        isUserRecording = false
        onRecordingStateChanged(false)
        onPartialResult("")
        try {
            speechRecognizer?.cancel()
        } catch (_: Exception) { }
    }

    fun destroy() {
        isUserRecording = false
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) { }
        speechRecognizer = null
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}

    override fun onError(error: Int) {
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Speech recognition error"
        }

        // If user hasn't pressed stop and error is timeout or no-match, auto-restart listening to keep dictation alive
        if (isUserRecording && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            try {
                speechRecognizer?.startListening(intent)
            } catch (_: Exception) {
                stopListening()
            }
        } else if (isUserRecording) {
            stopListening()
            onError(errorMessage)
        }
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()
        if (!text.isNullOrBlank()) {
            val formattedText = SpeechTextFormatter.format(text)
            onFinalResult(formattedText)
            onPartialResult("")
        }

        // Keep dictation alive if user hasn't explicitly stopped recording
        if (isUserRecording) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            try {
                speechRecognizer?.startListening(intent)
            } catch (_: Exception) {
                stopListening()
            }
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val partialText = matches?.firstOrNull()
        if (!partialText.isNullOrBlank()) {
            val formattedPartial = SpeechTextFormatter.format(partialText)
            onPartialResult(formattedPartial)
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}

/**
 * Remembers a ContinuousSpeechManager instance tied to the composable lifecycle.
 */
@Composable
fun rememberContinuousSpeechManager(
    onPartialResult: (String) -> Unit,
    onFinalResult: (String) -> Unit,
    onError: (String) -> Unit = {},
    onRecordingStateChanged: (Boolean) -> Unit = {}
): ContinuousSpeechManager {
    val context = LocalContext.current

    val manager = remember(context) {
        ContinuousSpeechManager(
            context = context,
            onPartialResult = onPartialResult,
            onFinalResult = onFinalResult,
            onError = onError,
            onRecordingStateChanged = onRecordingStateChanged
        )
    }

    DisposableEffect(manager) {
        onDispose {
            manager.destroy()
        }
    }

    return manager
}
