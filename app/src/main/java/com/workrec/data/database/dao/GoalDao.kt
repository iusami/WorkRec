package com.workrec.data.database.dao

import androidx.room.*
import com.workrec.data.database.entities.GoalEntity
import com.workrec.domain.entities.GoalType
import kotlinx.coroutines.flow.Flow

/**
 * 目標データアクセスオブジェクト
 */
@Dao
interface GoalDao {
    
    /**
     * すべての目標を取得（作成日降順）
     */
    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>
    
    /**
     * アクティブ（未完了）な目標を取得
     */
    @Query("SELECT * FROM goals WHERE isCompleted = 0 ORDER BY createdAt DESC")
    fun getActiveGoals(): Flow<List<GoalEntity>>
    
    /**
     * 完了済みの目標を取得
     */
    @Query("SELECT * FROM goals WHERE isCompleted = 1 ORDER BY createdAt DESC")
    fun getCompletedGoals(): Flow<List<GoalEntity>>
    
    /**
     * 指定したIDの目標を取得
     */
    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Long): GoalEntity?
    
    /**
     * 指定したタイプの目標を取得
     */
    @Query("SELECT * FROM goals WHERE type = :type ORDER BY createdAt DESC")
    suspend fun getGoalsByType(type: GoalType): List<GoalEntity>
    
    /**
     * 目標を挿入
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long
    
    /**
     * 目標を更新
     */
    @Update
    suspend fun updateGoal(goal: GoalEntity)
    
    /**
     * 目標を削除
     */
    @Delete
    suspend fun deleteGoal(goal: GoalEntity)
    
    /**
     * 指定したIDの目標を削除
     */
    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoalById(id: Long)
    
    /**
     * 目標の進捗を更新
     */
    @Query("UPDATE goals SET currentValue = :currentValue WHERE id = :id")
    suspend fun updateGoalProgress(id: Long, currentValue: Double)
    
    /**
     * 目標を完了状態に変更
     */
    @Query("UPDATE goals SET isCompleted = 1 WHERE id = :id")
    suspend fun markGoalAsCompleted(id: Long)
    
    /**
     * アクティブな目標数を取得
     */
    @Query("SELECT COUNT(*) FROM goals WHERE isCompleted = 0")
    suspend fun getActiveGoalCount(): Int
}