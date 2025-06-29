package com.workrec.data.database.converters

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate

/**
 * kotlinx.datetime.LocalDateのRoomタイプコンバーター
 */
class DateConverters {
    
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }
    
    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }
}