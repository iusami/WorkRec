package com.workrec.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workrec.domain.entities.Goal

/**
 * 目標表示用のカードコンポーネント
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalCard(
    goal: Goal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // タイトルとタイプ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                ElevatedAssistChip(
                    onClick = { },
                    label = { 
                        Text(
                            text = goal.type.displayName,
                            style = MaterialTheme.typography.labelSmall
                        ) 
                    }
                )
            }

            // 進捗バー
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${String.format("%.1f", goal.currentValue)} / ${String.format("%.1f", goal.targetValue)} ${goal.unit}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${(goal.progressPercentage * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                LinearProgressIndicator(
                    progress = { goal.progressPercentage },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // 説明文
            goal.description?.let { description ->
                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 期限
            goal.deadline?.let { deadline ->
                Text(
                    text = "期限: ${deadline}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 完了状態
            if (goal.isCompleted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Badge {
                        Text("完了")
                    }
                }
            }
        }
    }
}