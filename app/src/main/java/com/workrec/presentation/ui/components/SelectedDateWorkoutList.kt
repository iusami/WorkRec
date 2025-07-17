package com.workrec.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workrec.domain.entities.Workout
import kotlinx.datetime.LocalDate

/**
 * 選択された日付のワークアウト一覧を表示するコンポーネント
 */
@Composable
fun SelectedDateWorkoutList(
    selectedDate: LocalDate?,
    workouts: List<Workout>,
    isLoading: Boolean,
    onWorkoutClick: (Long) -> Unit,
    onWorkoutDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // 選択日付のヘッダー
        selectedDate?.let { date ->
            Text(
                text = "${date.year}年${date.monthNumber}月${date.dayOfMonth}日のワークアウト",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        when {
            isLoading -> {
                // ローディング状態
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            workouts.isEmpty() -> {
                // 空の状態
                EmptyWorkoutState(
                    selectedDate = selectedDate,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            else -> {
                // ワークアウト一覧
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(workouts) { workout ->
                        WorkoutCard(
                            workout = workout,
                            onClick = { onWorkoutClick(workout.id) },
                            onDelete = { onWorkoutDelete(workout.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * ワークアウトが存在しない日付の空の状態を表示
 */
@Composable
private fun EmptyWorkoutState(
    selectedDate: LocalDate?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        
        Text(
            text = if (selectedDate != null) {
                "この日はワークアウトが記録されていません"
            } else {
                "日付を選択してワークアウトを確認してください"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        selectedDate?.let {
            Text(
                text = "新しいワークアウトを追加するには、右下の＋ボタンをタップしてください",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}