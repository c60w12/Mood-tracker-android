package com.example.newproject30.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

/**
 * DataStore preferences repository managing Dark Mode, Custom Tags, Reminders, and Auto-Start Voice settings.
 */
class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val CUSTOM_TAGS = stringSetPreferencesKey("custom_tags")
        val DAILY_REMINDERS_ENABLED = booleanPreferencesKey("daily_reminders_enabled")
        val AUTO_START_VOICE_ON_OPEN = booleanPreferencesKey("auto_start_voice_on_open")
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_DARK_MODE] ?: false
    }

    val customTags: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.CUSTOM_TAGS] ?: setOf("Mindfulness", "Gratitude", "Journal", "Self-Care")
    }

    val dailyRemindersEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DAILY_REMINDERS_ENABLED] ?: false
    }

    val autoStartVoiceOnOpen: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_START_VOICE_ON_OPEN] ?: false
    }

    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = isDark
        }
    }

    suspend fun addCustomTag(tag: String) {
        if (tag.isBlank()) return
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.CUSTOM_TAGS] ?: setOf("Mindfulness", "Gratitude", "Journal", "Self-Care")
            preferences[PreferencesKeys.CUSTOM_TAGS] = current + tag.trim()
        }
    }

    suspend fun removeCustomTag(tag: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.CUSTOM_TAGS] ?: setOf("Mindfulness", "Gratitude", "Journal", "Self-Care")
            preferences[PreferencesKeys.CUSTOM_TAGS] = current - tag
        }
    }

    suspend fun setDailyRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DAILY_REMINDERS_ENABLED] = enabled
        }
    }

    suspend fun setAutoStartVoiceOnOpen(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_START_VOICE_ON_OPEN] = enabled
        }
    }
}
