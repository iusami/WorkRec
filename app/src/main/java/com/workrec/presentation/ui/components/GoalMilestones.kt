package com.workrec.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workrec.domain.entities.GoalProgress
import kotlinx.datetime.LocalDate

/**
 * 目標達成マイルストーン表示コンポーネント
 */
@Composable
fun GoalMilestonesTimeline(
    goalProgress: GoalProgress,
    modifier: Modifier = Modifier,
    shouldPlayAnimations: Boolean = true
) {
    val milestones = remember(goalProgress) {
        generateMilestones(goalProgress)
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ヘッダー
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "マイルストーン",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = goalProgress.goal.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // マイルストーンタイムライン
            if (milestones.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(milestones) { milestone ->
                        MilestoneItem(
                            milestone = milestone,
                            isFirst = milestones.first() == milestone,
                            isLast = milestones.last() == milestone,
                            shouldPlayAnimations = shouldPlayAnimations
                        )
                    }
                }
            } else {
                EmptyMilestonesState()
            }
        }
    }
}

/**
 * マイルストーンアイテム
 */
@Composable
private fun MilestoneItem(
    milestone: Milestone,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    shouldPlayAnimations: Boolean = true
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedScale by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0.8f,
        animationSpec = tween(durationMillis = 600),
        label = "scale_animation"
    )
    
    LaunchedEffect(milestone) {
        kotlinx.coroutines.delay(100) // 少し遅延して順次アニメーション
        animationPlayed = true
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // タイムライン部分
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            // 上のライン
            if (!isFirst) {
                Canvas(
                    modifier = Modifier
                        .width(2.dp)
                        .height(20.dp)
                ) {
                    drawLine(
                        color = if (milestone.status == MilestoneStatus.COMPLETED) {
                            Color(0xFF4CAF50)
                        } else {
                            Color(0xFFE0E0E0)
                        },
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
            
            // マイルストーンマーカー
            MilestoneMarker(
                milestone = milestone,
                scale = animatedScale,
                shouldPlayAnimations = shouldPlayAnimations
            )
            
            // 下のライン
            if (!isLast) {
                Canvas(
                    modifier = Modifier
                        .width(2.dp)
                        .height(20.dp)
                ) {
                    drawLine(
                        color = if (milestone.status == MilestoneStatus.COMPLETED) {
                            Color(0xFF4CAF50)
                        } else {
                            Color(0xFFE0E0E0)
                        },
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // マイルストーン詳細
        MilestoneDetails(
            milestone = milestone,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * マイルストーンマーカー
 */
@Composable
private fun MilestoneMarker(
    milestone: Milestone,
    scale: Float,
    shouldPlayAnimations: Boolean = true
) {
    Box(
        modifier = Modifier
            .size((24 * scale).dp)
            .clip(CircleShape)
            .background(
                when (milestone.status) {
                    MilestoneStatus.COMPLETED -> Color(0xFF4CAF50)
                    MilestoneStatus.CURRENT -> MaterialTheme.colorScheme.primary
                    MilestoneStatus.UPCOMING -> Color(0xFFE0E0E0)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when (milestone.status) {
            MilestoneStatus.COMPLETED -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
            MilestoneStatus.CURRENT -> {
                // 脈動するドット（スクロール制御対応）
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = if (shouldPlayAnimations) 0.3f else 0.6f,
                    targetValue = if (shouldPlayAnimations) 1f else 0.6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = if (shouldPlayAnimations) 1000 else Int.MAX_VALUE
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha"
                )
                
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = alpha))
                )
            }
            MilestoneStatus.UPCOMING -> {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
            }
        }
    }
}

/**
 * マイルストーン詳細
 */
@Composable
private fun MilestoneDetails(
    milestone: Milestone,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (milestone.status) {
                MilestoneStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                MilestoneStatus.CURRENT -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                MilestoneStatus.UPCOMING -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = when (milestone.status) {
                        MilestoneStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                        MilestoneStatus.CURRENT -> MaterialTheme.colorScheme.primary
                        MilestoneStatus.UPCOMING -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                // 進捗率バッジ
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (milestone.status) {
                        MilestoneStatus.COMPLETED -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                        MilestoneStatus.CURRENT -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        MilestoneStatus.UPCOMING -> Color.Gray.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = "${milestone.percentage}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = when (milestone.status) {
                            MilestoneStatus.COMPLETED -> Color(0xFF4CAF50)
                            MilestoneStatus.CURRENT -> MaterialTheme.colorScheme.primary
                            MilestoneStatus.UPCOMING -> Color.Gray
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            if (milestone.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = milestone.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            milestone.targetDate?.let { date ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "予定日: $date",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 空のマイルストーン状態
 */
@Composable
private fun EmptyMilestonesState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Flag,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "マイルストーンを設定しましょう",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "目標を小さなステップに分けて達成しやすくしましょう",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * マイルストーンデータクラス
 */
data class Milestone(
    val title: String,
    val description: String,
    val percentage: Int,
    val targetValue: Double,
    val status: MilestoneStatus,
    val targetDate: LocalDate? = null,
    val achievedDate: LocalDate? = null
)

/**
 * マイルストーンステータス
 */
enum class MilestoneStatus {
    COMPLETED,  // 完了済み
    CURRENT,    // 現在進行中
    UPCOMING    // 今後予定
}

/**
 * マイルストーンを生成
 */
private fun generateMilestones(goalProgress: GoalProgress): List<Milestone> {
    val goal = goalProgress.goal
    val currentProgress = goal.progressPercentage
    
    val milestonePercentages = listOf(25, 50, 75, 100)
    
    return milestonePercentages.map { percentage ->
        val targetValue = goal.targetValue * (percentage / 100.0)
        val isCompleted = currentProgress >= (percentage / 100.0)
        val isCurrent = !isCompleted && currentProgress >= ((percentage - 25) / 100.0)
        
        Milestone(
            title = when (percentage) {
                25 -> "スタートダッシュ"
                50 -> "中間地点"
                75 -> "最終段階"
                100 -> "目標達成"
                else -> "${percentage}%地点"
            },
            description = "${targetValue.toInt()} ${goal.unit}に到達",
            percentage = percentage,
            targetValue = targetValue,
            status = when {
                isCompleted -> MilestoneStatus.COMPLETED
                isCurrent -> MilestoneStatus.CURRENT
                else -> MilestoneStatus.UPCOMING
            },
            targetDate = goal.deadline // 簡易実装、実際は期間を4分割する
        )
    }
}