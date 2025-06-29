package com.workrec.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.workrec.data.database.converters.DurationConverters
import com.workrec.domain.entities.ExerciseSet
import kotlin.time.Duration

/**
 * Roomデータベース用のエクササイズセットエンティティ
 */
@Entity(
    tableName = "exercise_sets",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["exerciseId"])]
)
@TypeConverters(DurationConverters::class)
data class ExerciseSetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseId: Long,
    val reps: Int,
    val weight: Double,
    val restTime: Duration? = null,
    val setOrder: Int // セットの順序を保持
)

/**
 * ExerciseSetEntityからドメインエンティティへの変換
 */
fun ExerciseSetEntity.toDomainModel(): ExerciseSet {
    return ExerciseSet(
        reps = reps,
        weight = weight,
        restTime = restTime
    )
}

/**
 * ドメインエンティティからExerciseSetEntityへの変換
 */
fun ExerciseSet.toEntity(exerciseId: Long, setOrder: Int): ExerciseSetEntity {
    return ExerciseSetEntity(
        exerciseId = exerciseId,
        reps = reps,
        weight = weight,
        restTime = restTime,
        setOrder = setOrder
    )
}