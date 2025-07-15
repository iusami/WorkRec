package com.workrec.domain.entities

import kotlinx.datetime.LocalDate
import org.junit.Test
import com.google.common.truth.Truth.assertThat
import java.time.YearMonth

class MonthDataTest {
    
    @Test
    fun `MonthData creation with empty workout dates`() {
        // Given
        val yearMonth = YearMonth.of(2024, 1)
        val days = listOf(
            CalendarDay(
                date = LocalDate(2024, 1, 1),
                hasWorkout = false,
                workoutCount = 0,
                isToday = false,
                isSelected = false,
                isCurrentMonth = true
            )
        )
        val workoutDates = emptySet<LocalDate>()
        
        // When
        val monthData = MonthData(
            yearMonth = yearMonth,
            days = days,
            workoutDates = workoutDates
        )
        
        // Then
        assertThat(monthData.yearMonth).isEqualTo(yearMonth)
        assertThat(monthData.days).hasSize(1)
        assertThat(monthData.workoutDates).isEmpty()
    }
    
    @Test
    fun `MonthData creation with workout dates`() {
        // Given
        val yearMonth = YearMonth.of(2024, 1)
        val workoutDate = LocalDate(2024, 1, 15)
        val days = listOf(
            CalendarDay(
                date = workoutDate,
                hasWorkout = true,
                workoutCount = 1,
                isToday = false,
                isSelected = false,
                isCurrentMonth = true
            )
        )
        val workoutDates = setOf(workoutDate)
        
        // When
        val monthData = MonthData(
            yearMonth = yearMonth,
            days = days,
            workoutDates = workoutDates
        )
        
        // Then
        assertThat(monthData.yearMonth).isEqualTo(yearMonth)
        assertThat(monthData.days).hasSize(1)
        assertThat(monthData.workoutDates).containsExactly(workoutDate)
        assertThat(monthData.days.first().hasWorkout).isTrue()
    }
    
    @Test
    fun `MonthData equality`() {
        // Given
        val yearMonth = YearMonth.of(2024, 1)
        val days = listOf(
            CalendarDay(
                date = LocalDate(2024, 1, 1),
                hasWorkout = false,
                workoutCount = 0,
                isToday = false,
                isSelected = false,
                isCurrentMonth = true
            )
        )
        val workoutDates = emptySet<LocalDate>()
        
        val monthData1 = MonthData(yearMonth, days, workoutDates)
        val monthData2 = MonthData(yearMonth, days, workoutDates)
        
        // Then
        assertThat(monthData1).isEqualTo(monthData2)
    }
}