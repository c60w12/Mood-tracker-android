package com.example.newproject30.ui.customization

import androidx.compose.runtime.Immutable

/**
 * UI State for the Customization Screen.
 *
 * @param isDarkMode Boolean flag for theme preference (true for Dark Mode, false for Light Mode).
 * @param customTags Set of user-created custom emotion tags.
 * @param newTagInput Input text for creating a new tag.
 */
@Immutable
data class CustomizationUiState(
    val isDarkMode: Boolean = false,
    val customTags: List<String> = emptyList(),
    val newTagInput: String = ""
)

/**
 * User Intents for the Customization Screen.
 */
sealed interface CustomizationUiEvent {
    data class SetDarkMode(val isDark: Boolean) : CustomizationUiEvent
    data class NewTagInputChanged(val text: String) : CustomizationUiEvent
    data object AddTag : CustomizationUiEvent
    data class RemoveTag(val tag: String) : CustomizationUiEvent
}
