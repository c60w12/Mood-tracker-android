package com.example.newproject30.ui.settings

import androidx.compose.runtime.Immutable

/**
 * UI State for the Settings Screen.
 *
 * @param dailyRemindersEnabled Flag indicating if daily notification reminders are active.
 * @param showClearDataDialog Flag controlling visibility of the Clear All Data confirmation dialog.
 * @param exportedDataMessage Serialized JSON string ready for sharing/export, or null.
 * @param appVersion Current application version name and build code string.
 */
@Immutable
data class SettingsUiState(
    val dailyRemindersEnabled: Boolean = false,
    val showClearDataDialog: Boolean = false,
    val exportedDataMessage: String? = null,
    val appVersion: String = "1.0.0 (Build 100)"
)

/**
 * User Intents for the Settings Screen.
 */
sealed interface SettingsUiEvent {
    data class ToggleDailyReminders(val enabled: Boolean) : SettingsUiEvent
    data object ShowClearDataDialog : SettingsUiEvent
    data object DismissClearDataDialog : SettingsUiEvent
    data object ConfirmClearData : SettingsUiEvent
    data object ExportDataJson : SettingsUiEvent
    data object ClearExportedDataMessage : SettingsUiEvent
}
