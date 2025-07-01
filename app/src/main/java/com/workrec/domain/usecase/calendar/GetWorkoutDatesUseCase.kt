package com.workrec.domain.usecase.calendar

import com.workrec.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * ワークアウト実施日一覧を取得するUseCase
 */
class GetWorkoutDatesUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    /**
     * 全てのワークアウト実施日を取得
     * @return ワークアウト実施日のSetを返すFlow
     */
    operator fun invoke(): Flow<Set<LocalDate>> {
        return workoutRepository.getAllWorkouts()
            .map { workouts ->
                workouts.map { it.date }.toSet()
            }
    }

    /**
     * 指定期間のワークアウト実施日を取得
     * @param startDate 開始日
     * @param endDate 終了日
     * @return 指定期間のワークアウト実施日のSetを返すFlow
     */
    operator fun invoke(startDate: LocalDate, endDate: LocalDate): Flow<Set<LocalDate>> {
        return workoutRepository.getWorkoutsByDateRangeFlow(startDate, endDate)
            .map { workouts ->
                workouts.map { it.date }.toSet()
            }
    }

    /**
     * 指定した月のワークアウト実施日を取得
     * @param year 年
     * @param month 月
     * @return 指定月のワークアウト実施日のSetを返すFlow
     */
    fun getWorkoutDatesForMonth(year: Int, month: Int): Flow<Set<LocalDate>> {
        val startDate = LocalDate(year, month, 1)
        val endDate = LocalDate(year, month, 
            when (month) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> if (isLeapYear(year)) 29 else 28
                else -> 31
            }
        )
        
        return invoke(startDate, endDate)
    }

    /**
     * うるう年かどうかを判定
     */
    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }
}