package com.workrec.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workrec.domain.entities.ProgressData
import com.workrec.domain.entities.TimePeriod
import com.workrec.domain.entities.WorkoutStatistics
import com.workrec.domain.usecase.progress.GetProgressOverTimeUseCase
import com.workrec.domain.usecase.progress.GetWorkoutStatisticsUseCase
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
    private val getWorkoutStatisticsUseCase: GetWorkoutStatisticsUseCase
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
        }
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