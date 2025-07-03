package com.workrec.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.workrec.domain.entities.*
import com.workrec.domain.repository.ExerciseTemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.concurrent.ConcurrentHashMap

/**
 * エクササイズ管理ViewModel
 * エクササイズテンプレートの検索、フィルタリング、CRUD操作を管理
 */
@HiltViewModel
class ExerciseManagerViewModel @Inject constructor(
    private val exerciseTemplateRepository: ExerciseTemplateRepository
) : ViewModel() {

    // 検索クエリ
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // フィルター
    private val _currentFilter = MutableStateFlow(ExerciseFilter())
    val currentFilter = _currentFilter.asStateFlow()

    // ソート順
    private val _sortOrder = MutableStateFlow(ExerciseSortOrder.NAME_ASC)
    val sortOrder = _sortOrder.asStateFlow()

    // ローディング状態
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // エラー状態
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // エクササイズリスト
    private val _exercises = MutableStateFlow<List<ExerciseTemplate>>(emptyList())
    val exercises = _exercises.asStateFlow()
    
    // 検索結果キャッシュ（パフォーマンス最適化）
    private val searchCache = ConcurrentHashMap<String, List<ExerciseTemplate>>()
    private val cacheTimeout = 5 * 60 * 1000L // 5分間キャッシュ
    private val cacheTimestamps = ConcurrentHashMap<String, Long>()

    // UI状態
    val uiState = combine(
        searchQuery,
        currentFilter,
        sortOrder,
        isLoading,
        errorMessage,
        exercises
    ) { flows ->
        val searchQuery = flows[0] as String
        val filter = flows[1] as ExerciseFilter
        val sortOrder = flows[2] as ExerciseSortOrder
        val isLoading = flows[3] as Boolean
        val errorMessage = flows[4] as String?
        val exercises = flows[5] as List<ExerciseTemplate>
        
        ExerciseManagerUiState(
            searchQuery = searchQuery,
            currentFilter = filter,
            sortOrder = sortOrder,
            isLoading = isLoading,
            errorMessage = errorMessage,
            exercises = exercises,
            activeFiltersCount = getActiveFiltersCount(filter)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExerciseManagerUiState()
    )

    init {
        // 初期データ読み込み
        loadExercises()
        
        // 検索・フィルター変更時の自動更新
        combine(searchQuery, currentFilter, sortOrder) { query, filter, sort ->
            Triple(query, filter, sort)
        }.distinctUntilChanged()
            .onEach { (query, filter, sort) ->
                searchExercises(query, filter, sort)
            }
            .launchIn(viewModelScope)
    }

    /**
     * 検索クエリを更新
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * フィルターを適用
     */
    fun applyFilter(filter: ExerciseFilter, sort: ExerciseSortOrder) {
        _currentFilter.value = filter
        _sortOrder.value = sort
    }

    /**
     * フィルターをクリア
     */
    fun clearFilter() {
        _currentFilter.value = ExerciseFilter()
        _sortOrder.value = ExerciseSortOrder.NAME_ASC
    }

    /**
     * カスタムエクササイズを追加
     */
    fun addCustomExercise(exerciseTemplate: ExerciseTemplate) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val userCreatedExercise = exerciseTemplate.copy(isUserCreated = true)
                exerciseTemplateRepository.saveExerciseTemplate(userCreatedExercise)
                
                // キャッシュをクリアして最新データを保証
                clearCache()
                
                // リストを再読み込み
                loadExercises()
                
            } catch (e: Exception) {
                _errorMessage.value = "エクササイズの追加に失敗しました: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * エクササイズを編集
     */
    fun editExercise(exerciseTemplate: ExerciseTemplate) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // ユーザー作成のエクササイズのみ編集可能
                if (exerciseTemplate.isUserCreated) {
                    exerciseTemplateRepository.updateExerciseTemplate(exerciseTemplate)
                    
                    // キャッシュをクリアして最新データを保証
                    clearCache()
                    loadExercises()
                } else {
                    _errorMessage.value = "事前定義されたエクササイズは編集できません"
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "エクササイズの編集に失敗しました: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * エクササイズを削除
     */
    fun deleteExercise(exerciseTemplate: ExerciseTemplate) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // ユーザー作成のエクササイズのみ削除可能
                if (exerciseTemplate.isUserCreated) {
                    exerciseTemplateRepository.deleteExerciseTemplateById(exerciseTemplate.id)
                    
                    // キャッシュをクリアして最新データを保証
                    clearCache()
                    loadExercises()
                } else {
                    _errorMessage.value = "事前定義されたエクササイズは削除できません"
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "エクササイズの削除に失敗しました: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * エラーメッセージをクリア
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * 全エクササイズを読み込み
     */
    private fun loadExercises() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                exerciseTemplateRepository.getAllExerciseTemplatesFlow()
                    .collect { exerciseList ->
                        _exercises.value = exerciseList
                    }
                    
            } catch (e: Exception) {
                _errorMessage.value = "エクササイズの読み込みに失敗しました: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 検索・フィルタリングされたエクササイズを取得（キャッシュ対応）
     */
    private fun searchExercises(
        query: String,
        filter: ExerciseFilter,
        sortOrder: ExerciseSortOrder
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val cacheKey = generateCacheKey(query, filter, sortOrder)
                
                // キャッシュチェック
                val cachedResult = getCachedResult(cacheKey)
                if (cachedResult != null) {
                    _exercises.value = cachedResult
                    _isLoading.value = false
                    return@launch
                }
                
                // キャッシュにない場合は新しく検索
                val searchFilter = filter.copy(searchQuery = query)
                val exerciseList = exerciseTemplateRepository.searchExerciseTemplates(
                    filter = searchFilter,
                    sortOrder = sortOrder
                )
                
                // 結果をキャッシュに保存
                setCachedResult(cacheKey, exerciseList)
                _exercises.value = exerciseList
                
            } catch (e: Exception) {
                _errorMessage.value = "エクササイズの検索に失敗しました: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * キャッシュキーを生成
     */
    private fun generateCacheKey(
        query: String,
        filter: ExerciseFilter,
        sortOrder: ExerciseSortOrder
    ): String {
        return "${query}_${filter.hashCode()}_${sortOrder.name}"
    }
    
    /**
     * キャッシュから結果を取得（タイムアウトチェック付き）
     */
    private fun getCachedResult(cacheKey: String): List<ExerciseTemplate>? {
        val timestamp = cacheTimestamps[cacheKey] ?: return null
        val currentTime = System.currentTimeMillis()
        
        return if (currentTime - timestamp < cacheTimeout) {
            searchCache[cacheKey]
        } else {
            // タイムアウトしたキャッシュを削除
            searchCache.remove(cacheKey)
            cacheTimestamps.remove(cacheKey)
            null
        }
    }
    
    /**
     * 結果をキャッシュに保存
     */
    private fun setCachedResult(cacheKey: String, result: List<ExerciseTemplate>) {
        searchCache[cacheKey] = result
        cacheTimestamps[cacheKey] = System.currentTimeMillis()
        
        // キャッシュサイズ制限（最大50エントリ）
        if (searchCache.size > 50) {
            val oldestKey = cacheTimestamps.minByOrNull { it.value }?.key
            oldestKey?.let {
                searchCache.remove(it)
                cacheTimestamps.remove(it)
            }
        }
    }
    
    /**
     * キャッシュをクリア
     */
    fun clearCache() {
        searchCache.clear()
        cacheTimestamps.clear()
    }
}

/**
 * エクササイズ管理画面のUI状態
 */
data class ExerciseManagerUiState(
    val searchQuery: String = "",
    val currentFilter: ExerciseFilter = ExerciseFilter(),
    val sortOrder: ExerciseSortOrder = ExerciseSortOrder.NAME_ASC,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val exercises: List<ExerciseTemplate> = emptyList(),
    val activeFiltersCount: Int = 0
)

/**
 * アクティブなフィルター数を取得するヘルパー関数
 */
private fun getActiveFiltersCount(filter: ExerciseFilter): Int {
    var count = 0
    if (filter.category != null) count++
    if (filter.equipment != null) count++
    if (filter.difficulty != null) count++
    if (!filter.muscle.isNullOrBlank()) count++
    if (!filter.showUserCreated) count++
    return count
}