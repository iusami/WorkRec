package com.workrec.data.di

import android.content.Context
import androidx.room.Room
import com.workrec.data.database.WorkoutDatabase
import com.workrec.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * データベース関連の依存性注入モジュール
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * WorkoutDatabaseのプロバイダー
     */
    @Provides
    @Singleton
    fun provideWorkoutDatabase(
        @ApplicationContext 
        context: Context
    ): WorkoutDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            WorkoutDatabase::class.java,
            WorkoutDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration() // 開発中のみ使用
        .build()
    }

    /**
     * WorkoutDaoのプロバイダー
     */
    @Provides
    fun provideWorkoutDao(database: WorkoutDatabase): WorkoutDao {
        return database.workoutDao()
    }

    /**
     * ExerciseDaoのプロバイダー
     */
    @Provides
    fun provideExerciseDao(database: WorkoutDatabase): ExerciseDao {
        return database.exerciseDao()
    }

    /**
     * ExerciseSetDaoのプロバイダー
     */
    @Provides
    fun provideExerciseSetDao(database: WorkoutDatabase): ExerciseSetDao {
        return database.exerciseSetDao()
    }

    /**
     * GoalDaoのプロバイダー
     */
    @Provides
    fun provideGoalDao(database: WorkoutDatabase): GoalDao {
        return database.goalDao()
    }
}