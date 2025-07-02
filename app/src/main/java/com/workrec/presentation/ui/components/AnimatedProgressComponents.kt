package com.workrec.presentation.ui.components

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.workrec.domain.entities.GoalProgress
import kotlin.math.*

/**
 * アニメーション付き進捗波形コンポーネント
 */
@Composable
fun AnimatedProgressWave(
    goalProgress: GoalProgress,
    modifier: Modifier = Modifier,
    shouldPlayAnimations: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_animation")
    
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (shouldPlayAnimations) 2 * PI.toFloat() else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (shouldPlayAnimations) 3000 else Int.MAX_VALUE,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )
    
    var animationPlayed by remember { mutableStateOf(false) }
    val progressAnimation by animateFloatAsState(
        targetValue = if (animationPlayed) goalProgress.progressPercentage else 0f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "progress_animation"
    )
    
    LaunchedEffect(goalProgress) {
        animationPlayed = true
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
                        text = goalProgress.goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "進捗の波形",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 波形表示
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                WaveCanvas(
                    progress = progressAnimation,
                    waveOffset = waveOffset,
                    modifier = Modifier.fillMaxSize()
                )
                
                // 進捗パーセンテージオーバーレイ
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${(progressAnimation * 100).toInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * 波形キャンバス
 */
@Composable
private fun WaveCanvas(
    progress: Float,
    waveOffset: Float,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    
    Canvas(modifier = modifier) {
        val waveHeight = size.height * progress
        val amplitude = 20.dp.toPx()
        val frequency = 0.02f
        
        // 背景波形（静的）
        drawWave(
            waveHeight = waveHeight * 0.5f,
            amplitude = amplitude * 0.5f,
            frequency = frequency,
            offset = 0f,
            color = secondaryColor.copy(alpha = 0.3f),
            strokeWidth = 2.dp.toPx()
        )
        
        // メイン波形（アニメーション）
        drawWave(
            waveHeight = waveHeight,
            amplitude = amplitude,
            frequency = frequency,
            offset = waveOffset,
            color = primaryColor,
            strokeWidth = 3.dp.toPx()
        )
        
        // 進捗塗りつぶし
        if (progress > 0) {
            drawWaveFill(
                waveHeight = waveHeight,
                amplitude = amplitude,
                frequency = frequency,
                offset = waveOffset,
                color = primaryColor.copy(alpha = 0.2f)
            )
        }
    }
}

/**
 * 波形を描画
 */
private fun DrawScope.drawWave(
    waveHeight: Float,
    amplitude: Float,
    frequency: Float,
    offset: Float,
    color: Color,
    strokeWidth: Float
) {
    val path = Path()
    
    for (x in 0..size.width.toInt()) {
        val y = size.height - waveHeight + 
                amplitude * sin(x * frequency + offset)
        
        if (x == 0) {
            path.moveTo(x.toFloat(), y)
        } else {
            path.lineTo(x.toFloat(), y)
        }
    }
    
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

/**
 * 波形の塗りつぶしを描画
 */
private fun DrawScope.drawWaveFill(
    waveHeight: Float,
    amplitude: Float,
    frequency: Float,
    offset: Float,
    color: Color
) {
    val path = Path()
    
    // 波形の上部
    for (x in 0..size.width.toInt()) {
        val y = size.height - waveHeight + 
                amplitude * sin(x * frequency + offset)
        
        if (x == 0) {
            path.moveTo(x.toFloat(), y)
        } else {
            path.lineTo(x.toFloat(), y)
        }
    }
    
    // 底部まで閉じる
    path.lineTo(size.width, size.height)
    path.lineTo(0f, size.height)
    path.close()
    
    drawPath(
        path = path,
        color = color
    )
}

/**
 * 脈動する円形進捗インジケーター
 */
@Composable
fun PulsingProgressIndicator(
    progress: Float,
    size: androidx.compose.ui.unit.Dp = 80.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    shouldPlayAnimations: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_animation")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (shouldPlayAnimations) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (shouldPlayAnimations) 1500 else Int.MAX_VALUE,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (shouldPlayAnimations) 0.3f else 0.2f,
        targetValue = if (shouldPlayAnimations) 0.1f else 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (shouldPlayAnimations) 1500 else Int.MAX_VALUE,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // 脈動する外側の円
        Canvas(
            modifier = Modifier.size(size * pulseScale)
        ) {
            drawCircle(
                color = color.copy(alpha = pulseAlpha),
                radius = this.size.width / 2
            )
        }
        
        // メインの進捗円
        Canvas(
            modifier = Modifier.size(size)
        ) {
            val strokeWidth = 8.dp.toPx()
            
            // 背景円
            drawCircle(
                color = color.copy(alpha = 0.2f),
                radius = (size.toPx() - strokeWidth) / 2,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            
            // 進捗円弧
            val sweepAngle = progress * 360f
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        // 中央のパーセンテージ
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * 目標達成アニメーション
 */
@Composable
fun GoalAchievementAnimation(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    shouldPlayAnimations: Boolean = true,
    onAnimationComplete: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "achievement_animation")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = if (shouldPlayAnimations) 0.8f else 1f,
        targetValue = if (shouldPlayAnimations) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (shouldPlayAnimations) 800 else Int.MAX_VALUE,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (shouldPlayAnimations) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (shouldPlayAnimations) 2000 else Int.MAX_VALUE,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_animation"
    )
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            kotlinx.coroutines.delay(3000) // アニメーション継続時間
            onAnimationComplete()
        }
    }
    
    if (isVisible) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            // 回転する星
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "目標達成",
                tint = Color(0xFFFFD700), // Gold color
                modifier = Modifier
                    .size((48 * scale).dp)
                    .graphicsLayer {
                        rotationZ = rotation
                    }
            )
            
            // 祝福テキスト
            Text(
                text = "目標達成！",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.offset(y = 60.dp)
            )
        }
    }
}

/**
 * 進捗バー（グラデーション＋アニメーション付き）
 */
@Composable
fun AnimatedGradientProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 8.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 4.dp,
    backgroundTint: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressGradient: List<Color> = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary
    )
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) progress else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "progress_animation"
    )
    
    LaunchedEffect(progress) {
        animationPlayed = true
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundTint)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    Brush.horizontalGradient(progressGradient)
                )
        )
    }
}