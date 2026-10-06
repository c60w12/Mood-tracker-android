package com.example.newproject30.ui.addentry

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.newproject30.data.local.AppDatabase
import com.example.newproject30.data.local.entity.MoodEntry
import com.example.newproject30.data.repository.MoodRepository
import com.example.newproject30.data.repository.MoodRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing state and persistence for creating a new Mood / Voice Note entry.
 */
class AddEntryViewModel(
    private val repository: MoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEntryUiState())
    val uiState: StateFlow<AddEntryUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddEntryUiEvent) {
        when (event) {
            is AddEntryUiEvent.SetMoodScore -> {
                _uiState.update { it.copy(moodScore = event.score) }
            }

            is AddEntryUiEvent.SetPrimaryEmotion -> {
                _uiState.update { it.copy(primaryEmotion = event.emotion) }
            }

            is AddEntryUiEvent.ContentChanged -> {
                _uiState.update { it.copy(content = event.content, partialSpeechText = "") }
            }

            is AddEntryUiEvent.UpdatePartialSpeech -> {
                _uiState.update { it.copy(partialSpeechText = event.text) }
            }

            is AddEntryUiEvent.CommitFinalSpeech -> {
                _uiState.update { current ->
                    val newContent = if (current.content.isBlank()) {
                        event.text
                    } else {
                        "${current.content} ${event.text}"
                    }
                    current.copy(
                        content = newContent,
                        partialSpeechText = "",
                        isVoiceTranscript = true
                    )
                }
            }

            is AddEntryUiEvent.CancelSpeech -> {
                _uiState.update { current ->
                    current.copy(
                        partialSpeechText = "",
                        isRecording = false
                    )
                }
            }

            is AddEntryUiEvent.ToggleTag -> {
                _uiState.update { current ->
                    val updatedTags = if (current.selectedTags.contains(event.tag)) {
                        current.selectedTags - event.tag
                    } else {
                        current.selectedTags + event.tag
                    }
                    current.copy(selectedTags = updatedTags)
                }
            }

            is AddEntryUiEvent.SetPermissionGranted -> {
                _uiState.update { it.copy(hasRecordAudioPermission = event.isGranted) }
            }

            is AddEntryUiEvent.SetRecordingState -> {
                _uiState.update { it.copy(isRecording = event.isRecording) }
            }

            is AddEntryUiEvent.SaveEntry -> {
                saveEntry()
            }

            is AddEntryUiEvent.ResetSavedState -> {
                _uiState.update { AddEntryUiState() }
            }
        }
    }

    private fun saveEntry() {
        viewModelScope.launch {
            val state = _uiState.value
            val finalBodyText = state.displayContent.trim()
            if (finalBodyText.isNotBlank()) {
                val combinedTags = (listOf(state.primaryEmotion) + state.selectedTags)
                    .distinct()
                    .filter { it.isNotBlank() }

                val entry = MoodEntry(
                    timestamp = System.currentTimeMillis(),
                    moodScore = state.moodScore,
                    primaryEmotion = state.primaryEmotion,
                    content = finalBodyText,
                    isVoiceTranscript = state.isVoiceTranscript,
                    isBookmarked = false,
                    tags = combinedTags
                )

                repository.insertEntry(entry)
                _uiState.update { it.copy(isEntrySaved = true) }
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(context.applicationContext)
                val repository = MoodRepositoryImpl(database.moodDao())
                return AddEntryViewModel(repository) as T
            }
        }
    }
}
