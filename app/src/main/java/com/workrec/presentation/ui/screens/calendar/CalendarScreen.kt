package com.workrec.presentation.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workrec.R
import com.workrec.presentation.viewmodel.CalendarViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * カレンダー画面 - ワークアウト履歴の可視化と日付選択
 * Material 3 DatePickerを使用したシンプルな実装
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.calendar)) },
                actions = {
                    // 今日に戻るボタン
                    IconButton(
                        onClick = { 
                            viewModel.onDateSelected(today)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "今日"
                        )
                    }
                    
                    // 日付選択ボタン
                    IconButton(
                        onClick = { showDatePicker = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "日付選択"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 選択日の詳細表示
            item {
                uiState.selectedDate?.let { selectedDate ->
                    SelectedDateCard(
                        selectedDate = selectedDate,
                        workouts = uiState.selectedDateWorkouts,
                        isLoading = uiState.isLoadingWorkouts,
                        onDatePickerClick = { showDatePicker = true }
                    )
                }
            }
            
            // ワークアウト統計サマリー
            item {
                if (uiState.workoutDates.isNotEmpty()) {
                    WorkoutStatsSummaryCard(
                        totalWorkouts = uiState.workoutDates.size,
                        currentStreak = uiState.currentStreak,
                        longestStreak = uiState.longestStreak
                    )
                }
            }
            
            // ワークアウト日付一覧
            item {
                if (uiState.workoutDates.isNotEmpty()) {
                    WorkoutDatesListCard(
                        workoutDates = uiState.workoutDates,
                        selectedDate = uiState.selectedDate,
                        onDateSelected = viewModel::onDateSelected
                    )
                }
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.selectedDate?.let { date ->
                // Convert LocalDate to milliseconds
                java.time.LocalDate.of(date.year, date.monthNumber, date.dayOfMonth)
                    .atStartOfDay(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }
        )
        
        DatePickerDialog(
            onDateSelected = { dateMillis ->
                dateMillis?.let { millis ->
                    val instant = java.time.Instant.ofEpochMilli(millis)
                    val localDate = instant.atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    val kotlinDate = LocalDate(localDate.year, localDate.monthValue, localDate.dayOfMonth)
                    viewModel.onDateSelected(kotlinDate)
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * DatePicker Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onDateSelected(null) }) {
                Text("確定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        },
        text = content
    )
}

/**
 * ワークアウト日付一覧カード
 */
@Composable
private fun WorkoutDatesListCard(
    workoutDates: Set<LocalDate>,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "ワークアウト実施日 (${workoutDates.size}日)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            if (workoutDates.isEmpty()) {
                Text(
                    text = "まだワークアウト記録がありません",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val sortedDates = workoutDates.sortedDescending().take(10) // 最新10件
                sortedDates.forEach { date ->
                    WorkoutDateItem(
                        date = date,
                        isSelected = date == selectedDate,
                        onClick = { onDateSelected(date) }
                    )
                }
                
                if (workoutDates.size > 10) {
                    Text(
                        text = "他 ${workoutDates.size - 10}日",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * ワークアウト日付アイテム
 */
@Composable
private fun WorkoutDateItem(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${date.year}年${date.monthNumber}月${date.dayOfMonth}日",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            
            val dayOfWeek = when (date.dayOfWeek.ordinal) {
                0 -> "月"
                1 -> "火"
                2 -> "水"
                3 -> "木"
                4 -> "金"
                5 -> "土"
                6 -> "日"
                else -> ""
            }
            
            Text(
                text = "($dayOfWeek)",
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

/**
 * 選択日の詳細カード
 */
@Composable
private fun SelectedDateCard(
    selectedDate: LocalDate,
    workouts: List<String>, // TODO: 実際のWorkoutエンティティに置き換え
    isLoading: Boolean,
    onDatePickerClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // クリック可能な日付表示
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDatePickerClick() }
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedDate.year}年${selectedDate.monthNumber}月${selectedDate.dayOfMonth}日",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "日付を変更",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            when {
                isLoading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Text(
                            text = "読み込み中...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                workouts.isEmpty() -> {
                    Text(
                        text = "この日はワークアウトを行っていません",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    Text(
                        text = "ワークアウト記録 (${workouts.size}件)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    workouts.forEach { workout ->
                        Text(
                            text = "• $workout",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * ワークアウト統計サマリーカード
 */
@Composable
private fun WorkoutStatsSummaryCard(
    totalWorkouts: Int,
    currentStreak: Int,
    longestStreak: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "ワークアウト統計",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "総回数",
                    value = "${totalWorkouts}回"
                )
                StatItem(
                    label = "現在のストリーク",
                    value = "${currentStreak}日"
                )
                StatItem(
                    label = "最長ストリーク",
                    value = "${longestStreak}日"
                )
            }
        }
    }
}

/**
 * 統計アイテム
 */
@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}