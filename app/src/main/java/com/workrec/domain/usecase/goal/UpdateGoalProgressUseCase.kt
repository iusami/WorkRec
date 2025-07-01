package com.workrec.domain.usecase.goal

import com.workrec.domain.entities.Goal
import com.workrec.domain.entities.GoalProgressRecord
import com.workrec.domain.repository.GoalRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * 目標進捗更新のユースケース
 */
class UpdateGoalProgressUseCase @Inject constructor(
    private val goalRepository: GoalRepository
) {
    /**
     * 目標の進捗を更新する
     * @param goalId 目標ID
     * @param progressValue 進捗値
     * @param notes 進捗記録のメモ（任意）
     * @return 更新成功時はtrue、失敗時は例外をthrow
     */
    suspend operator fun invoke(
        goalId: Long,
        progressValue: Double,
        notes: String? = null
    ): Result<Unit> {
        return try {
            // 目標の存在確認
            val goal = goalRepository.getGoalById(goalId)
                ?: throw IllegalArgumentException("指定された目標が見つかりません")
            
            // 進捗値の検証
            validateProgressValue(progressValue, goal)
            
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            
            // 進捗記録を保存
            val progressRecord = GoalProgressRecord(
                goalId = goalId,
                recordDate = today,
                progressValue = progressValue,
                notes = notes?.trim(),
                createdAt = today
            )
            goalRepository.saveProgressRecord(progressRecord)
            
            // 目標の現在値を更新
            goalRepository.updateGoalProgress(goalId, progressValue)
            
            // 目標達成チェック
            if (progressValue >= goal.targetValue && !goal.isCompleted) {
                goalRepository.markGoalAsCompleted(goalId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 進捗値のバリデーション
     */
    private fun validateProgressValue(progressValue: Double, goal: Goal) {
        // 進捗値が負の値でないことを確認
        if (progressValue < 0) {
            throw IllegalArgumentException("進捗値は0以上である必要があります")
        }
        
        // 目標タイプに応じた追加検証
        when (goal.type) {
            com.workrec.domain.entities.GoalType.WEIGHT_LOSS -> {
                // 体重減少目標では、進捗値が目標値より小さいことが期待される
                // ただし、柔軟性のため警告のみとする
            }
            com.workrec.domain.entities.GoalType.BODY_FAT -> {
                // 体脂肪率は0-100%の範囲内である必要がある
                if (progressValue > 100) {
                    throw IllegalArgumentException("体脂肪率は100%以下である必要があります")
                }
            }
            else -> {
                // その他の目標タイプでは特別な制約なし
            }
        }
    }
}