package com.example.timepool.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.util.UUID

@Entity(tableName = "time_blocks")
data class TimeBlock(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val duration: Float, // in hours
    val completedTime: Float = 0f,
    val categoryId: String,
    val date: String, // yyyy-MM-dd
    val priority: Int = 1,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "templates")
data class Template(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val duration: Float,
    val categoryId: String,
    val priority: Int = 1,
    val note: String = ""
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: String // Hex string like #7e5bef
)
