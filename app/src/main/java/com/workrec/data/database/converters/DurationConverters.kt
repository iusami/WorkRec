package com.workrec.data.database.converters

import androidx.room.TypeConverter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * kotlin.time.DurationのRoomタイプコンバーター
 */
class DurationConverters {
    
    @TypeConverter
    fun fromDuration(duration: Duration?): Long? {
        return duration?.inWholeMilliseconds
    }
    
    @TypeConverter
    fun toDuration(milliseconds: Long?): Duration? {
        return milliseconds?.milliseconds
    }
}