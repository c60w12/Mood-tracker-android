package com.example.newproject30.ui.addentry

import androidx.compose.runtime.Immutable

/**
 * UI State for the Add Entry / Voice Transcript bottom sheet.
 *
 * @param moodScore Selected mood rating on a scale of 1 to 5.
 * @param primaryEmotion Primary emotion tag selected by user.
 * @param availableEmotions Dynamic list of emotion tags fetched from Room.
 * @param availableContextTags Dynamic list of contextual tags fetched from Room.
 * @param content Text body for the note/journal.
 * @param partialSpeechText Live streaming interim partial text from SpeechRecognizer.
 * @param isVoiceTranscript Flag indicating if text was appended/spoken via voice.
 * @param selectedTags Additional category tags selected for the entry.
 * @param isRecording Flag indicating if speech recognition is currently active.
 * @param hasRecordAudioPermission Flag indicating if RECORD_AUDIO permission was granted.
 * @param isEntrySaved Flag set to true when entry is successfully persisted to close sheet.
 * @param showAddTagDialog Flag indicating if the Add Tag/Emotion AlertDialog is visible.
 * @param tagDialogCategory Category for new tag creation ("EMOTION" or "CONTEXT").
 * @param newTagInput Field value for creating a new custom tag.
 */
@Immutable
data class AddEntryUiState(
    val moodScore: Int = 3,
    val primaryEmotion: String = "Calm",
    val availableEmotions: List<String> = listOf("Calm", "Anxious", "Grateful", "Exhausted", "Energetic", "Reflective", "Focused", "Overwhelmed"),
    val availableContextTags: List<String> = listOf("Work", "Wellness", "Mindfulness", "Ideas", "Reflections"),
    val content: String = "",
    val partialSpeechText: String = "",
    val isVoiceTranscript: Boolean = false,
    val selectedTags: List<String> = listOf("Journal"),
    val isRecording: Boolean = false,
    val hasRecordAudioPermission: Boolean = false,
    val isEntrySaved: Boolean = false,
    val showAddTagDialog: Boolean = false,
    val tagDialogCategory: String = "CONTEXT",
    val newTagInput: String = ""
) {
    val displayContent: String
        get() = SpeechTextFormatter.appendText(content, partialSpeechText)

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
    data class RemoveTag(val tag: String) : AddEntryUiEvent
    data class SetPermissionGranted(val isGranted: Boolean) : AddEntryUiEvent
    data class SetRecordingState(val isRecording: Boolean) : AddEntryUiEvent
    data class ShowAddTagDialog(val category: String = "CONTEXT") : AddEntryUiEvent
    data object DismissAddTagDialog : AddEntryUiEvent
    data class NewTagInputChanged(val text: String) : AddEntryUiEvent
    data object SaveNewCustomTag : AddEntryUiEvent
    data object SaveEntry : AddEntryUiEvent
    data object ResetSavedState : AddEntryUiEvent
}
