package com.workrec.domain.entities

import kotlinx.datetime.LocalDate
import java.time.YearMonth

/**
 * Represents monthly calendar data including all days and workout information
 */
data class MonthData(
    val yearMonth: YearMonth,
    val days: List<CalendarDay>,
    val workoutDates: Set<LocalDate>
)