package com.workrec.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.workrec.domain.entities.Goal
import com.workrec.domain.entities.GoalProgress
import com.workrec.domain.entities.GoalType
import kotlin.math.cos
import kotlin.math.sin

/**
 * 高度な目標進捗カードコンポーネント
 * アニメーション、グラデーション、詳細統計を含む
 */
@Composable
fun AdvancedGoalCard(
    goalProgress: GoalProgress,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onUpdateProgress: () -> Unit = {}
) {
    val goal = goalProgress.goal
    
    // アニメーション設定
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) goalProgress.progressPercentage else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "progress_animation"
    )
    
    LaunchedEffect(goalProgress) {
        animationPlayed = true
    }
    
    // 目標タイプに応じた色とアイコン
    val (primaryColor, secondaryColor, icon) = getGoalTypeTheme(goal.type)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // ヘッダー部分
            GoalCardHeader(
                goal = goal,
                icon = icon,
                primaryColor = primaryColor,
                onUpdateProgress = onUpdateProgress
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // メイン進捗表示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 円形進捗インジケーター
                AdvancedProgressRing(
                    progress = animatedProgress,
                    size = 100.dp,
                    strokeWidth = 12.dp,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor
                )
                
                // 進捗詳細情報
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    GoalProgressDetails(
                        goalProgress = goalProgress,
                        primaryColor = primaryColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // フッター統計
            GoalCardFooter(
                goalProgress = goalProgress,
                primaryColor = primaryColor
            )
        }
    }
}

/**
 * 目標カードヘッダー
 */
@Composable
private fun GoalCardHeader(
    goal: Goal,
    icon: ImageVector,
    primaryColor: Color,
    onUpdateProgress: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // アイコン
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.2f),
                                primaryColor.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // タイトルと説明
            Column {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                goal.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        
        // アクションボタン
        IconButton(
            onClick = onUpdateProgress,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "進捗更新",
                tint = primaryColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 高度な円形進捗リング
 */
@Composable
private fun AdvancedProgressRing(
    progress: Float,
    size: androidx.compose.ui.unit.Dp,
    strokeWidth: androidx.compose.ui.unit.Dp,
    primaryColor: Color,
    secondaryColor: Color
) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.toPx()
            val strokeWidthPx = strokeWidth.toPx()
            val radius = (canvasSize - strokeWidthPx) / 2
            
            // 背景の円
            drawCircle(
                color = secondaryColor.copy(alpha = 0.2f),
                radius = radius,
                style = Stroke(strokeWidthPx, cap = StrokeCap.Round)
            )
            
            // 進捗の円弧
            val sweepAngle = progress * 360f
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        primaryColor,
                        primaryColor.copy(alpha = 0.7f),
                        primaryColor
                    )
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(strokeWidthPx, cap = StrokeCap.Round)
            )
        }
        
        // 中央のパーセンテージ
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
            Text(
                text = "完了",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 進捗詳細情報
 */
@Composable
private fun GoalProgressDetails(
    goalProgress: GoalProgress,
    primaryColor: Color
) {
    val goal = goalProgress.goal
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 現在値/目標値
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "進捗",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${goal.currentValue.toInt()} / ${goal.targetValue.toInt()} ${goal.unit}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        
        // 残り日数
        goalProgress.remainingDays?.let { days ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "残り日数",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${days}日",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = when {
                        days <= 7 -> MaterialTheme.colorScheme.error
                        days <= 30 -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
        
        // 進捗ステータス
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "状態",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (goalProgress.isOnTrack) Icons.Default.TrendingUp else Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (goalProgress.isOnTrack) primaryColor else MaterialTheme.colorScheme.error
                )
                Text(
                    text = if (goalProgress.isOnTrack) "順調" else "要注意",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (goalProgress.isOnTrack) primaryColor else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * 目標カードフッター
 */
@Composable
private fun GoalCardFooter(
    goalProgress: GoalProgress,
    primaryColor: Color
) {
    val goal = goalProgress.goal
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 目標タイプバッジ
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = primaryColor.copy(alpha = 0.1f)
        ) {
            Text(
                text = goal.type.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = primaryColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        
        // 完了予定日
        goalProgress.projectedCompletion?.let { completion ->
            Text(
                text = "完了予定: ${completion}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 目標タイプに応じたテーマ色とアイコンを取得
 */
private fun getGoalTypeTheme(goalType: GoalType): Triple<Color, Color, ImageVector> {
    return when (goalType) {
        GoalType.FREQUENCY -> Triple(
            Color(0xFF4CAF50), // Green
            Color(0xFF81C784),
            Icons.Default.Repeat
        )
        GoalType.VOLUME -> Triple(
            Color(0xFF2196F3), // Blue
            Color(0xFF64B5F6),
            Icons.Default.FitnessCenter
        )
        GoalType.STRENGTH -> Triple(
            Color(0xFFFF5722), // Deep Orange
            Color(0xFFFF8A65),
            Icons.Default.Security
        )
        GoalType.ENDURANCE -> Triple(
            Color(0xFF9C27B0), // Purple
            Color(0xFFBA68C8),
            Icons.Default.DirectionsRun
        )
        GoalType.DURATION -> Triple(
            Color(0xFFFF9800), // Orange
            Color(0xFFFFB74D),
            Icons.Default.Schedule
        )
        else -> Triple(
            Color(0xFF607D8B), // Blue Grey
            Color(0xFF90A4AE),
            Icons.Default.Star
        )
    }
}

/**
 * 目標進捗カードのリスト表示
 */
@Composable
fun AdvancedGoalCardList(
    goalProgressList: List<GoalProgress>,
    modifier: Modifier = Modifier,
    onGoalClick: (GoalProgress) -> Unit = {},
    onUpdateProgress: (GoalProgress) -> Unit = {}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        goalProgressList.forEach { goalProgress ->
            AdvancedGoalCard(
                goalProgress = goalProgress,
                onClick = { onGoalClick(goalProgress) },
                onUpdateProgress = { onUpdateProgress(goalProgress) }
            )
        }
    }
}