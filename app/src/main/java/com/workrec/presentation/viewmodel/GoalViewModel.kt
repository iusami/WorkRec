package com.workrec.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workrec.domain.entities.Goal
import com.workrec.domain.usecase.goal.GetGoalProgressUseCase
import com.workrec.domain.usecase.goal.SetGoalUseCase
// import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
// import javax.inject.Inject

/**
 * 目標画面のViewModel
 */
// @HiltViewModel
class GoalViewModel constructor(
    private val setGoalUseCase: SetGoalUseCase,
    private val getGoalProgressUseCase: GetGoalProgressUseCase
) : ViewModel() {

    // UI状態の管理
    private val _uiState = MutableStateFlow(GoalUiState())
    val uiState: StateFlow<GoalUiState> = _uiState.asStateFlow()

    // アクティブな目標のFlow
    val activeGoals: StateFlow<List<Goal>> = getGoalProgressUseCase.getActiveGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 完了済み目標のFlow
    val completedGoals: StateFlow<List<Goal>> = getGoalProgressUseCase.getCompletedGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * 目標を設定
     */
    fun setGoal(goal: Goal) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            setGoalUseCase(goal)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "目標を設定しました"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "目標の設定に失敗しました"
                    )
                }
        }
    }

    /**
     * 指定したIDの目標を取得
     */
    fun getGoalById(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val goal = getGoalProgressUseCase.getGoalById(id)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    selectedGoal = goal
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "目標の取得に失敗しました"
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

    /**
     * 選択された目標をクリア
     */
    fun clearSelectedGoal() {
        _uiState.value = _uiState.value.copy(selectedGoal = null)
    }
}

/**
 * 目標画面のUI状態
 */
data class GoalUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val message: String? = null,
    val selectedGoal: Goal? = null
)