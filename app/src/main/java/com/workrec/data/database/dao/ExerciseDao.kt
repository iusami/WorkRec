package com.workrec.data.database.dao

import androidx.room.*
import com.workrec.data.database.entities.ExerciseEntity
import com.workrec.domain.entities.ExerciseCategory

/**
 * エクササイズデータアクセスオブジェクト
 */
@Dao
interface ExerciseDao {
    
    /**
     * 指定したワークアウトIDのエクササイズを取得
     */
    @Query("SELECT * FROM exercises WHERE workoutId = :workoutId ORDER BY id")
    suspend fun getExercisesByWorkoutId(workoutId: Long): List<ExerciseEntity>
    
    /**
     * 指定したIDのエクササイズを取得
     */
    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: Long): ExerciseEntity?
    
    /**
     * エクササイズ名で検索
     */
    @Query("SELECT * FROM exercises WHERE name LIKE '%' || :exerciseName || '%'")
    suspend fun searchExercisesByName(exerciseName: String): List<ExerciseEntity>
    
    /**
     * カテゴリー別のエクササイズを取得
     */
    @Query("SELECT * FROM exercises WHERE category = :category")
    suspend fun getExercisesByCategory(category: ExerciseCategory): List<ExerciseEntity>
    
    /**
     * ユニークなエクササイズ名を取得
     */
    @Query("SELECT DISTINCT name FROM exercises ORDER BY name")
    suspend fun getUniqueExerciseNames(): List<String>
    
    /**
     * エクササイズを挿入
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: ExerciseEntity): Long
    
    /**
     * 複数のエクササイズを挿入
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<ExerciseEntity>): List<Long>
    
    /**
     * エクササイズを更新
     */
    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)
    
    /**
     * エクササイズを削除
     */
    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)
    
    /**
     * 指定したワークアウトIDのエクササイズをすべて削除
     */
    @Query("DELETE FROM exercises WHERE workoutId = :workoutId")
    suspend fun deleteExercisesByWorkoutId(workoutId: Long)
}