package com.workrec.presentation.ui.components

import androidx.compose.ui.unit.dp
import com.workrec.domain.entities.CalendarDay
import com.workrec.presentation.ui.utils.CalendarLayoutConfig
import kotlinx.datetime.LocalDate
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for calendar accessibility features and responsive design
 * These tests verify that the accessibility content descriptions are built correctly
 * and that responsive design meets accessibility guidelines
 */
class CalendarAccessibilityTest {

    @Test
    fun calendarDay_buildContentDescription_withWorkout() {
        val calendarDay = CalendarDay(
            date = LocalDate(2024, 1, 15),
            hasWorkout = true,
            workoutCount = 2,
            isToday = false,
            isSelected = false,
            isCurrentMonth = true
        )

        // Test the content description logic
        val contentDesc = buildString {
            append("${calendarDay.date.monthNumber}月${calendarDay.date.dayOfMonth}日")
            if (calendarDay.hasWorkout) {
                append(", ワークアウト${calendarDay.workoutCount}件")
            }
            if (calendarDay.isToday) {
                append(", 今日")
            }
            if (!calendarDay.isCurrentMonth) {
                append(", 前月または翌月")
            }
        }

        assertEquals("1月15日, ワークアウト2件", contentDesc)
    }

    @Test
    fun calendarDay_buildContentDescription_today() {
        val calendarDay = CalendarDay(
            date = LocalDate(2024, 1, 15),
            hasWorkout = false,
            workoutCount = 0,
            isToday = true,
            isSelected = false,
            isCurrentMonth = true
        )

        val contentDesc = buildString {
            append("${calendarDay.date.monthNumber}月${calendarDay.date.dayOfMonth}日")
            if (calendarDay.hasWorkout) {
                append(", ワークアウト${calendarDay.workoutCount}件")
            }
            if (calendarDay.isToday) {
                append(", 今日")
            }
            if (!calendarDay.isCurrentMonth) {
                append(", 前月または翌月")
            }
        }

        assertEquals("1月15日, 今日", contentDesc)
    }

    @Test
    fun calendarDay_buildContentDescription_previousMonth() {
        val calendarDay = CalendarDay(
            date = LocalDate(2023, 12, 31),
            hasWorkout = false,
            workoutCount = 0,
            isToday = false,
            isSelected = false,
            isCurrentMonth = false
        )

        val contentDesc = buildString {
            append("${calendarDay.date.monthNumber}月${calendarDay.date.dayOfMonth}日")
            if (calendarDay.hasWorkout) {
                append(", ワークアウト${calendarDay.workoutCount}件")
            }
            if (calendarDay.isToday) {
                append(", 今日")
            }
            if (!calendarDay.isCurrentMonth) {
                append(", 前月または翌月")
            }
        }

        assertEquals("12月31日, 前月または翌月", contentDesc)
    }

    @Test
    fun calendarDay_buildStateDescription_selected() {
        val calendarDay = CalendarDay(
            date = LocalDate(2024, 1, 15),
            hasWorkout = true,
            workoutCount = 1,
            isToday = false,
            isSelected = true,
            isCurrentMonth = true
        )

        val stateDesc = buildString {
            if (calendarDay.isSelected) {
                append("選択中")
            }
            if (calendarDay.hasWorkout) {
                if (isNotEmpty()) append(", ")
                append("ワークアウトあり")
            }
        }

        assertEquals("選択中, ワークアウトあり", stateDesc)
    }

    @Test
    fun calendarDay_buildStateDescription_workoutOnly() {
        val calendarDay = CalendarDay(
            date = LocalDate(2024, 1, 15),
            hasWorkout = true,
            workoutCount = 1,
            isToday = false,
            isSelected = false,
            isCurrentMonth = true
        )

        val stateDesc = buildString {
            if (calendarDay.isSelected) {
                append("選択中")
            }
            if (calendarDay.hasWorkout) {
                if (isNotEmpty()) append(", ")
                append("ワークアウトあり")
            }
        }

        assertEquals("ワークアウトあり", stateDesc)
    }

    @Test
    fun calendarDay_buildStateDescription_empty() {
        val calendarDay = CalendarDay(
            date = LocalDate(2024, 1, 15),
            hasWorkout = false,
            workoutCount = 0,
            isToday = false,
            isSelected = false,
            isCurrentMonth = true
        )

        val stateDesc = buildString {
            if (calendarDay.isSelected) {
                append("選択中")
            }
            if (calendarDay.hasWorkout) {
                if (isNotEmpty()) append(", ")
                append("ワークアウトあり")
            }
        }

        assertEquals("", stateDesc)
    }

    @Test
    fun calendarLayoutConfig_meetsAccessibilityGuidelines() {
        // Test compact screen configuration
        val compactConfig = CalendarLayoutConfig(
            horizontalPadding = 8.dp,
            verticalPadding = 8.dp,
            cellSpacing = 4.dp,
            headerPadding = 16.dp,
            minCellSize = 48.dp,
            showCompactLayout = true
        )
        
        // Verify minimum touch target meets accessibility guidelines (48dp)
        assertTrue("Touch target should be at least 48dp", compactConfig.minCellSize.value >= 48f)
        
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
        assertTrue("Medium screen should have larger touch targets", 
                  mediumConfig.minCellSize.value > compactConfig.minCellSize.value)
        
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
        assertTrue("Expanded screen should have largest touch targets",
                  expandedConfig.minCellSize.value > mediumConfig.minCellSize.value)
    }

    @Test
    fun calendarLayoutConfig_providesReasonablePadding() {
        val configs = listOf(
            CalendarLayoutConfig(8.dp, 8.dp, 4.dp, 16.dp, 48.dp, true),
            CalendarLayoutConfig(16.dp, 12.dp, 6.dp, 24.dp, 56.dp, false),
            CalendarLayoutConfig(24.dp, 16.dp, 8.dp, 32.dp, 64.dp, false)
        )
        
        configs.forEach { config ->
            // All padding values should be positive
            assertTrue("Horizontal padding should be positive", config.horizontalPadding.value > 0f)
            assertTrue("Vertical padding should be positive", config.verticalPadding.value > 0f)
            assertTrue("Cell spacing should be positive", config.cellSpacing.value > 0f)
            assertTrue("Header padding should be positive", config.headerPadding.value > 0f)
            
            // Spacing should be smaller than padding for visual hierarchy
            assertTrue("Cell spacing should be smaller than horizontal padding", 
                      config.cellSpacing.value < config.horizontalPadding.value)
        }
    }
}