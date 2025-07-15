package com.workrec.presentation.ui.screens.workout

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workrec.R
import com.workrec.domain.entities.Workout
import com.workrec.domain.entities.MonthData
import com.workrec.domain.utils.CalendarUtils
import com.workrec.presentation.ui.components.WorkoutCard
import com.workrec.presentation.ui.components.MonthlyCalendarGrid
import com.workrec.presentation.ui.components.CalendarNavigationHeader
import com.workrec.presentation.ui.components.SelectedDateWorkoutList
import com.workrec.presentation.ui.utils.ResponsiveUtils
import com.workrec.presentation.viewmodel.WorkoutViewModel

/**
 * ワークアウト一覧画面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListScreen(
    onNavigateToAddWorkout: () -> Unit,
    onNavigateToWorkoutDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: WorkoutViewModel
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // エラーメッセージの表示
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    // 成功メッセージの表示
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.workout_list)) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "設定"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddWorkout
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_workout)
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        val responsivePadding = ResponsiveUtils.getResponsivePadding()
        val isTabletOrLarger = ResponsiveUtils.isTabletOrLarger()
        
        if (isTabletOrLarger) {
            // Tablet/Large screen layout - side by side
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = responsivePadding)
            ) {
                // Calendar section
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    CalendarNavigationHeader(
                        currentMonth = uiState.currentMonth,
                        onPreviousMonth = viewModel::navigateToPreviousMonth,
                        onNextMonth = viewModel::navigateToNextMonth,
                        onTodayClick = viewModel::navigateToToday
                    )
                    
                    val monthData = remember(uiState.currentMonth, uiState.workoutDates, uiState.selectedDate) {
                        CalendarUtils.createMonthData(uiState.currentMonth, uiState.workoutDates, uiState.selectedDate)
                    }
                    
                    MonthlyCalendarGrid(
                        monthData = monthData,
                        selectedDate = uiState.selectedDate,
                        onDateSelected = viewModel::onDateSelected
                    )
                }
                
                // Vertical divider
                VerticalDivider(
                    modifier = Modifier.padding(horizontal = responsivePadding),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                
                // Workout list section
                SelectedDateWorkoutList(
                    selectedDate = uiState.selectedDate,
                    workouts = uiState.selectedDateWorkouts,
                    isLoading = uiState.isLoadingWorkouts,
                    onWorkoutClick = onNavigateToWorkoutDetail,
                    onWorkoutDelete = { workoutId ->
                        uiState.selectedDateWorkouts.find { it.id == workoutId }?.let { workout ->
                            viewModel.deleteWorkout(workout)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        } else {
            // Phone layout - stacked vertically
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Calendar Navigation Header
                CalendarNavigationHeader(
                    currentMonth = uiState.currentMonth,
                    onPreviousMonth = viewModel::navigateToPreviousMonth,
                    onNextMonth = viewModel::navigateToNextMonth,
                    onTodayClick = viewModel::navigateToToday
                )
                
                // Calendar Grid
                val monthData = remember(uiState.currentMonth, uiState.workoutDates, uiState.selectedDate) {
                    CalendarUtils.createMonthData(uiState.currentMonth, uiState.workoutDates, uiState.selectedDate)
                }
                
                MonthlyCalendarGrid(
                    monthData = monthData,
                    selectedDate = uiState.selectedDate,
                    onDateSelected = viewModel::onDateSelected
                )
                
                // Divider between calendar and workout list
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = responsivePadding / 2),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                
                // Selected date workout display
                SelectedDateWorkoutList(
                    selectedDate = uiState.selectedDate,
                    workouts = uiState.selectedDateWorkouts,
                    isLoading = uiState.isLoadingWorkouts,
                    onWorkoutClick = onNavigateToWorkoutDetail,
                    onWorkoutDelete = { workoutId ->
                        // Find the workout by ID and delete it
                        uiState.selectedDateWorkouts.find { it.id == workoutId }?.let { workout ->
                            viewModel.deleteWorkout(workout)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

