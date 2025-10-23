package com.omer.offlinereminder.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "snippet_lists")
data class SnippetListEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "frequency_minutes") val frequencyMinutes: Long,
    @ColumnInfo(name = "current_index") val currentIndex: Int = 0,
    @ColumnInfo(name = "is_active") val isActive: Boolean = true
)

@Entity(tableName = "snippets")
data class SnippetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "list_id") val listId: Int,
    val text: String,
    @ColumnInfo(name = "order_index") val orderIndex: Int
)
