package com.workrec.domain.usecase.progress

import com.workrec.domain.entities.GoalProgress
import com.workrec.domain.entities.PersonalRecord
import com.workrec.domain.entities.ProgressData
import com.workrec.domain.entities.ProgressMetrics
import com.workrec.domain.entities.TimePeriod
import com.workrec.domain.entities.WeeklyData
import com.workrec.domain.repository.GoalRepository
import com.workrec.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * 時間経過に伴う進捗データを取得するUseCase
 */
class GetProgressOverTimeUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val goalRepository: GoalRepository
) {
    /**
     * 包括的な進捗データを取得
     */
    operator fun invoke(period: TimePeriod): Flow<ProgressData> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val startDate = when (period) {
            TimePeriod.WEEK -> LocalDate(today.year, today.month, maxOf(today.dayOfMonth - 7, 1))
            TimePeriod.MONTH -> LocalDate(today.year, today.month, 1)
            TimePeriod.THREE_MONTHS -> LocalDate(today.year, 1, 1) // 年始に設定
            TimePeriod.YEAR -> LocalDate(today.year - 1, today.month, today.dayOfMonth)
        }

        val workoutsFlow = workoutRepository.getWorkoutsByDateRangeFlow(startDate, today)
        val allWorkoutsFlow = workoutRepository.getAllWorkouts() // 個人記録計算用
        val goalsFlow = goalRepository.getActiveGoals()

        return combine(workoutsFlow, allWorkoutsFlow, goalsFlow) { workouts, allWorkouts, goals ->
            ProgressData(
                workoutStatistics = ProgressMetrics.calculateWorkoutStatistics(workouts, period),
                goalProgress = ProgressMetrics.calculateGoalProgress(goals, today),
                weeklyTrend = ProgressMetrics.calculateWeeklyData(workouts),
                personalRecords = ProgressMetrics.calculatePersonalRecords(allWorkouts)
            )
        }
    }

    /**
     * 週間進捗トレンドを取得
     */
    fun getWeeklyTrend(weeks: Int = 12): Flow<List<WeeklyData>> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val startDate = LocalDate(today.year, today.month, maxOf(today.dayOfMonth - (weeks * 7), 1))

        return workoutRepository.getWorkoutsByDateRangeFlow(startDate, today)
            .combine(workoutRepository.getAllWorkouts()) { workouts, _ ->
                ProgressMetrics.calculateWeeklyData(workouts, weeks)
            }
    }

    /**
     * 目標進捗リストを取得
     */
    fun getGoalProgress(): Flow<List<GoalProgress>> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return goalRepository.getActiveGoals()
            .combine(goalRepository.getAllGoals()) { activeGoals, _ ->
                ProgressMetrics.calculateGoalProgress(activeGoals, today)
            }
    }

    /**
     * 個人記録を取得
     */
    fun getPersonalRecords(): Flow<List<PersonalRecord>> {
        return workoutRepository.getAllWorkouts()
            .combine(workoutRepository.getAllWorkouts()) { workouts, _ ->
                ProgressMetrics.calculatePersonalRecords(workouts)
            }
    }
}