package com.workrec.data.database.converters

import androidx.room.TypeConverter
import com.workrec.domain.entities.ExerciseCategory

/**
 * ExerciseCategoryのRoomタイプコンバーター
 */
class ExerciseCategoryConverters {
    
    @TypeConverter
    fun fromExerciseCategory(category: ExerciseCategory): String {
        return category.name
    }
    
    @TypeConverter
    fun toExerciseCategory(categoryName: String): ExerciseCategory {
        return ExerciseCategory.valueOf(categoryName)
    }
}