package com.workrec.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * アプリの画面定義
 * Navigation Compose 3.0の型安全なナビゲーションを使用
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
}