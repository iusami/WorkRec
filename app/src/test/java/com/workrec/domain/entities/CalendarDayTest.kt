package com.workrec.domain.entities

import kotlinx.datetime.LocalDate
import org.junit.Test
import com.google.common.truth.Truth.assertThat

class CalendarDayTest {
    
    @Test
    fun `CalendarDay creation with all properties`() {
        // Given
        val date = LocalDate(2024, 1, 15)
        
        // When
        val calendarDay = CalendarDay(
            date = date,
            hasWorkout = true,
            workoutCount = 2,
            isToday = true,
            isSelected = false,
            isCurrentMonth = true
        )
        
        // Then
        assertThat(calendarDay.date).isEqualTo(date)
        assertThat(calendarDay.hasWorkout).isTrue()
        assertThat(calendarDay.workoutCount).isEqualTo(2)
        assertThat(calendarDay.isToday).isTrue()
        assertThat(calendarDay.isSelected).isFalse()
        assertThat(calendarDay.isCurrentMonth).isTrue()
    }
    
    @Test
    fun `CalendarDay with no workout`() {
        // Given
        val date = LocalDate(2024, 1, 10)
        
        // When
        val calendarDay = CalendarDay(
            date = date,
            hasWorkout = false,
            workoutCount = 0,
            isToday = false,
            isSelected = true,
            isCurrentMonth = true
        )
        
        // Then
        assertThat(calendarDay.hasWorkout).isFalse()
        assertThat(calendarDay.workoutCount).isEqualTo(0)
        assertThat(calendarDay.isSelected).isTrue()
    }
    
    @Test
    fun `CalendarDay equality`() {
        // Given
        val date = LocalDate(2024, 1, 15)
        val calendarDay1 = CalendarDay(
            date = date,
            hasWorkout = true,
            workoutCount = 1,
            isToday = false,
            isSelected = false,
            isCurrentMonth = true
        )
        val calendarDay2 = CalendarDay(
            date = date,
            hasWorkout = true,
            workoutCount = 1,
            isToday = false,
            isSelected = false,
            isCurrentMonth = true
        )
        
        // Then
        assertThat(calendarDay1).isEqualTo(calendarDay2)
    }
}