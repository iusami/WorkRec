package com.workrec.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.workrec.domain.entities.CalendarDay
import com.workrec.presentation.ui.theme.WorkRecTheme
import com.workrec.presentation.ui.utils.ResponsiveUtils
import kotlinx.datetime.LocalDate

/**
 * Individual calendar day cell component that displays a single day with workout indicators
 * 
 * @param calendarDay The calendar day data containing date and workout information
 * @param onClick Callback when the day is clicked
 * @param minSize Minimum size for the cell to ensure accessibility
 * @param modifier Modifier for styling
 */
@Composable
fun CalendarDayCell(
    calendarDay: CalendarDay,
    onClick: () -> Unit,
    minSize: Dp = ResponsiveUtils.getMinTouchTargetSize(),
    modifier: Modifier = Modifier
) {
    val dayNumber = calendarDay.date.dayOfMonth
    
    // Build accessibility content description
    val contentDesc = buildString {
        append("${calendarDay.date.monthNumber}月${dayNumber}日")
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
    
    // Build state description for accessibility
    val stateDesc = buildString {
        if (calendarDay.isSelected) {
            append("選択中")
        }
        if (calendarDay.hasWorkout) {
            if (isNotEmpty()) append(", ")
            append("ワークアウトあり")
        }
    }
    
    Box(
        modifier = modifier
            .sizeIn(minWidth = minSize, minHeight = minSize)
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable { onClick() }
            .background(
                color = when {
                    calendarDay.isSelected -> MaterialTheme.colorScheme.primary
                    calendarDay.isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                },
                shape = CircleShape
            )
            .semantics {
                this.contentDescription = contentDesc
                if (stateDesc.isNotEmpty()) {
                    this.stateDescription = stateDesc
                }
                role = Role.Button
                selected = calendarDay.isSelected
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = dayNumber.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (calendarDay.isToday) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                color = when {
                    !calendarDay.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    calendarDay.isSelected -> MaterialTheme.colorScheme.onPrimary
                    calendarDay.isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.padding(bottom = 2.dp)
            )
            
            // Workout indicator dot
            if (calendarDay.hasWorkout) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            color = when {
                                calendarDay.isSelected -> MaterialTheme.colorScheme.onPrimary
                                calendarDay.isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.primary
                            },
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarDayCellPreview() {
    WorkRecTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Regular day
            CalendarDayCell(
                calendarDay = CalendarDay(
                    date = LocalDate(2024, 1, 15),
                    hasWorkout = false,
                    workoutCount = 0,
                    isToday = false,
                    isSelected = false,
                    isCurrentMonth = true
                ),
                onClick = {}
            )
            
            // Day with workout
            CalendarDayCell(
                calendarDay = CalendarDay(
                    date = LocalDate(2024, 1, 16),
                    hasWorkout = true,
                    workoutCount = 2,
                    isToday = false,
                    isSelected = false,
                    isCurrentMonth = true
                ),
                onClick = {}
            )
            
            // Today
            CalendarDayCell(
                calendarDay = CalendarDay(
                    date = LocalDate(2024, 1, 17),
                    hasWorkout = true,
                    workoutCount = 1,
                    isToday = true,
                    isSelected = false,
                    isCurrentMonth = true
                ),
                onClick = {}
            )
            
            // Selected day
            CalendarDayCell(
                calendarDay = CalendarDay(
                    date = LocalDate(2024, 1, 18),
                    hasWorkout = true,
                    workoutCount = 3,
                    isToday = false,
                    isSelected = true,
                    isCurrentMonth = true
                ),
                onClick = {}
            )
            
            // Previous month day (dimmed)
            CalendarDayCell(
                calendarDay = CalendarDay(
                    date = LocalDate(2023, 12, 31),
                    hasWorkout = false,
                    workoutCount = 0,
                    isToday = false,
                    isSelected = false,
                    isCurrentMonth = false
                ),
                onClick = {}
            )
        }
    }
}