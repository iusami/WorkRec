package com.workrec.domain.usecase.progress

import com.workrec.domain.entities.ProgressMetrics
import com.workrec.domain.entities.TimePeriod
import com.workrec.domain.entities.WorkoutStatistics
import com.workrec.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * ワークアウト統計を取得するUseCase
 */
class GetWorkoutStatisticsUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    /**
     * 指定期間のワークアウト統計を取得
     */
    operator fun invoke(period: TimePeriod): Flow<WorkoutStatistics> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val startDate = when (period) {
            TimePeriod.WEEK -> LocalDate(today.year, today.month, maxOf(today.dayOfMonth - 7, 1))
            TimePeriod.MONTH -> LocalDate(today.year, today.month, 1)
            TimePeriod.THREE_MONTHS -> LocalDate(today.year, 1, 1) // 年始に設定
            TimePeriod.YEAR -> LocalDate(today.year - 1, today.month, today.dayOfMonth)
        }

        return workoutRepository.getWorkoutsByDateRangeFlow(startDate, today)
            .map { workouts ->
                ProgressMetrics.calculateWorkoutStatistics(workouts, period)
            }
    }

    /**
     * 指定期間のワークアウト統計を取得（日付範囲指定）
     */
    fun getStatisticsForDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<WorkoutStatistics> {
        val days = startDate.daysUntil(endDate)
        val period = when {
            days <= 7 -> TimePeriod.WEEK
            days <= 30 -> TimePeriod.MONTH
            days <= 90 -> TimePeriod.THREE_MONTHS
            else -> TimePeriod.YEAR
        }

        return workoutRepository.getWorkoutsByDateRangeFlow(startDate, endDate)
            .map { workouts ->
                ProgressMetrics.calculateWorkoutStatistics(workouts, period)
            }
    }
}