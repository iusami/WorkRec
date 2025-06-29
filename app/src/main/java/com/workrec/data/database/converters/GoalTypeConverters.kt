package com.workrec.data.database.converters

import androidx.room.TypeConverter
import com.workrec.domain.entities.GoalType

/**
 * GoalTypeのRoomタイプコンバーター
 */
class GoalTypeConverters {
    
    @TypeConverter
    fun fromGoalType(goalType: GoalType): String {
        return goalType.name
    }
    
    @TypeConverter
    fun toGoalType(goalTypeName: String): GoalType {
        return GoalType.valueOf(goalTypeName)
    }
}