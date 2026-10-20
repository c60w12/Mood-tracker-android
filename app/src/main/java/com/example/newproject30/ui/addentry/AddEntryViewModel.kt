package com.example.newproject30.ui.addentry

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.newproject30.data.local.AppDatabase
import com.example.newproject30.data.local.entity.CustomTag
import com.example.newproject30.data.local.entity.MoodEntry
import com.example.newproject30.data.repository.MoodRepository
import com.example.newproject30.data.repository.MoodRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing state and persistence for creating a new Mood / Voice Note entry.
 * Parses voice commands ("save entry", "save note", "cancel entry", "clear note") for hands-free workflow.
 */
class AddEntryViewModel(
    private val repository: MoodRepository
) : ViewModel() {

    private val _baseState = MutableStateFlow(AddEntryUiState())

    val uiState: StateFlow<AddEntryUiState> = combine(
        _baseState,
        repository.getCustomTagsByCategory("EMOTION"),
        repository.getCustomTagsByCategory("CONTEXT")
    ) { base, emotions, contextTags ->
        val emotionNames = emotions.map { it.name }.distinct().sorted()
        val contextTagNames = contextTags.map { it.name }.distinct().sorted()

        base.copy(
            availableEmotions = if (emotionNames.isNotEmpty()) emotionNames else base.availableEmotions,
            availableContextTags = if (contextTagNames.isNotEmpty()) contextTagNames else base.availableContextTags
        )
    }
    .flowOn(Dispatchers.IO)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AddEntryUiState()
    )

    fun onEvent(event: AddEntryUiEvent) {
        when (event) {
            is AddEntryUiEvent.SetMoodScore -> {
                _baseState.update { it.copy(moodScore = event.score) }
            }

            is AddEntryUiEvent.SetPrimaryEmotion -> {
                _baseState.update { it.copy(primaryEmotion = event.emotion) }
            }

            is AddEntryUiEvent.ContentChanged -> {
                _baseState.update { it.copy(content = event.content, partialSpeechText = "") }
            }

            is AddEntryUiEvent.UpdatePartialSpeech -> {
                _baseState.update { it.copy(partialSpeechText = event.text) }
            }

            is AddEntryUiEvent.CommitFinalSpeech -> {
                _baseState.update { current ->
                    val lower = event.text.lowercase()
                    when {
                        lower.contains("cancel entry") || lower.contains("clear note") -> {
                            current.copy(
                                content = "",
                                partialSpeechText = "",
                                isRecording = false
                            )
                        }

                        lower.contains("save entry") || lower.contains("save note") -> {
                            val cleanedText = event.text
                                .replace(Regex("(?i)save\\s+entry"), "")
                                .replace(Regex("(?i)save\\s+note"), "")

                            val updatedContent = SpeechTextFormatter.appendText(current.content, cleanedText)
                            val finalBodyText = updatedContent.ifBlank { current.displayContent }.trim()

                            if (finalBodyText.isNotBlank()) {
                                saveEntryDirectly(finalBodyText)
                            }

                            current.copy(
                                content = finalBodyText,
                                partialSpeechText = "",
                                isRecording = false
                            )
                        }

                        else -> {
                            val updatedContent = SpeechTextFormatter.appendText(current.content, event.text)
                            current.copy(
                                content = updatedContent,
                                partialSpeechText = "",
                                isVoiceTranscript = true
                            )
                        }
                    }
                }
            }

            is AddEntryUiEvent.CancelSpeech -> {
                _baseState.update { current ->
                    current.copy(
                        partialSpeechText = "",
                        isRecording = false
                    )
                }
            }

            is AddEntryUiEvent.ToggleTag -> {
                _baseState.update { current ->
                    val updatedTags = if (current.selectedTags.contains(event.tag)) {
                        current.selectedTags - event.tag
                    } else {
                        current.selectedTags + event.tag
                    }
                    current.copy(selectedTags = updatedTags)
                }
            }

            is AddEntryUiEvent.RemoveTag -> {
                _baseState.update { current ->
                    current.copy(selectedTags = current.selectedTags - event.tag)
                }
            }

            is AddEntryUiEvent.SetPermissionGranted -> {
                _baseState.update { it.copy(hasRecordAudioPermission = event.isGranted) }
            }

            is AddEntryUiEvent.SetRecordingState -> {
                _baseState.update { it.copy(isRecording = event.isRecording) }
            }

            is AddEntryUiEvent.ShowAddTagDialog -> {
                _baseState.update { it.copy(showAddTagDialog = true, tagDialogCategory = event.category, newTagInput = "") }
            }

            is AddEntryUiEvent.DismissAddTagDialog -> {
                _baseState.update { it.copy(showAddTagDialog = false, newTagInput = "") }
            }

            is AddEntryUiEvent.NewTagInputChanged -> {
                _baseState.update { it.copy(newTagInput = event.text) }
            }

            is AddEntryUiEvent.SaveNewCustomTag -> {
                val newTag = _baseState.value.newTagInput.trim()
                val category = _baseState.value.tagDialogCategory
                if (newTag.isNotBlank()) {
                    viewModelScope.launch(Dispatchers.IO) {
                        val customTag = CustomTag(name = newTag, isDefault = false, category = category)
                        repository.insertCustomTag(customTag)

                        _baseState.update { current ->
                            if (category == "EMOTION") {
                                current.copy(
                                    primaryEmotion = newTag,
                                    showAddTagDialog = false,
                                    newTagInput = ""
                                )
                            } else {
                                current.copy(
                                    selectedTags = (current.selectedTags + newTag).distinct(),
                                    showAddTagDialog = false,
                                    newTagInput = ""
                                )
                            }
                        }
                    }
                }
            }

            is AddEntryUiEvent.SaveEntry -> {
                saveEntry()
            }

            is AddEntryUiEvent.ResetSavedState -> {
                _baseState.update { AddEntryUiState() }
            }
        }
    }

    private fun saveEntryDirectly(bodyText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val state = uiState.value
            val combinedTags = (listOf(state.primaryEmotion) + state.selectedTags)
                .distinct()
                .filter { it.isNotBlank() }

            val entry = MoodEntry(
                timestamp = System.currentTimeMillis(),
                moodScore = state.moodScore,
                primaryEmotion = state.primaryEmotion,
                content = bodyText,
                isVoiceTranscript = true,
                isBookmarked = false,
                tags = combinedTags
            )

            repository.insertEntry(entry)
            _baseState.update { it.copy(isEntrySaved = true) }
        }
    }

    private fun saveEntry() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = uiState.value
            val finalBodyText = state.displayContent.trim()
            if (finalBodyText.isNotBlank()) {
                saveEntryDirectly(finalBodyText)
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(context.applicationContext)
                val repository = MoodRepositoryImpl(database.moodDao(), database.customTagDao(), database.analyticsDao())
                return AddEntryViewModel(repository) as T
            }
        }
    }
}
