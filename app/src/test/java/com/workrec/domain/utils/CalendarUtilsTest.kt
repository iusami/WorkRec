package com.workrec.domain.utils

import kotlinx.datetime.LocalDate
import org.junit.Test
import com.google.common.truth.Truth.assertThat
import java.time.YearMonth

class CalendarUtilsTest {
    
    @Test
    fun `yearMonthToLocalDate converts correctly`() {
        // Given
        val yearMonth = YearMonth.of(2024, 3)
        
        // When
        val result = CalendarUtils.yearMonthToLocalDate(yearMonth)
        
        // Then
        assertThat(result).isEqualTo(LocalDate(2024, 3, 1))
    }
    
    @Test
    fun `localDateToYearMonth converts correctly`() {
        // Given
        val date = LocalDate(2024, 3, 15)
        
        // When
        val result = CalendarUtils.localDateToYearMonth(date)
        
        // Then
        assertThat(result).isEqualTo(YearMonth.of(2024, 3))
    }
    
    @Test
    fun `getDaysInMonth returns correct number of days`() {
        // Given
        val january = YearMonth.of(2024, 1)
        val february = YearMonth.of(2024, 2) // Leap year
        val februaryNonLeap = YearMonth.of(2023, 2)
        
        // When & Then
        assertThat(CalendarUtils.getDaysInMonth(january)).isEqualTo(31)
        assertThat(CalendarUtils.getDaysInMonth(february)).isEqualTo(29) // Leap year
        assertThat(CalendarUtils.getDaysInMonth(februaryNonLeap)).isEqualTo(28)
    }
    
    @Test
    fun `getFirstDayOfWeek returns correct day`() {
        // Given - January 1, 2024 is a Monday (1)
        val yearMonth = YearMonth.of(2024, 1)
        
        // When
        val result = CalendarUtils.getFirstDayOfWeek(yearMonth)
        
        // Then
        assertThat(result).isEqualTo(1) // Monday
    }
    
    @Test
    fun `getDayOfWeek returns correct day`() {
        // Given - January 1, 2024 is a Monday
        val date = LocalDate(2024, 1, 1)
        
        // When
        val result = CalendarUtils.getDayOfWeek(date)
        
        // Then
        assertThat(result).isEqualTo(1) // Monday
    }
    
    @Test
    fun `getWeeksInMonth calculates correct number of weeks`() {
        // Given
        val january2024 = YearMonth.of(2024, 1) // 31 days, starts on Monday
        val february2024 = YearMonth.of(2024, 2) // 29 days, starts on Thursday
        
        // When & Then
        assertThat(CalendarUtils.getWeeksInMonth(january2024)).isEqualTo(5)
        assertThat(CalendarUtils.getWeeksInMonth(february2024)).isEqualTo(5)
    }
    
    @Test
    fun `generateCalendarDates returns 42 dates`() {
        // Given
        val yearMonth = YearMonth.of(2024, 1)
        
        // When
        val result = CalendarUtils.generateCalendarDates(yearMonth)
        
        // Then
        assertThat(result).hasSize(42) // 6 weeks * 7 days
    }
    
    @Test
    fun `generateCalendarDates includes previous month dates`() {
        // Given - January 2024 starts on Monday, so no previous month dates needed
        val yearMonth = YearMonth.of(2024, 2) // February starts on Thursday
        
        // When
        val result = CalendarUtils.generateCalendarDates(yearMonth)
        
        // Then
        // First few dates should be from January
        assertThat(result[0]).isEqualTo(LocalDate(2024, 1, 29)) // Monday before Feb 1
        assertThat(result[1]).isEqualTo(LocalDate(2024, 1, 30))
        assertThat(result[2]).isEqualTo(LocalDate(2024, 1, 31))
        assertThat(result[3]).isEqualTo(LocalDate(2024, 2, 1)) // First day of February
    }
    
    @Test
    fun `generateCalendarDates includes next month dates`() {
        // Given
        val yearMonth = YearMonth.of(2024, 1)
        
        // When
        val result = CalendarUtils.generateCalendarDates(yearMonth)
        
        // Then
        // Last few dates should be from February
        val lastDate = result.last()
        assertThat(lastDate.monthNumber).isEqualTo(2) // February
    }
    
    @Test
    fun `createMonthData generates correct CalendarDay objects`() {
        // Given
        val yearMonth = YearMonth.of(2024, 1)
        val workoutDates = setOf(LocalDate(2024, 1, 15), LocalDate(2024, 1, 20))
        val selectedDate = LocalDate(2024, 1, 15)
        
        // When
        val result = CalendarUtils.createMonthData(yearMonth, workoutDates, selectedDate)
        
        // Then
        assertThat(result.yearMonth).isEqualTo(yearMonth)
        assertThat(result.days).hasSize(42)
        assertThat(result.workoutDates).isEqualTo(workoutDates)
        
        // Check specific dates
        val workoutDay = result.days.find { it.date == LocalDate(2024, 1, 15) }
        assertThat(workoutDay?.hasWorkout).isTrue()
        assertThat(workoutDay?.isSelected).isTrue()
        assertThat(workoutDay?.isCurrentMonth).isTrue()
        
        val nonWorkoutDay = result.days.find { it.date == LocalDate(2024, 1, 10) }
        assertThat(nonWorkoutDay?.hasWorkout).isFalse()
        assertThat(nonWorkoutDay?.isSelected).isFalse()
    }
    
    @Test
    fun `isDateInMonth returns correct boolean`() {
        // Given
        val yearMonth = YearMonth.of(2024, 1)
        val dateInMonth = LocalDate(2024, 1, 15)
        val dateNotInMonth = LocalDate(2024, 2, 15)
        
        // When & Then
        assertThat(CalendarUtils.isDateInMonth(dateInMonth, yearMonth)).isTrue()
        assertThat(CalendarUtils.isDateInMonth(dateNotInMonth, yearMonth)).isFalse()
    }
    
    @Test
    fun `getPreviousMonth returns correct month`() {
        // Given
        val yearMonth = YearMonth.of(2024, 3)
        
        // When
        val result = CalendarUtils.getPreviousMonth(yearMonth)
        
        // Then
        assertThat(result).isEqualTo(YearMonth.of(2024, 2))
    }
    
    @Test
    fun `getPreviousMonth handles year boundary`() {
        // Given
        val yearMonth = YearMonth.of(2024, 1)
        
        // When
        val result = CalendarUtils.getPreviousMonth(yearMonth)
        
        // Then
        assertThat(result).isEqualTo(YearMonth.of(2023, 12))
    }
    
    @Test
    fun `getNextMonth returns correct month`() {
        // Given
        val yearMonth = YearMonth.of(2024, 3)
        
        // When
        val result = CalendarUtils.getNextMonth(yearMonth)
        
        // Then
        assertThat(result).isEqualTo(YearMonth.of(2024, 4))
    }
    
    @Test
    fun `getNextMonth handles year boundary`() {
        // Given
        val yearMonth = YearMonth.of(2024, 12)
        
        // When
        val result = CalendarUtils.getNextMonth(yearMonth)
        
        // Then
        assertThat(result).isEqualTo(YearMonth.of(2025, 1))
    }
}