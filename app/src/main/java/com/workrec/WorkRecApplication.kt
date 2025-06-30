package com.workrec

import android.app.Application
import androidx.room.Room
import com.workrec.data.database.WorkoutDatabase
import com.workrec.data.repository.GoalRepositoryImpl
import com.workrec.data.repository.WorkoutRepositoryImpl
import com.workrec.domain.repository.GoalRepository
import com.workrec.domain.repository.WorkoutRepository
import com.workrec.domain.usecase.goal.GetGoalProgressUseCase
import com.workrec.domain.usecase.goal.SetGoalUseCase
import com.workrec.domain.usecase.workout.AddWorkoutUseCase
import com.workrec.domain.usecase.workout.DeleteWorkoutUseCase
import com.workrec.domain.usecase.workout.GetWorkoutHistoryUseCase
import dagger.hilt.android.HiltAndroidApp

/**
 * WorkRecアプリケーションクラス
 * Manual DI - 依存関係の初期化とグローバル設定を行う
 */
@HiltAndroidApp
class WorkRecApplication : Application() {
    
    // Database
    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            WorkoutDatabase::class.java,
            WorkoutDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    // Repositories
    val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepositoryImpl(
            workoutDao = database.workoutDao(),
            exerciseDao = database.exerciseDao(),
            exerciseSetDao = database.exerciseSetDao()
        )
    }
    
    val goalRepository: GoalRepository by lazy {
        GoalRepositoryImpl(
            goalDao = database.goalDao()
        )
    }
    
    // UseCases
    val addWorkoutUseCase by lazy {
        AddWorkoutUseCase(workoutRepository)
    }
    
    val getWorkoutHistoryUseCase by lazy {
        GetWorkoutHistoryUseCase(workoutRepository)
    }
    
    val deleteWorkoutUseCase by lazy {
        DeleteWorkoutUseCase(workoutRepository)
    }
    
    val setGoalUseCase by lazy {
        SetGoalUseCase(goalRepository)
    }
    
    val getGoalProgressUseCase by lazy {
        GetGoalProgressUseCase(goalRepository)
    }
}