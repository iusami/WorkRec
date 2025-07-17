# Implementation Plan

- [x] 1. Create calendar data models and utilities

  - Create CalendarDay data class with workout information and display states
  - Create MonthData data class to represent monthly calendar data
  - Implement utility functions for calendar calculations (days in month, first day of week, etc.)
  - Write unit tests for calendar data models and utility functions
  - _Requirements: 1.1, 1.3, 4.1_

- [x] 2. Extend WorkoutViewModel with calendar functionality

  - Add calendar-related state properties to WorkoutUiState (currentMonth, selectedDate, workoutDates, selectedDateWorkouts)
  - Implement month navigation functions (navigateToNextMonth, navigateToPreviousMonth, navigateToToday)
  - Implement date selection functionality (onDateSelected)
  - Add calendar data loading logic using existing GetWorkoutDatesUseCase and GetWorkoutsByDateUseCase
  - Write unit tests for new ViewModel calendar functionality
  - _Requirements: 2.1, 2.2, 3.1, 3.2, 3.3_

- [x] 3. Create calendar UI components
- [x] 3.1 Implement CalendarDayCell component

  - Create composable for individual calendar day display
  - Add visual indicators for workout presence (dot or highlight)
  - Implement selection state styling
  - Add today indicator styling
  - Include proper accessibility content descriptions
  - _Requirements: 1.2, 1.3, 2.1, 5.2_

- [x] 3.2 Implement MonthlyCalendarGrid component

  - Create LazyVerticalGrid layout for calendar display
  - Add day-of-week headers (Sun, Mon, Tue, etc.)
  - Implement proper grid spacing and sizing
  - Handle month boundary days (previous/next month dates)
  - Add click handling for date selection
  - _Requirements: 1.1, 2.1, 3.1, 5.1_

- [x] 3.3 Implement CalendarNavigationHeader component

  - Create month/year display with navigation arrows
  - Add "Today" button to jump to current date
  - Implement month navigation controls
  - Style according to Material Design 3 guidelines
  - _Requirements: 3.1, 3.2, 3.3_

- [x] 4. Create selected date workout display components
- [x] 4.1 Implement SelectedDateWorkoutList component

  - Create composable to display workouts for selected date
  - Reuse existing WorkoutCard component for consistency
  - Add empty state when no workouts exist for selected date
  - Implement loading state during workout data fetch
  - _Requirements: 2.2, 2.3, 4.2_

- [x] 4.2 Implement WorkoutSummaryCard component

  - Create compact workout summary for calendar view
  - Display key metrics (exercise count, total sets, volume)
  - Add click handler to navigate to workout detail
  - Style for calendar context (more compact than full WorkoutCard)
  - _Requirements: 2.3, 4.1, 4.2_

- [x] 5. Update WorkoutListScreen with calendar layout
- [x] 5.1 Replace list layout with calendar layout

  - Remove LazyColumn with workout list
  - Add MonthlyCalendarGrid as main content
  - Add CalendarNavigationHeader for month navigation
  - Maintain existing TopAppBar and FloatingActionButton
  - _Requirements: 1.1, 1.4, 4.3_

- [x] 5.2 Integrate selected date workout display

  - Add SelectedDateWorkoutList below calendar grid
  - Implement proper spacing and layout
  - Add loading states and error handling
  - Ensure smooth transitions between date selections
  - _Requirements: 2.2, 2.4, 4.2_

- [x] 5.3 Update empty state handling

  - Modify EmptyWorkoutState for calendar context
  - Show appropriate message when no workouts exist
  - Maintain add workout functionality
  - _Requirements: 2.2, 4.3_

- [x] 6. Connect calendar functionality to ViewModel

  - Wire up month navigation actions to ViewModel methods
  - Connect date selection to ViewModel state updates
  - Implement proper state observation in UI components
  - Add error handling for calendar data loading
  - _Requirements: 2.1, 2.4, 3.1, 3.2, 4.4_

- [x] 7. Add accessibility support

  - Implement proper content descriptions for calendar cells
  - Add semantic roles for calendar navigation
  - Ensure keyboard navigation support
  - Test with screen reader functionality
  - _Requirements: 5.2, 5.3_

- [x] 8. Implement responsive design

  - Ensure calendar adapts to different screen sizes
  - Optimize layout for tablets and large screens
  - Test calendar grid sizing and spacing
  - Verify touch target sizes meet accessibility guidelines
  - _Requirements: 5.1, 5.4_

- [x] 9. Write integration tests

  - Create tests for calendar navigation functionality
  - Test date selection and workout display flow
  - Verify proper state updates when navigating between months
  - Test error handling scenarios
  - _Requirements: 1.1, 2.1, 3.1, 4.4_

- [x] 10. Update existing tests
  - Modify WorkoutListScreen tests for new calendar layout
  - Update WorkoutViewModel tests for new calendar state
  - Ensure existing workout functionality tests still pass
  - Add tests for new calendar-specific features
  - _Requirements: 4.1, 4.3, 4.4_
