package com.workrec.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.workrec.domain.usecase.goal.GetGoalProgressUseCase
import com.workrec.domain.usecase.goal.SetGoalUseCase
import com.workrec.domain.usecase.workout.AddWorkoutUseCase
import com.workrec.domain.usecase.workout.DeleteWorkoutUseCase
import com.workrec.domain.usecase.workout.GetWorkoutHistoryUseCase

/**
 * ViewModelFactory for Manual DI
 */
class ViewModelFactory(
    private val addWorkoutUseCase: AddWorkoutUseCase,
    private val getWorkoutHistoryUseCase: GetWorkoutHistoryUseCase,
    private val deleteWorkoutUseCase: DeleteWorkoutUseCase,
    private val setGoalUseCase: SetGoalUseCase,
    private val getGoalProgressUseCase: GetGoalProgressUseCase
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            AddWorkoutViewModel::class.java -> {
                AddWorkoutViewModel(addWorkoutUseCase) as T
            }
            WorkoutViewModel::class.java -> {
                WorkoutViewModel(
                    addWorkoutUseCase,
                    getWorkoutHistoryUseCase,
                    deleteWorkoutUseCase
                ) as T
            }
            GoalViewModel::class.java -> {
                GoalViewModel(
                    setGoalUseCase,
                    getGoalProgressUseCase
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}