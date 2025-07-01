package com.workrec.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workrec.domain.entities.Workout
import com.workrec.domain.usecase.workout.DeleteWorkoutUseCase
import com.workrec.domain.usecase.workout.GetWorkoutByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ワークアウト詳細画面のViewModel
 * 特定のワークアウトの詳細表示、編集、削除機能を提供
 */
@HiltViewModel
class WorkoutDetailViewModel @Inject constructor(
    private val getWorkoutByIdUseCase: GetWorkoutByIdUseCase,
    private val deleteWorkoutUseCase: DeleteWorkoutUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workoutId: Long = checkNotNull(savedStateHandle["workoutId"])
    
    private val _uiState = MutableStateFlow(WorkoutDetailUiState())
    val uiState: StateFlow<WorkoutDetailUiState> = _uiState.asStateFlow()

    init {
        loadWorkout()
    }

    /**
     * ワークアウトデータを読み込み
     */
    private fun loadWorkout() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )
                
                getWorkoutByIdUseCase(workoutId)
                    .catch { exception ->
                        handleError("ワークアウトの読み込みに失敗しました: ${exception.message}")
                    }
                    .onEach { workout ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            workout = workout,
                            errorMessage = null
                        )
                    }
                    .launchIn(this)
                    
            } catch (exception: Exception) {
                handleError("ワークアウトの読み込みに失敗しました: ${exception.message}")
            }
        }
    }

    /**
     * ワークアウトを削除
     */
    fun deleteWorkout() {
        val currentWorkout = _uiState.value.workout ?: return
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                deleteWorkoutUseCase(currentWorkout)
                
                // 削除後はナビゲーション側で戻る処理を行う
                _uiState.value = _uiState.value.copy(isLoading = false)
                
            } catch (exception: Exception) {
                handleError("ワークアウトの削除に失敗しました: ${exception.message}")
            }
        }
    }

    /**
     * ワークアウトを共有
     */
    fun shareWorkout() {
        val workout = _uiState.value.workout ?: return
        
        viewModelScope.launch {
            try {
                val shareText = generateShareText(workout)
                _uiState.value = _uiState.value.copy(shareText = shareText)
                
            } catch (exception: Exception) {
                handleError("共有データの生成に失敗しました: ${exception.message}")
            }
        }
    }

    /**
     * 共有テキストをクリア
     */
    fun clearShareText() {
        _uiState.value = _uiState.value.copy(shareText = null)
    }

    /**
     * 再試行
     */
    fun retry() {
        loadWorkout()
    }

    /**
     * エラー処理
     */
    private fun handleError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = message
        )
    }

    /**
     * 共有用テキストを生成
     */
    private fun generateShareText(workout: Workout): String {
        val date = "${workout.date.year}年${workout.date.monthNumber}月${workout.date.dayOfMonth}日"
        val exercises = workout.exercises.joinToString("\n") { exercise ->
            val sets = exercise.sets.joinToString(" ") { set ->
                "${set.weight}kg × ${set.reps}回"
            }
            "・${exercise.name}: $sets"
        }
        
        val totalVolume = workout.totalVolume
        val totalSets = workout.totalSets
        
        return buildString {
            appendLine("📅 $date のワークアウト")
            appendLine("")
            appendLine("🏋️ エクササイズ:")
            appendLine(exercises)
            appendLine("")
            appendLine("📊 統計:")
            appendLine("・総セット数: ${totalSets}セット")
            appendLine("・総ボリューム: ${String.format("%.1f", totalVolume)}kg")
            
            workout.totalDuration?.let { duration ->
                appendLine("・所要時間: ${duration.inWholeMinutes}分")
            }
            
            if (!workout.notes.isNullOrBlank()) {
                appendLine("")
                appendLine("📝 メモ:")
                appendLine(workout.notes)
            }
            
            appendLine("")
            appendLine("#ワークアウト #筋トレ #WorkRec")
        }
    }
}

/**
 * ワークアウト詳細画面のUI状態
 */
data class WorkoutDetailUiState(
    val isLoading: Boolean = false,
    val workout: Workout? = null,
    val errorMessage: String? = null,
    val shareText: String? = null
) {
    /**
     * エラー状態かどうか
     */
    val isError: Boolean
        get() = errorMessage != null

    /**
     * データが存在するかどうか
     */
    val hasData: Boolean
        get() = workout != null && !isLoading

    /**
     * ワークアウトが見つからない状態かどうか
     */
    val isNotFound: Boolean
        get() = !isLoading && workout == null && !isError
}