package com.gympilot.ironlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gympilot.ironlog.ui.ProgressScreen
import com.gympilot.ironlog.ui.WorkoutScreen
import com.gympilot.ironlog.ui.theme.IronLogTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IronLogTheme {
                IronLogApp()
            }
        }
    }
}

@Composable
private fun IronLogApp() {
    val navController = rememberNavController()
    val tabs = listOf(
        IronTab("workout", "Workout", Icons.Filled.FitnessCenter),
        IronTab("progress", "Progress", Icons.Filled.Insights)
    )
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: "workout"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE8E8EC))
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                tabs.forEach { tab ->
                    BottomNavItem(
                        tab = tab,
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = "workout") {
            composable("workout") { WorkoutScreen(padding) }
            composable("progress") { ProgressScreen(padding) }
        }
    }
}

@Composable
private fun BottomNavItem(tab: IronTab, selected: Boolean, onClick: () -> Unit) {
    val activeGreen = Color(0xFF18A84F)
    val inactive = Color(0xFF858698)
    Surface(onClick = onClick, color = Color.Transparent) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier.size(42.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Surface(shape = CircleShape, color = Color(0xFFDDF1E5), modifier = Modifier.size(40.dp)) {}
                }
                Icon(
                    tab.icon,
                    contentDescription = tab.label,
                    tint = if (selected) activeGreen else inactive,
                    modifier = Modifier.size(25.dp)
                )
            }
            Text(
                tab.label,
                color = if (selected) activeGreen else inactive,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

private data class IronTab(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
