package com.example.newproject30.ui.notes

import androidx.compose.runtime.Immutable
import com.example.newproject30.data.local.entity.MoodEntry

/**
 * UI State for the Notes screen. Marked @Immutable for Compose recomposition optimization.
 *
 * @param isLoading Flag indicating initial or background data loading.
 * @param entries Filtered list of mood/note entries.
 * @param availableEmotions Available primary emotions for filtering.
 * @param searchQuery Current search text query.
 * @param selectedEmotion Currently selected primary emotion filter, or null if all entries are shown.
 * @param showBookmarkedOnly Flag indicating if only bookmarked entries are shown.
 */
@Immutable
data class NotesUiState(
    val isLoading: Boolean = true,
    val entries: List<MoodEntry> = emptyList(),
    val availableEmotions: List<String> = emptyList(),
    val searchQuery: String = "",
    val selectedEmotion: String? = null,
    val showBookmarkedOnly: Boolean = false
) {
    @Deprecated("Renamed to availableEmotions", ReplaceWith("availableEmotions"))
    val availableCategories: List<String>
        get() = availableEmotions

    @Deprecated("Renamed to selectedEmotion", ReplaceWith("selectedEmotion"))
    val selectedCategory: String?
        get() = selectedEmotion
}

/**
 * User Intents / UI Events for the Notes screen.
 */
sealed interface NotesUiEvent {
    data class SearchQueryChanged(val query: String) : NotesUiEvent
    data class SelectEmotion(val emotion: String?) : NotesUiEvent
    data object ToggleBookmarkFilter : NotesUiEvent
    data class ToggleBookmark(val entryId: Long) : NotesUiEvent
    data class DeleteEntry(val entryId: Long) : NotesUiEvent
}
