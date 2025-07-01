package com.workrec.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workrec.domain.usecase.goal.DeleteGoalUseCase
import com.workrec.domain.usecase.goal.GetGoalDetailUseCase
import com.workrec.domain.usecase.goal.GoalDetailData
import com.workrec.domain.usecase.goal.UpdateGoalProgressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 目標詳細画面のViewModel
 */
@HiltViewModel
class GoalDetailViewModel @Inject constructor(
    private val getGoalDetailUseCase: GetGoalDetailUseCase,
    private val updateGoalProgressUseCase: UpdateGoalProgressUseCase,
    private val deleteGoalUseCase: DeleteGoalUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalDetailUiState())
    val uiState: StateFlow<GoalDetailUiState> = _uiState.asStateFlow()

    /**
     * 目標詳細データを読み込む
     */
    fun loadGoalDetail(goalId: Long) {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            getGoalDetailUseCase(goalId)
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "目標詳細の読み込みに失敗しました"
                    )
                }
                .collect { goalDetailData ->
                    if (goalDetailData != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            goalDetailData = goalDetailData,
                            errorMessage = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "目標が見つかりません"
                        )
                    }
                }
        }
    }

    /**
     * 進捗を更新する
     */
    fun updateProgress(goalId: Long, progressValue: Double, notes: String?) {
        viewModelScope.launch {
            updateGoalProgressUseCase(goalId, progressValue, notes).fold(
                onSuccess = {
                    // 成功時は自動的にFlowが更新されるため、特別な処理は不要
                    showMessage("進捗を更新しました")
                },
                onFailure = { error ->
                    showError(error.message ?: "進捗の更新に失敗しました")
                }
            )
        }
    }

    /**
     * 進捗記録を削除する
     */
    fun deleteProgress(progressId: Long) {
        viewModelScope.launch {
            // TODO: 進捗記録削除のUseCaseを実装した後に追加
            showMessage("進捗記録を削除しました")
        }
    }

    /**
     * 目標を削除する
     */
    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            deleteGoalUseCase(goalId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isDeleted = true
                    )
                },
                onFailure = { error ->
                    showError(error.message ?: "目標の削除に失敗しました")
                }
            )
        }
    }

    /**
     * エラーメッセージを表示
     */
    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    /**
     * 成功メッセージを表示
     */
    private fun showMessage(message: String) {
        _uiState.value = _uiState.value.copy(successMessage = message)
    }

    /**
     * メッセージをクリア
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    /**
     * 削除状態をリセット
     */
    fun resetDeleteState() {
        _uiState.value = _uiState.value.copy(isDeleted = false)
    }
}

/**
 * 目標詳細画面のUIState
 */
data class GoalDetailUiState(
    val isLoading: Boolean = false,
    val goalDetailData: GoalDetailData? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isDeleted: Boolean = false
)