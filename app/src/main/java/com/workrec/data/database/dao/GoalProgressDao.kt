package com.workrec.data.database.dao

import androidx.room.*
import com.workrec.data.database.entities.GoalProgressEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * 目標進捗データアクセスオブジェクト
 */
@Dao
interface GoalProgressDao {
    
    /**
     * 指定目標の全進捗記録を取得（日付降順）
     */
    @Query("SELECT * FROM goal_progress WHERE goalId = :goalId ORDER BY recordDate DESC")
    fun getProgressByGoalIdFlow(goalId: Long): Flow<List<GoalProgressEntity>>
    
    /**
     * 指定目標の全進捗記録を取得（一回限り）
     */
    @Query("SELECT * FROM goal_progress WHERE goalId = :goalId ORDER BY recordDate DESC")
    suspend fun getProgressByGoalId(goalId: Long): List<GoalProgressEntity>
    
    /**
     * 指定目標の最新進捗記録を取得
     */
    @Query("SELECT * FROM goal_progress WHERE goalId = :goalId ORDER BY recordDate DESC LIMIT 1")
    suspend fun getLatestProgressByGoalId(goalId: Long): GoalProgressEntity?
    
    /**
     * 指定目標の最新進捗記録を取得（リアルタイム）
     */
    @Query("SELECT * FROM goal_progress WHERE goalId = :goalId ORDER BY recordDate DESC LIMIT 1")
    fun getLatestProgressByGoalIdFlow(goalId: Long): Flow<GoalProgressEntity?>
    
    /**
     * 指定目標の指定期間の進捗記録を取得
     */
    @Query("SELECT * FROM goal_progress WHERE goalId = :goalId AND recordDate BETWEEN :startDate AND :endDate ORDER BY recordDate DESC")
    suspend fun getProgressByGoalIdAndDateRange(goalId: Long, startDate: LocalDate, endDate: LocalDate): List<GoalProgressEntity>
    
    /**
     * 指定目標の指定期間の進捗記録を取得（リアルタイム）
     */
    @Query("SELECT * FROM goal_progress WHERE goalId = :goalId AND recordDate BETWEEN :startDate AND :endDate ORDER BY recordDate DESC")
    fun getProgressByGoalIdAndDateRangeFlow(goalId: Long, startDate: LocalDate, endDate: LocalDate): Flow<List<GoalProgressEntity>>
    
    /**
     * 全ての進捗記録を取得
     */
    @Query("SELECT * FROM goal_progress ORDER BY recordDate DESC")
    fun getAllProgressFlow(): Flow<List<GoalProgressEntity>>
    
    /**
     * 指定IDの進捗記録を取得
     */
    @Query("SELECT * FROM goal_progress WHERE id = :progressId")
    suspend fun getProgressById(progressId: Long): GoalProgressEntity?
    
    /**
     * 指定目標の進捗記録数を取得
     */
    @Query("SELECT COUNT(*) FROM goal_progress WHERE goalId = :goalId")
    suspend fun getProgressCountByGoalId(goalId: Long): Int
    
    /**
     * 指定目標の特定日の進捗記録を取得
     */
    @Query("SELECT * FROM goal_progress WHERE goalId = :goalId AND recordDate = :date")
    suspend fun getProgressByGoalIdAndDate(goalId: Long, date: LocalDate): GoalProgressEntity?
    
    /**
     * 指定目標の平均進捗値を取得
     */
    @Query("SELECT AVG(progressValue) FROM goal_progress WHERE goalId = :goalId")
    suspend fun getAverageProgressByGoalId(goalId: Long): Double?
    
    /**
     * 指定目標の最大進捗値を取得
     */
    @Query("SELECT MAX(progressValue) FROM goal_progress WHERE goalId = :goalId")
    suspend fun getMaxProgressByGoalId(goalId: Long): Double?
    
    /**
     * 進捗記録を挿入
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: GoalProgressEntity): Long
    
    /**
     * 複数の進捗記録を挿入
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressList(progressList: List<GoalProgressEntity>)
    
    /**
     * 進捗記録を更新
     */
    @Update
    suspend fun updateProgress(progress: GoalProgressEntity)
    
    /**
     * 進捗記録を削除
     */
    @Delete
    suspend fun deleteProgress(progress: GoalProgressEntity)
    
    /**
     * 指定IDの進捗記録を削除
     */
    @Query("DELETE FROM goal_progress WHERE id = :progressId")
    suspend fun deleteProgressById(progressId: Long)
    
    /**
     * 指定目標の全進捗記録を削除
     */
    @Query("DELETE FROM goal_progress WHERE goalId = :goalId")
    suspend fun deleteProgressByGoalId(goalId: Long)
    
    /**
     * 全ての進捗記録を削除
     */
    @Query("DELETE FROM goal_progress")
    suspend fun deleteAllProgress()
}