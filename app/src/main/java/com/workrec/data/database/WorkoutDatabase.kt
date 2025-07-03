package com.workrec.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.workrec.data.database.converters.*
import com.workrec.data.database.dao.*
import com.workrec.data.database.entities.*

/**
 * WorkRecアプリのメインデータベース
 * Room Database設定
 */
@Database(
    entities = [
        WorkoutEntity::class,
        ExerciseEntity::class,
        ExerciseSetEntity::class,
        GoalEntity::class,
        GoalProgressEntity::class,
        ExerciseTemplateEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(
    DateConverters::class,
    DurationConverters::class,
    ExerciseCategoryConverters::class,
    StringListConverters::class,
    GoalTypeConverters::class
)
abstract class WorkoutDatabase : RoomDatabase() {
    
    // DAOのアクセサー
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun exerciseSetDao(): ExerciseSetDao
    abstract fun goalDao(): GoalDao
    abstract fun goalProgressDao(): GoalProgressDao
    abstract fun exerciseTemplateDao(): ExerciseTemplateDao
    
    companion object {
        const val DATABASE_NAME = "workout_database"
    }
}