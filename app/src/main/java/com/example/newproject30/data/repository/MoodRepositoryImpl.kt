package com.example.newproject30.data.repository

import com.example.newproject30.data.local.dao.AnalyticsDao
import com.example.newproject30.data.local.dao.CustomTagDao
import com.example.newproject30.data.local.dao.EmotionCount
import com.example.newproject30.data.local.dao.MoodDao
import com.example.newproject30.data.local.dao.MoodStats
import com.example.newproject30.data.local.entity.CustomTag
import com.example.newproject30.data.local.entity.MoodEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

private val DEFAULT_EMOTION_TAGS = listOf(
    "Calm",
    "Anxious",
    "Grateful",
    "Exhausted",
    "Energetic",
    "Reflective",
    "Focused",
    "Overwhelmed"
)

private val DEFAULT_CONTEXT_TAGS = listOf(
    "Work",
    "Wellness",
    "Mindfulness",
    "Ideas",
    "Reflections"
)

/**
 * Concrete implementation of [MoodRepository] delegating to [MoodDao], [CustomTagDao], and [AnalyticsDao].
 */
class MoodRepositoryImpl(
    private val moodDao: MoodDao,
    private val customTagDao: CustomTagDao,
    private val analyticsDao: AnalyticsDao
) : MoodRepository {

    override fun getAllEntries(): Flow<List<MoodEntry>> {
        return moodDao.getAllEntries()
    }

    override fun getBookmarkedEntries(): Flow<List<MoodEntry>> {
        return moodDao.getBookmarkedEntries()
    }

    override fun getEntriesByCategory(tag: String): Flow<List<MoodEntry>> {
        return moodDao.getEntriesByCategory(tag)
    }

    override fun getEntriesBetween(startTime: Long, endTime: Long): Flow<List<MoodEntry>> {
        return moodDao.getEntriesBetween(startTime, endTime)
    }

    override suspend fun getEntryById(id: Long): MoodEntry? {
        return moodDao.getEntryById(id)
    }

    override suspend fun insertEntry(entry: MoodEntry): Long {
        return moodDao.insertEntry(entry)
    }

    override suspend fun updateEntry(entry: MoodEntry) {
        moodDao.updateEntry(entry)
    }

    override suspend fun deleteEntry(entry: MoodEntry) {
        moodDao.deleteEntry(entry)
    }

    override suspend fun deleteEntryById(id: Long) {
        moodDao.deleteEntryById(id)
    }

    override fun getAllCustomTags(): Flow<List<CustomTag>> {
        return customTagDao.getAllCustomTags().onStart {
            ensureDefaultTagsExist()
        }
    }

    override fun getCustomTagsByCategory(category: String): Flow<List<CustomTag>> {
        return customTagDao.getCustomTagsByCategory(category).onStart {
            ensureDefaultTagsExist()
        }
    }

    override suspend fun insertCustomTag(tag: CustomTag) {
        customTagDao.insertCustomTag(tag)
    }

    override suspend fun deleteCustomTag(tag: CustomTag) {
        customTagDao.deleteCustomTag(tag)
    }

    override fun getEmotionCountsSince(sinceTimestamp: Long): Flow<List<EmotionCount>> {
        return analyticsDao.getEmotionCountsSince(sinceTimestamp)
    }

    override fun getMoodStatsSince(sinceTimestamp: Long): Flow<MoodStats> {
        return analyticsDao.getMoodStatsSince(sinceTimestamp)
    }

    override fun getMoodScoresSince(sinceTimestamp: Long): Flow<List<Int>> {
        return analyticsDao.getMoodScoresSince(sinceTimestamp)
    }

    override fun getAllTimestamps(): Flow<List<Long>> {
        return analyticsDao.getAllTimestamps()
    }

    override fun getTotalCheckInCount(): Flow<Int> {
        return analyticsDao.getTotalCheckInCount()
    }

    private suspend fun ensureDefaultTagsExist() {
        DEFAULT_EMOTION_TAGS.forEach { name ->
            customTagDao.insertCustomTag(
                CustomTag(
                    id = "default_emotion_$name",
                    name = name,
                    isDefault = true,
                    category = "EMOTION"
                )
            )
        }
        DEFAULT_CONTEXT_TAGS.forEach { name ->
            customTagDao.insertCustomTag(
                CustomTag(
                    id = "default_context_$name",
                    name = name,
                    isDefault = true,
                    category = "CONTEXT"
                )
            )
        }
    }
}
