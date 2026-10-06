package com.example.newproject30.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room TypeConverters for converting complex types to database primitive types.
 */
class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isBlank()) {
            emptyList()
        } else {
            try {
                Json.decodeFromString(value)
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}
