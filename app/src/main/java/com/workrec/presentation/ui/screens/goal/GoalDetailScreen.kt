package com.workrec.presentation.ui.screens.goal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.workrec.domain.entities.Goal
import com.workrec.domain.entities.GoalProgressRecord
import com.workrec.domain.usecase.goal.GoalDetailData
import com.workrec.domain.usecase.goal.ProgressStats
import com.workrec.presentation.viewmodel.GoalDetailViewModel
import kotlinx.datetime.LocalDate

/**
 * 目標詳細画面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    goalId: Long,
    onNavigateBack: () -> Unit,
    onEditGoal: (Long) -> Unit,
    onAddProgress: (Long) -> Unit,
    viewModel: GoalDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(goalId) {
        viewModel.loadGoalDetail(goalId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.goalDetailData?.goal?.title ?: "目標詳細") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            uiState.goalDetailData?.goal?.let { goal ->
                                onEditGoal(goal.id)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "編集")
                    }
                    
                    IconButton(
                        onClick = { onAddProgress(goalId) }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "進捗追加")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddProgress(goalId) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "進捗追加")
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            else -> {
                uiState.errorMessage?.let { errorMessage ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                } ?: uiState.goalDetailData?.let { goalDetailData ->
                    GoalDetailContent(
                        goalDetailData = goalDetailData,
                        onUpdateProgress = viewModel::updateProgress,
                        onDeleteProgress = viewModel::deleteProgress,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalDetailContent(
    goalDetailData: GoalDetailData,
    onUpdateProgress: (Long, Double, String?) -> Unit,
    onDeleteProgress: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 目標概要カード
        item {
            GoalOverviewCard(goal = goalDetailData.goal)
        }
        
        // 進捗統計カード
        item {
            ProgressStatsCard(stats = goalDetailData.progressStats)
        }
        
        // 進捗状況カード
        item {
            ProgressStatusCard(goal = goalDetailData.goal)
        }
        
        // 最近の進捗履歴
        item {
            Text(
                text = "最近の進捗",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        if (goalDetailData.recentProgress.isEmpty()) {
            item {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "進捗記録がありません",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "「+」ボタンから進捗を記録しましょう",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(goalDetailData.recentProgress) { progressRecord ->
                ProgressRecordCard(
                    progressRecord = progressRecord,
                    goal = goalDetailData.goal,
                    onDelete = { onDeleteProgress(progressRecord.id) }
                )
            }
        }
    }
}

@Composable
private fun GoalOverviewCard(goal: Goal) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = goal.type.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (goal.isCompleted) {
                    AssistChip(
                        onClick = { },
                        label = { Text("完了") },
                        leadingIcon = {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                        }
                    )
                }
            }
            
            if (!goal.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = goal.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 期限表示
            goal.deadline?.let { deadline ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "期限: $deadline",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressStatsCard(stats: ProgressStats) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "進捗統計",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "記録数",
                    value = "${stats.totalRecords}回"
                )
                StatItem(
                    label = "平均",
                    value = String.format("%.1f", stats.averageProgress)
                )
                StatItem(
                    label = "最高記録",
                    value = String.format("%.1f", stats.bestProgress)
                )
                StatItem(
                    label = "改善率",
                    value = "${String.format("%.1f", stats.improvementRate)}%"
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProgressStatusCard(goal: Goal) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "進捗状況",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${String.format("%.1f", goal.currentValue)} / ${String.format("%.1f", goal.targetValue)} ${goal.unit}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${String.format("%.1f", goal.progressPercentage * 100)}%",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = goal.progressPercentage,
                modifier = Modifier.fillMaxWidth()
            )
            
            if (goal.remainingValue > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "残り ${String.format("%.1f", goal.remainingValue)} ${goal.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProgressRecordCard(
    progressRecord: GoalProgressRecord,
    goal: Goal,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${String.format("%.1f", progressRecord.progressValue)} ${goal.unit}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = progressRecord.recordDate.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(
                    onClick = { showDeleteDialog = true }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "削除")
                }
            }
            
            if (!progressRecord.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = progressRecord.notes,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("進捗記録を削除") },
            text = { Text("この進捗記録を削除しますか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("削除")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("キャンセル")
                }
            }
        )
    }
}