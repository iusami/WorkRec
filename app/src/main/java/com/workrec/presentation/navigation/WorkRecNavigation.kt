package com.workrec.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.workrec.R
import com.workrec.presentation.ui.screens.workout.WorkoutListScreen
import com.workrec.presentation.ui.screens.calendar.CalendarScreen
import com.workrec.presentation.ui.screens.progress.ProgressScreen
import com.workrec.presentation.ui.screens.goal.GoalScreen

/**
 * アプリのメインナビゲーション
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkRecNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            WorkRecBottomNavigation(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.WorkoutList,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ワークアウト一覧画面
            composable<Screen.WorkoutList> {
                WorkoutListScreen(
                    onNavigateToAddWorkout = {
                        navController.navigate(Screen.AddWorkout)
                    },
                    onNavigateToWorkoutDetail = { workoutId ->
                        navController.navigate(Screen.WorkoutDetail(workoutId))
                    }
                )
            }
            
            // ワークアウト詳細画面
            composable<Screen.WorkoutDetail> { backStackEntry ->
                val workoutDetail: Screen.WorkoutDetail = backStackEntry.toRoute()
                // TODO: ワークアウト詳細画面の実装
            }
            
            // ワークアウト追加画面
            composable<Screen.AddWorkout> {
                // TODO: ワークアウト追加画面の実装
            }
            
            // カレンダー画面
            composable<Screen.Calendar> {
                CalendarScreen()
            }
            
            // 進捗画面
            composable<Screen.Progress> {
                ProgressScreen()
            }
            
            // 目標一覧画面
            composable<Screen.GoalList> {
                GoalScreen(
                    onNavigateToAddGoal = {
                        navController.navigate(Screen.AddGoal)
                    },
                    onNavigateToGoalDetail = { goalId ->
                        navController.navigate(Screen.GoalDetail(goalId))
                    }
                )
            }
            
            // 目標詳細画面
            composable<Screen.GoalDetail> { backStackEntry ->
                val goalDetail: Screen.GoalDetail = backStackEntry.toRoute()
                // TODO: 目標詳細画面の実装
            }
            
            // 目標追加画面
            composable<Screen.AddGoal> {
                // TODO: 目標追加画面の実装
            }
        }
    }
}

/**
 * ボトムナビゲーションバー
 */
@Composable
fun WorkRecBottomNavigation(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route::class.qualifiedName } == true,
                onClick = {
                    navController.navigate(item.route) {
                        // ボトムナビゲーションの標準的な動作
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

/**
 * ボトムナビゲーションのアイテム定義
 */
data class BottomNavItem(
    val route: Screen,
    val icon: ImageVector,
    val label: String
)

private val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.WorkoutList,
        icon = Icons.Default.FitnessCenter,
        label = "ワークアウト"
    ),
    BottomNavItem(
        route = Screen.Progress,
        icon = Icons.Default.ShowChart,
        label = "進捗"
    ),
    BottomNavItem(
        route = Screen.Calendar,
        icon = Icons.Default.CalendarMonth,
        label = "カレンダー"
    ),
    BottomNavItem(
        route = Screen.GoalList,
        icon = Icons.Default.TrackChanges,
        label = "目標"
    )
)