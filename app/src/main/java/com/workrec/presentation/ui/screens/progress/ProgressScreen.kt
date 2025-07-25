package com.workrec.presentation.ui.screens.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workrec.presentation.viewmodel.ProgressUiState
import com.workrec.R
import com.workrec.domain.entities.TimePeriod
import com.workrec.presentation.ui.components.*
import com.workrec.presentation.ui.components.GoalProgressData
import com.workrec.presentation.viewmodel.ProgressViewModel
import com.workrec.presentation.ui.utils.rememberScrollAwareAnimationState

/**
 * 進捗画面 - 包括的なフィットネス進捗ダッシュボード
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.progress)) },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshData() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "更新"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    // 初期読み込み中
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "進捗データを読み込み中...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }

                uiState.errorMessage != null -> {
                    // エラー状態
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "エラーが発生しました",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(
                                onClick = { viewModel.refreshData() }
                            ) {
                                Text("再試行")
                            }
                        }
                    }
                }

                !uiState.hasData -> {
                    // データなし状態
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "まだワークアウトデータがありません",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "ワークアウトを記録して進捗を確認しましょう",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    // データ表示
                    ProgressContent(
                        uiState = uiState,
                        onTimePeriodSelected = viewModel::selectTimePeriod,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * 進捗コンテンツの表示
 */
@Composable
private fun ProgressContent(
    uiState: ProgressUiState,
    onTimePeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    // スクロール状態を監視
    val listState = rememberLazyListState()
    val scrollAwareState = rememberScrollAwareAnimationState(listState)
    
    LazyColumn(
        state = listState,
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 時間期間セレクター
        item {
            TimePeriodSelector(
                selectedPeriod = uiState.selectedTimePeriod,
                onPeriodSelected = onTimePeriodSelected
            )
        }

        // 統計サマリー
        uiState.workoutStatistics?.let { statistics ->
            item {
                ProgressSummary(statistics = statistics)
            }
        }

        // 高度な目標進捗セクション
        uiState.progressData?.goalProgress?.let { goalProgress ->
            if (goalProgress.isNotEmpty()) {
                item {
                    Text(
                        text = "目標進捗",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                // 高度な目標カードリスト
                items(goalProgress.take(3)) { progress ->
                    AdvancedGoalCard(
                        goalProgress = progress,
                        onClick = { /* 目標詳細画面へ */ },
                        onUpdateProgress = { /* 進捗更新機能 */ }
                    )
                }
                
                // 最初の目標の予測チャート
                if (goalProgress.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        GoalPredictionChart(
                            goalProgress = goalProgress.first(),
                            showPrediction = true
                        )
                    }
                }
                
                // 目標カテゴリ別ダッシュボード
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    GoalCategoryDashboard(
                        goalProgressList = goalProgress,
                        onCategoryClick = { goalType ->
                            // カテゴリ別目標画面へのナビゲーション
                        }
                    )
                }
                
                // 進捗波形アニメーション（最初の目標）
                if (goalProgress.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        AnimatedProgressWave(
                            goalProgress = goalProgress.first(),
                            shouldPlayAnimations = scrollAwareState.shouldAnimate()
                        )
                    }
                }
                
                // マイルストーンタイムライン（最初の目標）
                if (goalProgress.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        GoalMilestonesTimeline(
                            goalProgress = goalProgress.first(),
                            shouldPlayAnimations = scrollAwareState.shouldAnimate()
                        )
                    }
                }
            }
        }

        // ワークアウトボリューム推移
        uiState.workoutStatistics?.volumeTrend?.let { volumeTrend ->
            if (volumeTrend.isNotEmpty()) {
                item {
                    WorkoutVolumeChart(
                        volumeData = volumeTrend,
                        title = "ボリューム推移 (${uiState.selectedTimePeriod.displayName})"
                    )
                }
            }
        }

        // 個人記録
        uiState.progressData?.personalRecords?.let { records ->
            if (records.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "個人記録",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }
                }
                
                items(records.take(5)) { record ->
                    ProgressCard(
                        title = record.exerciseName,
                        value = "${record.value.toInt()} ${record.unit}",
                        subtitle = "${record.recordType.name} - ${record.achievedDate}"
                    )
                }
            }
        }

        // 週間トレンド
        uiState.progressData?.weeklyTrend?.let { weeklyTrend ->
            if (weeklyTrend.isNotEmpty()) {
                item {
                    val weeklyData = weeklyTrend.takeLast(7).map { data ->
                        val dayOfWeek = when (data.weekStart.dayOfWeek.ordinal) {
                            0 -> "月"
                            1 -> "火"
                            2 -> "水"
                            3 -> "木"
                            4 -> "金"
                            5 -> "土"
                            6 -> "日"
                            else -> "?"
                        }
                        dayOfWeek to data.totalVolume
                    }
                    
                    WeeklyVolumeChart(weeklyData = weeklyData)
                }
            }
        }
    }
}