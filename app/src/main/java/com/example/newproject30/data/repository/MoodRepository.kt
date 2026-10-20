package com.example.newproject30.data.repository

import com.example.newproject30.data.local.dao.EmotionCount
import com.example.newproject30.data.local.dao.MoodStats
import com.example.newproject30.data.local.entity.CustomTag
import com.example.newproject30.data.local.entity.MoodEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface exposing pure Kotlin Flows for Mood & Note data, Custom Tags, and Analytics.
 */
interface MoodRepository {

    /**
     * Retrieves all mood entries sorted by timestamp descending.
     */
    fun getAllEntries(): Flow<List<MoodEntry>>

    /**
     * Retrieves all bookmarked mood entries sorted by timestamp descending.
     */
    fun getBookmarkedEntries(): Flow<List<MoodEntry>>

    /**
     * Retrieves entries matching a specific category/tag.
     */
    fun getEntriesByCategory(tag: String): Flow<List<MoodEntry>>

    /**
     * Retrieves entries recorded between [startTime] and [endTime].
     */
    fun getEntriesBetween(startTime: Long, endTime: Long): Flow<List<MoodEntry>>

    /**
     * Retrieves a single entry by its unique ID.
     */
    suspend fun getEntryById(id: Long): MoodEntry?

    /**
     * Inserts or replaces a mood entry in the database.
     * @return The auto-generated row ID.
     */
    suspend fun insertEntry(entry: MoodEntry): Long

    /**
     * Updates an existing mood entry in the database.
     */
    suspend fun updateEntry(entry: MoodEntry)

    /**
     * Deletes a mood entry from the database.
     */
    suspend fun deleteEntry(entry: MoodEntry)

    /**
     * Deletes a mood entry by its unique ID.
     */
    suspend fun deleteEntryById(id: Long)

    /**
     * Deletes all mood entries from the database.
     */
    suspend fun deleteAllEntries()

    /**
     * Retrieves all default and user-created custom emotion & contextual tags.
     */
    fun getAllCustomTags(): Flow<List<CustomTag>>

    /**
     * Retrieves custom tags filtered by category ("EMOTION" or "CONTEXT").
     */
    fun getCustomTagsByCategory(category: String): Flow<List<CustomTag>>

    /**
     * Inserts or updates a custom tag in the local Room database.
     */
    suspend fun insertCustomTag(tag: CustomTag)

    /**
     * Deletes a custom tag from the database.
     */
    suspend fun deleteCustomTag(tag: CustomTag)

    /**
     * Targeted SQL aggregation query for primary emotion distribution counts.
     */
    fun getEmotionCountsSince(sinceTimestamp: Long): Flow<List<EmotionCount>>

    /**
     * Targeted SQL aggregation query for mood score stats (avg, min, max, totalCount).
     */
    fun getMoodStatsSince(sinceTimestamp: Long): Flow<MoodStats>

    /**
     * Targeted SQL query for raw scores since [sinceTimestamp] for variance calculation.
     */
    fun getMoodScoresSince(sinceTimestamp: Long): Flow<List<Int>>

    /**
     * Targeted SQL query for distinct timestamps for streak calculation.
     */
    fun getAllTimestamps(): Flow<List<Long>>

    /**
     * Targeted SQL query for total check-in count.
     */
    fun getTotalCheckInCount(): Flow<Int>
}
