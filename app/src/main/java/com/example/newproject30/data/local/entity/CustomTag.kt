package com.example.newproject30.data.local.entity

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Room Entity representing default and user-created custom tags.
 *
 * @param id Unique UUID identifier string.
 * @param name Display label for the tag.
 * @param isDefault Flag indicating if this is a built-in default tag.
 * @param category Tag classification ("EMOTION" for primary emotion, "CONTEXT" for secondary tag).
 */
@Immutable
@Serializable
@Entity(tableName = "custom_tags")
data class CustomTag(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isDefault: Boolean = false,
    val category: String = "EMOTION"
)
