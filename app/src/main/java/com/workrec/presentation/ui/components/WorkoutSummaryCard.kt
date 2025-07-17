package com.workrec.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.workrec.domain.entities.Workout

/**
 * カレンダービュー用のコンパクトなワークアウトサマリーカード
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSummaryCard(
    workout: Workout,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // メトリクス行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // エクササイズ数とセット数
                Text(
                    text = "${workout.exercises.size}種目・${workout.totalSets}セット",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // 総ボリューム
                if (workout.totalVolume > 0) {
                    Text(
                        text = "${String.format("%.0f", workout.totalVolume)}kg",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // エクササイズ名のプレビュー（最大2つまで）
            if (workout.exercises.isNotEmpty()) {
                val exerciseNames = workout.exercises.map { it.name }.take(2)
                val displayText = if (workout.exercises.size > 2) {
                    exerciseNames.joinToString(", ") + " など"
                } else {
                    exerciseNames.joinToString(", ")
                }
                
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 時間情報（もしあれば）
            workout.totalDuration?.let { duration ->
                Text(
                    text = "${duration.inWholeMinutes}分",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * より小さなワークアウトサマリーカード（カレンダーセル内用）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactWorkoutSummaryCard(
    workout: Workout,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // 基本メトリクス
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${workout.exercises.size}種目",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (workout.totalVolume > 0) {
                    Text(
                        text = "${String.format("%.0f", workout.totalVolume)}kg",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 主要エクササイズ名（1つのみ）
            if (workout.exercises.isNotEmpty()) {
                val primaryExercise = workout.exercises.first().name
                Text(
                    text = primaryExercise,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}