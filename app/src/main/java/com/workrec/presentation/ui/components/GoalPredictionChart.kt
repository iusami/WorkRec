package com.workrec.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.workrec.domain.entities.GoalProgress
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlin.math.max
import kotlin.math.min

/**
 * 目標達成予測チャートコンポーネント
 * 現在の進捗と予測される達成軌道を表示
 */
@Composable
fun GoalPredictionChart(
    goalProgress: GoalProgress,
    modifier: Modifier = Modifier,
    showPrediction: Boolean = true
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "chart_animation"
    )
    
    LaunchedEffect(goalProgress) {
        animationPlayed = true
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
            PredictionChartHeader(goalProgress = goalProgress)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // チャート本体
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                PredictionChartCanvas(
                    goalProgress = goalProgress,
                    animationProgress = animatedProgress,
                    showPrediction = showPrediction,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 凡例とインサイト
            PredictionChartLegend(goalProgress = goalProgress)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            PredictionInsights(goalProgress = goalProgress)
        }
    }
}

/**
 * 予測チャートヘッダー
 */
@Composable
private fun PredictionChartHeader(goalProgress: GoalProgress) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "達成予測",
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
            imageVector = Icons.Default.TrendingUp,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 予測チャートキャンバス
 */
@Composable
private fun PredictionChartCanvas(
    goalProgress: GoalProgress,
    animationProgress: Float,
    showPrediction: Boolean,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    
    Canvas(modifier = modifier) {
        val padding = 40.dp.toPx()
        val chartWidth = size.width - (padding * 2)
        val chartHeight = size.height - (padding * 2)
        
        // チャート背景
        drawRoundRect(
            color = surfaceColor.copy(alpha = 0.1f),
            topLeft = Offset(padding, padding),
            size = Size(chartWidth, chartHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
        )
        
        // グリッド線
        drawChartGrid(
            padding = padding,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            gridColor = surfaceColor.copy(alpha = 0.3f)
        )
        
        // 目標達成ライン
        drawTargetLine(
            padding = padding,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            targetColor = primaryColor.copy(alpha = 0.5f)
        )
        
        // 現在の進捗ライン
        drawProgressLine(
            goalProgress = goalProgress,
            padding = padding,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            progressColor = primaryColor,
            animationProgress = animationProgress
        )
        
        // 予測ライン
        if (showPrediction) {
            drawPredictionLine(
                goalProgress = goalProgress,
                padding = padding,
                chartWidth = chartWidth,
                chartHeight = chartHeight,
                predictionColor = secondaryColor,
                animationProgress = animationProgress
            )
        }
        
        // 現在位置マーカー
        drawCurrentPositionMarker(
            goalProgress = goalProgress,
            padding = padding,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            markerColor = primaryColor
        )
    }
}

/**
 * チャートグリッド線を描画
 */
private fun DrawScope.drawChartGrid(
    padding: Float,
    chartWidth: Float,
    chartHeight: Float,
    gridColor: Color
) {
    // 横線（進捗レベル）
    for (i in 0..4) {
        val y = padding + (chartHeight * i / 4)
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(padding + chartWidth, y),
            strokeWidth = 1.dp.toPx()
        )
    }
    
    // 縦線（時間軸）
    for (i in 0..6) {
        val x = padding + (chartWidth * i / 6)
        drawLine(
            color = gridColor,
            start = Offset(x, padding),
            end = Offset(x, padding + chartHeight),
            strokeWidth = 1.dp.toPx()
        )
    }
}

/**
 * 目標達成ラインを描画
 */
private fun DrawScope.drawTargetLine(
    padding: Float,
    chartWidth: Float,
    chartHeight: Float,
    targetColor: Color
) {
    drawLine(
        color = targetColor,
        start = Offset(padding, padding),
        end = Offset(padding + chartWidth, padding),
        strokeWidth = 2.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(10.dp.toPx(), 5.dp.toPx())
        )
    )
}

/**
 * 現在の進捗ラインを描画
 */
private fun DrawScope.drawProgressLine(
    goalProgress: GoalProgress,
    padding: Float,
    chartWidth: Float,
    chartHeight: Float,
    progressColor: Color,
    animationProgress: Float
) {
    val goal = goalProgress.goal
    val progressRatio = (goal.currentValue / goal.targetValue).toFloat()
    val timeRatio = 0.3f // 現在時点を30%の位置に設定（簡易実装）
    
    val startX = padding
    val startY = padding + chartHeight
    val endX = padding + (chartWidth * timeRatio * animationProgress)
    val endY = padding + chartHeight - (chartHeight * progressRatio * animationProgress)
    
    // 進捗パス
    val path = Path().apply {
        moveTo(startX, startY)
        
        // 滑らかな曲線で進捗を表示
        val controlX1 = startX + (endX - startX) * 0.3f
        val controlY1 = startY
        val controlX2 = startX + (endX - startX) * 0.7f
        val controlY2 = endY
        
        cubicTo(controlX1, controlY1, controlX2, controlY2, endX, endY)
    }
    
    drawPath(
        path = path,
        color = progressColor,
        style = Stroke(
            width = 3.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

/**
 * 予測ラインを描画
 */
private fun DrawScope.drawPredictionLine(
    goalProgress: GoalProgress,
    padding: Float,
    chartWidth: Float,
    chartHeight: Float,
    predictionColor: Color,
    animationProgress: Float
) {
    val goal = goalProgress.goal
    val progressRatio = (goal.currentValue / goal.targetValue).toFloat()
    val timeRatio = 0.3f
    
    val startX = padding + (chartWidth * timeRatio)
    val startY = padding + chartHeight - (chartHeight * progressRatio)
    val endX = padding + (chartWidth * animationProgress)
    val endY = padding + if (goalProgress.isOnTrack) 0f else chartHeight * 0.2f
    
    // 予測パス（点線）
    val path = Path().apply {
        moveTo(startX, startY)
        lineTo(endX, endY)
    }
    
    drawPath(
        path = path,
        color = predictionColor,
        style = Stroke(
            width = 2.dp.toPx(),
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(8.dp.toPx(), 4.dp.toPx())
            )
        )
    )
}

/**
 * 現在位置マーカーを描画
 */
private fun DrawScope.drawCurrentPositionMarker(
    goalProgress: GoalProgress,
    padding: Float,
    chartWidth: Float,
    chartHeight: Float,
    markerColor: Color
) {
    val goal = goalProgress.goal
    val progressRatio = (goal.currentValue / goal.targetValue).toFloat()
    val timeRatio = 0.3f
    
    val markerX = padding + (chartWidth * timeRatio)
    val markerY = padding + chartHeight - (chartHeight * progressRatio)
    
    // 外側の円
    drawCircle(
        color = markerColor.copy(alpha = 0.3f),
        radius = 8.dp.toPx(),
        center = Offset(markerX, markerY)
    )
    
    // 内側の円
    drawCircle(
        color = markerColor,
        radius = 4.dp.toPx(),
        center = Offset(markerX, markerY)
    )
}

/**
 * チャート凡例
 */
@Composable
private fun PredictionChartLegend(goalProgress: GoalProgress) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LegendItem(
            color = MaterialTheme.colorScheme.primary,
            label = "実績",
            isLine = true
        )
        LegendItem(
            color = MaterialTheme.colorScheme.secondary,
            label = "予測",
            isDashed = true
        )
        LegendItem(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            label = "目標",
            isDashed = true
        )
    }
}

/**
 * 凡例アイテム
 */
@Composable
private fun LegendItem(
    color: Color,
    label: String,
    isLine: Boolean = true,
    isDashed: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (isLine) {
            Canvas(
                modifier = Modifier.size(width = 16.dp, height = 2.dp)
            ) {
                drawLine(
                    color = color,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = if (isDashed) {
                        PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 2.dp.toPx()))
                    } else null
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, shape = androidx.compose.foundation.shape.CircleShape)
            )
        }
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 予測インサイト
 */
@Composable
private fun PredictionInsights(goalProgress: GoalProgress) {
    val goal = goalProgress.goal
    val isOnTrack = goalProgress.isOnTrack
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (isOnTrack) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isOnTrack) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isOnTrack) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                modifier = Modifier.size(20.dp)
            )
            
            Column {
                Text(
                    text = if (isOnTrack) "順調に進んでいます" else "ペースアップが必要です",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                val remainingValue = goal.targetValue - goal.currentValue
                val dailyRequired = goalProgress.remainingDays?.let { days ->
                    if (days > 0) (remainingValue / days).toFloat() else 0f
                } ?: 0f
                
                if (dailyRequired > 0) {
                    Text(
                        text = "1日あたり${dailyRequired.toInt()} ${goal.unit}必要",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}