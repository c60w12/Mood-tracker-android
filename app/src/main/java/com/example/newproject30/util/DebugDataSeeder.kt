package com.example.newproject30.util

import com.example.newproject30.data.local.dao.MoodDao
import com.example.newproject30.data.local.entity.MoodEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Random

/**
 * Debug-only utility for generating synthetic mood entries and voice transcripts over a 14 to 30 day period.
 */
object DebugDataSeeder {

    private val EMOTIONS = listOf(
        "Calm" to 4,
        "Happy" to 5,
        "Anxious" to 2,
        "Sad" to 1,
        "Grateful" to 5,
        "Exhausted" to 1,
        "Energetic" to 5,
        "Reflective" to 3,
        "Focused" to 4,
        "Overwhelmed" to 2,
        "Joyful" to 5,
        "Inspired" to 4
    )

    private val TAG_COMBINATIONS = listOf(
        listOf("Wellness", "Mindfulness"),
        listOf("Work", "Ideas"),
        listOf("Reflections"),
        listOf("Family", "Gratitude"),
        listOf("Work", "Exercise"),
        listOf("Ideas", "Mindfulness")
    )

    private val SHORT_NOTES = listOf(
        "Took a peaceful 20-minute morning walk in the park. Feeling refreshed.",
        "Productive deep work session on Kotlin coroutines and room architecture.",
        "A bit stressed about deadlines today, taking slow deep breaths.",
        "Had a wonderful dinner with family. So grateful for quiet moments.",
        "Early morning workout session gave me great energy for the day!"
    )

    private val LONG_TRANSCRIPTS = listOf(
        "Brainstormed new design concepts for the Material 3 navigation shell. We discussed using a clean Sage Green and Warm Cream color palette to keep the app serene and welcoming. I really want the voice transcription feature to feel seamless with live continuous streaming and auto-restart across pauses.",
        "Reflected on weekly wellness goals and habit tracking. The 14-day mood stability index is looking very positive. It really helps seeing the emotion distribution breakdown right on the dashboard instead of guessing how the week went.",
        "Long afternoon meeting discussing project milestones. Broke down the remaining tasks into smaller actionable steps. Feeling much more focused and ready for tomorrow."
    )

    /**
     * Generates and inserts [days] worth of synthetic mood entries into Room database.
     */
    suspend fun seedSyntheticEntries(moodDao: MoodDao, days: Int = 14) = withContext(Dispatchers.IO) {
        val random = Random(42)
        val now = System.currentTimeMillis()
        val dayMs = 24 * 3600 * 1000L

        val entries = mutableListOf<MoodEntry>()

        for (dayIndex in 0 until days) {
            val entriesToday = random.nextInt(2) + 1
            for (entryInDay in 0 until entriesToday) {
                val timeOffset = (dayIndex * dayMs) + (entryInDay * (3 * 3600 * 1000L)) + random.nextInt(3600 * 1000)
                val timestamp = now - timeOffset

                val (emotion, defaultScore) = EMOTIONS[random.nextInt(EMOTIONS.size)]
                val moodScore = (defaultScore + random.nextInt(3) - 1).coerceIn(1, 5)
                val isVoice = random.nextBoolean()
                val isBookmarked = random.nextFloat() < 0.2f

                val content = if (isVoice) {
                    LONG_TRANSCRIPTS[random.nextInt(LONG_TRANSCRIPTS.size)]
                } else {
                    SHORT_NOTES[random.nextInt(SHORT_NOTES.size)]
                }

                val extraTags = TAG_COMBINATIONS[random.nextInt(TAG_COMBINATIONS.size)]
                val combinedTags = (listOf(emotion) + extraTags).distinct()

                entries.add(
                    MoodEntry(
                        timestamp = timestamp,
                        moodScore = moodScore,
                        primaryEmotion = emotion,
                        content = content,
                        isVoiceTranscript = isVoice,
                        isBookmarked = isBookmarked,
                        tags = combinedTags
                    )
                )
            }
        }

        entries.forEach { moodDao.insertEntry(it) }
    }

    /**
     * Clears all entries from the mood database.
     */
    suspend fun clearAllEntries(moodDao: MoodDao) = withContext(Dispatchers.IO) {
        moodDao.deleteAllEntries()
    }
}
