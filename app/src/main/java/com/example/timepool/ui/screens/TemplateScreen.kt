package com.example.timepool.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.timepool.ui.TimePoolViewModel
import com.example.timepool.ui.components.GlassCard

@Composable
fun TemplateScreen(viewModel: TimePoolViewModel) {
    val templates by viewModel.allTemplates.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                if (categories.isNotEmpty()) {
                    viewModel.addTemplate("新任务", 1f, categories[0].id)
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Template")
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("默认模板管理", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(templates) { tpl ->
                    GlassCard {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = tpl.name,
                                    onValueChange = { viewModel.updateTemplate(tpl.copy(name = it)) },
                                    label = { Text("名称") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = tpl.duration.toString(),
                                        onValueChange = { viewModel.updateTemplate(tpl.copy(duration = it.toFloatOrNull() ?: 0f)) },
                                        label = { Text("时长 (h)") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    categories.forEach { cat ->
                                        FilterChip(
                                            selected = tpl.categoryId == cat.id,
                                            onClick = { viewModel.updateTemplate(tpl.copy(categoryId = cat.id)) },
                                            label = { Text(cat.name, style = MaterialTheme.typography.labelSmall) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(android.graphics.Color.parseColor(cat.color)).copy(alpha = 0.3f),
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                            IconButton(onClick = { viewModel.deleteTemplate(tpl) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }
    }
}
