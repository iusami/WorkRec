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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
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
import kotlinx.datetime.Instant
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import java.util.Calendar

/**
 * ワークアウト追加画面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToExerciseManager: () -> Unit = {},
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
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "実施日",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatDateForDisplay(uiState.selectedDate),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        // 今日かどうかの表示
                        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                        if (uiState.selectedDate == today) {
                            Text(
                                text = "今日",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.semantics {
                            contentDescription = "実施日を変更: 現在${formatDateForDisplay(uiState.selectedDate)}"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
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
                            },
                            onNavigateToExerciseManager = onNavigateToExerciseManager
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
 * 日付選択ダイアログ（Material3 DatePicker）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    // DatePickerStateを初期化（選択された日付から開始）
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toEpochMillis(),
        initialDisplayMode = DisplayMode.Picker,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // 今日以前の日付のみ選択可能（未来日制限）
                return utcTimeMillis <= getTodayInMillis()
            }
        }
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(millis.toLocalDate())
                    }
                },
                enabled = datePickerState.selectedDateMillis != null
            ) {
                Text("選択")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            modifier = Modifier.padding(16.dp),
            title = {
                Text(
                    text = "実施日を選択",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
            },
            headline = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val date = millis.toLocalDate()
                    Text(
                        text = formatDateForDisplay(date),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        )
    }
}

/**
 * 日付変換ユーティリティ関数
 */

/**
 * LocalDateをUTC millisecondsに変換
 * Material3 DatePickerで使用
 */
private fun LocalDate.toEpochMillis(): Long {
    return this.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}

/**
 * UTC millisecondsをLocalDateに変換
 * Material3 DatePickerから取得したデータを変換
 */
private fun Long.toLocalDate(): LocalDate {
    return Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date
}

/**
 * 今日の日付をUTC millisecondsで取得
 */
private fun getTodayInMillis(): Long {
    return Clock.System.todayIn(TimeZone.currentSystemDefault()).toEpochMillis()
}

/**
 * 日付を表示用フォーマットに変換
 */
private fun formatDateForDisplay(date: LocalDate): String {
    val dayOfWeek = when (date.dayOfWeek.value) {
        1 -> "月"
        2 -> "火"
        3 -> "水" 
        4 -> "木"
        5 -> "金"
        6 -> "土"
        7 -> "日"
        else -> ""
    }
    return "${date.year}年${date.monthNumber}月${date.dayOfMonth}日（${dayOfWeek}）"
}