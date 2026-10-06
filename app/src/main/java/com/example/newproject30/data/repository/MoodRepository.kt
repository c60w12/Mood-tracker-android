package com.example.newproject30.data.repository

import com.example.newproject30.data.local.entity.MoodEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface exposing pure Kotlin Flows for Mood & Note data.
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
}
