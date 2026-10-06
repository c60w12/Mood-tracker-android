package com.example.newproject30.ui.settings

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.newproject30.data.local.AppDatabase
import com.example.newproject30.data.local.entity.MoodEntry
import com.example.newproject30.data.preferences.UserPreferencesRepository
import com.example.newproject30.data.reminder.ReminderScheduler
import com.example.newproject30.data.repository.MoodRepository
import com.example.newproject30.data.repository.MoodRepositoryImpl
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
 * ViewModel managing settings, Storage Access Framework JSON data exports, clear data dialogs, notification toggles, and developer test seeders.
 */
class SettingsViewModel(
    private val repository: MoodRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val _showClearDataDialog = MutableStateFlow(false)
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    private val _developerMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.dailyRemindersEnabled,
        _showClearDataDialog,
        _snackbarMessage,
        _developerMessage
    ) { remindersEnabled, showDialog, snackbarMsg, devMsg ->
        SettingsUiState(
            dailyRemindersEnabled = remindersEnabled,
            showClearDataDialog = showDialog,
            exportedDataMessage = snackbarMsg,
            appVersion = "1.0.0 (Build 100)",
            developerMessage = devMsg
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
                // Serialize data BEFORE opening output stream to prevent 0-byte corrupted file creation on error
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
                        ReminderScheduler.scheduleDailyReminder(context)
                    } else {
                        ReminderScheduler.cancelDailyReminder(context)
                    }
                }
            }

            is SettingsUiEvent.SendTestNotification -> {
                ReminderScheduler.sendTestNotification(context)
            }

            is SettingsUiEvent.ShowClearDataDialog -> {
                _showClearDataDialog.value = true
            }

            is SettingsUiEvent.DismissClearDataDialog -> {
                _showClearDataDialog.value = false
            }

            is SettingsUiEvent.ConfirmClearData -> {
                viewModelScope.launch {
                    val entries = repository.getAllEntries().first()
                    entries.forEach { repository.deleteEntry(it) }
                    _showClearDataDialog.value = false
                    _developerMessage.value = "Database cleared cleanly!"
                }
            }

            is SettingsUiEvent.ExportDataJson -> {
                // Handled via SAF exportLauncher in SettingsScreen
            }

            is SettingsUiEvent.ClearExportedDataMessage -> {
                _snackbarMessage.value = null
            }

            is SettingsUiEvent.SeedContinuousStreak -> {
                viewModelScope.launch {
                    val existing = repository.getAllEntries().first()
                    existing.forEach { repository.deleteEntry(it) }

                    val now = System.currentTimeMillis()
                    val dayMs = 24 * 3600 * 1000L
                    val continuousSample = listOf(
                        MoodEntry(timestamp = now, moodScore = 5, primaryEmotion = "Calm", content = "Day 7: Mindful morning walk in the garden.", isVoiceTranscript = false, isBookmarked = true, tags = listOf("Wellness", "Mindfulness")),
                        MoodEntry(timestamp = now - (dayMs * 1), moodScore = 4, primaryEmotion = "Focused", content = "Day 6: Productive deep work session on Android architecture.", isVoiceTranscript = true, isBookmarked = false, tags = listOf("Work", "Ideas")),
                        MoodEntry(timestamp = now - (dayMs * 2), moodScore = 3, primaryEmotion = "Reflective", content = "Day 5: Steady afternoon with quiet reading and journaling.", isVoiceTranscript = false, isBookmarked = false, tags = listOf("Reflections")),
                        MoodEntry(timestamp = now - (dayMs * 3), moodScore = 5, primaryEmotion = "Joyful", content = "Day 4: Great dinner and quality time with family.", isVoiceTranscript = false, isBookmarked = true, tags = listOf("Family")),
                        MoodEntry(timestamp = now - (dayMs * 4), moodScore = 4, primaryEmotion = "Grateful", content = "Day 3: Expressed gratitude for team collaboration.", isVoiceTranscript = false, isBookmarked = false, tags = listOf("Gratitude")),
                        MoodEntry(timestamp = now - (dayMs * 5), moodScore = 2, primaryEmotion = "Exhausted", content = "Day 2: Long day of travel and meetings.", isVoiceTranscript = true, isBookmarked = false, tags = listOf("Work")),
                        MoodEntry(timestamp = now - (dayMs * 6), moodScore = 4, primaryEmotion = "Energetic", content = "Day 1: Started the week with an early workout.", isVoiceTranscript = false, isBookmarked = false, tags = listOf("Exercise"))
                    )
                    continuousSample.forEach { repository.insertEntry(it) }
                    _developerMessage.value = "Seeded 7-Day Continuous Streak! Check Dashboard streak & analytics."
                }
            }

            is SettingsUiEvent.SeedBrokenStreak -> {
                viewModelScope.launch {
                    val existing = repository.getAllEntries().first()
                    existing.forEach { repository.deleteEntry(it) }

                    val now = System.currentTimeMillis()
                    val dayMs = 24 * 3600 * 1000L
                    val brokenSample = listOf(
                        MoodEntry(timestamp = now, moodScore = 4, primaryEmotion = "Calm", content = "Today's journal entry.", isVoiceTranscript = false, isBookmarked = false, tags = listOf("Mindfulness")),
                        MoodEntry(timestamp = now - (dayMs * 1), moodScore = 4, primaryEmotion = "Grateful", content = "Yesterday's reflection.", isVoiceTranscript = false, isBookmarked = true, tags = listOf("Gratitude")),
                        MoodEntry(timestamp = now - (dayMs * 4), moodScore = 2, primaryEmotion = "Exhausted", content = "Entry from 4 days ago.", isVoiceTranscript = false, isBookmarked = false, tags = listOf("Work")),
                        MoodEntry(timestamp = now - (dayMs * 5), moodScore = 3, primaryEmotion = "Focused", content = "Entry from 5 days ago.", isVoiceTranscript = false, isBookmarked = false, tags = listOf("Ideas"))
                    )
                    brokenSample.forEach { repository.insertEntry(it) }
                    _developerMessage.value = "Seeded Broken Streak (Skipped 2 & 3 days ago). Streak resets to 2 Days!"
                }
            }

            is SettingsUiEvent.ClearDeveloperMessage -> {
                _developerMessage.value = null
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(context.applicationContext)
                val repository = MoodRepositoryImpl(database.moodDao())
                val preferencesRepository = UserPreferencesRepository(context.applicationContext)
                return SettingsViewModel(repository, preferencesRepository) as T
            }
        }
    }
}
