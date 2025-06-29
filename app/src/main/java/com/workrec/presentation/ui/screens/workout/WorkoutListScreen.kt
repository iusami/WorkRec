package com.workrec.presentation.ui.screens.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workrec.R
import com.workrec.domain.entities.Workout
import com.workrec.presentation.ui.components.WorkoutCard
import com.workrec.presentation.viewmodel.WorkoutViewModel

/**
 * ワークアウト一覧画面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListScreen(
    onNavigateToAddWorkout: () -> Unit,
    onNavigateToWorkoutDetail: (Long) -> Unit,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // エラーメッセージの表示
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // TODO: Snackbarでエラーメッセージを表示
            viewModel.clearError()
        }
    }

    // 成功メッセージの表示
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            // TODO: Snackbarでメッセージを表示
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.workout_list)) }
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
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                workouts.isEmpty() -> {
                    EmptyWorkoutState(
                        onAddWorkout = onNavigateToAddWorkout,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(workouts) { workout ->
                            WorkoutCard(
                                workout = workout,
                                onClick = { onNavigateToWorkoutDetail(workout.id) },
                                onDelete = { viewModel.deleteWorkout(workout) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * ワークアウトが空の場合の表示
 */
@Composable
private fun EmptyWorkoutState(
    onAddWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "まだワークアウトが記録されていません",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "最初のワークアウトを追加しましょう！",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = onAddWorkout
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.add_workout))
        }
    }
}