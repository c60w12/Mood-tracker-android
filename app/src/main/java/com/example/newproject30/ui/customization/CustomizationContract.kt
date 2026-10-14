package com.example.newproject30.ui.customization

import androidx.compose.runtime.Immutable
import com.example.newproject30.data.local.entity.CustomTag

/**
 * UI State for the Customization Screen.
 *
 * @param isDarkMode Boolean flag for theme preference.
 * @param primaryEmotions List of saved [CustomTag] items for primary emotions.
 * @param contextTags List of saved [CustomTag] items for secondary context tags.
 * @param showAddDialog Flag indicating if the tag creation dialog is visible.
 * @param dialogCategory Category for new tag creation ("EMOTION" or "CONTEXT").
 * @param newTagInput Field value for creating a new tag.
 */
@Immutable
data class CustomizationUiState(
    val isDarkMode: Boolean = false,
    val primaryEmotions: List<CustomTag> = emptyList(),
    val contextTags: List<CustomTag> = emptyList(),
    val showAddDialog: Boolean = false,
    val dialogCategory: String = "EMOTION",
    val newTagInput: String = ""
)

/**
 * User Intents for the Customization Screen.
 */
sealed interface CustomizationUiEvent {
    data class SetDarkMode(val isDark: Boolean) : CustomizationUiEvent
    data class ShowAddDialog(val category: String) : CustomizationUiEvent
    data object DismissAddDialog : CustomizationUiEvent
    data class NewTagInputChanged(val text: String) : CustomizationUiEvent
    data object SaveNewTag : CustomizationUiEvent
    data class DeleteTag(val tag: CustomTag) : CustomizationUiEvent
}
