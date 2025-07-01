package com.workrec.domain.usecase.goal

import com.workrec.domain.repository.GoalRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 目標削除のユースケース
 */
class DeleteGoalUseCase @Inject constructor(
    private val goalRepository: GoalRepository
) {
    /**
     * 指定された目標を削除する
     * @param goalId 削除する目標のID
     * @return 削除成功時はtrue、失敗時は例外をthrow
     */
    suspend operator fun invoke(goalId: Long): Result<Unit> {
        return try {
            // 目標の存在確認
            val goal = goalRepository.getGoalById(goalId)
                ?: throw IllegalArgumentException("指定された目標が見つかりません")
            
            // 関連する進捗記録を先に削除
            goalRepository.deleteAllProgressByGoalId(goalId)
            
            // 目標を削除
            goalRepository.deleteGoalById(goalId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 複数の目標を一括削除する
     * @param goalIds 削除する目標のIDリスト
     * @return 削除成功時はtrue、失敗時は例外をthrow
     */
    suspend fun deleteMultiple(goalIds: List<Long>): Result<Unit> {
        return try {
            goalIds.forEach { goalId ->
                // 各目標を個別に削除（トランザクション処理は将来的にリポジトリ層で実装）
                invoke(goalId).getOrThrow()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 完了済みの目標をすべて削除する
     * @return 削除成功時はtrue、失敗時は例外をthrow
     */
    suspend fun deleteAllCompleted(): Result<Int> {
        return try {
            var deletedCount = 0
            
            // 完了済みの目標を取得して削除
            // Note: Flowから一度だけ値を取得するためfirst()を使用
            val completedGoals = goalRepository.getCompletedGoals().first()
            completedGoals.forEach { goal ->
                invoke(goal.id).getOrThrow()
                deletedCount++
            }
            
            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}