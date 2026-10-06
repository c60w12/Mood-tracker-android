package com.example.newproject30.data.repository

import com.example.newproject30.data.local.dao.MoodDao
import com.example.newproject30.data.local.entity.MoodEntry
import kotlinx.coroutines.flow.Flow

/**
 * Concrete implementation of [MoodRepository] delegating to [MoodDao].
 */
class MoodRepositoryImpl(
    private val moodDao: MoodDao
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
}
