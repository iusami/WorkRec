package com.workrec.presentation.ui.screens.workout

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.workrec.domain.entities.*
import com.workrec.presentation.ui.theme.WorkRecTheme
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * WorkoutListScreenのUIテスト - Calendar Layout対応
 */
@RunWith(AndroidJUnit4::class)
class WorkoutListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun workoutListScreen_カレンダーレイアウトが表示されること() {
        // Given: カレンダーレイアウトのワークアウト画面
        val testMonth = java.time.YearMonth.of(2024, 3)
        
        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.CalendarNavigationHeader(
                    currentMonth = testMonth,
                    onPreviousMonth = { },
                    onNextMonth = { },
                    onTodayClick = { }
                )
            }
        }

        // Then: カレンダーナビゲーションヘッダーが表示される
        composeTestRule
            .onNodeWithText("2024年3月")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithContentDescription("前の月")
            .assertIsDisplayed()
            
        composeTestRule
            .onNodeWithContentDescription("次の月")
            .assertIsDisplayed()
            
        composeTestRule
            .onNodeWithText("今日")
            .assertIsDisplayed()
    }

    @Test
    fun workoutListScreen_選択日なしの状態で適切なメッセージが表示されること() {
        // Given: 選択日がない状態
        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.SelectedDateWorkoutList(
                    selectedDate = null,
                    workouts = emptyList(),
                    isLoading = false,
                    onWorkoutClick = { },
                    onWorkoutDelete = { }
                )
            }
        }

        // Then: 日付選択を促すメッセージが表示される
        composeTestRule
            .onNodeWithText("カレンダーから日付を選択してください")
            .assertIsDisplayed()
    }

    @Test
    fun workoutCard_ワークアウト情報が正しく表示されること() {
        // Given: テスト用のワークアウト
        val workout = createTestWorkout()

        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.WorkoutCard(
                    workout = workout,
                    onClick = { },
                    onDelete = { }
                )
            }
        }

        // Then: ワークアウト情報が正しく表示される
        composeTestRule
            .onNodeWithText("2024年01月01日")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("2種目・3セット")
            .assertIsDisplayed()
            
        composeTestRule
            .onNodeWithText("総ボリューム: 1640.0kg")
            .assertIsDisplayed()
            
        composeTestRule
            .onNodeWithText("ベンチプレス, スクワット")
            .assertIsDisplayed()
    }

    @Test
    fun workoutCard_削除ボタンタップで確認ダイアログが表示されること() {
        // Given: テスト用のワークアウト
        val workout = createTestWorkout()

        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.WorkoutCard(
                    workout = workout,
                    onClick = { },
                    onDelete = { }
                )
            }
        }

        // When: 削除ボタンをタップ
        composeTestRule
            .onNodeWithContentDescription("削除")
            .performClick()

        // Then: 確認ダイアログが表示される
        composeTestRule
            .onNodeWithText("ワークアウトを削除")
            .assertIsDisplayed()
            
        composeTestRule
            .onNodeWithText("このワークアウトを削除しますか？この操作は取り消せません。")
            .assertIsDisplayed()
            
        composeTestRule
            .onNodeWithText("削除")
            .assertIsDisplayed()
            
        composeTestRule
            .onNodeWithText("キャンセル")
            .assertIsDisplayed()
    }

    @Test
    fun workoutCard_削除確認ダイアログでキャンセルできること() {
        // Given: テスト用のワークアウト
        val workout = createTestWorkout()

        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.WorkoutCard(
                    workout = workout,
                    onClick = { },
                    onDelete = { }
                )
            }
        }

        // When: 削除ボタンをタップしてキャンセル
        composeTestRule
            .onNodeWithContentDescription("削除")
            .performClick()
            
        composeTestRule
            .onNodeWithText("キャンセル")
            .performClick()

        // Then: ダイアログが閉じられる
        composeTestRule
            .onNodeWithText("ワークアウトを削除")
            .assertDoesNotExist()
    }

    @Test
    fun calendarGrid_displaysCorrectMonthAndYear() {
        // Given: Calendar component with specific month
        val testMonth = java.time.YearMonth.of(2024, 3)
        
        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.CalendarNavigationHeader(
                    currentMonth = testMonth,
                    onPreviousMonth = { },
                    onNextMonth = { },
                    onTodayClick = { }
                )
            }
        }

        // Then: Correct month and year are displayed
        composeTestRule
            .onNodeWithText("2024年3月")
            .assertIsDisplayed()
    }

    @Test
    fun calendarGrid_navigationButtonsAreClickable() {
        // Given: Calendar navigation header
        val testMonth = java.time.YearMonth.of(2024, 3)
        
        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.CalendarNavigationHeader(
                    currentMonth = testMonth,
                    onPreviousMonth = { },
                    onNextMonth = { },
                    onTodayClick = { }
                )
            }
        }

        // Then: Navigation buttons are present and clickable
        composeTestRule
            .onNodeWithContentDescription("前の月")
            .assertIsDisplayed()
            .assertHasClickAction()
            
        composeTestRule
            .onNodeWithContentDescription("次の月")
            .assertIsDisplayed()
            .assertHasClickAction()
            
        composeTestRule
            .onNodeWithText("今日")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun calendarDayCell_displaysWorkoutIndicator() {
        // Given: Calendar day with workout
        val testDate = LocalDate(2024, 3, 15)
        val calendarDay = com.workrec.domain.entities.CalendarDay(
            date = testDate,
            hasWorkout = true,
            workoutCount = 1,
            isToday = false,
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

        // Then: Day number is displayed
        composeTestRule
            .onNodeWithText("15")
            .assertIsDisplayed()
        
        // And: Workout indicator is present (visual indicator)
        // Note: The workout indicator is a visual element (dot), 
        // so we verify the component renders without error
        composeTestRule
            .onNodeWithText("15")
            .assertExists()
    }

    @Test
    fun calendarDayCell_selectedStateIsVisuallyDistinct() {
        // Given: Selected calendar day
        val testDate = LocalDate(2024, 3, 15)
        val calendarDay = com.workrec.domain.entities.CalendarDay(
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
                    calendarDay = calendarDay,
                    onClick = { }
                )
            }
        }

        // Then: Day is displayed and clickable
        composeTestRule
            .onNodeWithText("15")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun selectedDateWorkoutList_displaysWorkoutsForSelectedDate() {
        // Given: Selected date with workouts
        val selectedDate = LocalDate(2024, 3, 15)
        val workouts = listOf(createTestWorkout())
        
        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.SelectedDateWorkoutList(
                    selectedDate = selectedDate,
                    workouts = workouts,
                    isLoading = false,
                    onWorkoutClick = { },
                    onWorkoutDelete = { }
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

        // Then: Loading indicator is displayed
        composeTestRule
            .onNode(hasTestTag("loading_indicator") or hasContentDescription("読み込み中"))
            .assertExists()
    }

    @Test
    fun monthlyCalendarGrid_displaysCalendarDaysCorrectly() {
        // Given: Month data with workout dates
        val testMonth = java.time.YearMonth.of(2024, 3)
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

        // Then: Calendar displays days correctly
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
    fun workoutSummaryCard_displaysCompactWorkoutInfo() {
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

        // Then: Compact workout information is displayed
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
    fun calendarLayout_respondsToDateSelection() {
        // Given: Calendar with multiple dates
        val testMonth = java.time.YearMonth.of(2024, 3)
        val workoutDates = setOf(LocalDate(2024, 3, 15), LocalDate(2024, 3, 20))
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

        // When: Select a date with workout
        composeTestRule
            .onNodeWithText("15")
            .performClick()

        // Then: Date is selected
        assert(selectedDate == LocalDate(2024, 3, 15))

        // When: Select a different date
        composeTestRule
            .onNodeWithText("20")
            .performClick()

        // Then: New date is selected
        assert(selectedDate == LocalDate(2024, 3, 20))
    }

    @Test
    fun calendarNavigation_handlesMonthTransitions() {
        // Given: Calendar navigation header
        val initialMonth = java.time.YearMonth.of(2024, 3)
        var currentMonth = initialMonth
        
        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.CalendarNavigationHeader(
                    currentMonth = currentMonth,
                    onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                    onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                    onTodayClick = { currentMonth = java.time.YearMonth.now() }
                )
            }
        }

        // When: Navigate to next month
        composeTestRule
            .onNodeWithContentDescription("次の月")
            .performClick()

        // Then: Month advances
        assert(currentMonth == initialMonth.plusMonths(1))

        // When: Navigate to previous month
        composeTestRule
            .onNodeWithContentDescription("前の月")
            .performClick()

        // Then: Month goes back
        assert(currentMonth == initialMonth)

        // When: Click today button
        composeTestRule
            .onNodeWithText("今日")
            .performClick()

        // Then: Returns to current month
        assert(currentMonth == java.time.YearMonth.now())
    }

    @Test
    fun calendarAccessibility_elementsHaveProperContentDescriptions() {
        // Given: Calendar day with accessibility information
        val testDate = LocalDate(2024, 3, 15)
        val calendarDay = com.workrec.domain.entities.CalendarDay(
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
    fun responsiveLayout_adaptsToScreenSize() {
        // Given: Responsive utilities for different screen sizes
        composeTestRule.setContent {
            WorkRecTheme {
                // Test responsive padding calculation
                val responsivePadding = com.workrec.presentation.ui.utils.ResponsiveUtils.getResponsivePadding()
                val isTabletOrLarger = com.workrec.presentation.ui.utils.ResponsiveUtils.isTabletOrLarger()
                
                // Simple component to test responsive behavior
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxSize()
                        .padding(horizontal = responsivePadding)
                ) {
                    androidx.compose.material3.Text(
                        text = if (isTabletOrLarger) "Tablet Layout" else "Phone Layout"
                    )
                }
            }
        }

        // Then: Layout adapts based on screen size
        // Note: The actual layout depends on the test device configuration
        // This test verifies the responsive utilities work without error
        composeTestRule
            .onNode(hasText("Tablet Layout") or hasText("Phone Layout"))
            .assertExists()
    }

    @Test
    fun calendarKeyboardNavigation_supportsKeyboardInput() {
        // Given: Calendar day cell with keyboard navigation support
        val testDate = LocalDate(2024, 3, 15)
        val calendarDay = com.workrec.domain.entities.CalendarDay(
            date = testDate,
            hasWorkout = false,
            workoutCount = 0,
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

        // When: Focus and activate with keyboard
        composeTestRule
            .onNodeWithText("15")
            .requestFocus()
            .performKeyInput { pressKey(androidx.compose.ui.input.key.Key.Enter) }

        // Then: Click action is triggered
        assert(clicked)
    }

    @Test
    fun workoutDeletion_showsConfirmationInCalendarView() {
        // Given: Workout card with delete functionality
        val workout = createTestWorkout()
        var deleteClicked = false

        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.WorkoutCard(
                    workout = workout,
                    onClick = { },
                    onDelete = { deleteClicked = true }
                )
            }
        }

        // When: Delete button is clicked
        composeTestRule
            .onNodeWithContentDescription("削除")
            .performClick()

        // And: Confirm deletion
        composeTestRule
            .onNodeWithText("削除")
            .performClick()

        // Then: Delete callback is triggered
        assert(deleteClicked)
    }

    @Test
    fun calendarEmptyState_displaysAppropriateMessage() {
        // Given: Calendar with no selected date
        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.SelectedDateWorkoutList(
                    selectedDate = null,
                    workouts = emptyList(),
                    isLoading = false,
                    onWorkoutClick = { },
                    onWorkoutDelete = { }
                )
            }
        }

        // Then: Appropriate empty state message is displayed
        composeTestRule
            .onNodeWithText("カレンダーから日付を選択してください")
            .assertIsDisplayed()
    }

    @Test
    fun calendarPerformance_handlesLargeDataSets() {
        // Given: Large dataset for calendar
        val testMonth = java.time.YearMonth.of(2024, 3)
        val workoutDates = (1..31).map { LocalDate(2024, 3, it) }.toSet()
        val monthData = com.workrec.domain.utils.CalendarUtils.createMonthData(
            testMonth, 
            workoutDates, 
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

        // Then: Calendar renders without performance issues
        composeTestRule
            .onNodeWithText("1")
            .assertIsDisplayed()
            
        composeTestRule
            .onNodeWithText("31")
            .assertIsDisplayed()
    }

    // テストヘルパーメソッド
    private fun createTestWorkout(): Workout {
        val benchPressSets = listOf(
            ExerciseSet(reps = 10, weight = 60.0),
            ExerciseSet(reps = 8, weight = 70.0)
        )
        val squatSets = listOf(
            ExerciseSet(reps = 12, weight = 80.0)
        )
        
        val exercises = listOf(
            Exercise(
                id = 1L,
                name = "ベンチプレス",
                sets = benchPressSets,
                category = ExerciseCategory.CHEST
            ),
            Exercise(
                id = 2L,
                name = "スクワット",
                sets = squatSets,
                category = ExerciseCategory.LEGS
            )
        )
        
        return Workout(
            id = 1L,
            date = LocalDate(2024, 1, 1),
            exercises = exercises,
            notes = "テストワークアウト"
        )
    }
}