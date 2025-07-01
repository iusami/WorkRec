package com.workrec.domain.usecase.goal

import com.workrec.domain.entities.Goal
import com.workrec.domain.entities.GoalProgressRecord
import com.workrec.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import javax.inject.Inject

/**
 * 目標詳細取得のユースケース
 */
class GetGoalDetailUseCase @Inject constructor(
    private val goalRepository: GoalRepository
) {
    /**
     * 目標詳細情報を取得する（リアルタイム）
     * @param goalId 目標ID
     * @return 目標と進捗記録のFlow
     */
    operator fun invoke(goalId: Long): Flow<GoalDetailData?> {
        val goalFlow = goalRepository.getAllGoals().map { goals ->
            goals.find { it.id == goalId }
        }
        val progressFlow = goalRepository.getProgressByGoalId(goalId)
        
        return combine(goalFlow, progressFlow) { goal, progressRecords ->
            goal?.let {
                GoalDetailData(
                    goal = it,
                    progressRecords = progressRecords,
                    progressStats = calculateProgressStats(progressRecords),
                    recentProgress = progressRecords.take(7) // 最新7件
                )
            }
        }
    }
    
    /**
     * 目標の最新進捗記録を取得する
     * @param goalId 目標ID
     * @return 最新進捗記録のFlow
     */
    fun getLatestProgress(goalId: Long): Flow<GoalProgressRecord?> {
        return goalRepository.getLatestProgressByGoalId(goalId)
    }
    
    /**
     * 指定期間の進捗記録を取得する
     * @param goalId 目標ID
     * @param days 過去何日分を取得するか
     * @return 進捗記録のFlow
     */
    fun getProgressForPeriod(goalId: Long, days: Int = 30): Flow<List<GoalProgressRecord>> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val startDate = today.minus(DatePeriod(days = days))
        return goalRepository.getProgressByGoalIdAndDateRange(goalId, startDate, today)
    }
    
    /**
     * 進捗統計を計算する
     */
    private fun calculateProgressStats(progressRecords: List<GoalProgressRecord>): ProgressStats {
        if (progressRecords.isEmpty()) {
            return ProgressStats(
                totalRecords = 0,
                averageProgress = 0.0,
                bestProgress = 0.0,
                worstProgress = 0.0,
                latestProgress = 0.0,
                improvementRate = 0.0
            )
        }
        
        val values = progressRecords.map { it.progressValue }
        val latest = progressRecords.first().progressValue
        val oldest = progressRecords.last().progressValue
        
        return ProgressStats(
            totalRecords = progressRecords.size,
            averageProgress = values.average(),
            bestProgress = values.maxOrNull() ?: 0.0,
            worstProgress = values.minOrNull() ?: 0.0,
            latestProgress = latest,
            improvementRate = if (oldest != 0.0) {
                ((latest - oldest) / oldest) * 100
            } else 0.0
        )
    }
}

/**
 * 目標詳細データ
 */
data class GoalDetailData(
    val goal: Goal,
    val progressRecords: List<GoalProgressRecord>,
    val progressStats: ProgressStats,
    val recentProgress: List<GoalProgressRecord>
)

/**
 * 進捗統計
 */
data class ProgressStats(
    val totalRecords: Int,
    val averageProgress: Double,
    val bestProgress: Double,
    val worstProgress: Double,
    val latestProgress: Double,
    val improvementRate: Double // 改善率（%）
)