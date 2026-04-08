package com.example.timepool.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timepool.data.TimeBlock
import com.example.timepool.ui.TimePoolViewModel
import com.example.timepool.ui.components.GlassCard
import java.time.LocalDate

@Composable
fun DashboardScreen(viewModel: TimePoolViewModel) {
    val weekRange by viewModel.weekRange.collectAsState()
    val dailyBlocks by viewModel.dailyBlocks.collectAsState()
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    var selectedIds by remember { mutableStateOf(setOf<String>()) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 600.dp
        
        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                Sidebar(
                    stats = weeklyStats, 
                    categories = categories,
                    selectedIds = selectedIds, 
                    onCancel = { selectedIds = emptySet() }, 
                    onDelete = { 
                        viewModel.deleteBlocks(selectedIds.toList())
                        selectedIds = emptySet()
                    },
                    onApplyWeek = { viewModel.applyTemplatesToWeek() },
                    onSelectAllWeek = {
                        val allIds = dailyBlocks.values.flatten().map { it.id }.toSet()
                        selectedIds = if (selectedIds.size == allIds.size) emptySet() else allIds
                    }
                )
                MainGrid(weekRange, dailyBlocks, categories, selectedIds, { id -> 
                    selectedIds = if (selectedIds.contains(id)) selectedIds - id else selectedIds + id
                }, { ids ->
                    selectedIds = if (selectedIds.containsAll(ids)) selectedIds - ids else selectedIds + ids
                }, viewModel, Modifier.weight(1f))
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Sidebar(
                    stats = weeklyStats, 
                    categories = categories,
                    selectedIds = selectedIds, 
                    onCancel = { selectedIds = emptySet() }, 
                    onDelete = { 
                        viewModel.deleteBlocks(selectedIds.toList())
                        selectedIds = emptySet()
                    },
                    onApplyWeek = { viewModel.applyTemplatesToWeek() },
                    onSelectAllWeek = {
                        val allIds = dailyBlocks.values.flatten().map { it.id }.toSet()
                        selectedIds = if (selectedIds.size == allIds.size) emptySet() else allIds
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                MainGrid(weekRange, dailyBlocks, categories, selectedIds, { id -> 
                    selectedIds = if (selectedIds.contains(id)) selectedIds - id else selectedIds + id
                }, { ids ->
                    selectedIds = if (selectedIds.containsAll(ids)) selectedIds - ids else selectedIds + ids
                }, viewModel, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun Sidebar(
    stats: com.example.timepool.ui.WeeklyStats, 
    categories: List<com.example.timepool.data.Category>,
    selectedIds: Set<String>, 
    onCancel: () -> Unit, 
    onDelete: () -> Unit,
    onApplyWeek: () -> Unit,
    onSelectAllWeek: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .then(if (modifier == Modifier) Modifier.width(280.dp) else Modifier)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("星期概览", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            IconButton(onClick = onApplyWeek, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Apply Week", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onSelectAllWeek, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.SelectAll, contentDescription = "Select All Week", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        GlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatRow("规划总量", "${"%.1f".format(stats.totalUsed)}h", modifier = Modifier.weight(1f))
                    StatRow("本周剩余", "${"%.1f".format(stats.totalRemaining)}h", color = Color(0xFF00FF88), modifier = Modifier.weight(1f))
                }
                
                // Weekly Pool Remaining Bar (User suggestion: Full 24h*8, empties over time)
                val totalCapacity = 8 * 24f
                val weekPct = (stats.totalRemaining / totalCapacity).coerceIn(0f, 1f)
                
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("全周剩余筹码", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("${"%.1f".format(weekPct * 100)}%", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00FF88))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { weekPct },
                        modifier = Modifier.fillMaxWidth().height(6.dp).border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(3.dp)),
                        color = Color(0xFF00FF88),
                        trackColor = Color.White.copy(alpha = 0.05f),
                        strokeCap = StrokeCap.Round
                    )
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.05f))
                
                categories.forEach { cat ->
                    val hours = stats.categoryHours[cat.id] ?: 0f
                    val pct = if (stats.totalUsed > 0) hours / stats.totalUsed else 0f
                    if (hours > 0) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(cat.name, style = MaterialTheme.typography.labelSmall, color = parseHexColor(cat.color))
                                Text("${"%.1f".format(hours)}h", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            LinearProgressIndicator(
                                progress = { pct },
                                modifier = Modifier.fillMaxWidth().height(2.dp),
                                color = parseHexColor(cat.color),
                                trackColor = Color.White.copy(alpha = 0.05f)
                            )
                        }
                    }
                }
            }
        }
        
        if (selectedIds.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            GlassCard(modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))) {
                Text("已选中 ${selectedIds.size} 项", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onCancel, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)), contentPadding = PaddingValues(0.dp)) {
                        Text("取消", fontSize = MaterialTheme.typography.bodySmall.fontSize)
                    }
                    Button(onClick = onDelete, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.6f)), contentPadding = PaddingValues(0.dp)) {
                        Text("删除", fontSize = MaterialTheme.typography.bodySmall.fontSize)
                    }
                }
            }
        }
    }
}

@Composable
fun MainGrid(
    weekRange: List<String>, 
    dailyBlocks: Map<String, List<TimeBlock>>, 
    categories: List<com.example.timepool.data.Category>,
    selectedIds: Set<String>,
    onToggleSelect: (String) -> Unit,
    onSelectAll: (Set<String>) -> Unit,
    viewModel: TimePoolViewModel,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        items(weekRange) { date ->
            val dayBlocks = dailyBlocks[date] ?: emptyList()
            DayCard(
                date = date,
                blocks = dayBlocks,
                categories = categories,
                selectedIds = selectedIds,
                onToggleSelect = onToggleSelect,
                onSelectAll = { onSelectAll(dayBlocks.map { it.id }.toSet()) },
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun DayCard(
    date: String, 
    blocks: List<TimeBlock>, 
    categories: List<com.example.timepool.data.Category>, 
    selectedIds: Set<String>,
    onToggleSelect: (String) -> Unit,
    onSelectAll: () -> Unit,
    viewModel: TimePoolViewModel
) {
    val isToday = date == viewModel.weekRange.value.firstOrNull()
    val isAllSelected = blocks.isNotEmpty() && blocks.all { selectedIds.contains(it.id) }
    
    var showAddDialog by remember { mutableStateOf(false) }
    
    val used = blocks.sumOf { (it.duration - it.completedTime).toDouble().coerceAtLeast(0.0) }.toFloat()
    
    val now = java.time.LocalDateTime.now()
    val passedHours = if (isToday) {
        val logicDayStart = if (now.hour < 1) {
            now.minusDays(1).withHour(1).withMinute(0).withSecond(0).withNano(0)
        } else {
            now.withHour(1).withMinute(0).withSecond(0).withNano(0)
        }
        val diffSeconds = java.time.Duration.between(logicDayStart, now).seconds
        (diffSeconds / 3600f).coerceAtLeast(0f)
    } else 0f
    
    val remaining = (24f - used - passedHours).coerceAtLeast(0f)

    if (showAddDialog) {
        AddBlockDialog(onDismiss = { showAddDialog = false }, onAdd = { name, duration, catId ->
            viewModel.addBlock(date, name, duration, catId)
            showAddDialog = false
        }, categories = categories)
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onSelectAll, modifier = Modifier.size(24.dp)) {
                    Icon(
                        if (isAllSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Select All",
                        tint = if (isAllSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(if (isToday) "今天" else date.substring(5).replace("-", "/"), style = MaterialTheme.typography.titleMedium)
                    Text("规划: ${"%.1f".format(used)}h | 剩余: ${"%.1f".format(remaining)}h", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.applyTemplatesToDay(date) }) {
                    Icon(Icons.Default.FlashOn, contentDescription = "Apply Template", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Block")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val usedPct = used / 24f
            val passedPct = passedHours / 24f
            
            LinearProgressIndicator(
                progress = { remaining / 24f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.White.copy(alpha = 0.05f),
                strokeCap = StrokeCap.Round
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            blocks.forEach { block ->
                val category = categories.find { it.id == block.categoryId }
                BlockItem(
                    block = block, 
                    category = category, 
                    isSelected = selectedIds.contains(block.id),
                    onToggleSelect = { onToggleSelect(block.id) },
                    onUpdate = { viewModel.updateBlock(it) }, 
                    onDelete = { viewModel.deleteBlock(it) }
                )
            }
        }
    }
}

fun parseHexColor(hex: String?): Color {
    if (hex == null) return Color.Gray
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
}

@Composable
fun BlockItem(
    block: TimeBlock, 
    category: com.example.timepool.data.Category?, 
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onUpdate: (TimeBlock) -> Unit, 
    onDelete: (TimeBlock) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    
    if (showEditDialog) {
        EditBlockDialog(block = block, onDismiss = { showEditDialog = false }, onUpdate = { onUpdate(it); showEditDialog = false })
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (isSelected) onToggleSelect() else showEditDialog = true }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(12.dp).background(
            parseHexColor(category?.color), 
            shape = RoundedCornerShape(2.dp)
        ))
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(block.name, style = MaterialTheme.typography.bodyMedium, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified)
            val remaining = (block.duration - block.completedTime).coerceAtLeast(0f)
            Text(if (block.completedTime > 0) "${"%.1f".format(remaining)}h / ${block.duration}h" else "${block.duration}h", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        } else {
            IconButton(onClick = onToggleSelect) {
                Icon(Icons.Default.RadioButtonUnchecked, contentDescription = "Select", tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
            }
        }
        
        IconButton(onClick = { onDelete(block) }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun EditBlockDialog(block: TimeBlock, onDismiss: () -> Unit, onUpdate: (TimeBlock) -> Unit) {
    var name by remember { mutableStateOf(block.name) }
    var duration by remember { mutableStateOf(block.duration.toString()) }
    var completed by remember { mutableStateOf(block.completedTime.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑时间块") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("名称") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("总时长") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = completed, onValueChange = { completed = it }, label = { Text("已完成") }, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                onUpdate(block.copy(name = name, duration = duration.toFloatOrNull() ?: block.duration, completedTime = completed.toFloatOrNull() ?: block.completedTime))
            }) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
fun AddBlockDialog(onDismiss: () -> Unit, onAdd: (String, Float, String) -> Unit, categories: List<com.example.timepool.data.Category>) {
    var name by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("1.0") }
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加时间块") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("名称") })
                OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("时长 (h)") })
                Text("分类", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text(cat.name, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = parseHexColor(cat.color).copy(alpha = 0.3f), selectedLabelColor = Color.White)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(name, duration.toFloatOrNull() ?: 1f, selectedCategoryId) }) { Text("添加") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

@Composable
fun StatRow(label: String, value: String, color: Color = MaterialTheme.colorScheme.primary, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodySmall, color = color)
    }
}
