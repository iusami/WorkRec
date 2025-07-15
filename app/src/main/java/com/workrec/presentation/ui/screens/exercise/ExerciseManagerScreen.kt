package com.workrec.presentation.ui.screens.exercise

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workrec.R
import com.workrec.domain.entities.*
import com.workrec.presentation.viewmodel.ExerciseManagerViewModel
import com.workrec.presentation.ui.components.EditExerciseDialog
import com.workrec.presentation.ui.components.CustomExerciseDialog

/**
 * エクササイズ管理画面
 * 事前定義されたエクササイズテンプレートの閲覧・管理
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseManagerScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExerciseManagerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editUiState by viewModel.editUiState.collectAsStateWithLifecycle()
    
    // 検索とフィルタの状態
    var showFilterDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    
    // Snackbar用の状態
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = { Text("エクササイズ管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "戻る"
                        )
                    }
                },
                actions = {
                    // フィルターボタン
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "フィルター"
                        )
                    }
                    // エクササイズ追加ボタン
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "エクササイズを追加"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 検索バー
            SearchBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::updateSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // フィルター情報表示
            if (uiState.activeFiltersCount > 0) {
                FilterChipsRow(
                    filter = uiState.currentFilter,
                    onClearFilter = viewModel::clearFilter,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // エクササイズリスト（ローディング状態統合）
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "エクササイズを読み込み中...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                uiState.exercises.isEmpty() -> {
                    EmptyState(
                        onResetFilters = viewModel::clearFilter,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.exercises) { exercise ->
                            ExerciseTemplateCard(
                                exerciseTemplate = exercise,
                                onEdit = { viewModel.openEditDialog(exercise) },
                                onDelete = { viewModel.deleteExercise(exercise) }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // 編集処理中のローディングオーバーレイ
    if (editUiState.isEditDialogVisible && uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "エクササイズを保存中...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    // フィルターダイアログ
    if (showFilterDialog) {
        ExerciseFilterDialog(
            currentFilter = uiState.currentFilter,
            sortOrder = uiState.sortOrder,
            onFilterApply = { filter: ExerciseFilter, sort: ExerciseSortOrder ->
                viewModel.applyFilter(filter, sort)
                showFilterDialog = false
            },
            onDismiss = { showFilterDialog = false }
        )
    }

    // エクササイズ追加ダイアログ（強化版CustomExerciseDialog使用）
    if (showAddDialog) {
        CustomExerciseDialog(
            onExerciseCreated = { exerciseTemplate ->
                viewModel.addCustomExercise(exerciseTemplate)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
    
    // エクササイズ編集ダイアログ（ローディング状態対応）
    editUiState.editingExercise?.let { editingExercise ->
        if (editUiState.isEditDialogVisible) {
            EditExerciseDialog(
                exerciseTemplate = editingExercise,
                onExerciseSaved = { updatedExercise ->
                    viewModel.saveEditedExercise(updatedExercise)
                },
                onDismiss = { viewModel.closeEditDialog() }
            )
        }
    }
    
    // 編集操作結果のスナックバー表示
    editUiState.operationResult?.let { result ->
        when (result) {
            is com.workrec.presentation.viewmodel.EditOperationResult.Success -> {
                LaunchedEffect(result) {
                    snackbarHostState.showSnackbar(
                        message = result.message,
                        actionLabel = "OK",
                        duration = SnackbarDuration.Short
                    )
                    viewModel.clearEditOperationResult()
                }
            }
            is com.workrec.presentation.viewmodel.EditOperationResult.Error -> {
                LaunchedEffect(result) {
                    snackbarHostState.showSnackbar(
                        message = result.message,
                        actionLabel = "閉じる",
                        duration = SnackbarDuration.Long
                    )
                    viewModel.clearEditOperationResult()
                }
            }
        }
    }
    
    // エラーメッセージ表示（従来のエラーハンドリング）
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                actionLabel = "閉じる",
                duration = SnackbarDuration.Long
            )
            viewModel.clearError()
        }
    }
}

/**
 * 検索バー
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = { Text("エクササイズを検索...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "検索"
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "クリア"
                    )
                }
            }
        },
        modifier = modifier,
        singleLine = true
    )
}

/**
 * フィルターチップ行
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipsRow(
    filter: ExerciseFilter,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "フィルター:",
            style = MaterialTheme.typography.labelMedium
        )
        
        filter.category?.let { category ->
            FilterChip(
                selected = true,
                onClick = onClearFilter,
                label = { Text(category.displayName) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "削除",
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
        
    }
}

/**
 * エクササイズテンプレートカード
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseTemplateCard(
    exerciseTemplate: ExerciseTemplate,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = { /* 詳細表示 */ }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // ヘッダー行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exerciseTemplate.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = exerciseTemplate.category.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // ユーザー作成の場合のみ編集・削除ボタン表示
                if (exerciseTemplate.isUserCreated) {
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "編集"
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "削除",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))


            // 説明
            exerciseTemplate.description?.let { description ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * 情報チップ
 */
@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 空の状態
 */
@Composable
private fun EmptyState(
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "エクササイズが見つかりません",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "検索条件を変更するか、フィルターをリセットしてください",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onResetFilters) {
            Text("フィルターをリセット")
        }
    }
}

/**
 * フィルターダイアログ（簡易版）
 */
@Composable
private fun ExerciseFilterDialog(
    currentFilter: ExerciseFilter,
    sortOrder: ExerciseSortOrder,
    onFilterApply: (ExerciseFilter, ExerciseSortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(currentFilter.category) }
    var selectedSortOrder by remember { mutableStateOf(sortOrder) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("フィルター設定") },
        text = {
            Column {
                Text("カテゴリー", style = MaterialTheme.typography.titleSmall)
                // カテゴリー選択UI（簡易実装）
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("ソート順", style = MaterialTheme.typography.titleSmall)
                // ソート選択UI（簡易実装）
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newFilter = currentFilter.copy(
                        category = selectedCategory
                    )
                    onFilterApply(newFilter, selectedSortOrder)
                }
            ) {
                Text("適用")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

// 古いAddExerciseDialogは新しいCustomExerciseDialogで置き換えられました
// /com/workrec/presentation/ui/components/CustomExerciseDialog.kt を使用