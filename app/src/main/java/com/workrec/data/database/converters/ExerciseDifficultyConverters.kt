package com.workrec.data.database.converters

import androidx.room.TypeConverter
import com.workrec.domain.entities.ExerciseDifficulty

/**
 * ExerciseDifficultyのRoomタイプコンバーター
 */
class ExerciseDifficultyConverters {
    
    @TypeConverter
    fun fromExerciseDifficulty(difficulty: ExerciseDifficulty): String {
        return difficulty.name
    }
    
    @TypeConverter
    fun toExerciseDifficulty(difficultyName: String): ExerciseDifficulty {
        return ExerciseDifficulty.valueOf(difficultyName)
    }
}