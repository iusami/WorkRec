package com.workrec.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.workrec.domain.entities.*

/**
 * エクササイズテンプレート編集ダイアログ
 * Material Design 3準拠の包括的な編集機能を提供
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExerciseDialog(
    exerciseTemplate: ExerciseTemplate,
    onExerciseSaved: (ExerciseTemplate) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 編集状態管理
    var name by remember { mutableStateOf(exerciseTemplate.name) }
    var selectedCategory by remember { mutableStateOf(exerciseTemplate.category) }
    var selectedEquipment by remember { mutableStateOf(exerciseTemplate.equipment) }
    var selectedDifficulty by remember { mutableStateOf(exerciseTemplate.difficulty) }
    var muscle by remember { mutableStateOf(exerciseTemplate.muscle) }
    var description by remember { mutableStateOf(exerciseTemplate.description ?: "") }
    
    // バリデーション状態
    var nameError by remember { mutableStateOf("") }
    var muscleError by remember { mutableStateOf("") }
    var descriptionError by remember { mutableStateOf("") }
    var hasChanges by remember { mutableStateOf(false) }
    var isFormValid by remember { mutableStateOf(true) }
    
    // 変更検知とリアルタイムバリデーション
    LaunchedEffect(name, selectedCategory, selectedEquipment, selectedDifficulty, muscle, description) {
        hasChanges = name != exerciseTemplate.name ||
                selectedCategory != exerciseTemplate.category ||
                selectedEquipment != exerciseTemplate.equipment ||
                selectedDifficulty != exerciseTemplate.difficulty ||
                muscle != exerciseTemplate.muscle ||
                description != (exerciseTemplate.description ?: "")
    }
    
    // バリデーション関数の定義
    fun validateExerciseName(value: String): String {
        return when {
            value.isBlank() && hasChanges -> "エクササイズ名は必須です"
            value.isNotBlank() && value.length < 2 -> "エクササイズ名は2文字以上で入力してください"
            value.length > 50 -> "エクササイズ名は50文字以下で入力してください"
            else -> ""
        }
    }
    
    fun validateMuscle(value: String): String {
        return when {
            value.isBlank() && hasChanges -> "対象筋肉は必須です"
            value.isNotBlank() && value.length < 2 -> "対象筋肉は2文字以上で入力してください"
            value.length > 30 -> "対象筋肉は30文字以下で入力してください"
            else -> ""
        }
    }
    
    fun validateDescription(value: String): String {
        return when {
            value.length > 500 -> "説明は500文字以下で入力してください"
            else -> ""
        }
    }
    
    // リアルタイムバリデーション（入力中）
    LaunchedEffect(name) {
        nameError = validateExerciseName(name)
    }
    
    LaunchedEffect(muscle) {
        muscleError = validateMuscle(muscle)
    }
    
    LaunchedEffect(description) {
        descriptionError = validateDescription(description)
    }
    
    LaunchedEffect(nameError, muscleError, descriptionError) {
        isFormValid = nameError.isEmpty() && muscleError.isEmpty() && descriptionError.isEmpty()
    }
    
    
    // 最終バリデーション関数（保存時用）
    fun validateInputs(): Boolean {
        return name.isNotBlank() && 
               name.length >= 2 && 
               name.length <= 50 &&
               muscle.isNotBlank() && 
               muscle.length >= 2 && 
               muscle.length <= 30 &&
               description.length <= 500
    }
    
    // 保存処理
    fun saveExercise() {
        if (validateInputs()) {
            val updatedExercise = exerciseTemplate.copy(
                name = name.trim(),
                category = selectedCategory,
                equipment = selectedEquipment,
                difficulty = selectedDifficulty,
                muscle = muscle.trim(),
                description = description.trim().ifBlank { null }
            )
            onExerciseSaved(updatedExercise)
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .semantics {
                    contentDescription = "エクササイズ編集ダイアログ"
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // ヘッダー
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "エクササイズを編集",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (hasChanges) {
                                Text(
                                    text = "未保存の変更があります",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.semantics {
                                contentDescription = "編集をキャンセル"
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "閉じる",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                
                // メインコンテンツ
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // エクササイズ名入力
                    ExerciseNameField(
                        value = name,
                        onValueChange = { name = it },
                        error = nameError,
                        isRequired = true
                    )
                    
                    // カテゴリー選択
                    ExerciseCategoryDropdown(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it }
                    )
                    
                    // 対象筋肉入力
                    ExerciseMuscleField(
                        value = muscle,
                        onValueChange = { muscle = it },
                        error = muscleError,
                        isRequired = true
                    )
                    
                    // 器具選択
                    ExerciseEquipmentDropdown(
                        selectedEquipment = selectedEquipment,
                        onEquipmentSelected = { selectedEquipment = it }
                    )
                    
                    // 難易度選択
                    ExerciseDifficultyDropdown(
                        selectedDifficulty = selectedDifficulty,
                        onDifficultySelected = { selectedDifficulty = it }
                    )
                    
                    // 説明入力（任意）
                    ExerciseDescriptionField(
                        value = description,
                        onValueChange = { description = it },
                        error = descriptionError
                    )
                    
                    // プレビューセクション
                    ExercisePreviewCard(
                        name = name,
                        category = selectedCategory,
                        muscle = muscle,
                        equipment = selectedEquipment,
                        difficulty = selectedDifficulty,
                        description = description
                    )
                }
                
                // フッター（アクションボタン）
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.semantics {
                                contentDescription = "変更をキャンセルして閉じる"
                            }
                        ) {
                            Text("キャンセル")
                        }
                        
                        Button(
                            onClick = { saveExercise() },
                            enabled = hasChanges && isFormValid && name.isNotBlank() && muscle.isNotBlank(),
                            modifier = Modifier.semantics {
                                contentDescription = when {
                                    !hasChanges -> "変更がないため保存できません"
                                    !isFormValid -> "入力エラーがあるため保存できません"
                                    else -> "変更を保存"
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("保存")
                        }
                    }
                }
            }
        }
    }
}

/**
 * エクササイズ名入力フィールド
 */
@Composable
private fun ExerciseNameField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String,
    isRequired: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("エクササイズ名")
                    if (isRequired) {
                        Text(
                            text = " *",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            placeholder = { Text("例: ベンチプレス") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null
                )
            },
            isError = error.isNotEmpty(),
            supportingText = if (error.isNotEmpty()) {
                { Text(text = error, color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "エクササイズ名入力フィールド。必須項目です。"
                },
            singleLine = true
        )
    }
}

/**
 * 対象筋肉入力フィールド
 */
@Composable
private fun ExerciseMuscleField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String,
    isRequired: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("対象筋肉")
                    if (isRequired) {
                        Text(
                            text = " *",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            placeholder = { Text("例: 大胸筋、三角筋") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Accessibility,
                    contentDescription = null
                )
            },
            isError = error.isNotEmpty(),
            supportingText = if (error.isNotEmpty()) {
                { Text(text = error, color = MaterialTheme.colorScheme.error) }
            } else null,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "対象筋肉入力フィールド。必須項目です。"
                },
            singleLine = true
        )
    }
}

/**
 * 説明入力フィールド
 */
@Composable
private fun ExerciseDescriptionField(
    value: String,
    onValueChange: (String) -> Unit,
    error: String = "",
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("説明（任意）") },
            placeholder = { Text("エクササイズの詳細な説明や注意点を入力...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null
                )
            },
            isError = error.isNotEmpty(),
            supportingText = if (error.isNotEmpty()) {
                { 
                    Text(
                        text = error, 
                        color = MaterialTheme.colorScheme.error
                    ) 
                }
            } else {
                { 
                    Text(
                        text = "${value.length}/500文字", 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "エクササイズ説明入力フィールド。任意項目です。"
                },
            maxLines = 4,
            minLines = 2
        )
    }
}

/**
 * エクササイズプレビューカード
 */
@Composable
private fun ExercisePreviewCard(
    name: String,
    category: ExerciseCategory,
    muscle: String,
    equipment: ExerciseEquipment,
    difficulty: ExerciseDifficulty,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Preview,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "プレビュー",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            if (name.isNotBlank()) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PreviewInfoChip(
                        icon = Icons.Default.Category,
                        text = category.displayName
                    )
                    PreviewInfoChip(
                        icon = Icons.Default.FitnessCenter,
                        text = equipment.displayName
                    )
                    PreviewInfoChip(
                        icon = Icons.Default.Star,
                        text = difficulty.displayName
                    )
                }
                
                if (muscle.isNotBlank()) {
                    Text(
                        text = "対象筋肉: $muscle",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (description.isNotBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "エクササイズ名を入力するとプレビューが表示されます",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * プレビュー情報チップ
 */
@Composable
private fun PreviewInfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * カテゴリー選択ドロップダウン
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseCategoryDropdown(
    selectedCategory: ExerciseCategory,
    onCategorySelected: (ExerciseCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCategory.displayName,
                onValueChange = { },
                readOnly = true,
                label = { Text("カテゴリー") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "カテゴリー選択ドロップダウン。現在の選択: ${selectedCategory.displayName}"
                    }
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                ExerciseCategory.values().forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.displayName) },
                        onClick = {
                            onCategorySelected(category)
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (category) {
                                    ExerciseCategory.CHEST -> Icons.Default.FitnessCenter
                                    ExerciseCategory.BACK -> Icons.Default.ViewInAr
                                    ExerciseCategory.LEGS -> Icons.Default.DirectionsRun
                                    ExerciseCategory.SHOULDERS -> Icons.Default.Accessibility
                                    ExerciseCategory.ARMS -> Icons.Default.SelfImprovement
                                    ExerciseCategory.CORE -> Icons.Default.FitnessCenter
                                    ExerciseCategory.CARDIO -> Icons.Default.DirectionsRun
                                    ExerciseCategory.OTHER -> Icons.Default.MoreHoriz
                                },
                                contentDescription = null,
                                tint = if (category == selectedCategory) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "${category.displayName}を選択"
                        }
                    )
                }
            }
        }
    }
}

/**
 * 器具選択ドロップダウン
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseEquipmentDropdown(
    selectedEquipment: ExerciseEquipment,
    onEquipmentSelected: (ExerciseEquipment) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedEquipment.displayName,
                onValueChange = { },
                readOnly = true,
                label = { Text("器具") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "器具選択ドロップダウン。現在の選択: ${selectedEquipment.displayName}"
                    }
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                ExerciseEquipment.values().forEach { equipment ->
                    DropdownMenuItem(
                        text = { Text(equipment.displayName) },
                        onClick = {
                            onEquipmentSelected(equipment)
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (equipment) {
                                    ExerciseEquipment.BARBELL -> Icons.Default.FitnessCenter
                                    ExerciseEquipment.DUMBBELL -> Icons.Default.SportsHandball
                                    ExerciseEquipment.MACHINE -> Icons.Default.Settings
                                    ExerciseEquipment.BODYWEIGHT -> Icons.Default.SelfImprovement
                                    ExerciseEquipment.CABLE -> Icons.Default.Cable
                                    ExerciseEquipment.RESISTANCE_BAND -> Icons.Default.LinearScale
                                    ExerciseEquipment.KETTLEBELL -> Icons.Default.FitnessCenter
                                    ExerciseEquipment.MEDICINE_BALL -> Icons.Default.SportsBaseball
                                    ExerciseEquipment.SUSPENSION -> Icons.Default.Link
                                    ExerciseEquipment.OTHER -> Icons.Default.MoreHoriz
                                },
                                contentDescription = null,
                                tint = if (equipment == selectedEquipment) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "${equipment.displayName}を選択"
                        }
                    )
                }
            }
        }
    }
}

/**
 * 難易度選択ドロップダウン
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseDifficultyDropdown(
    selectedDifficulty: ExerciseDifficulty,
    onDifficultySelected: (ExerciseDifficulty) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedDifficulty.displayName,
                onValueChange = { },
                readOnly = true,
                label = { Text("難易度") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "難易度選択ドロップダウン。現在の選択: ${selectedDifficulty.displayName}"
                    }
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                ExerciseDifficulty.values().forEach { difficulty ->
                    DropdownMenuItem(
                        text = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(difficulty.displayName)
                                // 難易度を星で表現
                                Row {
                                    repeat(
                                        when (difficulty) {
                                            ExerciseDifficulty.BEGINNER -> 1
                                            ExerciseDifficulty.INTERMEDIATE -> 2
                                            ExerciseDifficulty.ADVANCED -> 3
                                            ExerciseDifficulty.EXPERT -> 4
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        },
                        onClick = {
                            onDifficultySelected(difficulty)
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (difficulty) {
                                    ExerciseDifficulty.BEGINNER -> Icons.Default.School
                                    ExerciseDifficulty.INTERMEDIATE -> Icons.Default.TrendingUp
                                    ExerciseDifficulty.ADVANCED -> Icons.Default.EmojiEvents
                                    ExerciseDifficulty.EXPERT -> Icons.Default.Star
                                },
                                contentDescription = null,
                                tint = if (difficulty == selectedDifficulty) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "${difficulty.displayName}を選択"
                        }
                    )
                }
            }
        }
    }
}