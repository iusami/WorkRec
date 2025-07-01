package com.workrec.presentation.ui.screens.goal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.workrec.domain.entities.GoalType
import com.workrec.presentation.viewmodel.AddGoalViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/**
 * 目標追加画面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(
    onNavigateBack: () -> Unit,
    onGoalAdded: () -> Unit,
    viewModel: AddGoalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新しい目標") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "戻る")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.addGoal()
                        },
                        enabled = uiState.isFormValid && !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 目標タイプ選択
            GoalTypeSelectionCard(
                selectedType = uiState.selectedType,
                onTypeSelected = viewModel::updateType
            )
            
            // 基本情報入力
            BasicInfoCard(
                title = uiState.title,
                description = uiState.description,
                onTitleChange = viewModel::updateTitle,
                onDescriptionChange = viewModel::updateDescription,
                isError = uiState.titleError != null
            )
            
            // 目標値・単位入力
            TargetValueCard(
                targetValue = uiState.targetValue,
                unit = uiState.unit,
                goalType = uiState.selectedType,
                onTargetValueChange = viewModel::updateTargetValue,
                onUnitChange = viewModel::updateUnit,
                isTargetValueError = uiState.targetValueError != null,
                isUnitError = uiState.unitError != null
            )
            
            // 期限設定
            DeadlineCard(
                deadline = uiState.deadline,
                onDeadlineChange = viewModel::updateDeadline
            )
            
            // エラーメッセージ表示
            uiState.errorMessage?.let { errorMessage ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // 成功時の処理
            LaunchedEffect(uiState.isSuccess) {
                if (uiState.isSuccess) {
                    onGoalAdded()
                }
            }
        }
    }
}

@Composable
private fun GoalTypeSelectionCard(
    selectedType: GoalType?,
    onTypeSelected: (GoalType) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "目標タイプ",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(GoalType.values()) { type ->
                    FilterChip(
                        onClick = { onTypeSelected(type) },
                        label = { Text(type.displayName) },
                        selected = selectedType == type
                    )
                }
            }
        }
    }
}

@Composable
private fun BasicInfoCard(
    title: String,
    description: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    isError: Boolean
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "基本情報",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("目標名") },
                modifier = Modifier.fillMaxWidth(),
                isError = isError,
                supportingText = if (isError) {
                    { Text("目標名は必須です") }
                } else null
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("説明（任意）") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )
        }
    }
}

@Composable
private fun TargetValueCard(
    targetValue: String,
    unit: String,
    goalType: GoalType?,
    onTargetValueChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    isTargetValueError: Boolean,
    isUnitError: Boolean
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "目標設定",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = targetValue,
                    onValueChange = onTargetValueChange,
                    label = { Text("目標値") },
                    modifier = Modifier.weight(2f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isTargetValueError,
                    supportingText = if (isTargetValueError) {
                        { Text("正の数値を入力してください") }
                    } else null
                )
                
                OutlinedTextField(
                    value = unit.ifEmpty { goalType?.defaultUnit ?: "" },
                    onValueChange = onUnitChange,
                    label = { Text("単位") },
                    modifier = Modifier.weight(1f),
                    isError = isUnitError,
                    supportingText = if (isUnitError) {
                        { Text("単位は必須です") }
                    } else null
                )
            }
            
            if (goalType != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "推奨単位: ${goalType.defaultUnit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DeadlineCard(
    deadline: LocalDate?,
    onDeadlineChange: (LocalDate?) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "期限設定",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Switch(
                    checked = deadline != null,
                    onCheckedChange = { hasDeadline ->
                        if (hasDeadline) {
                            showDatePicker = true
                        } else {
                            onDeadlineChange(null)
                        }
                    }
                )
            }
            
            if (deadline != null) {
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(deadline.toString())
                }
            }
        }
    }
    
    // 日付選択ダイアログ（将来的に実装）
    if (showDatePicker) {
        // TODO: DatePicker の実装
        // 現在は今日から1週間後をデフォルトとして設定
        LaunchedEffect(Unit) {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val defaultDeadline = today.plus(DatePeriod(days = 7))
            onDeadlineChange(defaultDeadline)
            showDatePicker = false
        }
    }
}