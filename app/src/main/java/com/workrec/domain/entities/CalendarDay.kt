package com.workrec.domain.entities

import kotlinx.datetime.LocalDate

/**
 * Represents a single day in the calendar view with workout information and display states
 */
data class CalendarDay(
    val date: LocalDate,
    val hasWorkout: Boolean,
    val workoutCount: Int,
    val isToday: Boolean,
    val isSelected: Boolean,
    val isCurrentMonth: Boolean
)