package com.example.timepool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LayoutGrid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.timepool.ui.TimePoolViewModel
import com.example.timepool.ui.TimePoolViewModelFactory
import com.example.timepool.ui.screens.CategoryScreen
import com.example.timepool.ui.screens.DashboardScreen
import com.example.timepool.ui.screens.TemplateScreen
import com.example.timepool.ui.theme.TimePoolTheme

class MainActivity : ComponentActivity() {
    private val viewModel: TimePoolViewModel by viewModels {
        TimePoolViewModelFactory((application as TimePoolApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimePoolTheme {
                TimePoolApp(viewModel)
            }
        }
    }
}

@Composable
fun TimePoolApp(viewModel: TimePoolViewModel) {
    val navController = rememberNavController()
    var currentDestination by remember { mutableStateOf(AppDestinations.DASHBOARD) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = { Icon(it.icon, contentDescription = it.label) },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { 
                        currentDestination = it
                        navController.navigate(it.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(navController = navController, startDestination = AppDestinations.DASHBOARD.route) {
                composable(AppDestinations.DASHBOARD.route) { 
                    DashboardScreen(viewModel) 
                }
                composable(AppDestinations.TEMPLATES.route) { 
                    TemplateScreen(viewModel) 
                }
                composable(AppDestinations.CATEGORIES.route) { 
                    CategoryScreen(viewModel) 
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    DASHBOARD("时间池", Icons.Default.Home, "dashboard"),
    TEMPLATES("预置模板", Icons.Default.LayoutGrid, "templates"),
    CATEGORIES("分类管理", Icons.Default.Tag, "categories"),
}