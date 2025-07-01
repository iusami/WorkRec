package com.workrec.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workrec.domain.entities.Goal
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.datetime.daysUntil

/**
 * 目標表示用のカードコンポーネント
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalCard(
    goal: Goal,
    onClick: () -> Unit,
    onProgressUpdate: ((Long, Double) -> Unit)? = null,
    onEdit: ((Long) -> Unit)? = null,
    onDelete: ((Long) -> Unit)? = null,
    showActions: Boolean = true,
    modifier: Modifier = Modifier
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val isOverdue = goal.deadline?.let { it < today && !goal.isCompleted } ?: false
    val isDueSoon = goal.deadline?.let { 
        val daysUntilDeadline = today.daysUntil(it)
        daysUntilDeadline in 1..7 && !goal.isCompleted
    } ?: false

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isOverdue -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                isDueSoon -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                goal.isCompleted -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ヘッダー行（タイトル、タイプ、ステータス）
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ElevatedAssistChip(
                            onClick = { },
                            label = { 
                                Text(
                                    text = goal.type.displayName,
                                    style = MaterialTheme.typography.labelSmall
                                ) 
                            }
                        )
                        
                        // ステータスバッジ
                        when {
                            goal.isCompleted -> {
                                AssistChip(
                                    onClick = { },
                                    label = { Text("完了") },
                                    leadingIcon = {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                )
                            }
                            isOverdue -> {
                                AssistChip(
                                    onClick = { },
                                    label = { Text("期限切れ") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Warning, contentDescription = null)
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    )
                                )
                            }
                            isDueSoon -> {
                                AssistChip(
                                    onClick = { },
                                    label = { Text("期限間近") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Schedule, contentDescription = null)
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 進捗セクション
            GoalProgressSection(goal = goal)

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

            // 期限情報
            goal.deadline?.let { deadline ->
                val daysUntilDeadline = today.daysUntil(deadline)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when {
                            isOverdue -> "期限: $deadline (${-daysUntilDeadline}日経過)"
                            daysUntilDeadline == 0 -> "期限: $deadline (今日)"
                            daysUntilDeadline > 0 -> "期限: $deadline (あと${daysUntilDeadline}日)"
                            else -> "期限: $deadline"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            isOverdue -> MaterialTheme.colorScheme.error
                            isDueSoon -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // アクションボタン
            if (showActions && !goal.isCompleted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    onProgressUpdate?.let { updateCallback ->
                        OutlinedButton(
                            onClick = { 
                                // TODO: 進捗入力ダイアログを表示
                                updateCallback(goal.id, goal.currentValue + 1.0)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("進捗追加")
                        }
                    }
                    
                    onEdit?.let { editCallback ->
                        OutlinedButton(
                            onClick = { editCallback(goal.id) }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "編集")
                        }
                    }
                    
                    onDelete?.let { deleteCallback ->
                        OutlinedButton(
                            onClick = { deleteCallback(goal.id) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "削除")
                        }
                    }
                }
            }
        }
    }
}

/**
 * 目標の進捗を表示するセクション
 */
@Composable
private fun GoalProgressSection(
    goal: Goal,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 進捗バー
        LinearProgressIndicator(
            progress = { goal.progressPercentage },
            modifier = Modifier.fillMaxWidth(),
            color = when {
                goal.isCompleted -> MaterialTheme.colorScheme.secondary
                goal.progressPercentage >= 0.8f -> MaterialTheme.colorScheme.primary
                goal.progressPercentage >= 0.5f -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline
            }
        )
        
        // 進捗テキスト
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${String.format("%.1f", goal.currentValue)} / ${String.format("%.1f", goal.targetValue)} ${goal.unit}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "${String.format("%.1f", goal.progressPercentage * 100)}%",
                style = MaterialTheme.typography.labelLarge,
                color = when {
                    goal.isCompleted -> MaterialTheme.colorScheme.secondary
                    goal.progressPercentage >= 0.8f -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = FontWeight.Bold
            )
        }
        
        // 残り値の表示（完了していない場合のみ）
        if (!goal.isCompleted && goal.remainingValue > 0) {
            Text(
                text = "残り ${String.format("%.1f", goal.remainingValue)} ${goal.unit}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}