package com.workrec.domain.usecase.goal

import com.workrec.domain.entities.Goal
import com.workrec.domain.repository.GoalRepository
import javax.inject.Inject

/**
 * 目標設定のユースケース
 */
class SetGoalUseCase @Inject constructor(
    private val goalRepository: GoalRepository
) {
    /**
     * 目標を設定する
     * @param goal 設定する目標
     * @return 設定された目標のID、失敗時はnull
     */
    suspend operator fun invoke(goal: Goal): Result<Long> {
        return try {
            // ビジネスルールの検証
            validateGoal(goal)
            
            // リポジトリを通じてデータを保存
            val goalId = goalRepository.saveGoal(goal)
            Result.success(goalId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 目標のバリデーション
     */
    private fun validateGoal(goal: Goal) {
        // タイトルが空でないことを確認
        if (goal.title.isBlank()) {
            throw IllegalArgumentException("目標のタイトルが必要です")
        }
        
        // 目標値が正の値であることを確認
        if (goal.targetValue <= 0) {
            throw IllegalArgumentException("目標値は0より大きい値である必要があります")
        }
        
        // 現在値が負の値でないことを確認
        if (goal.currentValue < 0) {
            throw IllegalArgumentException("現在値は0以上である必要があります")
        }
        
        // 単位が空でないことを確認
        if (goal.unit.isBlank()) {
            throw IllegalArgumentException("目標の単位が必要です")
        }
    }
}