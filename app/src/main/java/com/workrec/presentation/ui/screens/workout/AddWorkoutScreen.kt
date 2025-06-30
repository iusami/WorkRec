package com.workrec.presentation.ui.screens.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
// import androidx.hilt.navigation.compose.hiltViewModel  // 一時的に無効化
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workrec.R
import com.workrec.domain.entities.*
import com.workrec.presentation.ui.components.ExerciseForm
import com.workrec.presentation.viewmodel.AddWorkoutViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * ワークアウト追加画面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddWorkoutViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 日付選択ダイアログの状態
    var showDatePicker by remember { mutableStateOf(false) }

    // 成功時のナビゲーション
    LaunchedEffect(uiState.isWorkoutSaved) {
        if (uiState.isWorkoutSaved) {
            onNavigateBack()
        }
    }

    // エラーハンドリング
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // TODO: Snackbarでエラー表示
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_workout)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "戻る"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveWorkout() },
                        enabled = !uiState.isLoading && uiState.exercises.isNotEmpty()
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.addExercise() }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "エクササイズを追加"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 日付選択セクション
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "実施日",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = uiState.selectedDate.toString(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "日付を選択"
                        )
                    }
                }
            }

            // メモ入力セクション
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.updateNotes(it) },
                    label = { Text(stringResource(R.string.workout_notes)) },
                    placeholder = { Text("今日のワークアウトメモ") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // エクササイズリスト
            if (uiState.exercises.isEmpty()) {
                // 空の状態
                EmptyExerciseState(
                    onAddExercise = { viewModel.addExercise() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(uiState.exercises) { index, exercise ->
                        ExerciseForm(
                            exercise = exercise,
                            onExerciseUpdate = { updatedExercise ->
                                viewModel.updateExercise(index, updatedExercise)
                            },
                            onExerciseDelete = { 
                                viewModel.removeExercise(index)
                            }
                        )
                    }
                    
                    // 最後にスペースを追加（FABとの重複を避ける）
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // ローディング表示
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    // 日付選択ダイアログ
    if (showDatePicker) {
        DatePickerDialog(
            selectedDate = uiState.selectedDate,
            onDateSelected = { date ->
                viewModel.updateSelectedDate(date)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

/**
 * エクササイズが空の場合の表示
 */
@Composable
private fun EmptyExerciseState(
    onAddExercise: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "エクササイズを追加しましょう",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "+ ボタンを押してエクササイズを追加してください",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddExercise) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("エクササイズを追加")
        }
    }
}

/**
 * 日付選択ダイアログ
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    // TODO: Material3 DatePickerの実装
    // 現在は簡易的なダイアログで代用
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("日付を選択") },
        text = { 
            Text("現在選択: $selectedDate\n\n※日付選択機能は今後実装予定です") 
        },
        confirmButton = {
            TextButton(onClick = { onDateSelected(selectedDate) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}