package com.workrec.domain.usecase.workout

import com.workrec.domain.entities.Workout
import com.workrec.domain.repository.WorkoutRepository
import javax.inject.Inject

/**
 * ワークアウト削除のユースケース
 */
class DeleteWorkoutUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    /**
     * ワークアウトを削除する
     * @param workout 削除するワークアウト
     */
    suspend operator fun invoke(workout: Workout): Result<Unit> {
        return try {
            workoutRepository.deleteWorkout(workout)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * IDを指定してワークアウトを削除する
     * @param workoutId 削除するワークアウトのID
     */
    suspend fun deleteById(workoutId: Long): Result<Unit> {
        return try {
            workoutRepository.deleteWorkoutById(workoutId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}