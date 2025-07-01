package com.workrec.domain.repository

import com.workrec.domain.entities.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * ワークアウトデータアクセスの抽象化インターフェース
 * データ層の実装詳細をドメイン層から隠蔽する
 */
interface WorkoutRepository {
    
    /**
     * すべてのワークアウトを取得（日付降順）
     */
    fun getAllWorkouts(): Flow<List<Workout>>
    
    /**
     * 指定したIDのワークアウトを取得
     */
    suspend fun getWorkoutById(id: Long): Workout?
    
    /**
     * 指定したIDのワークアウトを取得（Flow版）
     */
    fun getWorkoutByIdFlow(id: Long): Flow<Workout?>
    
    /**
     * 指定した日付のワークアウトを取得
     */
    suspend fun getWorkoutsByDate(date: LocalDate): List<Workout>
    
    /**
     * 指定した期間のワークアウトを取得
     */
    suspend fun getWorkoutsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Workout>
    
    /**
     * 指定した期間のワークアウトを取得（Flow版）
     */
    fun getWorkoutsByDateRangeFlow(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<Workout>>
    
    /**
     * ワークアウトを保存または更新
     */
    suspend fun saveWorkout(workout: Workout): Long
    
    /**
     * ワークアウトを削除
     */
    suspend fun deleteWorkout(workout: Workout)
    
    /**
     * 指定したIDのワークアウトを削除
     */
    suspend fun deleteWorkoutById(id: Long)
    
    /**
     * エクササイズ名で検索
     */
    suspend fun searchWorkoutsByExerciseName(exerciseName: String): List<Workout>
    
    /**
     * ワークアウト統計情報を取得
     */
    suspend fun getWorkoutStats(
        startDate: LocalDate,
        endDate: LocalDate
    ): WorkoutStats
}

/**
 * ワークアウト統計情報
 */
data class WorkoutStats(
    val totalWorkouts: Int,
    val totalVolume: Double,
    val totalSets: Int,
    val averageWorkoutDuration: kotlin.time.Duration?,
    val mostFrequentExercises: List<String>
)