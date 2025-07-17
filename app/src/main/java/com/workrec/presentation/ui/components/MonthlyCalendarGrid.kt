package com.workrec.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription

import com.workrec.domain.entities.CalendarDay
import com.workrec.domain.entities.MonthData
import com.workrec.presentation.ui.theme.WorkRecTheme
import com.workrec.presentation.ui.utils.ResponsiveUtils
import com.workrec.presentation.ui.utils.CalendarLayoutConfig
import kotlinx.datetime.LocalDate
import java.time.YearMonth

/**
 * Monthly calendar grid component that displays a full month view with day-of-week headers
 * 
 * @param monthData The month data containing all calendar days
 * @param selectedDate Currently selected date (nullable)
 * @param onDateSelected Callback when a date is selected
 * @param modifier Modifier for styling
 */
@Composable
fun MonthlyCalendarGrid(
    monthData: MonthData,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val layoutConfig = ResponsiveUtils.getCalendarLayoutConfig()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "カレンダーグリッド, ${monthData.yearMonth.year}年${monthData.yearMonth.monthValue}月"
            }
    ) {
        // Day of week headers
        DayOfWeekHeaders(layoutConfig = layoutConfig)
        
        // Calendar grid with responsive sizing
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(layoutConfig.cellSpacing),
            horizontalArrangement = Arrangement.spacedBy(layoutConfig.cellSpacing),
            modifier = Modifier
                .padding(horizontal = layoutConfig.horizontalPadding)
                .heightIn(min = layoutConfig.minCellSize * 6) // Ensure minimum height for 6 rows
        ) {
            items(monthData.days) { calendarDay ->
                CalendarDayCell(
                    calendarDay = calendarDay.copy(isSelected = calendarDay.date == selectedDate),
                    onClick = { onDateSelected(calendarDay.date) },
                    minSize = layoutConfig.minCellSize
                )
            }
        }
    }
}

/**
 * Day of week headers (Sun, Mon, Tue, etc.)
 */
@Composable
private fun DayOfWeekHeaders(
    layoutConfig: CalendarLayoutConfig,
    modifier: Modifier = Modifier
) {
    val dayHeaders = listOf("日", "月", "火", "水", "木", "金", "土")
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = layoutConfig.horizontalPadding, 
                vertical = layoutConfig.verticalPadding
            )
            .semantics {
                contentDescription = "曜日ヘッダー"
            }
    ) {
        items(dayHeaders.size) { index ->
            val dayHeader = dayHeaders[index]
            val dayName = when (index) {
                0 -> "日曜日"
                1 -> "月曜日"
                2 -> "火曜日"
                3 -> "水曜日"
                4 -> "木曜日"
                5 -> "金曜日"
                6 -> "土曜日"
                else -> dayHeader
            }
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(vertical = layoutConfig.verticalPadding / 2)
                    .semantics {
                        contentDescription = dayName
                    }
            ) {
                Text(
                    text = dayHeader,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Preview parameter provider for different screen sizes
private class ScreenSizePreviewParameterProvider : PreviewParameterProvider<ResponsiveUtils.ScreenSize> {
    override val values = sequenceOf(
        ResponsiveUtils.ScreenSize.COMPACT,
        ResponsiveUtils.ScreenSize.MEDIUM,
        ResponsiveUtils.ScreenSize.EXPANDED
    )
}

@Preview(showBackground = true, name = "Phone", widthDp = 360, heightDp = 640)
@Preview(showBackground = true, name = "Tablet", widthDp = 840, heightDp = 1024)
@Preview(showBackground = true, name = "Desktop", widthDp = 1200, heightDp = 800)
@Composable
private fun MonthlyCalendarGridPreview() {
    WorkRecTheme {
        // Create sample month data for January 2024
        val sampleDays = mutableListOf<CalendarDay>()
        
        // Add some previous month days (Dec 2023)
        for (day in 31..31) {
            sampleDays.add(
                CalendarDay(
                    date = LocalDate(2023, 12, day),
                    hasWorkout = false,
                    workoutCount = 0,
                    isToday = false,
                    isSelected = false,
                    isCurrentMonth = false
                )
            )
        }
        
        // Add current month days (Jan 2024)
        for (day in 1..31) {
            sampleDays.add(
                CalendarDay(
                    date = LocalDate(2024, 1, day),
                    hasWorkout = day % 3 == 0, // Some days have workouts
                    workoutCount = if (day % 3 == 0) (day % 3) + 1 else 0,
                    isToday = day == 15, // 15th is today
                    isSelected = false,
                    isCurrentMonth = true
                )
            )
        }
        
        // Add some next month days (Feb 2024)
        for (day in 1..3) {
            sampleDays.add(
                CalendarDay(
                    date = LocalDate(2024, 2, day),
                    hasWorkout = false,
                    workoutCount = 0,
                    isToday = false,
                    isSelected = false,
                    isCurrentMonth = false
                )
            )
        }
        
        val monthData = MonthData(
            yearMonth = YearMonth.of(2024, 1),
            days = sampleDays,
            workoutDates = setOf(
                LocalDate(2024, 1, 3),
                LocalDate(2024, 1, 6),
                LocalDate(2024, 1, 9),
                LocalDate(2024, 1, 12)
            )
        )
        
        MonthlyCalendarGrid(
            monthData = monthData,
            selectedDate = LocalDate(2024, 1, 10),
            onDateSelected = { },
            modifier = Modifier.padding(ResponsiveUtils.getResponsivePadding())
        )
    }
}