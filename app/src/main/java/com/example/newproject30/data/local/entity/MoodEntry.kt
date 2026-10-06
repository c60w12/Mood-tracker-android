package com.example.newproject30.data.local.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Room Entity representing a single mood and note entry. Marked @Immutable for Compose stability and @Serializable for JSON export.
 *
 * @param id Unique primary key ID (auto-generated).
 * @param timestamp Epoch timestamp in milliseconds.
 * @param moodScore Mood rating on a scale of 1 to 5.
 * @param primaryEmotion Primary emotion descriptor (e.g., "Calm", "Anxious", "Joyful").
 * @param content Text note or voice transcript body.
 * @param isVoiceTranscript Flag indicating if this entry originated from a voice recording.
 * @param isBookmarked Flag indicating if the entry is bookmarked for quick access.
 * @param tags Categorization tags attached to the entry.
 */
@Immutable
@Serializable
@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long,
    val moodScore: Int,
    val primaryEmotion: String,
    val content: String,
    val isVoiceTranscript: Boolean,
    val isBookmarked: Boolean,
    val tags: List<String>
)
