package com.workrec.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workrec.domain.entities.GoalType
import com.workrec.domain.usecase.goal.AddGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * 目標追加画面のViewModel
 */
@HiltViewModel
class AddGoalViewModel @Inject constructor(
    private val addGoalUseCase: AddGoalUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddGoalUiState())
    val uiState: StateFlow<AddGoalUiState> = _uiState.asStateFlow()

    /**
     * 目標タイプを更新
     */
    fun updateType(type: GoalType) {
        _uiState.value = _uiState.value.copy(
            selectedType = type,
            unit = type.defaultUnit // デフォルト単位を自動設定
        )
        validateForm()
    }

    /**
     * タイトルを更新
     */
    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(
            title = title,
            titleError = null
        )
        validateForm()
    }

    /**
     * 説明を更新
     */
    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    /**
     * 目標値を更新
     */
    fun updateTargetValue(value: String) {
        _uiState.value = _uiState.value.copy(
            targetValue = value,
            targetValueError = null
        )
        validateForm()
    }

    /**
     * 単位を更新
     */
    fun updateUnit(unit: String) {
        _uiState.value = _uiState.value.copy(
            unit = unit,
            unitError = null
        )
        validateForm()
    }

    /**
     * 期限を更新
     */
    fun updateDeadline(deadline: LocalDate?) {
        _uiState.value = _uiState.value.copy(deadline = deadline)
    }

    /**
     * 目標を追加
     */
    fun addGoal() {
        val currentState = _uiState.value
        
        if (!currentState.isFormValid) {
            validateForm()
            return
        }

        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val targetValue = currentState.targetValue.toDoubleOrNull() ?: 0.0
                
                val result = addGoalUseCase(
                    type = currentState.selectedType!!,
                    title = currentState.title.trim(),
                    description = currentState.description.trim().takeIf { it.isNotEmpty() },
                    targetValue = targetValue,
                    unit = currentState.unit.trim(),
                    deadline = currentState.deadline
                )

                result.fold(
                    onSuccess = {
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = currentState.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "目標の追加に失敗しました"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "予期しないエラーが発生しました"
                )
            }
        }
    }

    /**
     * フォームバリデーション
     */
    private fun validateForm() {
        val currentState = _uiState.value
        
        val titleError = if (currentState.title.isBlank()) "目標名は必須です" else null
        val targetValueError = run {
            val value = currentState.targetValue.toDoubleOrNull()
            when {
                currentState.targetValue.isBlank() -> "目標値は必須です"
                value == null -> "正の数値を入力してください"
                value <= 0 -> "目標値は0より大きい値である必要があります"
                else -> null
            }
        }
        val unitError = if (currentState.unit.isBlank()) "単位は必須です" else null
        val typeError = if (currentState.selectedType == null) "目標タイプを選択してください" else null

        val isFormValid = titleError == null && 
                         targetValueError == null && 
                         unitError == null && 
                         typeError == null

        _uiState.value = currentState.copy(
            titleError = titleError,
            targetValueError = targetValueError,
            unitError = unitError,
            isFormValid = isFormValid
        )
    }

    /**
     * エラーメッセージをクリア
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

/**
 * 目標追加画面のUIState
 */
data class AddGoalUiState(
    val selectedType: GoalType? = null,
    val title: String = "",
    val description: String = "",
    val targetValue: String = "",
    val unit: String = "",
    val deadline: LocalDate? = null,
    
    // エラー状態
    val titleError: String? = null,
    val targetValueError: String? = null,
    val unitError: String? = null,
    
    // UI状態
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isFormValid: Boolean = false,
    val errorMessage: String? = null
)