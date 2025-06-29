package com.workrec.domain.repository

import com.workrec.domain.entities.Goal
import com.workrec.domain.entities.GoalType
import kotlinx.coroutines.flow.Flow

/**
 * 目標データアクセスの抽象化インターフェース
 */
interface GoalRepository {
    
    /**
     * すべての目標を取得
     */
    fun getAllGoals(): Flow<List<Goal>>
    
    /**
     * アクティブ（未完了）な目標を取得
     */
    fun getActiveGoals(): Flow<List<Goal>>
    
    /**
     * 完了済みの目標を取得
     */
    fun getCompletedGoals(): Flow<List<Goal>>
    
    /**
     * 指定したIDの目標を取得
     */
    suspend fun getGoalById(id: Long): Goal?
    
    /**
     * 指定したタイプの目標を取得
     */
    suspend fun getGoalsByType(type: GoalType): List<Goal>
    
    /**
     * 目標を保存または更新
     */
    suspend fun saveGoal(goal: Goal): Long
    
    /**
     * 目標を削除
     */
    suspend fun deleteGoal(goal: Goal)
    
    /**
     * 指定したIDの目標を削除
     */
    suspend fun deleteGoalById(id: Long)
    
    /**
     * 目標の進捗を更新
     */
    suspend fun updateGoalProgress(id: Long, currentValue: Double)
    
    /**
     * 目標を完了状態に変更
     */
    suspend fun markGoalAsCompleted(id: Long)
}