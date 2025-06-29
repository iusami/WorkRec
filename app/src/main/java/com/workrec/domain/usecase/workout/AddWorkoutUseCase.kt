package com.workrec.domain.usecase.workout

import com.workrec.domain.entities.Workout
import com.workrec.domain.repository.WorkoutRepository
import javax.inject.Inject

/**
 * ワークアウト追加のユースケース
 * ビジネスルールとデータ検証を含む
 */
class AddWorkoutUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    /**
     * ワークアウトを追加する
     * @param workout 追加するワークアウト
     * @return 追加されたワークアウトのID、失敗時はnull
     */
    suspend operator fun invoke(workout: Workout): Result<Long> {
        return try {
            // ビジネスルールの検証
            validateWorkout(workout)
            
            // リポジトリを通じてデータを保存
            val workoutId = workoutRepository.saveWorkout(workout)
            Result.success(workoutId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * ワークアウトのバリデーション
     */
    private fun validateWorkout(workout: Workout) {
        // エクササイズが空でないことを確認
        if (workout.exercises.isEmpty()) {
            throw IllegalArgumentException("ワークアウトには少なくとも1つのエクササイズが必要です")
        }
        
        // 各エクササイズにセットが存在することを確認
        workout.exercises.forEach { exercise ->
            if (exercise.sets.isEmpty()) {
                throw IllegalArgumentException("エクササイズ「${exercise.name}」にはセットが必要です")
            }
            
            // セットの値が有効であることを確認
            exercise.sets.forEach { set ->
                if (set.reps <= 0) {
                    throw IllegalArgumentException("回数は1以上である必要があります")
                }
                if (set.weight < 0) {
                    throw IllegalArgumentException("重量は0以上である必要があります")
                }
            }
        }
    }
}