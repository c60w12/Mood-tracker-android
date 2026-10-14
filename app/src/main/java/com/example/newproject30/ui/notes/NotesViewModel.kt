package com.example.newproject30.ui.notes

import android.content.Context
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
 * Filtering logic executes on [Dispatchers.IO] to keep UI frame-rates smooth.
 */
class NotesViewModel(
    private val repository: MoodRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _showBookmarkedOnly = MutableStateFlow(false)

    val uiState: StateFlow<NotesUiState> = combine(
        repository.getAllEntries(),
        _searchQuery,
        _selectedCategory,
        _showBookmarkedOnly
    ) { allEntries, query, selectedCat, bookmarkedOnly ->
        // Extract all available categories from tags dynamically
        val categories = allEntries
            .flatMap { it.tags }
            .distinct()
            .sorted()

        // Filter entries based on search, exact category containment, and bookmark filter
        val filtered = allEntries.filter { entry ->
            val matchesSearch = query.isBlank() ||
                    entry.content.contains(query, ignoreCase = true) ||
                    entry.primaryEmotion.contains(query, ignoreCase = true) ||
                    entry.tags.any { it.contains(query, ignoreCase = true) }

            val matchesCategory = selectedCat == null || entry.tags.any { it.equals(selectedCat, ignoreCase = true) }

            val matchesBookmark = !bookmarkedOnly || entry.isBookmarked

            matchesSearch && matchesCategory && matchesBookmark
        }

        NotesUiState(
            isLoading = false,
            entries = filtered,
            availableCategories = categories,
            searchQuery = query,
            selectedCategory = selectedCat,
            showBookmarkedOnly = bookmarkedOnly
        )
    }
    .flowOn(Dispatchers.IO)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NotesUiState(isLoading = true)
    )

    /**
     * Handles incoming user events in accordance with MVI pattern.
     */
    fun onEvent(event: NotesUiEvent) {
        when (event) {
            is NotesUiEvent.SearchQueryChanged -> {
                _searchQuery.value = event.query
            }

            is NotesUiEvent.SelectCategory -> {
                // Toggle selection if clicking the same category again
                _selectedCategory.value = if (_selectedCategory.value == event.category) {
                    null
                } else {
                    event.category
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
