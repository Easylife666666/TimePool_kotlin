package com.example.timepool.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TimePoolDao {
    // Blocks
    @Query("SELECT * FROM time_blocks WHERE date = :date")
    fun getBlocksForDate(date: String): Flow<List<TimeBlock>>

    @Query("SELECT * FROM time_blocks WHERE date IN (:dates)")
    fun getBlocksForDates(dates: List<String>): Flow<List<TimeBlock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: TimeBlock)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlocks(blocks: List<TimeBlock>)

    @Update
    suspend fun updateBlock(block: TimeBlock)

    @Delete
    suspend fun deleteBlock(block: TimeBlock)

    @Query("DELETE FROM time_blocks WHERE id IN (:ids)")
    suspend fun deleteBlocksByIds(ids: List<String>)

    // Templates
    @Query("SELECT * FROM templates")
    fun getAllTemplates(): Flow<List<Template>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: Template)

    @Update
    suspend fun updateTemplate(template: Template)

    @Delete
    suspend fun deleteTemplate(template: Template)

    // Categories
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)
}
