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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.workrec.R
import com.workrec.presentation.ui.screens.workout.WorkoutListScreen
import com.workrec.presentation.ui.screens.workout.AddWorkoutScreen
import com.workrec.presentation.ui.screens.workout.WorkoutDetailScreen
import com.workrec.presentation.ui.screens.calendar.CalendarScreen
import com.workrec.presentation.ui.screens.progress.ProgressScreen
import com.workrec.presentation.ui.screens.goal.GoalScreen
import com.workrec.presentation.ui.screens.goal.AddGoalScreen
import com.workrec.presentation.ui.screens.goal.GoalDetailScreen
import com.workrec.presentation.ui.screens.exercise.ExerciseManagerScreen
import com.workrec.presentation.viewmodel.AddWorkoutViewModel
import com.workrec.presentation.viewmodel.AddGoalViewModel
import com.workrec.presentation.viewmodel.GoalDetailViewModel
import com.workrec.presentation.viewmodel.GoalViewModel
import com.workrec.presentation.viewmodel.WorkoutViewModel

/**
 * アプリのメインナビゲーション - Hilt DI対応
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
            startDestination = Routes.WORKOUT_LIST,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ワークアウト一覧画面
            composable(Routes.WORKOUT_LIST) {
                val workoutViewModel: WorkoutViewModel = hiltViewModel()
                WorkoutListScreen(
                    onNavigateToAddWorkout = {
                        navController.navigate(Routes.ADD_WORKOUT)
                    },
                    onNavigateToWorkoutDetail = { workoutId ->
                        navController.navigate("workout_detail/$workoutId")
                    },
                    viewModel = workoutViewModel
                )
            }
            
            // ワークアウト詳細画面
            composable(
                route = Routes.WORKOUT_DETAIL,
                arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
            ) { backStackEntry ->
                WorkoutDetailScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToEdit = { editWorkoutId ->
                        navController.navigate(Routes.ADD_WORKOUT) {
                            // 編集モードでAddWorkoutScreenを開く
                            // 実際の編集機能は将来的にAddWorkoutScreenで実装予定
                        }
                    }
                )
            }
            
            // ワークアウト追加画面
            composable(Routes.ADD_WORKOUT) {
                val addWorkoutViewModel: AddWorkoutViewModel = hiltViewModel()
                AddWorkoutScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToExerciseManager = {
                        navController.navigate(Routes.EXERCISE_MANAGER)
                    },
                    viewModel = addWorkoutViewModel
                )
            }
            
            // カレンダー画面
            composable(Routes.CALENDAR) {
                CalendarScreen()
            }
            
            // 進捗画面
            composable(Routes.PROGRESS) {
                ProgressScreen()
            }
            
            // 目標一覧画面
            composable(Routes.GOAL_LIST) {
                val goalViewModel: GoalViewModel = hiltViewModel()
                GoalScreen(
                    onNavigateToAddGoal = {
                        navController.navigate(Routes.ADD_GOAL)
                    },
                    onNavigateToGoalDetail = { goalId ->
                        navController.navigate("goal_detail/$goalId")
                    },
                    viewModel = goalViewModel
                )
            }
            
            // 目標詳細画面
            composable(
                route = Routes.GOAL_DETAIL,
                arguments = listOf(navArgument("goalId") { type = NavType.LongType })
            ) { backStackEntry ->
                val goalId = backStackEntry.arguments?.getLong("goalId") ?: 0L
                GoalDetailScreen(
                    goalId = goalId,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onEditGoal = { editGoalId ->
                        navController.navigate(Routes.ADD_GOAL) {
                            // 編集モードでAddGoalScreenを開く（将来的に実装予定）
                        }
                    },
                    onAddProgress = { progressGoalId ->
                        // 進捗追加機能（将来的に実装予定）
                    }
                )
            }
            
            // 目標追加画面
            composable(Routes.ADD_GOAL) {
                val addGoalViewModel: AddGoalViewModel = hiltViewModel()
                AddGoalScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onGoalAdded = {
                        navController.popBackStack()
                    },
                    viewModel = addGoalViewModel
                )
            }
            
            // エクササイズ管理画面
            composable(Routes.EXERCISE_MANAGER) {
                ExerciseManagerScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
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
                selected = currentDestination?.route == item.route,
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
    val route: String,
    val icon: ImageVector,
    val label: String
)

private val bottomNavItems = listOf(
    BottomNavItem(
        route = Routes.WORKOUT_LIST,
        icon = Icons.Default.FitnessCenter,
        label = "ワークアウト"
    ),
    BottomNavItem(
        route = Routes.PROGRESS,
        icon = Icons.Default.ShowChart,
        label = "進捗"
    ),
    BottomNavItem(
        route = Routes.CALENDAR,
        icon = Icons.Default.CalendarMonth,
        label = "カレンダー"
    ),
    BottomNavItem(
        route = Routes.GOAL_LIST,
        icon = Icons.Default.TrackChanges,
        label = "目標"
    )
)