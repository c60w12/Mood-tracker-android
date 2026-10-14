package com.example.newproject30.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.newproject30.data.local.dao.AnalyticsDao
import com.example.newproject30.data.local.dao.CustomTagDao
import com.example.newproject30.data.local.dao.MoodDao
import com.example.newproject30.data.local.entity.CustomTag
import com.example.newproject30.data.local.entity.MoodEntry

/**
 * Main Room Database configuration for the application.
 */
@Database(
    entities = [MoodEntry::class, CustomTag::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun moodDao(): MoodDao
    abstract fun customTagDao(): CustomTagDao
    abstract fun analyticsDao(): AnalyticsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mood_note_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
