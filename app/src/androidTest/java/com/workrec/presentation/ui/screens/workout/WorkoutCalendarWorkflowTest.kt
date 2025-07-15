package com.workrec.presentation.ui.screens.workout

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.workrec.domain.entities.*
import com.workrec.presentation.ui.theme.WorkRecTheme
import kotlinx.datetime.LocalDate
import java.time.YearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Calendar workflow integration tests
 * Tests complex calendar component interactions and workflows
 */
@RunWith(AndroidJUnit4::class)
class WorkoutCalendarWorkflowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun calendarNavigationWorkflow_multipleMonthNavigationMaintainsState() {
        // Given: Calendar navigation header with month navigation
        val initialMonth = YearMonth.of(2024, 3)
        var currentMonth = initialMonth
        
        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.CalendarNavigationHeader(
                    currentMonth = currentMonth,
                    onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                    onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                    onTodayClick = { currentMonth = YearMonth.now() }
                )
            }
        }

        // When: Navigate through multiple months
        composeTestRule
            .onNodeWithContentDescription("次の月")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription("次の月")
            .performClick()

        // Then: Month has advanced correctly
        assert(currentMonth == initialMonth.plusMonths(2))

        // When: Navigate back
        composeTestRule
            .onNodeWithContentDescription("前の月")
            .performClick()

        // Then: Month has moved back correctly
        assert(currentMonth == initialMonth.plusMonths(1))
    }

    @Test
    fun calendarDateSelection_multipleSelectionsMaintainState() {
        // Given: Calendar with multiple dates
        val testMonth = YearMonth.of(2024, 3)
        val workoutDates = setOf(
            LocalDate(2024, 3, 15),
            LocalDate(2024, 3, 20),
            LocalDate(2024, 3, 25)
        )
        val monthData = com.workrec.domain.utils.CalendarUtils.createMonthData(
            testMonth, 
            workoutDates, 
            null
        )
        var selectedDate: LocalDate? = null
        
        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.MonthlyCalendarGrid(
                    monthData = monthData,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it }
                )
            }
        }

        // When: Select first date
        composeTestRule
            .onNodeWithText("15")
            .performClick()

        // Then: First date is selected
        assert(selectedDate == LocalDate(2024, 3, 15))

        // When: Select second date
        composeTestRule
            .onNodeWithText("20")
            .performClick()

        // Then: Second date is selected
        assert(selectedDate == LocalDate(2024, 3, 20))

        // When: Select third date
        composeTestRule
            .onNodeWithText("25")
            .performClick()

        // Then: Third date is selected
        assert(selectedDate == LocalDate(2024, 3, 25))
    }

    @Test
    fun workoutListDisplay_showsWorkoutsForSelectedDate() {
        // Given: Selected date with workouts
        val selectedDate = LocalDate(2024, 3, 15)
        val workouts = listOf(
            createTestWorkout(selectedDate),
            createTestWorkout(selectedDate).copy(id = 2L)
        )
        var clickedWorkoutId: Long? = null
        
        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.SelectedDateWorkoutList(
                    selectedDate = selectedDate,
                    workouts = workouts,
                    isLoading = false,
                    onWorkoutClick = { clickedWorkoutId = it },
                    onWorkoutDelete = { }
                )
            }
        }

        // Then: Multiple workouts are displayed
        composeTestRule
            .onNodeWithText("2024年3月15日のワークアウト")
            .assertIsDisplayed()

        composeTestRule
            .onAllNodesWithText("ベンチプレス")
            .assertCountEquals(2)

        // When: First workout is clicked
        composeTestRule
            .onAllNodesWithText("ベンチプレス")[0]
            .performClick()

        // Then: Click callback is triggered
        assert(clickedWorkoutId == 1L)
    }

    @Test
    fun todayButtonNavigation_returnsToCurrentMonth() {
        // Given: Calendar navigation with today button
        val initialMonth = YearMonth.of(2024, 1)
        var currentMonth = initialMonth
        var todayClicked = false
        
        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.CalendarNavigationHeader(
                    currentMonth = currentMonth,
                    onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                    onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                    onTodayClick = { 
                        currentMonth = YearMonth.now()
                        todayClicked = true
                    }
                )
            }
        }

        // When: Navigate to different month
        repeat(3) {
            composeTestRule
                .onNodeWithContentDescription("次の月")
                .performClick()
        }

        // Then: Month has changed
        assert(currentMonth == initialMonth.plusMonths(3))

        // When: Click today button
        composeTestRule
            .onNodeWithText("今日")
            .performClick()

        // Then: Today callback is triggered and month is reset
        assert(todayClicked)
        assert(currentMonth == YearMonth.now())
    }

    @Test
    fun workoutSummaryCardInteraction_handlesClicksCorrectly() {
        // Given: Workout summary card
        val workout = createTestWorkout()
        var clicked = false
        
        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.WorkoutSummaryCard(
                    workout = workout,
                    onClick = { clicked = true }
                )
            }
        }

        // When: Card is clicked
        composeTestRule
            .onNodeWithText("ベンチプレス")
            .performClick()

        // Then: Click callback is triggered
        assert(clicked)
    }

    @Test
    fun calendarAccessibility_elementsHaveProperSemantics() {
        // Given: Calendar day with accessibility information
        val testDate = LocalDate(2024, 3, 15)
        val calendarDay = CalendarDay(
            date = testDate,
            hasWorkout = true,
            workoutCount = 2,
            isToday = true,
            isSelected = false,
            isCurrentMonth = true
        )
        
        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.CalendarDayCell(
                    calendarDay = calendarDay,
                    onClick = { }
                )
            }
        }

        // Then: Calendar cell has proper accessibility semantics
        composeTestRule
            .onNodeWithText("15")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun calendarGrid_handlesEmptyStateCorrectly() {
        // Given: Month data with no workout dates
        val testMonth = YearMonth.of(2024, 3)
        val monthData = com.workrec.domain.utils.CalendarUtils.createMonthData(
            testMonth, 
            emptySet(), 
            null
        )
        
        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.MonthlyCalendarGrid(
                    monthData = monthData,
                    selectedDate = null,
                    onDateSelected = { }
                )
            }
        }

        // Then: Calendar displays all days without workout indicators
        composeTestRule
            .onNodeWithText("1")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("15")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("31")
            .assertIsDisplayed()
    }

    // Test helper methods
    private fun createTestWorkout(date: LocalDate = LocalDate(2024, 1, 1)): Workout {
        val sets = listOf(
            ExerciseSet(reps = 10, weight = 60.0),
            ExerciseSet(reps = 8, weight = 70.0)
        )
        val exercise = Exercise(
            id = 1L,
            name = "ベンチプレス",
            sets = sets,
            category = ExerciseCategory.CHEST
        )
        
        return Workout(
            id = 1L,
            date = date,
            exercises = listOf(exercise),
            notes = "テストワークアウト"
        )
    }
}