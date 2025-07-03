package com.workrec.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.workrec.data.database.converters.DateConverters
import com.workrec.data.database.converters.GoalTypeConverters
import com.workrec.domain.entities.Goal
import com.workrec.domain.entities.GoalType
import kotlinx.datetime.LocalDate

/**
 * Roomデータベース用の目標エンティティ
 */
@Entity(tableName = "goals")
@TypeConverters(DateConverters::class, GoalTypeConverters::class)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: GoalType,
    val title: String,
    val description: String? = null,
    val targetValue: Double,
    val currentValue: Double = 0.0,
    val unit: String,
    val deadline: LocalDate? = null,
    val isCompleted: Boolean = false,
    val createdAt: LocalDate,
    val updatedAt: LocalDate
)

/**
 * GoalEntityからドメインエンティティへの変換
 */
fun GoalEntity.toDomainModel(): Goal {
    return Goal(
        id = id,
        type = type,
        title = title,
        description = description,
        targetValue = targetValue,
        currentValue = currentValue,
        unit = unit,
        deadline = deadline,
        isCompleted = isCompleted,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * ドメインエンティティからGoalEntityへの変換
 */
fun Goal.toEntity(): GoalEntity {
    return GoalEntity(
        id = id,
        type = type,
        title = title,
        description = description,
        targetValue = targetValue,
        currentValue = currentValue,
        unit = unit,
        deadline = deadline,
        isCompleted = isCompleted,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}