package com.workrec.data.di

import com.workrec.data.repository.GoalRepositoryImpl
import com.workrec.data.repository.WorkoutRepositoryImpl
import com.workrec.domain.repository.GoalRepository
import com.workrec.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * リポジトリ関連の依存性注入モジュール
 * インターフェースと実装をバインドする
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * WorkoutRepositoryの実装をバインド
     */
    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(
        workoutRepositoryImpl: WorkoutRepositoryImpl
    ): WorkoutRepository

    /**
     * GoalRepositoryの実装をバインド
     */
    @Binds
    @Singleton
    abstract fun bindGoalRepository(
        goalRepositoryImpl: GoalRepositoryImpl
    ): GoalRepository
}