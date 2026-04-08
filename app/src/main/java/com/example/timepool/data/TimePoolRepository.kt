package com.example.timepool.data

import kotlinx.coroutines.flow.Flow

class TimePoolRepository(private val dao: TimePoolDao) {
    val allTemplates: Flow<List<Template>> = dao.getAllTemplates()
    val allCategories: Flow<List<Category>> = dao.getAllCategories()

    fun getBlocksForDate(date: String): Flow<List<TimeBlock>> = dao.getBlocksForDate(date)
    fun getBlocksForDates(dates: List<String>): Flow<List<TimeBlock>> = dao.getBlocksForDates(dates)

    suspend fun insertBlock(block: TimeBlock) = dao.insertBlock(block)
    suspend fun insertBlocks(blocks: List<TimeBlock>) = dao.insertBlocks(blocks)
    suspend fun updateBlock(block: TimeBlock) = dao.updateBlock(block)
    suspend fun deleteBlock(block: TimeBlock) = dao.deleteBlock(block)
    suspend fun deleteBlocksByIds(ids: List<String>) = dao.deleteBlocksByIds(ids)

    suspend fun insertTemplate(template: Template) = dao.insertTemplate(template)
    suspend fun updateTemplate(template: Template) = dao.updateTemplate(template)
    suspend fun deleteTemplate(template: Template) = dao.deleteTemplate(template)

    suspend fun insertCategory(category: Category) = dao.insertCategory(category)
    suspend fun updateCategory(category: Category) = dao.updateCategory(category)
    suspend fun deleteCategory(category: Category) = dao.deleteCategory(category)
}
