package com.workrec.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workrec.domain.entities.Workout
import com.workrec.domain.usecase.workout.AddWorkoutUseCase
import com.workrec.domain.usecase.workout.DeleteWorkoutUseCase
import com.workrec.domain.usecase.workout.GetWorkoutHistoryUseCase
import com.workrec.domain.usecase.calendar.GetWorkoutDatesUseCase
import com.workrec.domain.usecase.calendar.GetWorkoutsByDateUseCase
import com.workrec.domain.utils.CalendarUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * ワークアウト画面のViewModel
 * UI状態の管理とビジネスロジックの調整を行う
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val addWorkoutUseCase: AddWorkoutUseCase,
    private val getWorkoutHistoryUseCase: GetWorkoutHistoryUseCase,
    private val deleteWorkoutUseCase: DeleteWorkoutUseCase,
    private val getWorkoutDatesUseCase: GetWorkoutDatesUseCase,
    private val getWorkoutsByDateUseCase: GetWorkoutsByDateUseCase
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

    // Calendar functionality
    
    // Current month workout dates flow
    private val currentMonthWorkoutDates: StateFlow<Set<LocalDate>> = 
        _uiState.flatMapLatest { state ->
            getWorkoutDatesUseCase.getWorkoutDatesForMonth(
                state.currentMonth.year,
                state.currentMonth.monthValue
            ).catch { exception ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = exception.message ?: "カレンダーデータの取得に失敗しました"
                )
                emit(emptySet())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    // Selected date workouts flow
    private val selectedDateWorkouts: StateFlow<List<Workout>> = 
        _uiState.flatMapLatest { state ->
            if (state.selectedDate != null) {
                getWorkoutsByDateUseCase(state.selectedDate).catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "ワークアウトデータの取得に失敗しました",
                        isLoadingWorkouts = false
                    )
                    emit(emptyList())
                }
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Observe workout dates and update UI state
        viewModelScope.launch {
            currentMonthWorkoutDates.collect { workoutDates ->
                _uiState.value = _uiState.value.copy(workoutDates = workoutDates)
            }
        }
        
        // Observe selected date workouts and update UI state
        viewModelScope.launch {
            selectedDateWorkouts.collect { workouts ->
                _uiState.value = _uiState.value.copy(
                    selectedDateWorkouts = workouts,
                    isLoadingWorkouts = false
                )
            }
        }
    }

    /**
     * 次の月に移動
     */
    fun navigateToNextMonth() {
        val nextMonth = CalendarUtils.getNextMonth(_uiState.value.currentMonth)
        _uiState.value = _uiState.value.copy(
            currentMonth = nextMonth,
            selectedDate = null // Clear selection when changing months
        )
    }

    /**
     * 前の月に移動
     */
    fun navigateToPreviousMonth() {
        val previousMonth = CalendarUtils.getPreviousMonth(_uiState.value.currentMonth)
        _uiState.value = _uiState.value.copy(
            currentMonth = previousMonth,
            selectedDate = null // Clear selection when changing months
        )
    }

    /**
     * 今日の月に移動
     */
    fun navigateToToday() {
        val currentMonth = CalendarUtils.getCurrentMonth()
        val today = CalendarUtils.getCurrentDate()
        _uiState.value = _uiState.value.copy(
            currentMonth = currentMonth,
            selectedDate = today,
            isLoadingWorkouts = true
        )
    }

    /**
     * 日付を選択
     */
    fun onDateSelected(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            isLoadingWorkouts = true
        )
        
        // Also directly load workouts for immediate response (for tests and immediate UI update)
        viewModelScope.launch {
            try {
                getWorkoutsByDateUseCase(date).collect { workouts ->
                    _uiState.value = _uiState.value.copy(
                        selectedDateWorkouts = workouts,
                        isLoadingWorkouts = false
                    )
                }
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingWorkouts = false,
                    errorMessage = exception.message ?: "ワークアウトデータの取得に失敗しました"
                )
            }
        }
    }

    /**
     * カレンダーデータの再読み込み
     */
    fun refreshCalendarData() {
        val currentMonth = _uiState.value.currentMonth
        _uiState.value = _uiState.value.copy(currentMonth = currentMonth)
    }

    /**
     * カレンダーエラーをクリア
     */
    fun clearCalendarError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

/**
 * ワークアウト画面のUI状態
 */
data class WorkoutUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val message: String? = null,
    val selectedDateWorkouts: List<Workout> = emptyList(),
    // Calendar-related state
    val currentMonth: YearMonth = CalendarUtils.getCurrentMonth(),
    val selectedDate: LocalDate? = null,
    val workoutDates: Set<LocalDate> = emptySet(),
    val isLoadingWorkouts: Boolean = false
)