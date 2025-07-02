package com.workrec.data.database.converters

import androidx.room.TypeConverter
import com.workrec.domain.entities.ExerciseEquipment

/**
 * ExerciseEquipmentのRoomタイプコンバーター
 */
class ExerciseEquipmentConverters {
    
    @TypeConverter
    fun fromExerciseEquipment(equipment: ExerciseEquipment): String {
        return equipment.name
    }
    
    @TypeConverter
    fun toExerciseEquipment(equipmentName: String): ExerciseEquipment {
        return ExerciseEquipment.valueOf(equipmentName)
    }
}