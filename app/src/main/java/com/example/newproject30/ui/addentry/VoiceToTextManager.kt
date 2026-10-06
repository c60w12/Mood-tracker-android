package com.example.newproject30.ui.addentry

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Helper manager handling speech-to-text activity launching, runtime RECORD_AUDIO permission checks, and result parsing.
 */
class VoiceToTextManager(
    private val context: Context,
    private val speechLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    private val permissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onRecordingStateChanged: (Boolean) -> Unit
) {
    fun startListening() {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        )

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            launchSpeechIntent()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun launchSpeechIntent() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now to record your entry...")
        }

        try {
            onRecordingStateChanged(true)
            speechLauncher.launch(intent)
        } catch (_: Exception) {
            onRecordingStateChanged(false)
            val errorMsg = "Speech recognition is unavailable on this device"
            onError(errorMsg)
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
        }
    }

    fun handleActivityResult(result: ActivityResult) {
        onRecordingStateChanged(false)
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                onResult(spokenText)
            }
        }
    }
}

/**
 * Remembers a VoiceToTextManager instance configured with ActivityResultLaunchers.
 */
@Composable
fun rememberVoiceToTextManager(
    onResult: (String) -> Unit,
    onError: (String) -> Unit = {},
    onRecordingStateChanged: (Boolean) -> Unit = {}
): VoiceToTextManager {
    val context = LocalContext.current

    var managerHolder: VoiceToTextManager? = null

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        managerHolder?.handleActivityResult(result)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            managerHolder?.launchSpeechIntent()
        } else {
            onRecordingStateChanged(false)
            val msg = "Microphone permission is required for voice notes"
            onError(msg)
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val manager = remember(context) {
        VoiceToTextManager(
            context = context,
            speechLauncher = speechLauncher,
            permissionLauncher = permissionLauncher,
            onResult = onResult,
            onError = onError,
            onRecordingStateChanged = onRecordingStateChanged
        )
    }

    managerHolder = manager
    return manager
}
