package com.example.newproject30.ui.detail

import androidx.compose.runtime.Immutable
import com.example.newproject30.data.local.entity.MoodEntry

/**
 * UI State for the Entry Detail screen.
 *
 * @param isLoading Flag indicating if the entry is being loaded from Room.
 * @param entry The loaded [MoodEntry] instance, or null if deleted/not found.
 */
@Immutable
data class EntryDetailUiState(
    val isLoading: Boolean = true,
    val entry: MoodEntry? = null
)

/**
 * User Events for the Entry Detail screen.
 */
sealed interface EntryDetailUiEvent {
    data object ToggleBookmark : EntryDetailUiEvent
    data object DeleteEntry : EntryDetailUiEvent
}
