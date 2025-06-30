package com.workrec.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workrec.domain.entities.Workout
import com.workrec.domain.usecase.workout.AddWorkoutUseCase
import com.workrec.domain.usecase.workout.DeleteWorkoutUseCase
import com.workrec.domain.usecase.workout.GetWorkoutHistoryUseCase
// import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
// import javax.inject.Inject

/**
 * ワークアウト画面のViewModel
 * UI状態の管理とビジネスロジックの調整を行う
 */
// @HiltViewModel
class WorkoutViewModel constructor(
    private val addWorkoutUseCase: AddWorkoutUseCase,
    private val getWorkoutHistoryUseCase: GetWorkoutHistoryUseCase,
    private val deleteWorkoutUseCase: DeleteWorkoutUseCase
) : ViewModel() {

    // UI状態の管理
    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    // ワークアウト履歴のFlow
    val workouts: StateFlow<List<Workout>> = getWorkoutHistoryUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * ワークアウトを追加
     */
    fun addWorkout(workout: Workout) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            addWorkoutUseCase(workout)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "ワークアウトを保存しました"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "エラーが発生しました"
                    )
                }
        }
    }

    /**
     * ワークアウトを削除
     */
    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            deleteWorkoutUseCase(workout)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "ワークアウトを削除しました"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "削除に失敗しました"
                    )
                }
        }
    }

    /**
     * 指定した日付のワークアウトを取得
     */
    fun getWorkoutsByDate(date: LocalDate) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val workouts = getWorkoutHistoryUseCase.getWorkoutsByDate(date)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedDateWorkouts = workouts
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "データの取得に失敗しました"
                )
            }
        }
    }

    /**
     * エラーメッセージをクリア
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * メッセージをクリア
     */
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}

/**
 * ワークアウト画面のUI状態
 */
data class WorkoutUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val message: String? = null,
    val selectedDateWorkouts: List<Workout> = emptyList()
)