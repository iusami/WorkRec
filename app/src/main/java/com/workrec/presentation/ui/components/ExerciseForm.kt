package com.workrec.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.workrec.domain.entities.*

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

            // エクササイズ名入力とテンプレート選択
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = exercise.name,
                    onValueChange = { newName ->
                        onExerciseUpdate(exercise.copy(name = newName))
                    },
                    label = { Text("エクササイズ名") },
                    placeholder = { Text("例: ベンチプレス") },
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = onNavigateToExerciseManager
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "エクササイズテンプレートから選択",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // カテゴリー選択
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = exercise.category.displayName,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("カテゴリー") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "カテゴリーを選択"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    ExerciseCategory.values().forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.displayName) },
                            onClick = {
                                onExerciseUpdate(exercise.copy(category = category))
                                expanded = false
                            }
                        )
                    }
                }
            }

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