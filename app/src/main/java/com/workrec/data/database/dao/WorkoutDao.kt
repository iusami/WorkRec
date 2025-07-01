package com.workrec.data.database.dao

import androidx.room.*
import com.workrec.data.database.entities.WorkoutEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * ワークアウトデータアクセスオブジェクト
 */
@Dao
interface WorkoutDao {
    
    /**
     * すべてのワークアウトを日付降順で取得
     */
    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>
    
    /**
     * 指定したIDのワークアウトを取得
     */
    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutById(id: Long): WorkoutEntity?
    
    /**
     * 指定したIDのワークアウトを取得（Flow版）
     */
    @Query("SELECT * FROM workouts WHERE id = :id")
    fun getWorkoutByIdFlow(id: Long): Flow<WorkoutEntity?>
    
    /**
     * 指定した日付のワークアウトを取得
     */
    @Query("SELECT * FROM workouts WHERE date = :date ORDER BY id DESC")
    suspend fun getWorkoutsByDate(date: LocalDate): List<WorkoutEntity>
    
    /**
     * 指定した期間のワークアウトを取得
     */
    @Query("SELECT * FROM workouts WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getWorkoutsByDateRange(startDate: LocalDate, endDate: LocalDate): List<WorkoutEntity>
    
    /**
     * 指定した期間のワークアウトを取得（Flow版）
     */
    @Query("SELECT * FROM workouts WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getWorkoutsByDateRangeFlow(startDate: LocalDate, endDate: LocalDate): Flow<List<WorkoutEntity>>
    
    /**
     * ワークアウトを挿入
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntity): Long
    
    /**
     * ワークアウトを更新
     */
    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)
    
    /**
     * ワークアウトを削除
     */
    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)
    
    /**
     * 指定したIDのワークアウトを削除
     */
    @Query("DELETE FROM workouts WHERE id = :id")
    suspend fun deleteWorkoutById(id: Long)
    
    /**
     * ワークアウト数を取得
     */
    @Query("SELECT COUNT(*) FROM workouts")
    suspend fun getWorkoutCount(): Int
    
    /**
     * 指定期間のワークアウト数を取得
     */
    @Query("SELECT COUNT(*) FROM workouts WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getWorkoutCountInRange(startDate: LocalDate, endDate: LocalDate): Int
}