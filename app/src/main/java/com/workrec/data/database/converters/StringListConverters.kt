package com.workrec.data.database.converters

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * List<String>のRoomタイプコンバーター
 */
class StringListConverters {
    
    @TypeConverter
    fun fromStringList(stringList: List<String>): String {
        return Json.encodeToString(stringList)
    }
    
    @TypeConverter
    fun toStringList(stringListString: String): List<String> {
        return if (stringListString.isBlank()) {
            emptyList()
        } else {
            Json.decodeFromString(stringListString)
        }
    }
}