package com.workrec.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.workrec.data.database.converters.ExerciseCategoryConverters
import com.workrec.domain.entities.Exercise
import com.workrec.domain.entities.ExerciseCategory

/**
 * Roomデータベース用のエクササイズエンティティ
 */
@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["workoutId"])]
)
@TypeConverters(ExerciseCategoryConverters::class)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutId: Long,
    val name: String,
    val category: ExerciseCategory,
    val notes: String? = null
)

/**
 * ExerciseEntityからドメインエンティティへの変換
 */
fun ExerciseEntity.toDomainModel(sets: List<com.workrec.domain.entities.ExerciseSet>): Exercise {
    return Exercise(
        id = id,
        name = name,
        sets = sets,
        category = category,
        notes = notes
    )
}

/**
 * ドメインエンティティからExerciseEntityへの変換
 */
fun Exercise.toEntity(workoutId: Long): ExerciseEntity {
    return ExerciseEntity(
        id = id,
        workoutId = workoutId,
        name = name,
        category = category,
        notes = notes
    )
}