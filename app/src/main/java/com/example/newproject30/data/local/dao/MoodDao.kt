package com.example.newproject30.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.newproject30.data.local.entity.MoodEntry
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for [MoodEntry] database operations.
 */
@Dao
interface MoodDao {

    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarkedEntries(): Flow<List<MoodEntry>>

    /**
     * Exact tokenized tag search matching `"tag"` in the serialized JSON array to prevent
     * substring collisions (e.g., searching for "Art" matching "Party").
     */
    @Query("SELECT * FROM mood_entries WHERE tags LIKE '%\"' || :tag || '\"%' ORDER BY timestamp DESC")
    fun getEntriesByCategory(tag: String): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getEntriesBetween(startTime: Long, endTime: Long): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): MoodEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: MoodEntry): Long

    @Update
    suspend fun updateEntry(entry: MoodEntry)

    @Delete
    suspend fun deleteEntry(entry: MoodEntry)

    @Query("DELETE FROM mood_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)
}
