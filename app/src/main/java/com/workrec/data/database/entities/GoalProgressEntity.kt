package com.workrec.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.workrec.data.database.converters.DateConverters
import com.workrec.domain.entities.GoalProgressRecord
import kotlinx.datetime.LocalDate

/**
 * Roomデータベース用の目標進捗エンティティ
 */
@Entity(
    tableName = "goal_progress",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(DateConverters::class)
data class GoalProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalId: Long,
    val recordDate: LocalDate,
    val progressValue: Double,
    val notes: String? = null,
    val createdAt: LocalDate
)

/**
 * GoalProgressEntityからドメインエンティティへの変換
 */
fun GoalProgressEntity.toDomainModel(): GoalProgressRecord {
    return GoalProgressRecord(
        id = id,
        goalId = goalId,
        recordDate = recordDate,
        progressValue = progressValue,
        notes = notes,
        createdAt = createdAt
    )
}

/**
 * ドメインエンティティからGoalProgressEntityへの変換
 */
fun GoalProgressRecord.toEntity(): GoalProgressEntity {
    return GoalProgressEntity(
        id = id,
        goalId = goalId,
        recordDate = recordDate,
        progressValue = progressValue,
        notes = notes,
        createdAt = createdAt
    )
}