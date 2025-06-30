package com.workrec.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workrec.domain.entities.TimePeriod
import com.workrec.domain.entities.WorkoutStatistics

/**
 * 進捗要約表示コンポーネント
 */
@Composable
fun ProgressSummary(
    statistics: WorkoutStatistics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "期間統計",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = statistics.period.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 統計グリッド
            StatisticsGrid(statistics = statistics)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 追加情報
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "最も活発な曜日",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = statistics.mostActiveDay,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column {
                    Text(
                        text = "週間頻度",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format("%.1f", statistics.workoutFrequency)}回/週",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * 統計データをグリッドで表示
 */
@Composable
fun StatisticsGrid(
    statistics: WorkoutStatistics,
    modifier: Modifier = Modifier
) {
    val statItems = listOf(
        StatItem(
            title = "ワークアウト",
            value = "${statistics.totalWorkouts}回",
            icon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) }
        ),
        StatItem(
            title = "総ボリューム",
            value = "${statistics.totalVolume.toInt()}kg",
            icon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) }
        ),
        StatItem(
            title = "平均ボリューム",
            value = "${statistics.averageVolume.toInt()}kg",
            icon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) }
        ),
        StatItem(
            title = "総セット数",
            value = "${statistics.totalSets}セット",
            icon = { Icon(Icons.Default.Repeat, contentDescription = null) }
        ),
        StatItem(
            title = "平均時間",
            value = "${statistics.averageDuration}分",
            icon = { Icon(Icons.Default.Timer, contentDescription = null) }
        )
    )
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(statItems) { item ->
            StatCard(
                title = item.title,
                value = item.value,
                icon = item.icon
            )
        }
    }
}

/**
 * 時間期間選択コンポーネント
 */
@Composable
fun TimePeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimePeriod.values().forEach { period ->
            FilterChip(
                onClick = { onPeriodSelected(period) },
                label = { Text(period.displayName) },
                selected = selectedPeriod == period,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 統計アイテムデータクラス
 */
private data class StatItem(
    val title: String,
    val value: String,
    val icon: @Composable () -> Unit
)