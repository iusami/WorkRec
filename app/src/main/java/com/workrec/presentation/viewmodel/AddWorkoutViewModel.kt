package com.workrec.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workrec.domain.entities.*
import com.workrec.domain.usecase.workout.AddWorkoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel  // 一時的に無効化
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject  // 一時的に無効化

/**
 * ワークアウト追加画面のViewModel（一時的にManual DIに変更）
 */
@HiltViewModel
class AddWorkoutViewModel @Inject constructor(
    private val addWorkoutUseCase: AddWorkoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddWorkoutUiState())
    val uiState: StateFlow<AddWorkoutUiState> = _uiState.asStateFlow()

    /**
     * 日付を更新
     */
    fun updateSelectedDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }

    /**
     * メモを更新
     */
    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    /**
     * エクササイズを追加
     */
    fun addExercise() {
        val newExercise = createEmptyExercise()
        val currentExercises = _uiState.value.exercises.toMutableList()
        currentExercises.add(newExercise)
        
        _uiState.value = _uiState.value.copy(exercises = currentExercises)
    }

    /**
     * エクササイズを更新
     */
    fun updateExercise(index: Int, exercise: Exercise) {
        val currentExercises = _uiState.value.exercises.toMutableList()
        if (index in currentExercises.indices) {
            currentExercises[index] = exercise
            _uiState.value = _uiState.value.copy(exercises = currentExercises)
        }
    }

    /**
     * エクササイズを削除
     */
    fun removeExercise(index: Int) {
        val currentExercises = _uiState.value.exercises.toMutableList()
        if (index in currentExercises.indices) {
            currentExercises.removeAt(index)
            _uiState.value = _uiState.value.copy(exercises = currentExercises)
        }
    }

    /**
     * ワークアウトを保存
     */
    fun saveWorkout() {
        val state = _uiState.value
        
        if (state.exercises.isEmpty()) {
            _uiState.value = state.copy(
                errorMessage = "少なくとも1つのエクササイズを追加してください"
            )
            return
        }

        // 有効なエクササイズのみをフィルタリング
        val validExercises = state.exercises.filter { exercise ->
            exercise.name.isNotBlank() && exercise.sets.isNotEmpty()
        }

        if (validExercises.isEmpty()) {
            _uiState.value = state.copy(
                errorMessage = "エクササイズ名とセットを入力してください"
            )
            return
        }

        val workout = Workout(
            date = state.selectedDate,
            exercises = validExercises,
            notes = state.notes.takeIf { it.isNotBlank() }
        )

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            
            addWorkoutUseCase(workout)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isWorkoutSaved = true
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "保存に失敗しました"
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
     * 空のエクササイズを作成
     */
    private fun createEmptyExercise(): Exercise {
        return Exercise(
            name = "",
            sets = listOf(createEmptySet()),
            category = ExerciseCategory.OTHER
        )
    }

    /**
     * 空のセットを作成
     */
    private fun createEmptySet(): ExerciseSet {
        return ExerciseSet(
            reps = 0,
            weight = 0.0
        )
    }
}

/**
 * ワークアウト追加画面のUI状態
 */
data class AddWorkoutUiState(
    val selectedDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val notes: String = "",
    val exercises: List<Exercise> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isWorkoutSaved: Boolean = false
)