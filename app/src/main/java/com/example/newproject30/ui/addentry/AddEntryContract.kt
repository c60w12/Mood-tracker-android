package com.example.newproject30.ui.addentry

import androidx.compose.runtime.Immutable

/**
 * UI State for the Add Entry / Voice Transcript bottom sheet.
 *
 * @param moodScore Selected mood rating on a scale of 1 to 5.
 * @param primaryEmotion Primary emotion tag selected by user.
 * @param content Text body for the note/journal.
 * @param partialSpeechText Live streaming interim partial text from SpeechRecognizer.
 * @param isVoiceTranscript Flag indicating if text was appended/spoken via voice.
 * @param selectedTags Additional category tags selected for the entry.
 * @param isRecording Flag indicating if speech recognition is currently active.
 * @param hasRecordAudioPermission Flag indicating if RECORD_AUDIO permission was granted.
 * @param isEntrySaved Flag set to true when entry is successfully persisted to close sheet.
 */
@Immutable
data class AddEntryUiState(
    val moodScore: Int = 3,
    val primaryEmotion: String = "Calm",
    val content: String = "",
    val partialSpeechText: String = "",
    val isVoiceTranscript: Boolean = false,
    val selectedTags: List<String> = listOf("Journal"),
    val isRecording: Boolean = false,
    val hasRecordAudioPermission: Boolean = false,
    val isEntrySaved: Boolean = false
) {
    val displayContent: String
        get() = when {
            partialSpeechText.isBlank() -> content
            content.isBlank() -> partialSpeechText
            else -> "$content $partialSpeechText"
        }

    val isSaveEnabled: Boolean
        get() = displayContent.isNotBlank()
}

/**
 * User Intents / UI Events for adding a mood entry.
 */
sealed interface AddEntryUiEvent {
    data class SetMoodScore(val score: Int) : AddEntryUiEvent
    data class SetPrimaryEmotion(val emotion: String) : AddEntryUiEvent
    data class ContentChanged(val content: String) : AddEntryUiEvent
    data class UpdatePartialSpeech(val text: String) : AddEntryUiEvent
    data class CommitFinalSpeech(val text: String) : AddEntryUiEvent
    data object CancelSpeech : AddEntryUiEvent
    data class ToggleTag(val tag: String) : AddEntryUiEvent
    data class SetPermissionGranted(val isGranted: Boolean) : AddEntryUiEvent
    data class SetRecordingState(val isRecording: Boolean) : AddEntryUiEvent
    data object SaveEntry : AddEntryUiEvent
    data object ResetSavedState : AddEntryUiEvent
}
