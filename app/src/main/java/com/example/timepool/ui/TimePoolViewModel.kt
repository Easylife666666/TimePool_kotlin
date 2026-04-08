package com.example.timepool.ui

import androidx.lifecycle.*
import com.example.timepool.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TimePoolViewModel(private val repository: TimePoolRepository) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private val _weekRange = MutableStateFlow<List<String>>(emptyList())
    val weekRange: StateFlow<List<String>> = _weekRange

    val allCategories = repository.allCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTemplates = repository.allTemplates.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Map of date string to list of blocks
    private val _dailyBlocks = MutableStateFlow<Map<String, List<TimeBlock>>>(emptyMap())
    val dailyBlocks: StateFlow<Map<String, List<TimeBlock>>> = _dailyBlocks

    init {
        updateWeekRange()
        observeBlocks()
    }

    fun updateWeekRange() {
        val now = LocalDateTime.now()
        val referenceDay = if (now.hour < 1) now.minusDays(1).toLocalDate() else now.toLocalDate()
        val range = (0..7).map { referenceDay.plusDays(it.toLong()).format(dateFormatter) }
        _weekRange.value = range
    }

    private val populatingDates = mutableSetOf<String>()

    private fun observeBlocks() {
        viewModelScope.launch {
            weekRange.collectLatest { range ->
                if (range.isEmpty()) return@collectLatest
                repository.getBlocksForDates(range).collect { blocks ->
                    _dailyBlocks.value = blocks.groupBy { it.date }
                    // Auto-populate logic REMOVED to respect manual deletions
                }
            }
        }
    }

    private fun populateDayWithTemplates(date: String) {
        // We use a simpler guard for manual triggers or ensure it doesn't get stuck
        viewModelScope.launch {
            if (populatingDates.contains(date)) return@launch
            populatingDates.add(date)
            try {
                // Ensure we have templates loaded
                var templates = allTemplates.value
                if (templates.isEmpty()) {
                    // Quick wait for flow emission if it's currently empty
                    kotlinx.coroutines.withTimeoutOrNull(1000) {
                        repository.allTemplates.collect { 
                            if (it.isNotEmpty()) {
                                templates = it
                                throw kotlinx.coroutines.CancellationException() // break collect
                            }
                        }
                    }
                }
                
                if (templates.isNotEmpty()) {
                    val blocks = templates.map { tpl ->
                        TimeBlock(
                            name = tpl.name,
                            duration = tpl.duration,
                            categoryId = tpl.categoryId,
                            date = date,
                            priority = tpl.priority,
                            note = "来自模板"
                        )
                    }
                    repository.insertBlocks(blocks)
                }
            } finally {
                kotlinx.coroutines.delay(300)
                populatingDates.remove(date)
            }
        }
    }

    // Stats
    val weeklyStats = combine(dailyBlocks, weekRange, allCategories) { blocks, range, categories ->
        val stats = mutableMapOf<String, Float>()
        var totalUsed = 0f
        var totalPassed = 0f
        var totalRemaining = 0f

        // The first day in range is always our "Logic Today"
        val todayStr = range.firstOrNull() ?: ""
        
        range.forEach { date ->
            val dayBlocks = blocks[date] ?: emptyList()
            val dayPassed = if (date == todayStr) getPassedHours() else 0f
            totalPassed += dayPassed
            
            var dayUsed = 0f
            dayBlocks.forEach { b ->
                val effective = (b.duration - b.completedTime).coerceAtLeast(0f)
                stats[b.categoryId] = (stats[b.categoryId] ?: 0f) + effective
                dayUsed += effective
            }
            totalUsed += dayUsed
            totalRemaining += (24f - dayUsed - dayPassed).coerceAtLeast(0f)
        }
        
        WeeklyStats(totalUsed, totalPassed, totalRemaining, stats)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeeklyStats())

    // Updated stats logic using Time Pool's definition of "Today"
    private fun getPassedHours(): Float {
        val now = LocalDateTime.now()
        // If it's before 1 AM, the "Logic Day" started at 1 AM yesterday
        val logicDayStart = if (now.hour < 1) {
            now.minusDays(1).withHour(1).withMinute(0).withSecond(0).withNano(0)
        } else {
            now.withHour(1).withMinute(0).withSecond(0).withNano(0)
        }
        val diffSeconds = java.time.Duration.between(logicDayStart, now).seconds
        return (diffSeconds / 3600f).coerceAtLeast(0f)
    }

    // Actions
    fun addBlock(date: String, name: String, duration: Float, categoryId: String) {
        viewModelScope.launch {
            repository.insertBlock(TimeBlock(name = name, duration = duration, categoryId = categoryId, date = date))
        }
    }

    fun updateBlock(block: TimeBlock) {
        viewModelScope.launch { repository.updateBlock(block) }
    }

    fun deleteBlock(block: TimeBlock) {
        viewModelScope.launch { repository.deleteBlock(block) }
    }

    fun deleteBlocks(ids: List<String>) {
        viewModelScope.launch { repository.deleteBlocksByIds(ids) }
    }

    fun applyTemplatesToDay(date: String) {
        populateDayWithTemplates(date)
    }

    fun applyTemplatesToWeek() {
        weekRange.value.forEach { populateDayWithTemplates(it) }
    }

    // Category Actions
    fun addCategory(name: String, color: String) {
        viewModelScope.launch { repository.insertCategory(Category(name = name, color = color)) }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch { repository.updateCategory(category) }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch { repository.deleteCategory(category) }
    }

    // Template Actions
    fun addTemplate(name: String, duration: Float, categoryId: String) {
        viewModelScope.launch { repository.insertTemplate(Template(name = name, duration = duration, categoryId = categoryId)) }
    }

    fun updateTemplate(template: Template) {
        viewModelScope.launch { repository.updateTemplate(template) }
    }

    fun deleteTemplate(template: Template) {
        viewModelScope.launch { repository.deleteTemplate(template) }
    }
}

data class WeeklyStats(
    val totalUsed: Float = 0f,
    val totalPassed: Float = 0f,
    val totalRemaining: Float = 0f,
    val categoryHours: Map<String, Float> = emptyMap()
)

class TimePoolViewModelFactory(private val repository: TimePoolRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimePoolViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimePoolViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
