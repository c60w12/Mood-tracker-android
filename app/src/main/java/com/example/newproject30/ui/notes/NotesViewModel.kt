package com.example.newproject30.ui.notes

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.newproject30.data.local.AppDatabase
import com.example.newproject30.data.repository.MoodRepository
import com.example.newproject30.data.repository.MoodRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel managing UI State and User Intents for the Notes & Mood tracking screen.
 * Indexing and filtering operate strictly on Primary Emotions with explicit Logcat debugging.
 */
class NotesViewModel(
    private val repository: MoodRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedEmotion = MutableStateFlow<String?>(null)
    private val _showBookmarkedOnly = MutableStateFlow(false)

    val uiState: StateFlow<NotesUiState> = combine(
        repository.getAllEntries(),
        repository.getCustomTagsByCategory("EMOTION"),
        _searchQuery,
        _selectedEmotion,
        _showBookmarkedOnly
    ) { allEntries, emotionTags, query, selectedEmotion, bookmarkedOnly ->
        Log.d("NOTES_DEBUG", "Filtered by Primary Emotion: $selectedEmotion")

        // Extract all primary emotions from saved custom tags and recorded entries
        val emotions = (emotionTags.map { it.name } + allEntries.map { it.primaryEmotion })
            .distinct()
            .filter { it.isNotBlank() }
            .sorted()

        // Filter entries based on search, exact primaryEmotion match, and bookmark filter
        val filtered = allEntries.filter { entry ->
            val matchesSearch = query.isBlank() ||
                    entry.content.contains(query, ignoreCase = true) ||
                    entry.primaryEmotion.contains(query, ignoreCase = true) ||
                    entry.tags.any { it.contains(query, ignoreCase = true) }

            val matchesEmotion = selectedEmotion == null || entry.primaryEmotion.equals(selectedEmotion, ignoreCase = true)

            val matchesBookmark = !bookmarkedOnly || entry.isBookmarked

            matchesSearch && matchesEmotion && matchesBookmark
        }

        NotesUiState(
            isLoading = false,
            entries = filtered,
            availableEmotions = emotions,
            searchQuery = query,
            selectedEmotion = selectedEmotion,
            showBookmarkedOnly = bookmarkedOnly
        )
    }
    .flowOn(Dispatchers.IO)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NotesUiState(isLoading = true)
    )

    fun onEvent(event: NotesUiEvent) {
        when (event) {
            is NotesUiEvent.SearchQueryChanged -> {
                _searchQuery.value = event.query
            }

            is NotesUiEvent.SelectEmotion -> {
                // Toggle selection if clicking the same emotion again
                _selectedEmotion.value = if (_selectedEmotion.value == event.emotion) {
                    null
                } else {
                    event.emotion
                }
            }

            is NotesUiEvent.ToggleBookmarkFilter -> {
                _showBookmarkedOnly.value = !_showBookmarkedOnly.value
            }

            is NotesUiEvent.ToggleBookmark -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val entry = repository.getEntryById(event.entryId)
                    if (entry != null) {
                        repository.updateEntry(entry.copy(isBookmarked = !entry.isBookmarked))
                    }
                }
            }

            is NotesUiEvent.DeleteEntry -> {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.deleteEntryById(event.entryId)
                }
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(context.applicationContext)
                val repository = MoodRepositoryImpl(database.moodDao(), database.customTagDao(), database.analyticsDao())
                return NotesViewModel(repository) as T
            }
        }
    }
}
