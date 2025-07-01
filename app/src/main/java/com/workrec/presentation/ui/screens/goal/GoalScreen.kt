package com.workrec.presentation.ui.screens.goal

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workrec.R
import com.workrec.domain.entities.Goal
import com.workrec.domain.entities.GoalType
import com.workrec.presentation.ui.components.GoalCard
import com.workrec.presentation.viewmodel.GoalViewModel
import com.workrec.presentation.viewmodel.CompletionFilter
import com.workrec.presentation.viewmodel.SortBy
import kotlinx.coroutines.launch

/**
 * 目標画面
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GoalScreen(
    onNavigateToAddGoal: () -> Unit,
    onNavigateToGoalDetail: (Long) -> Unit,
    viewModel: GoalViewModel
) {
    val activeGoals by viewModel.activeGoals.collectAsStateWithLifecycle()
    val completedGoals by viewModel.completedGoals.collectAsStateWithLifecycle()
    val filteredGoals by viewModel.filteredGoals.collectAsStateWithLifecycle()
    val goalStats by viewModel.goalStats.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    
    var showFilterBottomSheet by remember { mutableStateOf(false) }
    var showSortBottomSheet by remember { mutableStateOf(false) }
    
    // タブの状態管理
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    
    val tabTitles = listOf("すべて", "進行中", "完了済み")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.goal)) },
                    actions = {
                        // フィルターボタン
                        IconButton(
                            onClick = { showFilterBottomSheet = true }
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "フィルター")
                        }
                        
                        // ソートボタン
                        IconButton(
                            onClick = { showSortBottomSheet = true }
                        ) {
                            Icon(Icons.Default.Sort, contentDescription = "ソート")
                        }
                    }
                )
                
                // 検索バー
                SearchBar(
                    query = filterState.searchQuery,
                    onQueryChange = viewModel::updateSearchQuery,
                    onClearQuery = { viewModel.updateSearchQuery("") }
                )
                
                // タブ
                TabRow(
                    selectedTabIndex = pagerState.currentPage
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { Text(title) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddGoal
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 統計カード
            GoalStatsCard(
                stats = goalStats,
                modifier = Modifier.padding(16.dp)
            )
            
            // タブのコンテンツ
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> GoalListContent(
                        goals = filteredGoals,
                        onGoalClick = onNavigateToGoalDetail,
                        onProgressUpdate = viewModel::updateGoalProgress,
                        onEditGoal = { /* TODO: 編集画面への遷移 */ },
                        onDeleteGoal = viewModel::deleteGoal,
                        emptyMessage = "目標がありません",
                        onAddGoal = onNavigateToAddGoal,
                        isLoading = uiState.isLoading
                    )
                    1 -> GoalListContent(
                        goals = activeGoals,
                        onGoalClick = onNavigateToGoalDetail,
                        onProgressUpdate = viewModel::updateGoalProgress,
                        onEditGoal = { /* TODO: 編集画面への遷移 */ },
                        onDeleteGoal = viewModel::deleteGoal,
                        emptyMessage = "進行中の目標がありません",
                        onAddGoal = onNavigateToAddGoal,
                        isLoading = uiState.isLoading
                    )
                    2 -> GoalListContent(
                        goals = completedGoals,
                        onGoalClick = onNavigateToGoalDetail,
                        onProgressUpdate = null, // 完了済みなので進捗更新は不要
                        onEditGoal = { /* TODO: 編集画面への遷移 */ },
                        onDeleteGoal = viewModel::deleteGoal,
                        emptyMessage = "完了した目標がありません",
                        onAddGoal = onNavigateToAddGoal,
                        isLoading = uiState.isLoading
                    )
                }
            }
        }
        
        // フィルターボトムシート
        if (showFilterBottomSheet) {
            FilterBottomSheet(
                currentFilter = filterState,
                onFilterChange = { newFilter ->
                    viewModel.updateTypeFilter(newFilter.selectedTypes)
                    viewModel.updateCompletionFilter(newFilter.completionFilter)
                },
                onDismiss = { showFilterBottomSheet = false }
            )
        }
        
        // ソートボトムシート
        if (showSortBottomSheet) {
            SortBottomSheet(
                currentSortBy = filterState.sortBy,
                currentAscending = filterState.ascending,
                onSortChange = { sortBy, ascending ->
                    viewModel.updateSortOrder(sortBy, ascending)
                },
                onDismiss = { showSortBottomSheet = false }
            )
        }
    }
}

/**
 * 目標が空の場合の表示
 */
@Composable
private fun EmptyGoalState(
    onAddGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "まだ目標が設定されていません",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "最初の目標を設定しましょう！",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = onAddGoal
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("目標を追加")
        }
    }
}

/**
 * 検索バー
 */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("目標を検索...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "検索")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearQuery) {
                    Icon(Icons.Default.Clear, contentDescription = "クリア")
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
            }
        ),
        singleLine = true
    )
}

/**
 * 目標統計カード
 */
@Composable
private fun GoalStatsCard(
    stats: com.workrec.presentation.viewmodel.GoalStats,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "目標の概要",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatsItem(
                    label = "総数",
                    value = "${stats.totalGoals}",
                    icon = Icons.Default.Assignment
                )
                StatsItem(
                    label = "進行中",
                    value = "${stats.activeGoals}",
                    icon = Icons.Default.TrendingUp
                )
                StatsItem(
                    label = "完了",
                    value = "${stats.completedGoals}",
                    icon = Icons.Default.CheckCircle
                )
                StatsItem(
                    label = "達成率",
                    value = "${String.format("%.1f", stats.completionRate * 100)}%",
                    icon = Icons.Default.BarChart
                )
            }
        }
    }
}

/**
 * 統計項目
 */
@Composable
private fun StatsItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 目標リストコンテンツ
 */
@Composable
private fun GoalListContent(
    goals: List<Goal>,
    onGoalClick: (Long) -> Unit,
    onProgressUpdate: ((Long, Double) -> Unit)?,
    onEditGoal: (Long) -> Unit,
    onDeleteGoal: (Long) -> Unit,
    emptyMessage: String,
    onAddGoal: () -> Unit,
    isLoading: Boolean
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            goals.isEmpty() -> {
                EmptyGoalState(
                    message = emptyMessage,
                    onAddGoal = onAddGoal,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(goals) { goal ->
                        GoalCard(
                            goal = goal,
                            onClick = { onGoalClick(goal.id) },
                            onProgressUpdate = onProgressUpdate,
                            onEdit = onEditGoal,
                            onDelete = onDeleteGoal
                        )
                    }
                }
            }
        }
    }
}

/**
 * 空の状態表示（オーバーロード版）
 */
@Composable
private fun EmptyGoalState(
    message: String,
    onAddGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "新しい目標を設定してみましょう！",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onAddGoal) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("目標を追加")
        }
    }
}

/**
 * フィルターボトムシート
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    currentFilter: com.workrec.presentation.viewmodel.GoalFilterState,
    onFilterChange: (com.workrec.presentation.viewmodel.GoalFilterState) -> Unit,
    onDismiss: () -> Unit
) {
    var tempFilter by remember { mutableStateOf(currentFilter) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "フィルター",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // 目標タイプフィルター
            Text(
                text = "目標タイプ",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(GoalType.values()) { type ->
                    FilterChip(
                        onClick = {
                            val newTypes = if (type in tempFilter.selectedTypes) {
                                tempFilter.selectedTypes - type
                            } else {
                                tempFilter.selectedTypes + type
                            }
                            tempFilter = tempFilter.copy(selectedTypes = newTypes)
                        },
                        label = { Text(type.displayName) },
                        selected = type in tempFilter.selectedTypes,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 完了状態フィルター
            Text(
                text = "完了状態",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            CompletionFilter.values().forEach { filter ->
                FilterChip(
                    onClick = {
                        tempFilter = tempFilter.copy(completionFilter = filter)
                    },
                    label = { Text(filter.displayName) },
                    selected = tempFilter.completionFilter == filter,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // ボタン
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("キャンセル")
                }
                
                Button(
                    onClick = {
                        onFilterChange(tempFilter)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("適用")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * ソートボトムシート
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortBottomSheet(
    currentSortBy: SortBy,
    currentAscending: Boolean,
    onSortChange: (SortBy, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "ソート",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            SortBy.values().forEach { sortBy ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentSortBy == sortBy,
                        onClick = {
                            onSortChange(sortBy, currentAscending)
                            onDismiss()
                        }
                    )
                    Text(
                        text = sortBy.displayName,
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 昇順・降順の選択
            Text(
                text = "順序",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentAscending,
                    onClick = {
                        onSortChange(currentSortBy, true)
                        onDismiss()
                    }
                )
                Text(
                    text = "昇順",
                    modifier = Modifier.padding(start = 8.dp)
                )
                
                Spacer(modifier = Modifier.width(24.dp))
                
                RadioButton(
                    selected = !currentAscending,
                    onClick = {
                        onSortChange(currentSortBy, false)
                        onDismiss()
                    }
                )
                Text(
                    text = "降順",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}