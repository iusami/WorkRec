package com.workrec.domain.usecase.workout

import com.workrec.domain.entities.Workout
import com.workrec.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
// import javax.inject.Inject

/**
 * ワークアウト履歴取得のユースケース
 */
class GetWorkoutHistoryUseCase constructor(
    private val workoutRepository: WorkoutRepository
) {
    /**
     * すべてのワークアウト履歴を取得
     */
    operator fun invoke(): Flow<List<Workout>> {
        return workoutRepository.getAllWorkouts()
    }
    
    /**
     * 指定した期間のワークアウト履歴を取得
     */
    suspend fun getWorkoutsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Workout> {
        return workoutRepository.getWorkoutsByDateRange(startDate, endDate)
    }
    
    /**
     * 指定した日付のワークアウトを取得
     */
    suspend fun getWorkoutsByDate(date: LocalDate): List<Workout> {
        return workoutRepository.getWorkoutsByDate(date)
    }
    
    /**
     * 最近のワークアウト履歴を取得（指定した件数）
     */
    fun getRecentWorkouts(limit: Int): Flow<List<Workout>> {
        return workoutRepository.getAllWorkouts().map { workouts ->
            workouts.take(limit)
        }
    }
    
    /**
     * エクササイズ名で検索
     */
    suspend fun searchByExerciseName(exerciseName: String): List<Workout> {
        return workoutRepository.searchWorkoutsByExerciseName(exerciseName)
    }
}