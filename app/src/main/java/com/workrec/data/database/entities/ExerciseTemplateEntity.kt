package com.workrec.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.workrec.data.database.converters.ExerciseCategoryConverters
import com.workrec.data.database.converters.ExerciseEquipmentConverters
import com.workrec.data.database.converters.ExerciseDifficultyConverters
import com.workrec.data.database.converters.StringListConverters
import com.workrec.domain.entities.ExerciseTemplate
import com.workrec.domain.entities.ExerciseCategory
import com.workrec.domain.entities.ExerciseEquipment
import com.workrec.domain.entities.ExerciseDifficulty

/**
 * Roomデータベース用のエクササイズテンプレートエンティティ
 */
@Entity(
    tableName = "exercise_templates",
    indices = [
        Index(value = ["name"]),
        Index(value = ["category"]),
        Index(value = ["equipment"]),
        Index(value = ["muscle"])
    ]
)
@TypeConverters(
    ExerciseCategoryConverters::class,
    ExerciseEquipmentConverters::class,
    ExerciseDifficultyConverters::class,
    StringListConverters::class
)
data class ExerciseTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: ExerciseCategory,
    val muscle: String,
    val equipment: ExerciseEquipment,
    val difficulty: ExerciseDifficulty,
    val description: String? = null,
    val instructions: List<String> = emptyList(),
    val tips: List<String> = emptyList(),
    val isUserCreated: Boolean = false
)

/**
 * ExerciseTemplateEntityからドメインエンティティへの変換
 */
fun ExerciseTemplateEntity.toDomainModel(): ExerciseTemplate {
    return ExerciseTemplate(
        id = id,
        name = name,
        category = category,
        muscle = muscle,
        equipment = equipment,
        difficulty = difficulty,
        description = description,
        instructions = instructions,
        tips = tips,
        isUserCreated = isUserCreated
    )
}

/**
 * ドメインエンティティからExerciseTemplateEntityへの変換
 */
fun ExerciseTemplate.toEntity(): ExerciseTemplateEntity {
    return ExerciseTemplateEntity(
        id = id,
        name = name,
        category = category,
        muscle = muscle,
        equipment = equipment,
        difficulty = difficulty,
        description = description,
        instructions = instructions,
        tips = tips,
        isUserCreated = isUserCreated
    )
}