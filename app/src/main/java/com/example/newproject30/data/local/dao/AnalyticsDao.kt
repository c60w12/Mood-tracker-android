package com.example.newproject30.data.local.dao

import androidx.compose.runtime.Immutable
import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

/**
 * Data class representing grouped emotion counts from Room SQL query.
 */
@Immutable
@Serializable
data class EmotionCount(
    val primaryEmotion: String,
    val count: Int
)

/**
 * Data class representing SQL aggregated stats for mood scores.
 */
@Immutable
@Serializable
data class MoodStats(
    val avgScore: Double?,
    val minScore: Int?,
    val maxScore: Int?,
    val totalCount: Int
)

/**
 * Room DAO dedicated to targeted SQL aggregation queries for 100% offline analytics.
 */
@Dao
interface AnalyticsDao {

    /**
     * Aggregates primary emotion counts grouped by emotion name since [sinceTimestamp].
     */
    @Query("""
        SELECT primaryEmotion, COUNT(*) as count 
        FROM mood_entries 
        WHERE timestamp >= :sinceTimestamp 
        GROUP BY primaryEmotion 
        ORDER BY count DESC
    """)
    fun getEmotionCountsSince(sinceTimestamp: Long): Flow<List<EmotionCount>>

    /**
     * Aggregates avg, min, max mood scores and total entry count since [sinceTimestamp].
     */
    @Query("""
        SELECT AVG(moodScore) as avgScore, MIN(moodScore) as minScore, MAX(moodScore) as maxScore, COUNT(*) as totalCount 
        FROM mood_entries 
        WHERE timestamp >= :sinceTimestamp
    """)
    fun getMoodStatsSince(sinceTimestamp: Long): Flow<MoodStats>

    /**
     * Fetches all mood scores recorded since [sinceTimestamp] for standard deviation / variance stability computation.
     */
    @Query("SELECT moodScore FROM mood_entries WHERE timestamp >= :sinceTimestamp")
    fun getMoodScoresSince(sinceTimestamp: Long): Flow<List<Int>>

    /**
     * Fetches distinct entry timestamps ordered descending for streak calculation.
     */
    @Query("SELECT timestamp FROM mood_entries ORDER BY timestamp DESC")
    fun getAllTimestamps(): Flow<List<Long>>

    /**
     * Counts total entries in database.
     */
    @Query("SELECT COUNT(*) FROM mood_entries")
    fun getTotalCheckInCount(): Flow<Int>
}
