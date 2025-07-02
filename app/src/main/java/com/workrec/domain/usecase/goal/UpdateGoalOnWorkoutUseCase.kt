package com.workrec.domain.usecase.goal

import com.workrec.domain.entities.Goal
import com.workrec.domain.entities.GoalType
import com.workrec.domain.entities.Workout
import com.workrec.domain.repository.GoalRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * ワークアウト完了時に関連する目標の進捗を自動更新するUseCase
 */
class UpdateGoalOnWorkoutUseCase @Inject constructor(
    private val goalRepository: GoalRepository
) {
    
    /**
     * ワークアウト完了時に関連する目標を自動更新
     */
    suspend operator fun invoke(workout: Workout): Result<Unit> = try {
        val goals = goalRepository.getAllGoals().first()
        val activeGoals = goals.filter { !it.isCompleted }
        
        // ワークアウトに関連する目標を特定し、進捗を更新
        activeGoals.forEach { goal ->
            val updatedGoal = updateGoalProgress(goal, workout)
            if (updatedGoal != null) {
                goalRepository.saveGoal(updatedGoal)
            }
        }
        
        Result.success(Unit)
    } catch (exception: Exception) {
        Result.failure(exception)
    }
    
    /**
     * 目標とワークアウトの関連性をチェックして進捗を更新
     */
    private fun updateGoalProgress(goal: Goal, workout: Workout): Goal? {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        
        return when (goal.type) {
            GoalType.FREQUENCY -> {
                // 頻度目標: ワークアウト実行回数をカウント
                if (goal.unit.contains("回")) {
                    val newCurrentValue = goal.currentValue + 1.0
                    goal.copy(
                        currentValue = newCurrentValue,
                        updatedAt = today,
                        isCompleted = newCurrentValue >= goal.targetValue
                    )
                } else null
            }
            
            GoalType.VOLUME -> {
                // ボリューム目標: 総重量を加算
                val workoutVolume = workout.exercises.sumOf { exercise ->
                    exercise.sets.sumOf { set ->
                        (set.weight ?: 0.0) * (set.reps ?: 0)
                    }
                }
                val newCurrentValue = goal.currentValue + workoutVolume
                goal.copy(
                    currentValue = newCurrentValue,
                    updatedAt = today,
                    isCompleted = newCurrentValue >= goal.targetValue
                )
            }
            
            GoalType.STRENGTH -> {
                // 筋力目標: 特定の種目の最大重量をチェック
                val maxWeightInWorkout = workout.exercises
                    .filter { exercise -> 
                        goal.title.contains(exercise.name, ignoreCase = true) ||
                        goal.description?.contains(exercise.name, ignoreCase = true) == true
                    }
                    .flatMap { it.sets }
                    .mapNotNull { it.weight }
                    .maxOrNull()
                
                if (maxWeightInWorkout != null && maxWeightInWorkout > goal.currentValue) {
                    goal.copy(
                        currentValue = maxWeightInWorkout,
                        updatedAt = today,
                        isCompleted = maxWeightInWorkout >= goal.targetValue
                    )
                } else null
            }
            
            GoalType.ENDURANCE -> {
                // 持久力目標: 特定の種目の最大回数をチェック
                val maxRepsInWorkout = workout.exercises
                    .filter { exercise -> 
                        goal.title.contains(exercise.name, ignoreCase = true) ||
                        goal.description?.contains(exercise.name, ignoreCase = true) == true
                    }
                    .flatMap { it.sets }
                    .mapNotNull { it.reps }
                    .maxOrNull()?.toDouble()
                
                if (maxRepsInWorkout != null && maxRepsInWorkout > goal.currentValue) {
                    goal.copy(
                        currentValue = maxRepsInWorkout,
                        updatedAt = today,
                        isCompleted = maxRepsInWorkout >= goal.targetValue
                    )
                } else null
            }
            
            GoalType.DURATION -> {
                // 継続目標: 連続日数の更新は別途実装
                // この場合は日別での更新が必要なため、ここでは処理しない
                null
            }
            
            else -> {
                // その他の目標タイプは手動更新のみ
                null
            }
        }
    }
}