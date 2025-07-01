package com.workrec.domain.usecase.calendar

import com.workrec.domain.entities.Workout
import com.workrec.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * 指定日のワークアウト一覧を取得するUseCase
 */
class GetWorkoutsByDateUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    /**
     * 指定日のワークアウト一覧を取得
     * @param date 指定日
     * @return 指定日のワークアウト一覧を返すFlow
     */
    operator fun invoke(date: LocalDate): Flow<List<Workout>> {
        return workoutRepository.getWorkoutsByDateRangeFlow(date, date)
    }

    /**
     * 指定日のワークアウト概要（タイトルのみ）を取得
     * @param date 指定日
     * @return ワークアウトタイトルのリストを返すFlow
     */
    fun getWorkoutTitlesForDate(date: LocalDate): Flow<List<String>> {
        return invoke(date).map { workouts ->
            workouts.map { workout ->
                if (workout.exercises.isNotEmpty()) {
                    // 主要なエクササイズ名を抽出
                    workout.exercises.take(3).joinToString(", ") { it.name }
                } else {
                    "ワークアウト"
                }
            }
        }
    }

    /**
     * 指定日のワークアウト統計情報を取得
     * @param date 指定日
     * @return ワークアウト統計を返すFlow
     */
    fun getWorkoutStatsForDate(date: LocalDate): Flow<WorkoutDayStats> {
        return invoke(date).map { workouts ->
            val totalWorkouts = workouts.size
            val totalVolume = workouts.sumOf { it.totalVolume }
            val totalSets = workouts.sumOf { it.totalSets }
            val totalExercises = workouts.sumOf { it.exercises.size }
            val totalDuration = workouts.mapNotNull { it.totalDuration }
                .takeIf { it.isNotEmpty() }
                ?.let { durations ->
                    durations.reduce { acc, duration -> acc + duration }
                }

            WorkoutDayStats(
                date = date,
                workoutCount = totalWorkouts,
                totalVolume = totalVolume,
                totalSets = totalSets,
                totalExercises = totalExercises,
                totalDuration = totalDuration
            )
        }
    }
}

/**
 * 指定日のワークアウト統計情報
 */
data class WorkoutDayStats(
    val date: LocalDate,
    val workoutCount: Int,
    val totalVolume: Double,
    val totalSets: Int,
    val totalExercises: Int,
    val totalDuration: kotlin.time.Duration?
)