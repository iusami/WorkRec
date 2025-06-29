package com.workrec.data.database.dao

import androidx.room.*
import com.workrec.data.database.entities.ExerciseSetEntity

/**
 * エクササイズセットデータアクセスオブジェクト
 */
@Dao
interface ExerciseSetDao {
    
    /**
     * 指定したエクササイズIDのセットを取得
     */
    @Query("SELECT * FROM exercise_sets WHERE exerciseId = :exerciseId ORDER BY setOrder")
    suspend fun getSetsByExerciseId(exerciseId: Long): List<ExerciseSetEntity>
    
    /**
     * 指定したIDのセットを取得
     */
    @Query("SELECT * FROM exercise_sets WHERE id = :id")
    suspend fun getSetById(id: Long): ExerciseSetEntity?
    
    /**
     * セットを挿入
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(exerciseSet: ExerciseSetEntity): Long
    
    /**
     * 複数のセットを挿入
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(exerciseSets: List<ExerciseSetEntity>): List<Long>
    
    /**
     * セットを更新
     */
    @Update
    suspend fun updateSet(exerciseSet: ExerciseSetEntity)
    
    /**
     * セットを削除
     */
    @Delete
    suspend fun deleteSet(exerciseSet: ExerciseSetEntity)
    
    /**
     * 指定したエクササイズIDのセットをすべて削除
     */
    @Query("DELETE FROM exercise_sets WHERE exerciseId = :exerciseId")
    suspend fun deleteSetsByExerciseId(exerciseId: Long)
    
    /**
     * 指定したエクササイズの総ボリューム（重量×回数）を計算
     */
    @Query("SELECT SUM(weight * reps) FROM exercise_sets WHERE exerciseId = :exerciseId")
    suspend fun getTotalVolumeByExerciseId(exerciseId: Long): Double?
    
    /**
     * 指定したエクササイズの最大重量を取得
     */
    @Query("SELECT MAX(weight) FROM exercise_sets WHERE exerciseId = :exerciseId")
    suspend fun getMaxWeightByExerciseId(exerciseId: Long): Double?
}