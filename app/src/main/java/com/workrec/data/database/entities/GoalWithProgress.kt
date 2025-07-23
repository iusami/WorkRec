package com.workrec.data.database.entities

import androidx.room.Embedded
import androidx.room.Relation

/**
 * 目標と進捗記録の関連データを表すRoom関係エンティティ
 */
data class GoalWithProgress(
    @Embedded val goal: GoalEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "goalId"
    )
    val progressRecords: List<GoalProgressEntity>
)