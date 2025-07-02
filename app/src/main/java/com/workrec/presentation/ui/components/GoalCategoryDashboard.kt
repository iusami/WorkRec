package com.workrec.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workrec.domain.entities.GoalProgress
import com.workrec.domain.entities.GoalType
import kotlin.math.cos
import kotlin.math.sin

/**
 * 目標カテゴリ別統計ダッシュボード
 */
@Composable
fun GoalCategoryDashboard(
    goalProgressList: List<GoalProgress>,
    modifier: Modifier = Modifier,
    onCategoryClick: (GoalType) -> Unit = {}
) {
    // カテゴリ別統計を計算
    val categoryStats = remember(goalProgressList) {
        calculateCategoryStatistics(goalProgressList)
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // ヘッダー
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "カテゴリ別進捗",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "目標タイプごとの統計",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.Dashboard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // カテゴリカード
            if (categoryStats.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categoryStats) { stat ->
                        CategoryStatCard(
                            categoryStatistic = stat,
                            onClick = { onCategoryClick(stat.type) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 全体的な統計
                OverallCategoryStatistics(categoryStats = categoryStats)
            } else {
                // 空の状態
                EmptyDashboardState()
            }
        }
    }
}

/**
 * カテゴリ統計カード
 */
@Composable
private fun CategoryStatCard(
    categoryStatistic: CategoryStatistic,
    onClick: () -> Unit
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) categoryStatistic.averageProgress else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "progress_animation"
    )
    
    LaunchedEffect(categoryStatistic) {
        animationPlayed = true
    }
    
    val (primaryColor, _, icon) = getGoalTypeTheme(categoryStatistic.type)
    
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // アイコンと進捗リング
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                // 背景円
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 4.dp.toPx()
                    val radius = (size.width - strokeWidth) / 2
                    
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.2f),
                        radius = radius,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                    
                    val sweepAngle = animatedProgress * 360f
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                    )
                }
                
                // アイコン
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // カテゴリ名
            Text(
                text = categoryStatistic.type.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            
            // 統計情報
            Text(
                text = "${categoryStatistic.activeGoals}個の目標",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 全体的なカテゴリ統計
 */
@Composable
private fun OverallCategoryStatistics(
    categoryStats: List<CategoryStatistic>
) {
    val totalGoals = categoryStats.sumOf { it.activeGoals }
    val completedGoals = categoryStats.sumOf { it.completedGoals }
    val averageProgress = if (categoryStats.isNotEmpty()) {
        categoryStats.map { it.averageProgress }.average().toFloat()
    } else 0f
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatisticItem(
            label = "総目標数",
            value = totalGoals.toString(),
            icon = Icons.Default.Assignment,
            color = MaterialTheme.colorScheme.primary
        )
        
        StatisticItem(
            label = "完了済み",
            value = completedGoals.toString(),
            icon = Icons.Default.CheckCircle,
            color = MaterialTheme.colorScheme.tertiary
        )
        
        StatisticItem(
            label = "平均進捗",
            value = "${(averageProgress * 100).toInt()}%",
            icon = Icons.Default.TrendingUp,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

/**
 * 統計アイテム
 */
@Composable
private fun StatisticItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 空のダッシュボード状態
 */
@Composable
private fun EmptyDashboardState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Assignment,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "まだ目標がありません",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "新しい目標を作成して進捗を追跡しましょう",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * カテゴリ統計データクラス
 */
data class CategoryStatistic(
    val type: GoalType,
    val activeGoals: Int,
    val completedGoals: Int,
    val averageProgress: Float,
    val totalTargetValue: Double,
    val totalCurrentValue: Double
)

/**
 * カテゴリ別統計を計算
 */
private fun calculateCategoryStatistics(goalProgressList: List<GoalProgress>): List<CategoryStatistic> {
    return goalProgressList
        .groupBy { it.goal.type }
        .map { (type, progressList) ->
            val activeGoals = progressList.filter { !it.goal.isCompleted }
            val completedGoals = progressList.filter { it.goal.isCompleted }
            
            CategoryStatistic(
                type = type,
                activeGoals = activeGoals.size,
                completedGoals = completedGoals.size,
                averageProgress = if (activeGoals.isNotEmpty()) {
                    activeGoals.map { it.progressPercentage }.average().toFloat()
                } else 0f,
                totalTargetValue = progressList.sumOf { it.goal.targetValue },
                totalCurrentValue = progressList.sumOf { it.goal.currentValue }
            )
        }
        .sortedByDescending { it.activeGoals }
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