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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * カレンダー画面のViewModel
 * カレンダー表示とワークアウトデータの管理を行う
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    // TODO: Phase 2で追加予定
    // private val getWorkoutDatesUseCase: GetWorkoutDatesUseCase,
    // private val getWorkoutsByDateUseCase: GetWorkoutsByDateUseCase
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
                // TODO: Phase 2で実装 - 実際のワークアウト日付を取得
                // getWorkoutDatesUseCase()
                //     .catch { exception ->
                //         handleError("ワークアウト日付の読み込みに失敗しました: ${exception.message}")
                //     }
                //     .onEach { dates ->
                //         _uiState.value = _uiState.value.copy(
                //             workoutDates = dates.toSet(),
                //             isLoading = false
                //         )
                //         calculateStreaks(dates)
                //     }
                //     .launchIn(this)
                
                // 一時的なモックデータ
                val mockWorkoutDates = generateMockWorkoutDates()
                _uiState.value = _uiState.value.copy(
                    workoutDates = mockWorkoutDates,
                    isLoading = false
                )
                calculateStreaks(mockWorkoutDates.toList())
                
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
                
                // TODO: Phase 2で実装 - 実際のワークアウトデータを取得
                // getWorkoutsByDateUseCase(date)
                //     .catch { exception ->
                //         handleError("ワークアウトの読み込みに失敗しました: ${exception.message}")
                //     }
                //     .onEach { workouts ->
                //         _uiState.value = _uiState.value.copy(
                //             selectedDateWorkouts = workouts.map { it.title }, // 簡略化
                //             isLoadingWorkouts = false
                //         )
                //     }
                //     .launchIn(this)
                
                // 一時的なモックデータ
                val hasWorkout = _uiState.value.workoutDates.contains(date)
                val mockWorkouts = if (hasWorkout) {
                    listOf("ベンチプレス", "スクワット", "デッドリフト")
                } else {
                    emptyList()
                }
                
                // 実際のデータ読み込みをシミュレート
                kotlinx.coroutines.delay(500)
                
                _uiState.value = _uiState.value.copy(
                    selectedDateWorkouts = mockWorkouts,
                    isLoadingWorkouts = false
                )
                
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
            checkDate = LocalDate(checkDate.year, checkDate.month, checkDate.dayOfMonth - 1)
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

    /**
     * モックワークアウト日付を生成（デモ用）
     */
    private fun generateMockWorkoutDates(): Set<LocalDate> {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val mockDates = mutableSetOf<LocalDate>()
        
        // 過去30日間でランダムにワークアウト日を生成
        repeat(15) { i ->
            val randomOffset = (0..30).random()
            val date = LocalDate(
                today.year,
                today.month,
                maxOf(1, today.dayOfMonth - randomOffset)
            )
            mockDates.add(date)
        }
        
        // 今日も含める
        mockDates.add(today)
        
        return mockDates
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