package com.workrec.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.workrec.domain.entities.*
import kotlinx.coroutines.delay

/**
 * エクササイズフォームコンポーネント
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseForm(
    exercise: Exercise,
    onExerciseUpdate: (Exercise) -> Unit,
    onExerciseDelete: () -> Unit,
    onNavigateToExerciseManager: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showExercisePicker by remember { mutableStateOf(false) }
    var exerciseJustSelected by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }
    
    // エクササイズ選択成功アニメーション
    val successScale by animateFloatAsState(
        targetValue = if (showSuccessAnimation) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    // エクササイズ名フィールドの色アニメーション
    val fieldColor by animateColorAsState(
        targetValue = if (exerciseJustSelected) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 800)
    )
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ヘッダー行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (exercise.name.isBlank()) "新しいエクササイズ" else exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onExerciseDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "削除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // エクササイズ名表示と選択ボタン（アニメーション対応）
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(successScale),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = exercise.name,
                    onValueChange = { /* 読み取り専用 */ },
                    label = { Text("エクササイズ名") },
                    placeholder = { Text("エクササイズを選択してください") },
                    modifier = Modifier.weight(1f),
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledContainerColor = fieldColor
                    ),
                    trailingIcon = {
                        // エクササイズ選択済みの場合にチェックアイコン表示
                        AnimatedVisibility(
                            visible = exercise.name.isNotBlank(),
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "選択済み",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
                
                Button(
                    onClick = { showExercisePicker = true },
                    modifier = Modifier.semantics {
                        contentDescription = if (exercise.name.isBlank()) {
                            "エクササイズを選択してください"
                        } else {
                            "現在選択中: ${exercise.name}。別のエクササイズを選択"
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "エクササイズを選択"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("選択")
                }
            }

            // カテゴリー表示（読み取り専用）
            OutlinedTextField(
                value = exercise.category.displayName,
                onValueChange = { },
                readOnly = true,
                label = { Text("カテゴリー") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )

            // セット入力セクション
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "セット (${exercise.sets.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                IconButton(
                    onClick = {
                        val newSets = exercise.sets + ExerciseSet(reps = 0, weight = 0.0)
                        onExerciseUpdate(exercise.copy(sets = newSets))
                    },
                    modifier = Modifier.semantics {
                        contentDescription = "新しいセットを追加。現在${exercise.sets.size}セット"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "セットを追加"
                    )
                }
            }

            // セットリスト
            if (exercise.sets.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ヘッダー行
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "セット",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "重量(kg)",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "回数",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.weight(1f)
                        )
                        // 削除ボタン用のスペース
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    exercise.sets.forEachIndexed { index, set ->
                        // 各セット行にアニメーションを適用
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically(
                                initialOffsetY = { -it },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy
                                )
                            ) + fadeIn(),
                            exit = slideOutVertically(
                                targetOffsetY = { -it },
                                animationSpec = spring()
                            ) + fadeOut()
                        ) {
                            SetInputRow(
                                setNumber = index + 1,
                                exerciseSet = set,
                                onSetUpdate = { updatedSet ->
                                    val newSets = exercise.sets.toMutableList()
                                    newSets[index] = updatedSet
                                    onExerciseUpdate(exercise.copy(sets = newSets))
                                },
                                onSetDelete = {
                                    val newSets = exercise.sets.toMutableList()
                                    newSets.removeAt(index)
                                    onExerciseUpdate(exercise.copy(sets = newSets))
                                }
                            )
                        }
                    }
                }
            } else {
                // セットが空の場合
                Text(
                    text = "+ ボタンを押してセットを追加してください",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // エクササイズ選択ダイアログ
    if (showExercisePicker) {
        ExercisePickerDialog(
            onExerciseSelected = { selectedTemplate ->
                // エクササイズテンプレートからExerciseオブジェクトを作成
                val updatedExercise = exercise.copy(
                    name = selectedTemplate.name,
                    category = selectedTemplate.category
                )
                onExerciseUpdate(updatedExercise)
                
                // 成功アニメーションを開始
                exerciseJustSelected = true
                showSuccessAnimation = true
                showExercisePicker = false
            },
            onDismiss = { showExercisePicker = false }
        )
    }
    
    // アニメーション効果のリセット
    LaunchedEffect(exerciseJustSelected) {
        if (exerciseJustSelected) {
            delay(500) // 0.5秒後にスケールアニメーションを戻す
            showSuccessAnimation = false
            delay(800) // さらに0.8秒後に色アニメーションを戻す
            exerciseJustSelected = false
        }
    }
}

/**
 * セット入力行コンポーネント
 */
@Composable
private fun SetInputRow(
    setNumber: Int,
    exerciseSet: ExerciseSet,
    onSetUpdate: (ExerciseSet) -> Unit,
    onSetDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // セット番号
        Text(
            text = "$setNumber",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        // 重量入力
        OutlinedTextField(
            value = if (exerciseSet.weight == 0.0) "" else exerciseSet.weight.toString(),
            onValueChange = { value ->
                val weight = value.toDoubleOrNull() ?: 0.0
                onSetUpdate(exerciseSet.copy(weight = weight))
            },
            placeholder = { Text("0") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f),
            singleLine = true
        )

        // 回数入力
        OutlinedTextField(
            value = if (exerciseSet.reps == 0) "" else exerciseSet.reps.toString(),
            onValueChange = { value ->
                val reps = value.toIntOrNull() ?: 0
                onSetUpdate(exerciseSet.copy(reps = reps))
            },
            placeholder = { Text("0") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            singleLine = true
        )

        // 削除ボタン
        IconButton(onClick = onSetDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "セットを削除",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}