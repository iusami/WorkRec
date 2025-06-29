package com.workrec.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.workrec.data.database.converters.DateConverters
import com.workrec.data.database.converters.DurationConverters
import com.workrec.domain.entities.Workout
import kotlinx.datetime.LocalDate
import kotlin.time.Duration

/**
 * Roomデータベース用のワークアウトエンティティ
 */
@Entity(tableName = "workouts")
@TypeConverters(DateConverters::class, DurationConverters::class)
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val totalDuration: Duration? = null,
    val notes: String? = null
)

/**
 * WorkoutEntityからドメインエンティティへの変換
 */
fun WorkoutEntity.toDomainModel(exercises: List<com.workrec.domain.entities.Exercise>): Workout {
    return Workout(
        id = id,
        date = date,
        exercises = exercises,
        totalDuration = totalDuration,
        notes = notes
    )
}

/**
 * ドメインエンティティからWorkoutEntityへの変換
 */
fun Workout.toEntity(): WorkoutEntity {
    return WorkoutEntity(
        id = id,
        date = date,
        totalDuration = totalDuration,
        notes = notes
    )
}