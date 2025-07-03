package com.workrec.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * アプリの画面定義
 * 文字列ルートと型安全なナビゲーションの両方に対応
 */
sealed interface Screen {
    
    @Serializable
    object WorkoutList : Screen
    
    @Serializable
    data class WorkoutDetail(val workoutId: Long) : Screen
    
    @Serializable
    object AddWorkout : Screen
    
    @Serializable
    object Calendar : Screen
    
    @Serializable
    object Progress : Screen
    
    @Serializable
    object GoalList : Screen
    
    @Serializable
    data class GoalDetail(val goalId: Long) : Screen
    
    @Serializable
    object AddGoal : Screen
    
    @Serializable
    object ExerciseManager : Screen
    
    @Serializable
    object Settings : Screen
}

/**
 * 文字列ルート定数（Navigation Compose 2.7.x互換）
 */
object Routes {
    const val WORKOUT_LIST = "workout_list"
    const val WORKOUT_DETAIL = "workout_detail/{workoutId}"
    const val ADD_WORKOUT = "add_workout"
    const val CALENDAR = "calendar"
    const val PROGRESS = "progress"
    const val GOAL_LIST = "goal_list"
    const val GOAL_DETAIL = "goal_detail/{goalId}"
    const val ADD_GOAL = "add_goal"
    const val EXERCISE_MANAGER = "exercise_manager"
    const val SETTINGS = "settings"
}