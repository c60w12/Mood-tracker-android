package com.example.newproject30.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.newproject30.data.local.entity.CustomTag
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [CustomTag] operations.
 */
@Dao
interface CustomTagDao {

    @Query("SELECT * FROM custom_tags ORDER BY isDefault DESC, name ASC")
    fun getAllCustomTags(): Flow<List<CustomTag>>

    @Query("SELECT * FROM custom_tags WHERE category = :category ORDER BY isDefault DESC, name ASC")
    fun getCustomTagsByCategory(category: String): Flow<List<CustomTag>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomTag(tag: CustomTag)

    @Delete
    suspend fun deleteCustomTag(tag: CustomTag)

    @Query("DELETE FROM custom_tags WHERE id = :id")
    suspend fun deleteCustomTagById(id: String)
}
