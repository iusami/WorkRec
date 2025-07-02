package com.workrec.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workrec.domain.entities.ProgressData
import com.workrec.domain.entities.TimePeriod
import com.workrec.domain.entities.WorkoutStatistics
import com.workrec.domain.entities.GoalProgress
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.daysUntil
import com.workrec.domain.usecase.progress.GetProgressOverTimeUseCase
import com.workrec.domain.usecase.progress.GetWorkoutStatisticsUseCase
import com.workrec.domain.usecase.goal.GetGoalProgressUseCase
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
 * 進捗画面のViewModel
 * 進捗データの取得と状態管理を行う
 */
@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val getProgressOverTimeUseCase: GetProgressOverTimeUseCase,
    private val getWorkoutStatisticsUseCase: GetWorkoutStatisticsUseCase,
    private val getGoalProgressUseCase: GetGoalProgressUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        loadProgressData()
    }

    /**
     * 時間期間を変更
     */
    fun selectTimePeriod(period: TimePeriod) {
        _uiState.value = _uiState.value.copy(
            selectedTimePeriod = period,
            isLoading = true,
            errorMessage = null
        )
        loadProgressData()
    }

    /**
     * 進捗データを更新
     */
    fun refreshData() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )
        loadProgressData()
    }

    /**
     * エラーをクリア
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * 進捗データを読み込み
     */
    private fun loadProgressData() {
        viewModelScope.launch {
            // 包括的な進捗データを取得
            getProgressOverTimeUseCase(_uiState.value.selectedTimePeriod)
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "データの読み込みに失敗しました: ${exception.message}"
                    )
                }
                .onEach { progressData ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        progressData = progressData,
                        errorMessage = null
                    )
                }
                .launchIn(this)

            // ワークアウト統計を個別に取得
            getWorkoutStatisticsUseCase(_uiState.value.selectedTimePeriod)
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "統計データの読み込みに失敗しました: ${exception.message}"
                    )
                }
                .onEach { statistics ->
                    _uiState.value = _uiState.value.copy(
                        workoutStatistics = statistics
                    )
                }
                .launchIn(this)
                
            // 目標進捗データを個別に取得
            getGoalProgressUseCase.getAllGoals()
                .catch { exception ->
                    // 目標データの取得エラーは警告レベルで処理（進捗画面の表示は継続）
                    println("Warning: 目標データの読み込みに失敗: ${exception.message}")
                }
                .onEach { goals ->
                    // GoalをGoalProgressに変換
                    val goalProgressList = goals.filter { !it.isCompleted }.map { goal ->
                        convertGoalToGoalProgress(goal)
                    }
                    
                    // 目標データが取得できた場合、ProgressDataに統合
                    val currentProgressData = _uiState.value.progressData
                    if (currentProgressData != null) {
                        val updatedProgressData = currentProgressData.copy(
                            goalProgress = goalProgressList
                        )
                        _uiState.value = _uiState.value.copy(
                            progressData = updatedProgressData
                        )
                    } else {
                        // ProgressDataがまだない場合、目標のみのProgressDataを作成
                        val goalOnlyProgressData = ProgressData(
                            workoutStatistics = WorkoutStatistics(
                                period = _uiState.value.selectedTimePeriod,
                                totalWorkouts = 0,
                                totalVolume = 0.0,
                                averageVolume = 0.0,
                                averageDuration = 0,
                                totalSets = 0,
                                mostActiveDay = "",
                                workoutFrequency = 0.0,
                                volumeTrend = emptyList()
                            ),
                            goalProgress = goalProgressList,
                            weeklyTrend = emptyList(),
                            personalRecords = emptyList()
                        )
                        _uiState.value = _uiState.value.copy(
                            progressData = goalOnlyProgressData
                        )
                    }
                }
                .launchIn(this)
        }
    }
    
    /**
     * GoalをGoalProgressに変換
     */
    private fun convertGoalToGoalProgress(goal: com.workrec.domain.entities.Goal): GoalProgress {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        
        return GoalProgress(
            goal = goal,
            progressPercentage = goal.progressPercentage,
            remainingDays = goal.deadline?.let { deadline ->
                val daysDifference = today.daysUntil(deadline)
                if (daysDifference >= 0) daysDifference else null
            },
            isOnTrack = goal.progressPercentage >= 0.7f, // 70%以上で順調と判定
            projectedCompletion = goal.deadline // 簡易実装
        )
    }
}

/**
 * 進捗画面のUI状態
 */
data class ProgressUiState(
    val isLoading: Boolean = false,
    val progressData: ProgressData? = null,
    val workoutStatistics: WorkoutStatistics? = null,
    val selectedTimePeriod: TimePeriod = TimePeriod.MONTH,
    val errorMessage: String? = null
) {
    /**
     * データが存在するかどうか
     */
    val hasData: Boolean
        get() = progressData != null && workoutStatistics != null

    /**
     * エラー状態かどうか
     */
    val isError: Boolean
        get() = errorMessage != null

    /**
     * 初期読み込み中かどうか
     */
    val isInitialLoading: Boolean
        get() = isLoading && progressData == null

    /**
     * リフレッシュ中かどうか
     */
    val isRefreshing: Boolean
        get() = isLoading && progressData != null
}