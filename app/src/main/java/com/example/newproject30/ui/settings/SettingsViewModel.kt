package com.example.newproject30.ui.settings

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.newproject30.data.local.AppDatabase
import com.example.newproject30.data.local.dao.MoodDao
import com.example.newproject30.data.preferences.UserPreferencesRepository
import com.example.newproject30.data.reminder.ReminderScheduler
import com.example.newproject30.data.repository.MoodRepository
import com.example.newproject30.data.repository.MoodRepositoryImpl
import com.example.newproject30.util.DebugDataSeeder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * ViewModel managing settings, Storage Access Framework JSON data exports, clear data dialogs, notification toggles, and auto-start voice options.
 */
class SettingsViewModel(
    private val moodDao: MoodDao,
    private val repository: MoodRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val _showClearDataDialog = MutableStateFlow(false)
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    private val _debugMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.dailyRemindersEnabled,
        preferencesRepository.autoStartVoiceOnOpen,
        _showClearDataDialog,
        _snackbarMessage,
        _debugMessage
    ) { remindersEnabled, autoVoice, showDialog, snackbarMsg, debugMsg ->
        SettingsUiState(
            dailyRemindersEnabled = remindersEnabled,
            autoStartVoiceOnOpen = autoVoice,
            showClearDataDialog = showDialog,
            exportedDataMessage = snackbarMsg,
            appVersion = "1.0.0 (Build 100)",
            debugMessage = debugMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun exportDataToUri(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val entries = repository.getAllEntries().first()
                val jsonString = json.encodeToString(entries)

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                    outputStream.flush()
                }

                _snackbarMessage.value = "Export successful! Saved to requested JSON file."
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    DocumentsContract.deleteDocument(context.contentResolver, uri)
                } catch (_: Exception) { }
                _snackbarMessage.value = "Export failed: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    fun onEvent(event: SettingsUiEvent, context: Context) {
        when (event) {
            is SettingsUiEvent.ToggleDailyReminders -> {
                viewModelScope.launch {
                    preferencesRepository.setDailyRemindersEnabled(event.enabled)
                    if (event.enabled) {
                        ReminderScheduler.scheduleNextDailyReminder(context)
                    } else {
                        ReminderScheduler.cancelDailyReminder(context)
                    }
                }
            }

            is SettingsUiEvent.ToggleAutoStartVoice -> {
                viewModelScope.launch {
                    preferencesRepository.setAutoStartVoiceOnOpen(event.enabled)
                }
            }

            is SettingsUiEvent.ShowClearDataDialog -> {
                _showClearDataDialog.value = true
            }

            is SettingsUiEvent.DismissClearDataDialog -> {
                _showClearDataDialog.value = false
            }

            is SettingsUiEvent.ConfirmClearData -> {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.deleteAllEntries()
                    _showClearDataDialog.value = false
                    _snackbarMessage.value = "Database cleared cleanly!"
                }
            }

            is SettingsUiEvent.SeedTestData -> {
                viewModelScope.launch(Dispatchers.IO) {
                    DebugDataSeeder.seedSyntheticEntries(moodDao, event.days)
                    _debugMessage.value = "Seeded ${event.days} days of synthetic test entries!"
                }
            }

            is SettingsUiEvent.ClearAllDebugEntries -> {
                viewModelScope.launch(Dispatchers.IO) {
                    DebugDataSeeder.clearAllEntries(moodDao)
                    _debugMessage.value = "Cleared all entries from database!"
                }
            }

            is SettingsUiEvent.ExportDataJson -> {
                // Handled via SAF exportLauncher in SettingsScreen
            }

            is SettingsUiEvent.ClearExportedDataMessage -> {
                _snackbarMessage.value = null
            }

            is SettingsUiEvent.ClearDebugMessage -> {
                _debugMessage.value = null
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(context.applicationContext)
                val repository = MoodRepositoryImpl(database.moodDao(), database.customTagDao(), database.analyticsDao())
                val preferencesRepository = UserPreferencesRepository(context.applicationContext)
                return SettingsViewModel(database.moodDao(), repository, preferencesRepository) as T
            }
        }
    }
}
