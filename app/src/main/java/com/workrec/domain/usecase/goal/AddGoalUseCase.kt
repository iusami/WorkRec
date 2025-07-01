package com.workrec.domain.usecase.goal

import com.workrec.domain.entities.Goal
import com.workrec.domain.entities.GoalType
import com.workrec.domain.repository.GoalRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * 目標追加のユースケース
 */
class AddGoalUseCase @Inject constructor(
    private val goalRepository: GoalRepository
) {
    /**
     * 新しい目標を追加する
     * @param type 目標のタイプ
     * @param title 目標のタイトル
     * @param description 目標の説明（任意）
     * @param targetValue 目標値
     * @param unit 単位
     * @param deadline 期限（任意）
     * @return 追加された目標のID、失敗時はnull
     */
    suspend operator fun invoke(
        type: GoalType,
        title: String,
        description: String? = null,
        targetValue: Double,
        unit: String,
        deadline: kotlinx.datetime.LocalDate? = null
    ): Result<Long> {
        return try {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            
            val goal = Goal(
                type = type,
                title = title.trim(),
                description = description?.trim(),
                targetValue = targetValue,
                currentValue = 0.0,
                unit = unit.trim(),
                deadline = deadline,
                isCompleted = false,
                createdAt = today,
                updatedAt = today
            )
            
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
        
        // 期限がある場合、未来の日付であることを確認
        goal.deadline?.let { deadline ->
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            if (deadline <= today) {
                throw IllegalArgumentException("期限は今日より後の日付である必要があります")
            }
        }
    }
}