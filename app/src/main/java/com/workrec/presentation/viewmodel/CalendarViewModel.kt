package com.workrec.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import com.workrec.domain.usecase.calendar.GetWorkoutDatesUseCase
import com.workrec.domain.usecase.calendar.GetWorkoutsByDateUseCase
import javax.inject.Inject

/**
 * カレンダー画面のViewModel
 * カレンダー表示とワークアウトデータの管理を行う
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val getWorkoutDatesUseCase: GetWorkoutDatesUseCase,
    private val getWorkoutsByDateUseCase: GetWorkoutsByDateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        initializeCalendar()
        loadWorkoutDates()
    }

    /**
     * カレンダーの初期化
     */
    private fun initializeCalendar() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        _uiState.value = _uiState.value.copy(
            selectedDate = today
        )
        loadWorkoutsForDate(today)
    }

    /**
     * 日付選択時の処理
     */
    fun onDateSelected(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            isLoadingWorkouts = true
        )
        loadWorkoutsForDate(date)
    }

    /**
     * ワークアウト実施日の読み込み
     */
    private fun loadWorkoutDates() {
        viewModelScope.launch {
            try {
                getWorkoutDatesUseCase()
                    .catch { exception ->
                        handleError("ワークアウト日付の読み込みに失敗しました: ${exception.message}")
                    }
                    .onEach { dates ->
                        _uiState.value = _uiState.value.copy(
                            workoutDates = dates,
                            isLoading = false
                        )
                        calculateStreaks(dates.toList())
                    }
                    .launchIn(this)
                
            } catch (exception: Exception) {
                handleError("ワークアウトデータの読み込みに失敗しました: ${exception.message}")
            }
        }
    }

    /**
     * 特定日のワークアウトデータを読み込み
     */
    private fun loadWorkoutsForDate(date: LocalDate) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoadingWorkouts = true)
                
                getWorkoutsByDateUseCase.getWorkoutTitlesForDate(date)
                    .catch { exception ->
                        handleError("ワークアウトの読み込みに失敗しました: ${exception.message}")
                    }
                    .onEach { workoutTitles ->
                        _uiState.value = _uiState.value.copy(
                            selectedDateWorkouts = workoutTitles,
                            isLoadingWorkouts = false
                        )
                    }
                    .launchIn(this)
                
            } catch (exception: Exception) {
                handleError("ワークアウトの読み込みに失敗しました: ${exception.message}")
            }
        }
    }

    /**
     * ストリーク（連続日数）を計算
     */
    private fun calculateStreaks(workoutDates: List<LocalDate>) {
        if (workoutDates.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                currentStreak = 0,
                longestStreak = 0
            )
            return
        }

        val sortedDates = workoutDates.sorted()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        
        // 現在のストリークを計算
        var currentStreak = 0
        var checkDate = today
        
        // 今日から逆算してストリークを計算
        while (sortedDates.contains(checkDate)) {
            currentStreak++
            checkDate = checkDate.minus(DatePeriod(days = 1))
        }
        
        // 最長ストリークを計算
        var longestStreak = 0
        var tempStreak = 1
        
        for (i in 1 until sortedDates.size) {
            val prevDate = sortedDates[i - 1]
            val currentDate = sortedDates[i]
            
            // 連続した日付かチェック
            if (prevDate.daysUntil(currentDate) == 1) {
                tempStreak++
            } else {
                longestStreak = maxOf(longestStreak, tempStreak)
                tempStreak = 1
            }
        }
        longestStreak = maxOf(longestStreak, tempStreak)

        _uiState.value = _uiState.value.copy(
            currentStreak = currentStreak,
            longestStreak = longestStreak
        )
    }

    /**
     * エラー処理
     */
    private fun handleError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isLoadingWorkouts = false,
            errorMessage = message
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
 * カレンダー画面のUI状態
 */
data class CalendarUiState(
    val isLoading: Boolean = true,
    val selectedDate: LocalDate? = null,
    val workoutDates: Set<LocalDate> = emptySet(),
    val selectedDateWorkouts: List<String> = emptyList(), // TODO: Workoutエンティティに変更
    val isLoadingWorkouts: Boolean = false,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val errorMessage: String? = null
) {
    /**
     * エラー状態かどうか
     */
    val isError: Boolean
        get() = errorMessage != null

    /**
     * 選択日にワークアウトがあるかどうか
     */
    val hasWorkoutOnSelectedDate: Boolean
        get() = selectedDate?.let { workoutDates.contains(it) } ?: false

    /**
     * データが読み込まれているかどうか
     */
    val hasData: Boolean
        get() = !isLoading && workoutDates.isNotEmpty()
}