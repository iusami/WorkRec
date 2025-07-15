package com.workrec.presentation.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Responsive design utilities for adapting UI to different screen sizes
 */
object ResponsiveUtils {
    
    /**
     * Screen size categories based on Material Design guidelines
     */
    enum class ScreenSize {
        COMPACT,    // < 600dp width (phones)
        MEDIUM,     // 600-840dp width (tablets, foldables)
        EXPANDED    // > 840dp width (large tablets, desktop)
    }
    
    /**
     * Get current screen size category
     */
    @Composable
    fun getScreenSize(): ScreenSize {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        
        return when {
            screenWidth < 600.dp -> ScreenSize.COMPACT
            screenWidth < 840.dp -> ScreenSize.MEDIUM
            else -> ScreenSize.EXPANDED
        }
    }
    
    /**
     * Get responsive padding based on screen size
     */
    @Composable
    fun getResponsivePadding(): Dp {
        return when (getScreenSize()) {
            ScreenSize.COMPACT -> 16.dp
            ScreenSize.MEDIUM -> 24.dp
            ScreenSize.EXPANDED -> 32.dp
        }
    }
    
    /**
     * Get responsive horizontal padding for calendar
     */
    @Composable
    fun getCalendarHorizontalPadding(): Dp {
        return when (getScreenSize()) {
            ScreenSize.COMPACT -> 8.dp
            ScreenSize.MEDIUM -> 16.dp
            ScreenSize.EXPANDED -> 24.dp
        }
    }
    
    /**
     * Get responsive spacing between calendar elements
     */
    @Composable
    fun getCalendarSpacing(): Dp {
        return when (getScreenSize()) {
            ScreenSize.COMPACT -> 4.dp
            ScreenSize.MEDIUM -> 6.dp
            ScreenSize.EXPANDED -> 8.dp
        }
    }
    
    /**
     * Get minimum touch target size for accessibility
     */
    @Composable
    fun getMinTouchTargetSize(): Dp {
        return 48.dp // Material Design minimum touch target
    }
    
    /**
     * Get responsive calendar day cell size
     */
    @Composable
    fun getCalendarDayCellMinSize(): Dp {
        return when (getScreenSize()) {
            ScreenSize.COMPACT -> 48.dp // Minimum touch target
            ScreenSize.MEDIUM -> 56.dp
            ScreenSize.EXPANDED -> 64.dp
        }
    }
    
    /**
     * Get responsive text size multiplier
     */
    @Composable
    fun getTextSizeMultiplier(): Float {
        return when (getScreenSize()) {
            ScreenSize.COMPACT -> 1.0f
            ScreenSize.MEDIUM -> 1.1f
            ScreenSize.EXPANDED -> 1.2f
        }
    }
    
    /**
     * Check if current screen is tablet size or larger
     */
    @Composable
    fun isTabletOrLarger(): Boolean {
        return getScreenSize() != ScreenSize.COMPACT
    }
    
    /**
     * Get responsive calendar layout configuration
     */
    @Composable
    fun getCalendarLayoutConfig(): CalendarLayoutConfig {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.COMPACT -> CalendarLayoutConfig(
                horizontalPadding = 8.dp,
                verticalPadding = 8.dp,
                cellSpacing = 4.dp,
                headerPadding = 16.dp,
                minCellSize = 48.dp,
                showCompactLayout = true
            )
            ScreenSize.MEDIUM -> CalendarLayoutConfig(
                horizontalPadding = 16.dp,
                verticalPadding = 12.dp,
                cellSpacing = 6.dp,
                headerPadding = 24.dp,
                minCellSize = 56.dp,
                showCompactLayout = false
            )
            ScreenSize.EXPANDED -> CalendarLayoutConfig(
                horizontalPadding = 24.dp,
                verticalPadding = 16.dp,
                cellSpacing = 8.dp,
                headerPadding = 32.dp,
                minCellSize = 64.dp,
                showCompactLayout = false
            )
        }
    }
}

/**
 * Configuration for calendar layout based on screen size
 */
data class CalendarLayoutConfig(
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val cellSpacing: Dp,
    val headerPadding: Dp,
    val minCellSize: Dp,
    val showCompactLayout: Boolean
)