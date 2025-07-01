package com.workrec.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workrec.domain.entities.Goal
import com.workrec.domain.entities.GoalType
import com.workrec.domain.usecase.goal.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

/**
 * 目標画面のViewModel
 */
@HiltViewModel
class GoalViewModel @Inject constructor(
    private val getGoalProgressUseCase: GetGoalProgressUseCase,
    private val addGoalUseCase: AddGoalUseCase,
    private val updateGoalProgressUseCase: UpdateGoalProgressUseCase,
    private val deleteGoalUseCase: DeleteGoalUseCase,
    private val getGoalDetailUseCase: GetGoalDetailUseCase
) : ViewModel() {

    // UI状態の管理
    private val _uiState = MutableStateFlow(GoalUiState())
    val uiState: StateFlow<GoalUiState> = _uiState.asStateFlow()

    // フィルター・ソート・検索状態
    private val _filterState = MutableStateFlow(GoalFilterState())
    val filterState: StateFlow<GoalFilterState> = _filterState.asStateFlow()

    // 全目標データ
    private val allGoals: StateFlow<List<Goal>> = getGoalProgressUseCase.getAllGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // フィルタリング・ソート・検索適用後の目標
    val filteredGoals: StateFlow<List<Goal>> = combine(
        allGoals,
        filterState
    ) { goals, filter ->
        applyFiltersAndSort(goals, filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // アクティブな目標のFlow
    val activeGoals: StateFlow<List<Goal>> = filteredGoals.map { goals ->
        goals.filter { !it.isCompleted }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 完了済み目標のFlow
    val completedGoals: StateFlow<List<Goal>> = filteredGoals.map { goals ->
        goals.filter { it.isCompleted }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 統計データ
    val goalStats: StateFlow<GoalStats> = allGoals.map { goals ->
        calculateGoalStats(goals)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GoalStats()
    )

    /**
     * 目標を追加
     */
    fun addGoal(
        type: GoalType,
        title: String,
        description: String? = null,
        targetValue: Double,
        unit: String,
        deadline: kotlinx.datetime.LocalDate? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            addGoalUseCase(type, title, description, targetValue, unit, deadline)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "目標を追加しました"
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "目標の追加に失敗しました"
                    )
                }
        }
    }

    /**
     * 目標の進捗を更新
     */
    fun updateGoalProgress(goalId: Long, progressValue: Double, notes: String? = null) {
        viewModelScope.launch {
            updateGoalProgressUseCase(goalId, progressValue, notes)
                .onSuccess {
                    showMessage("進捗を更新しました")
                }
                .onFailure { exception ->
                    showError(exception.message ?: "進捗の更新に失敗しました")
                }
        }
    }

    /**
     * 目標を削除
     */
    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            deleteGoalUseCase(goalId)
                .onSuccess {
                    showMessage("目標を削除しました")
                }
                .onFailure { exception ->
                    showError(exception.message ?: "目標の削除に失敗しました")
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

    // フィルタリング・ソート・検索メソッド

    /**
     * 検索クエリを更新
     */
    fun updateSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    /**
     * 目標タイプフィルターを更新
     */
    fun updateTypeFilter(types: Set<GoalType>) {
        _filterState.value = _filterState.value.copy(selectedTypes = types)
    }

    /**
     * 完了状態フィルターを更新
     */
    fun updateCompletionFilter(filter: CompletionFilter) {
        _filterState.value = _filterState.value.copy(completionFilter = filter)
    }

    /**
     * ソート順を更新
     */
    fun updateSortOrder(sortBy: SortBy, ascending: Boolean = true) {
        _filterState.value = _filterState.value.copy(
            sortBy = sortBy,
            ascending = ascending
        )
    }

    /**
     * フィルターをリセット
     */
    fun resetFilters() {
        _filterState.value = GoalFilterState()
    }

    // プライベートヘルパーメソッド

    /**
     * フィルタリング・ソート・検索を適用
     */
    private fun applyFiltersAndSort(goals: List<Goal>, filter: GoalFilterState): List<Goal> {
        var filteredGoals = goals

        // 検索フィルター
        if (filter.searchQuery.isNotBlank()) {
            filteredGoals = filteredGoals.filter { goal ->
                goal.title.contains(filter.searchQuery, ignoreCase = true) ||
                goal.description?.contains(filter.searchQuery, ignoreCase = true) == true
            }
        }

        // タイプフィルター
        if (filter.selectedTypes.isNotEmpty()) {
            filteredGoals = filteredGoals.filter { goal ->
                goal.type in filter.selectedTypes
            }
        }

        // 完了状態フィルター
        filteredGoals = when (filter.completionFilter) {
            CompletionFilter.ALL -> filteredGoals
            CompletionFilter.ACTIVE -> filteredGoals.filter { !it.isCompleted }
            CompletionFilter.COMPLETED -> filteredGoals.filter { it.isCompleted }
        }

        // ソート
        filteredGoals = when (filter.sortBy) {
            SortBy.TITLE -> filteredGoals.sortedBy { it.title }
            SortBy.CREATED_DATE -> filteredGoals.sortedBy { it.createdAt }
            SortBy.DEADLINE -> filteredGoals.sortedBy { it.deadline ?: kotlinx.datetime.LocalDate(9999, 12, 31) }
            SortBy.PROGRESS -> filteredGoals.sortedBy { it.progressPercentage }
            SortBy.TYPE -> filteredGoals.sortedBy { it.type.displayName }
        }

        return if (filter.ascending) filteredGoals else filteredGoals.reversed()
    }

    /**
     * 目標統計を計算
     */
    private fun calculateGoalStats(goals: List<Goal>): GoalStats {
        if (goals.isEmpty()) {
            return GoalStats()
        }

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val activeGoals = goals.filter { !it.isCompleted }
        val completedGoals = goals.filter { it.isCompleted }
        val overdueGoals = activeGoals.filter { goal ->
            goal.deadline?.let { it < today } == true
        }

        val totalProgress = goals.sumOf { it.progressPercentage.toDouble() }
        val averageProgress = totalProgress / goals.size

        return GoalStats(
            totalGoals = goals.size,
            activeGoals = activeGoals.size,
            completedGoals = completedGoals.size,
            overdueGoals = overdueGoals.size,
            averageProgress = averageProgress.toFloat(),
            completionRate = if (goals.isNotEmpty()) completedGoals.size.toFloat() / goals.size else 0f
        )
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
        _uiState.value = _uiState.value.copy(message = message)
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

/**
 * フィルター状態
 */
data class GoalFilterState(
    val searchQuery: String = "",
    val selectedTypes: Set<GoalType> = emptySet(),
    val completionFilter: CompletionFilter = CompletionFilter.ALL,
    val sortBy: SortBy = SortBy.CREATED_DATE,
    val ascending: Boolean = false
)

/**
 * 完了状態フィルター
 */
enum class CompletionFilter(val displayName: String) {
    ALL("すべて"),
    ACTIVE("進行中"),
    COMPLETED("完了済み")
}

/**
 * ソート項目
 */
enum class SortBy(val displayName: String) {
    TITLE("タイトル"),
    CREATED_DATE("作成日"),
    DEADLINE("期限"),
    PROGRESS("進捗率"),
    TYPE("タイプ")
}

/**
 * 目標統計
 */
data class GoalStats(
    val totalGoals: Int = 0,
    val activeGoals: Int = 0,
    val completedGoals: Int = 0,
    val overdueGoals: Int = 0,
    val averageProgress: Float = 0f,
    val completionRate: Float = 0f
)