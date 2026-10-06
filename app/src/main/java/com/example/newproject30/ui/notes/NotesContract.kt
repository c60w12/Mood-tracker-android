package com.example.newproject30.ui.notes

import androidx.compose.runtime.Immutable
import com.example.newproject30.data.local.entity.MoodEntry

/**
 * UI State for the Notes screen. Marked @Immutable for Compose recomposition optimization.
 *
 * @param isLoading Flag indicating initial or background data loading.
 * @param entries Filtered list of mood/note entries.
 * @param availableCategories All unique tags/categories present across entries.
 * @param searchQuery Current search text query.
 * @param selectedCategory Currently selected tag filter, or null if all tags are shown.
 * @param showBookmarkedOnly Flag indicating if only bookmarked entries are shown.
 */
@Immutable
data class NotesUiState(
    val isLoading: Boolean = true,
    val entries: List<MoodEntry> = emptyList(),
    val availableCategories: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val showBookmarkedOnly: Boolean = false
)

/**
 * User Intents / UI Events for the Notes screen.
 */
sealed interface NotesUiEvent {
    data class SearchQueryChanged(val query: String) : NotesUiEvent
    data class SelectCategory(val category: String?) : NotesUiEvent
    data object ToggleBookmarkFilter : NotesUiEvent
    data class ToggleBookmark(val entryId: Long) : NotesUiEvent
    data class DeleteEntry(val entryId: Long) : NotesUiEvent
}
