package com.example.timepool.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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

    Row(modifier = Modifier.fillMaxSize()) {
        // Stats Sidebar
        Column(
            modifier = Modifier
                .width(280.dp)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            Text("星期概览", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            GlassCard {
                StatRow("规划总量", "${"%.1f".format(weeklyStats.totalUsed)}h")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.05f))
                StatRow("本周剩余", "${"%.1f".format(weeklyStats.totalRemaining)}h", color = Color(0xFF00FF88))
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            if (selectedIds.isNotEmpty()) {
                GlassCard(modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))) {
                    Text("已选中 ${selectedIds.size} 项", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { selectedIds = emptySet() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                        ) {
                            Text("取消", fontSize = MaterialTheme.typography.bodySmall.fontSize)
                        }
                        Button(
                            onClick = { 
                                viewModel.deleteBlocks(selectedIds.toList())
                                selectedIds = emptySet()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.6f))
                        ) {
                            Text("删除", fontSize = MaterialTheme.typography.bodySmall.fontSize)
                        }
                    }
                }
            }
        }

        // Days Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(weekRange) { date ->
                val dayBlocks = dailyBlocks[date] ?: emptyList()
                DayCard(
                    date = date,
                    blocks = dayBlocks,
                    categories = categories,
                    selectedIds = selectedIds,
                    onToggleSelect = { id ->
                        selectedIds = if (selectedIds.contains(id)) selectedIds - id else selectedIds + id
                    },
                    onSelectAll = {
                        val ids = dayBlocks.map { it.id }.toSet()
                        selectedIds = if (selectedIds.containsAll(ids)) selectedIds - ids else selectedIds + ids
                    },
                    viewModel = viewModel
                )
            }
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
    val isToday = date == LocalDate.now().toString()
    val isAllSelected = blocks.isNotEmpty() && blocks.all { selectedIds.contains(it.id) }
    
    var showAddDialog by remember { mutableStateOf(false) }
    
    if (showAddDialog) {
        AddBlockDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, duration, catId ->
                viewModel.addBlock(date, name, duration, catId)
                showAddDialog = false
            },
            categories = categories
        )
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
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
            Text(if (isToday) "今天" else date.substring(5).replace("-", "/"), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { viewModel.applyTemplatesToDay(date) }) {
                Icon(Icons.Default.Bolt, contentDescription = "Apply Template", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Block")
            }
        }
        
        // Progress Bar
        val used = blocks.sumOf { (it.duration - it.completedTime).toDouble().coerceAtLeast(0.0) }.toFloat()
        LinearProgressIndicator(
            progress = { used / 24f },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color.White.copy(alpha = 0.05f)
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

@Composable
fun BlockItem(
    block: TimeBlock, 
    category: com.example.timepool.data.Category?, 
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onUpdate: (TimeBlock) -> Unit, 
    onDelete: (TimeBlock) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleSelect() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(12.dp).background(
            Color(android.graphics.Color.parseColor(category?.color ?: "#FFFFFF")), 
            shape = RoundedCornerShape(2.dp)
        ))
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                block.name, 
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified
            )
            Text("${block.duration}h", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = { onDelete(block) }) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun AddBlockDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Float, String) -> Unit,
    categories: List<com.example.timepool.data.Category>
) {
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text(cat.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(android.graphics.Color.parseColor(cat.color)).copy(alpha = 0.3f),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(name, duration.toFloatOrNull() ?: 1f, selectedCategoryId) }) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun StatRow(label: String, value: String, color: Color = MaterialTheme.colorScheme.primary) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodySmall, color = color)
    }
}
