package com.workrec.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workrec.domain.entities.*
import com.workrec.presentation.viewmodel.ExerciseManagerViewModel
import kotlinx.coroutines.delay

/**
 * エクササイズテンプレート選択ダイアログ
 * 事前定義されたエクササイズから選択、または新規カスタムエクササイズの作成が可能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerDialog(
    onExerciseSelected: (ExerciseTemplate) -> Unit,
    onDismiss: () -> Unit,
    onNavigateToExerciseManager: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ExerciseManagerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // ダイアログの状態管理
    var selectedTab by remember { mutableStateOf(0) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var isDialogReady by remember { mutableStateOf(false) }
    
    // ダイアログ表示時の遅延読み込み初期化
    LaunchedEffect(Unit) {
        delay(100) // 100ms遅延でUIレスポンシブ性向上
        isDialogReady = true
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // ヘッダー
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "エクササイズを選択",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row {
                        // エクササイズ管理ボタン
                        IconButton(
                            onClick = {
                                onDismiss() // ダイアログを閉じてから管理画面に移動
                                onNavigateToExerciseManager()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "エクササイズ管理"
                            )
                        }
                        
                        // カスタムエクササイズ追加ボタン
                        IconButton(
                            onClick = { showCustomDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "カスタムエクササイズを追加"
                            )
                        }
                        
                        // 閉じるボタン
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "閉じる"
                            )
                        }
                    }
                }

                // タブ選択
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("すべて") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("お気に入り") }
                    )
                }

                // 検索バー
                SearchBarSection(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    modifier = Modifier.padding(16.dp)
                )

                // フィルターチップ
                if (uiState.activeFiltersCount > 0) {
                    FilterChipsSection(
                        filter = uiState.currentFilter,
                        onClearFilter = viewModel::clearFilter,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // エクササイズリスト（遅延読み込み対応）
                if (isDialogReady) {
                    ExerciseListSection(
                        exercises = uiState.exercises,
                        isLoading = uiState.isLoading,
                        onExerciseSelected = onExerciseSelected,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // 初期読み込み中プレースホルダー
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                // フッター（クイックフィルター）
                QuickFilterSection(
                    currentFilter = uiState.currentFilter,
                    onFilterApply = viewModel::applyFilter,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    // カスタムエクササイズ追加ダイアログ（強化版）
    if (showCustomDialog) {
        CustomExerciseDialog(
            onExerciseCreated = { exerciseTemplate ->
                viewModel.addCustomExercise(exerciseTemplate)
                showCustomDialog = false
                onExerciseSelected(exerciseTemplate)
            },
            onDismiss = { showCustomDialog = false }
        )
    }
}

/**
 * 検索バーセクション
 */
@Composable
private fun SearchBarSection(
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
                IconButton(
                    onClick = { onSearchQueryChange("") },
                    modifier = Modifier.semantics {
                        contentDescription = "検索クエリをクリア"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "クリア"
                    )
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "エクササイズ検索フィールド"
            },
        singleLine = true
    )
}

/**
 * フィルターチップセクション
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipsSection(
    filter: ExerciseFilter,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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
        
        filter.equipment?.let { equipment ->
            FilterChip(
                selected = true,
                onClick = onClearFilter,
                label = { Text(equipment.displayName) },
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
 * エクササイズリストセクション
 */
@Composable
private fun ExerciseListSection(
    exercises: List<ExerciseTemplate>,
    isLoading: Boolean,
    onExerciseSelected: (ExerciseTemplate) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            exercises.isEmpty() -> {
                EmptyExerciseState(
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                val lazyListState = rememberLazyListState()
                
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = exercises,
                        key = { exercise -> exercise.id } // キーベースの最適化
                    ) { exercise ->
                        ExercisePickerCard(
                            exerciseTemplate = exercise,
                            onSelect = { onExerciseSelected(exercise) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * エクササイズ選択カード
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExercisePickerCard(
    exerciseTemplate: ExerciseTemplate,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "エクササイズ: ${exerciseTemplate.name}, " +
                        "カテゴリー: ${exerciseTemplate.category.displayName}, " +
                        "対象筋肉: ${exerciseTemplate.muscle}, " +
                        "器具: ${exerciseTemplate.equipment.displayName}, " +
                        "難易度: ${exerciseTemplate.difficulty.displayName}"
                role = Role.Button
            },
        onClick = onSelect
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // エクササイズ名
            Text(
                text = exerciseTemplate.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // カテゴリーと筋肉
            Text(
                text = "${exerciseTemplate.category.displayName} • ${exerciseTemplate.muscle}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 詳細情報
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip(
                    icon = Icons.Default.FitnessCenter,
                    text = exerciseTemplate.equipment.displayName
                )
                InfoChip(
                    icon = Icons.Default.Star,
                    text = exerciseTemplate.difficulty.displayName
                )
            }
            
            // 説明（あれば）
            exerciseTemplate.description?.let { description ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
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
 * クイックフィルターセクション
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickFilterSection(
    currentFilter: ExerciseFilter,
    onFilterApply: (ExerciseFilter, ExerciseSortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "クイックフィルター",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // カテゴリーフィルター
            FilterChip(
                selected = currentFilter.category == ExerciseCategory.CHEST,
                onClick = {
                    val newFilter = if (currentFilter.category == ExerciseCategory.CHEST) {
                        currentFilter.copy(category = null)
                    } else {
                        currentFilter.copy(category = ExerciseCategory.CHEST)
                    }
                    onFilterApply(newFilter, ExerciseSortOrder.NAME_ASC)
                },
                label = { Text("胸") }
            )
            
            FilterChip(
                selected = currentFilter.category == ExerciseCategory.BACK,
                onClick = {
                    val newFilter = if (currentFilter.category == ExerciseCategory.BACK) {
                        currentFilter.copy(category = null)
                    } else {
                        currentFilter.copy(category = ExerciseCategory.BACK)
                    }
                    onFilterApply(newFilter, ExerciseSortOrder.NAME_ASC)
                },
                label = { Text("背中") }
            )
            
            FilterChip(
                selected = currentFilter.category == ExerciseCategory.LEGS,
                onClick = {
                    val newFilter = if (currentFilter.category == ExerciseCategory.LEGS) {
                        currentFilter.copy(category = null)
                    } else {
                        currentFilter.copy(category = ExerciseCategory.LEGS)
                    }
                    onFilterApply(newFilter, ExerciseSortOrder.NAME_ASC)
                },
                label = { Text("脚") }
            )
        }
    }
}

/**
 * 空の状態表示
 */
@Composable
private fun EmptyExerciseState(
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
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "検索条件を変更してください",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// 旧いCustomExerciseDialogは新しい強化版ファイルで置き換えられました
// /com/workrec/presentation/ui/components/CustomExerciseDialog.kt を参照