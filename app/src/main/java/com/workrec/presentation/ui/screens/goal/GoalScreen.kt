package com.workrec.presentation.ui.screens.goal

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
import com.workrec.domain.entities.Goal
import com.workrec.presentation.ui.components.GoalCard
import com.workrec.presentation.viewmodel.GoalViewModel

/**
 * 目標画面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(
    onNavigateToAddGoal: () -> Unit,
    onNavigateToGoalDetail: (Long) -> Unit,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val activeGoals by viewModel.activeGoals.collectAsStateWithLifecycle()
    val completedGoals by viewModel.completedGoals.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.goal)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddGoal
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add)
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
                
                activeGoals.isEmpty() && completedGoals.isEmpty() -> {
                    EmptyGoalState(
                        onAddGoal = onNavigateToAddGoal,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (activeGoals.isNotEmpty()) {
                            item {
                                Text(
                                    text = "アクティブな目標",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            
                            items(activeGoals) { goal ->
                                GoalCard(
                                    goal = goal,
                                    onClick = { onNavigateToGoalDetail(goal.id) }
                                )
                            }
                        }
                        
                        if (completedGoals.isNotEmpty()) {
                            item {
                                Text(
                                    text = "完了した目標",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(top = if (activeGoals.isNotEmpty()) 16.dp else 0.dp, bottom = 8.dp)
                                )
                            }
                            
                            items(completedGoals) { goal ->
                                GoalCard(
                                    goal = goal,
                                    onClick = { onNavigateToGoalDetail(goal.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 目標が空の場合の表示
 */
@Composable
private fun EmptyGoalState(
    onAddGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "まだ目標が設定されていません",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "最初の目標を設定しましょう！",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = onAddGoal
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("目標を追加")
        }
    }
}