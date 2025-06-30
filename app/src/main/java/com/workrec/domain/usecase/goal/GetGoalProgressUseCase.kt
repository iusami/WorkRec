package com.workrec.domain.usecase.goal

import com.workrec.domain.entities.Goal
import com.workrec.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
// import javax.inject.Inject

/**
 * 目標進捗取得のユースケース
 */
class GetGoalProgressUseCase constructor(
    private val goalRepository: GoalRepository
) {
    /**
     * すべての目標を取得
     */
    fun getAllGoals(): Flow<List<Goal>> {
        return goalRepository.getAllGoals()
    }
    
    /**
     * アクティブな目標を取得
     */
    fun getActiveGoals(): Flow<List<Goal>> {
        return goalRepository.getActiveGoals()
    }
    
    /**
     * 完了済みの目標を取得
     */
    fun getCompletedGoals(): Flow<List<Goal>> {
        return goalRepository.getCompletedGoals()
    }
    
    /**
     * 指定したIDの目標を取得
     */
    suspend fun getGoalById(id: Long): Goal? {
        return goalRepository.getGoalById(id)
    }
    
    /**
     * 期限切れの目標を取得
     */
    suspend fun getOverdueGoals(): List<Goal> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val allGoals = goalRepository.getActiveGoals()
        
        // Flowから最新の値を取得する必要があるため、suspend関数として実装
        // 実際の実装では、リポジトリに期限切れ目標を取得するメソッドを追加することを推奨
        throw NotImplementedError("期限切れ目標の取得は、リポジトリレベルで実装する必要があります")
    }
    
    /**
     * 目標の進捗率を計算
     */
    fun calculateProgress(goal: Goal): Float {
        return goal.progressPercentage
    }
    
    /**
     * 目標達成まで必要な残り値を計算
     */
    fun calculateRemainingValue(goal: Goal): Double {
        return goal.remainingValue
    }
}