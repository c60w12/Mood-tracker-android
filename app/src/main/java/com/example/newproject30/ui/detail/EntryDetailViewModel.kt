package com.example.newproject30.ui.detail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.newproject30.data.local.AppDatabase
import com.example.newproject30.data.repository.MoodRepository
import com.example.newproject30.data.repository.MoodRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing state and actions for displaying a detailed journal entry/transcript.
 */
class EntryDetailViewModel(
    private val entryId: Long,
    private val repository: MoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EntryDetailUiState())
    val uiState: StateFlow<EntryDetailUiState> = _uiState.asStateFlow()

    init {
        loadEntry()
    }

    private fun loadEntry() {
        viewModelScope.launch(Dispatchers.IO) {
            val entry = repository.getEntryById(entryId)
            _uiState.update {
                it.copy(isLoading = false, entry = entry)
            }
        }
    }

    fun onEvent(event: EntryDetailUiEvent, onDeleted: () -> Unit = {}) {
        when (event) {
            is EntryDetailUiEvent.ToggleBookmark -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val currentEntry = _uiState.value.entry ?: return@launch
                    val updated = currentEntry.copy(isBookmarked = !currentEntry.isBookmarked)
                    repository.updateEntry(updated)
                    _uiState.update { it.copy(entry = updated) }
                }
            }

            is EntryDetailUiEvent.DeleteEntry -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val currentEntry = _uiState.value.entry ?: return@launch
                    repository.deleteEntry(currentEntry)
                    _uiState.update { it.copy(entry = null) }
                    launch(Dispatchers.Main) {
                        onDeleted()
                    }
                }
            }
        }
    }

    companion object {
        fun provideFactory(entryId: Long, context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(context.applicationContext)
                val repository = MoodRepositoryImpl(database.moodDao(), database.customTagDao(), database.analyticsDao())
                return EntryDetailViewModel(entryId, repository) as T
            }
        }
    }
}
