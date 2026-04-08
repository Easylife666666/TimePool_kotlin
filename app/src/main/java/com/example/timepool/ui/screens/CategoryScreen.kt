package com.example.timepool.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
fun CategoryScreen(viewModel: TimePoolViewModel) {
    val categories by viewModel.allCategories.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.addCategory("新分类", "#7E5BEF") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("分类词条管理", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(categories) { cat ->
                    GlassCard {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            // Color Preview
                            val parsedColor = remember(cat.color) { 
                                try { Color(android.graphics.Color.parseColor(cat.color)) } catch (e: Exception) { Color.Gray } 
                            }
                            Box(modifier = Modifier.size(32.dp).background(parsedColor, CircleShape))
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = cat.name,
                                    onValueChange = { viewModel.updateCategory(cat.copy(name = it)) },
                                    label = { Text("名称") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = cat.color,
                                    onValueChange = { viewModel.updateCategory(cat.copy(color = it)) },
                                    label = { Text("颜色 (HEX)") },
                                    placeholder = { Text("#RRGGBB") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            
                            IconButton(onClick = { viewModel.deleteCategory(cat) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }
    }
}
