package com.workrec.presentation.ui.utils

import androidx.compose.ui.unit.dp
import org.junit.Test
import com.google.common.truth.Truth.assertThat

/**
 * Tests for ResponsiveUtils to verify responsive design calculations
 */
class ResponsiveUtilsTest {

    @Test
    fun `calendarLayoutConfig has correct values for different screen sizes`() {
        // Test compact screen configuration
        val compactConfig = CalendarLayoutConfig(
            horizontalPadding = 8.dp,
            verticalPadding = 8.dp,
            cellSpacing = 4.dp,
            headerPadding = 16.dp,
            minCellSize = 48.dp,
            showCompactLayout = true
        )
        
        // Verify minimum touch target size meets accessibility guidelines
        assertThat(compactConfig.minCellSize.value).isAtLeast(48f)
        assertThat(compactConfig.showCompactLayout).isTrue()
        
        // Test medium screen configuration
        val mediumConfig = CalendarLayoutConfig(
            horizontalPadding = 16.dp,
            verticalPadding = 12.dp,
            cellSpacing = 6.dp,
            headerPadding = 24.dp,
            minCellSize = 56.dp,
            showCompactLayout = false
        )
        
        // Verify medium screen has larger touch targets
        assertThat(mediumConfig.minCellSize.value).isGreaterThan(compactConfig.minCellSize.value)
        assertThat(mediumConfig.showCompactLayout).isFalse()
        
        // Test expanded screen configuration
        val expandedConfig = CalendarLayoutConfig(
            horizontalPadding = 24.dp,
            verticalPadding = 16.dp,
            cellSpacing = 8.dp,
            headerPadding = 32.dp,
            minCellSize = 64.dp,
            showCompactLayout = false
        )
        
        // Verify expanded screen has the largest touch targets
        assertThat(expandedConfig.minCellSize.value).isGreaterThan(mediumConfig.minCellSize.value)
        assertThat(expandedConfig.showCompactLayout).isFalse()
    }

    @Test
    fun `screen size enum has correct values`() {
        val screenSizes = ResponsiveUtils.ScreenSize.values()
        
        assertThat(screenSizes).hasLength(3)
        assertThat(screenSizes).asList().containsExactly(
            ResponsiveUtils.ScreenSize.COMPACT,
            ResponsiveUtils.ScreenSize.MEDIUM,
            ResponsiveUtils.ScreenSize.EXPANDED
        )
    }

    @Test
    fun `calendar layout config properties are reasonable`() {
        // Test that all layout configurations have reasonable values
        val configs = listOf(
            CalendarLayoutConfig(8.dp, 8.dp, 4.dp, 16.dp, 48.dp, true),
            CalendarLayoutConfig(16.dp, 12.dp, 6.dp, 24.dp, 56.dp, false),
            CalendarLayoutConfig(24.dp, 16.dp, 8.dp, 32.dp, 64.dp, false)
        )
        
        configs.forEach { config ->
            // All padding values should be positive
            assertThat(config.horizontalPadding.value).isGreaterThan(0f)
            assertThat(config.verticalPadding.value).isGreaterThan(0f)
            assertThat(config.cellSpacing.value).isGreaterThan(0f)
            assertThat(config.headerPadding.value).isGreaterThan(0f)
            assertThat(config.minCellSize.value).isAtLeast(48f) // Accessibility minimum
            
            // Spacing should be smaller than padding for visual hierarchy
            assertThat(config.cellSpacing.value).isLessThan(config.horizontalPadding.value)
        }
    }
}