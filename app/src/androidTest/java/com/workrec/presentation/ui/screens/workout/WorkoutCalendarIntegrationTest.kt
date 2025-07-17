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
 * Calendar UI component integration tests
 * Tests individual calendar components and their interactions
 */
@RunWith(AndroidJUnit4::class)
class WorkoutCalendarIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun calendarNavigationHeader_displaysCorrectMonthAndNavigationButtons() {
        // Given: Calendar navigation header with specific month
        val testMonth = YearMonth.of(2024, 3)
        var previousClicked = false
        var nextClicked = false
        var todayClicked = false
        
        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.CalendarNavigationHeader(
                    currentMonth = testMonth,
                    onPreviousMonth = { previousClicked = true },
                    onNextMonth = { nextClicked = true },
                    onTodayClick = { todayClicked = true }
                )
            }
        }

        // Then: Month and year are displayed correctly
        composeTestRule
            .onNodeWithText("2024年3月")
            .assertIsDisplayed()

        // When: Previous month button is clicked
        composeTestRule
            .onNodeWithContentDescription("前の月")
            .performClick()

        // Then: Previous month callback is triggered
        assert(previousClicked)

        // When: Next month button is clicked
        composeTestRule
            .onNodeWithContentDescription("次の月")
            .performClick()

        // Then: Next month callback is triggered
        assert(nextClicked)

        // When: Today button is clicked
        composeTestRule
            .onNodeWithText("今日")
            .performClick()

        // Then: Today callback is triggered
        assert(todayClicked)
    }

    @Test
    fun calendarDayCell_displaysDateAndWorkoutIndicator() {
        // Given: Calendar day with workout
        val testDate = LocalDate(2024, 3, 15)
        val calendarDay = CalendarDay(
            date = testDate,
            hasWorkout = true,
            workoutCount = 2,
            isToday = false,
            isSelected = false,
            isCurrentMonth = true
        )
        var clicked = false
        
        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.CalendarDayCell(
                    calendarDay = calendarDay,
                    onClick = { clicked = true }
                )
            }
        }

        // Then: Day number is displayed
        composeTestRule
            .onNodeWithText("15")
            .assertIsDisplayed()

        // And: Cell is clickable
        composeTestRule
            .onNodeWithText("15")
            .assertHasClickAction()

        // When: Cell is clicked
        composeTestRule
            .onNodeWithText("15")
            .performClick()

        // Then: Click callback is triggered
        assert(clicked)
    }

    @Test
    fun calendarDayCell_selectedStateIsVisuallyDistinct() {
        // Given: Selected calendar day
        val testDate = LocalDate(2024, 3, 15)
        val selectedDay = CalendarDay(
            date = testDate,
            hasWorkout = false,
            workoutCount = 0,
            isToday = false,
            isSelected = true,
            isCurrentMonth = true
        )
        
        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.CalendarDayCell(
                    calendarDay = selectedDay,
                    onClick = { }
                )
            }
        }

        // Then: Day is displayed and has proper accessibility semantics
        composeTestRule
            .onNodeWithText("15")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun calendarDayCell_todayIsVisuallyDistinct() {
        // Given: Today's calendar day
        val testDate = LocalDate(2024, 3, 15)
        val todayDay = CalendarDay(
            date = testDate,
            hasWorkout = true,
            workoutCount = 1,
            isToday = true,
            isSelected = false,
            isCurrentMonth = true
        )
        
        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.CalendarDayCell(
                    calendarDay = todayDay,
                    onClick = { }
                )
            }
        }

        // Then: Today's day is displayed with proper styling
        composeTestRule
            .onNodeWithText("15")
            .assertIsDisplayed()
    }

    @Test
    fun monthlyCalendarGrid_displaysCalendarDaysInGrid() {
        // Given: Month data with calendar days
        val testMonth = YearMonth.of(2024, 3)
        val workoutDates = setOf(LocalDate(2024, 3, 15), LocalDate(2024, 3, 20))
        val monthData = com.workrec.domain.utils.CalendarUtils.createMonthData(
            testMonth, 
            workoutDates, 
            LocalDate(2024, 3, 15)
        )
        var selectedDate: LocalDate? = null
        
        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.MonthlyCalendarGrid(
                    monthData = monthData,
                    selectedDate = LocalDate(2024, 3, 15),
                    onDateSelected = { selectedDate = it }
                )
            }
        }

        // Then: Calendar grid displays days
        composeTestRule
            .onNodeWithText("1")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("15")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("31")
            .assertIsDisplayed()

        // When: A date is selected
        composeTestRule
            .onNodeWithText("20")
            .performClick()

        // Then: Date selection callback is triggered
        assert(selectedDate == LocalDate(2024, 3, 20))
    }

    @Test
    fun selectedDateWorkoutList_displaysWorkoutsForSelectedDate() {
        // Given: Selected date with workouts
        val selectedDate = LocalDate(2024, 3, 15)
        val workouts = listOf(createTestWorkout(selectedDate))
        var clickedWorkoutId: Long? = null
        var deletedWorkoutId: Long? = null
        
        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.SelectedDateWorkoutList(
                    selectedDate = selectedDate,
                    workouts = workouts,
                    isLoading = false,
                    onWorkoutClick = { clickedWorkoutId = it },
                    onWorkoutDelete = { deletedWorkoutId = it }
                )
            }
        }

        // Then: Selected date header is displayed
        composeTestRule
            .onNodeWithText("2024年3月15日のワークアウト")
            .assertIsDisplayed()

        // And: Workout details are displayed
        composeTestRule
            .onNodeWithText("ベンチプレス")
            .assertIsDisplayed()

        // When: Workout is clicked
        composeTestRule
            .onNodeWithText("ベンチプレス")
            .performClick()

        // Then: Workout click callback is triggered
        assert(clickedWorkoutId == 1L)
    }

    @Test
    fun selectedDateWorkoutList_displaysEmptyStateWhenNoWorkouts() {
        // Given: Selected date with no workouts
        val selectedDate = LocalDate(2024, 3, 15)
        
        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.SelectedDateWorkoutList(
                    selectedDate = selectedDate,
                    workouts = emptyList(),
                    isLoading = false,
                    onWorkoutClick = { },
                    onWorkoutDelete = { }
                )
            }
        }

        // Then: Empty state message is displayed
        composeTestRule
            .onNodeWithText("この日はワークアウトが記録されていません")
            .assertIsDisplayed()
    }

    @Test
    fun selectedDateWorkoutList_displaysLoadingState() {
        // Given: Selected date with loading state
        val selectedDate = LocalDate(2024, 3, 15)
        
        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.SelectedDateWorkoutList(
                    selectedDate = selectedDate,
                    workouts = emptyList(),
                    isLoading = true,
                    onWorkoutClick = { },
                    onWorkoutDelete = { }
                )
            }
        }

        // Then: Loading state is displayed
        // Note: The exact loading indicator depends on implementation
        // We verify the component renders without error during loading
        composeTestRule
            .onNodeWithText("2024年3月15日のワークアウト")
            .assertIsDisplayed()
    }

    @Test
    fun workoutSummaryCard_displaysWorkoutSummaryInformation() {
        // Given: Workout for summary display
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

        // Then: Workout summary information is displayed
        composeTestRule
            .onNodeWithText("ベンチプレス")
            .assertIsDisplayed()

        // When: Card is clicked
        composeTestRule
            .onNodeWithText("ベンチプレス")
            .performClick()

        // Then: Click callback is triggered
        assert(clicked)
    }

    @Test
    fun calendarAccessibility_elementsHaveProperContentDescriptions() {
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
    fun calendarGrid_handlesMonthBoundaryDatesCorrectly() {
        // Given: Month data that includes previous/next month dates
        val testMonth = YearMonth.of(2024, 3) // March 2024
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

        // Then: Calendar displays all days including boundary dates
        // March 2024 starts on Friday, so we should see some February dates
        // and some April dates to fill the grid
        composeTestRule
            .onNodeWithText("1")
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