package com.workrec.domain.utils

import com.workrec.domain.entities.CalendarDay
import com.workrec.domain.entities.MonthData
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.YearMonth
import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Utility functions for calendar calculations and data generation
 */
object CalendarUtils {
    
    /**
     * Gets the current date in the system timezone
     */
    fun getCurrentDate(): LocalDate {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    
    /**
     * Converts YearMonth to kotlinx LocalDate for the first day of the month
     */
    fun yearMonthToLocalDate(yearMonth: YearMonth): LocalDate {
        return LocalDate(yearMonth.year, yearMonth.monthValue, 1)
    }
    
    /**
     * Converts kotlinx LocalDate to java.time YearMonth
     */
    fun localDateToYearMonth(date: LocalDate): YearMonth {
        return YearMonth.of(date.year, date.monthNumber)
    }
    
    /**
     * Gets the number of days in a given month
     */
    fun getDaysInMonth(yearMonth: YearMonth): Int {
        return yearMonth.lengthOfMonth()
    }
    
    /**
     * Gets the first day of week for a given month (1 = Monday, 7 = Sunday)
     */
    fun getFirstDayOfWeek(yearMonth: YearMonth): Int {
        val firstDay = yearMonth.atDay(1)
        return firstDay.dayOfWeek.value
    }
    
    /**
     * Gets the day of week for a given date (1 = Monday, 7 = Sunday)
     */
    fun getDayOfWeek(date: LocalDate): Int {
        val javaDate = java.time.LocalDate.of(date.year, date.monthNumber, date.dayOfMonth)
        return javaDate.dayOfWeek.value
    }
    
    /**
     * Calculates the number of weeks needed to display a month in calendar grid
     */
    fun getWeeksInMonth(yearMonth: YearMonth): Int {
        val daysInMonth = getDaysInMonth(yearMonth)
        val firstDayOfWeek = getFirstDayOfWeek(yearMonth)
        val totalCells = daysInMonth + firstDayOfWeek - 1
        return (totalCells + 6) / 7 // Round up to nearest week
    }
    
    /**
     * Generates a list of dates for calendar display including previous/next month dates
     * to fill the calendar grid (42 days = 6 weeks)
     */
    fun generateCalendarDates(yearMonth: YearMonth): List<LocalDate> {
        val dates = mutableListOf<LocalDate>()
        val firstDayOfMonth = yearMonth.atDay(1)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value
        
        // Add dates from previous month to fill the first week
        val previousMonth = yearMonth.minusMonths(1)
        val daysInPreviousMonth = previousMonth.lengthOfMonth()
        for (i in firstDayOfWeek - 1 downTo 1) {
            val day = daysInPreviousMonth - i + 1
            dates.add(LocalDate(previousMonth.year, previousMonth.monthValue, day))
        }
        
        // Add all dates from current month
        val daysInCurrentMonth = yearMonth.lengthOfMonth()
        for (day in 1..daysInCurrentMonth) {
            dates.add(LocalDate(yearMonth.year, yearMonth.monthValue, day))
        }
        
        // Add dates from next month to fill remaining cells (up to 42 total)
        val nextMonth = yearMonth.plusMonths(1)
        val remainingCells = 42 - dates.size
        for (day in 1..remainingCells) {
            dates.add(LocalDate(nextMonth.year, nextMonth.monthValue, day))
        }
        
        return dates
    }
    
    /**
     * Creates MonthData with CalendarDay objects for a given month
     */
    fun createMonthData(
        yearMonth: YearMonth,
        workoutDates: Set<LocalDate>,
        selectedDate: LocalDate? = null
    ): MonthData {
        val today = getCurrentDate()
        val calendarDates = generateCalendarDates(yearMonth)
        
        val calendarDays = calendarDates.map { date ->
            CalendarDay(
                date = date,
                hasWorkout = workoutDates.contains(date),
                workoutCount = if (workoutDates.contains(date)) 1 else 0, // Simplified for now
                isToday = date == today,
                isSelected = date == selectedDate,
                isCurrentMonth = date.year == yearMonth.year && date.monthNumber == yearMonth.monthValue
            )
        }
        
        return MonthData(
            yearMonth = yearMonth,
            days = calendarDays,
            workoutDates = workoutDates
        )
    }
    
    /**
     * Checks if a date is in the current month
     */
    fun isDateInMonth(date: LocalDate, yearMonth: YearMonth): Boolean {
        return date.year == yearMonth.year && date.monthNumber == yearMonth.monthValue
    }
    
    /**
     * Gets the previous month
     */
    fun getPreviousMonth(yearMonth: YearMonth): YearMonth {
        return yearMonth.minusMonths(1)
    }
    
    /**
     * Gets the next month
     */
    fun getNextMonth(yearMonth: YearMonth): YearMonth {
        return yearMonth.plusMonths(1)
    }
    
    /**
     * Gets the current month
     */
    fun getCurrentMonth(): YearMonth {
        val today = getCurrentDate()
        return YearMonth.of(today.year, today.monthNumber)
    }
}