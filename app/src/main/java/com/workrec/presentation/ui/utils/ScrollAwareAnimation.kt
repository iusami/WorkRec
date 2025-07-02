package com.workrec.presentation.ui.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import kotlinx.coroutines.delay

/**
 * スクロール状態を監視してアニメーション制御を行うユーティリティ
 * 
 * スクロール中は無限アニメーションを一時停止して、
 * GPU/CPU負荷を軽減し、スムーズなスクロール体験を提供します。
 */

/**
 * スクロール状態を監視して、アニメーション実行可否を制御
 * 
 * @param listState LazyColumnのスクロール状態
 * @param scrollDetectionDelay スクロール終了検知の遅延時間（ミリ秒）
 * @return スクロール中かどうかのフラグ
 */
@Composable
fun rememberScrollAwareAnimationState(
    listState: LazyListState,
    scrollDetectionDelay: Long = 300L
): ScrollAwareAnimationState {
    var isScrolling by remember { mutableStateOf(false) }
    var lastScrollTime by remember { mutableLongStateOf(0L) }
    
    // スクロール位置の変化を監視
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        isScrolling = true
        lastScrollTime = System.currentTimeMillis()
        
        // 一定時間後にスクロール終了を検知
        delay(scrollDetectionDelay)
        if (System.currentTimeMillis() - lastScrollTime >= scrollDetectionDelay) {
            isScrolling = false
        }
    }
    
    return ScrollAwareAnimationState(
        isScrolling = isScrolling,
        shouldPlayInfiniteAnimations = !isScrolling
    )
}

/**
 * スクロール状態に基づくアニメーション制御状態
 */
data class ScrollAwareAnimationState(
    val isScrolling: Boolean,
    val shouldPlayInfiniteAnimations: Boolean
) {
    /**
     * 無限アニメーションを実行すべきかどうかを判定
     * 
     * @param forceDisable 強制的にアニメーションを無効化するフラグ
     * @return アニメーション実行可否
     */
    fun shouldAnimate(forceDisable: Boolean = false): Boolean {
        return shouldPlayInfiniteAnimations && !forceDisable
    }
    
    /**
     * Canvas描画を実行すべきかどうかを判定
     * 軽量なCanvas描画は継続、重い描画はスクロール中は停止
     * 
     * @param isHeavyRendering 重い描画処理かどうか
     * @return 描画実行可否
     */
    fun shouldRender(isHeavyRendering: Boolean = false): Boolean {
        return if (isHeavyRendering) {
            !isScrolling
        } else {
            true // 軽量な描画は継続
        }
    }
}

/**
 * デバッグ用：アニメーション実行数をカウント
 */
@Composable
fun rememberAnimationDebugState(): AnimationDebugState {
    var activeAnimationCount by remember { mutableIntStateOf(0) }
    var canvasRenderCount by remember { mutableIntStateOf(0) }
    
    return AnimationDebugState(
        activeAnimationCount = activeAnimationCount,
        canvasRenderCount = canvasRenderCount,
        incrementAnimationCount = { activeAnimationCount++ },
        decrementAnimationCount = { activeAnimationCount-- },
        incrementCanvasCount = { canvasRenderCount++ }
    )
}

/**
 * アニメーションデバッグ状態
 */
data class AnimationDebugState(
    val activeAnimationCount: Int,
    val canvasRenderCount: Int,
    val incrementAnimationCount: () -> Unit,
    val decrementAnimationCount: () -> Unit,
    val incrementCanvasCount: () -> Unit
)

/**
 * パフォーマンス監視用ユーティリティ
 */
@Composable
fun rememberPerformanceMonitor(): PerformanceMonitorState {
    var frameDropCount by remember { mutableIntStateOf(0) }
    var lastFrameTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // 60FPS基準
            val currentTime = System.currentTimeMillis()
            val frameDuration = currentTime - lastFrameTime
            
            if (frameDuration > 32) { // 30FPS以下でフレームドロップ検知
                frameDropCount++
            }
            
            lastFrameTime = currentTime
        }
    }
    
    return PerformanceMonitorState(
        frameDropCount = frameDropCount,
        isPerformanceGood = frameDropCount < 10
    )
}

/**
 * パフォーマンス監視状態
 */
data class PerformanceMonitorState(
    val frameDropCount: Int,
    val isPerformanceGood: Boolean
)