package com.workrec.data.database.dao

import androidx.room.*
import com.workrec.data.database.entities.ExerciseTemplateEntity
import com.workrec.domain.entities.ExerciseCategory
import kotlinx.coroutines.flow.Flow

/**
 * エクササイズテンプレートデータアクセスオブジェクト
 */
@Dao
interface ExerciseTemplateDao {
    
    /**
     * すべてのエクササイズテンプレートを取得（Flow版）
     */
    @Query("SELECT * FROM exercise_templates ORDER BY name")
    fun getAllExerciseTemplatesFlow(): Flow<List<ExerciseTemplateEntity>>
    
    /**
     * すべてのエクササイズテンプレートを取得
     */
    @Query("SELECT * FROM exercise_templates ORDER BY name")
    suspend fun getAllExerciseTemplates(): List<ExerciseTemplateEntity>
    
    /**
     * 指定したIDのエクササイズテンプレートを取得
     */
    @Query("SELECT * FROM exercise_templates WHERE id = :id")
    suspend fun getExerciseTemplateById(id: Long): ExerciseTemplateEntity?
    
    /**
     * エクササイズテンプレート名で検索
     */
    @Query("SELECT * FROM exercise_templates WHERE name LIKE '%' || :name || '%' ORDER BY name")
    suspend fun searchExerciseTemplatesByName(name: String): List<ExerciseTemplateEntity>
    
    /**
     * カテゴリー別のエクササイズテンプレートを取得
     */
    @Query("SELECT * FROM exercise_templates WHERE category = :category ORDER BY name")
    suspend fun getExerciseTemplatesByCategory(category: ExerciseCategory): List<ExerciseTemplateEntity>
    
    
    /**
     * 複合条件でエクササイズテンプレートを検索
     */
    @Query("""
        SELECT * FROM exercise_templates 
        WHERE (:name IS NULL OR name LIKE '%' || :name || '%')
        AND (:category IS NULL OR category = :category)
        AND (:showUserCreated = 1 OR isUserCreated = 0)
        ORDER BY 
            CASE :sortOrder
                WHEN 'NAME_ASC' THEN name
                WHEN 'NAME_DESC' THEN name
                WHEN 'CATEGORY' THEN category
                ELSE name
            END ASC,
            CASE :sortOrder
                WHEN 'NAME_DESC' THEN name
            END DESC
    """)
    suspend fun searchExerciseTemplates(
        name: String? = null,
        category: ExerciseCategory? = null,
        showUserCreated: Boolean = true,
        sortOrder: String = "NAME_ASC"
    ): List<ExerciseTemplateEntity>
    
    /**
     * ユーザー作成のエクササイズテンプレートのみ取得
     */
    @Query("SELECT * FROM exercise_templates WHERE isUserCreated = 1 ORDER BY name")
    suspend fun getUserCreatedExerciseTemplates(): List<ExerciseTemplateEntity>
    
    /**
     * 事前定義されたエクササイズテンプレートのみ取得
     */
    @Query("SELECT * FROM exercise_templates WHERE isUserCreated = 0 ORDER BY name")
    suspend fun getPredefinedExerciseTemplates(): List<ExerciseTemplateEntity>
    
    /**
     * ユニークなカテゴリー一覧を取得
     */
    @Query("SELECT DISTINCT category FROM exercise_templates ORDER BY category")
    suspend fun getUniqueCategories(): List<ExerciseCategory>
    
    
    /**
     * エクササイズテンプレートを挿入
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseTemplate(exerciseTemplate: ExerciseTemplateEntity): Long
    
    /**
     * 複数のエクササイズテンプレートを挿入
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseTemplates(exerciseTemplates: List<ExerciseTemplateEntity>): List<Long>
    
    /**
     * エクササイズテンプレートを更新
     */
    @Update
    suspend fun updateExerciseTemplate(exerciseTemplate: ExerciseTemplateEntity)
    
    /**
     * エクササイズテンプレートを削除
     */
    @Delete
    suspend fun deleteExerciseTemplate(exerciseTemplate: ExerciseTemplateEntity)
    
    /**
     * 指定したIDのエクササイズテンプレートを削除
     */
    @Query("DELETE FROM exercise_templates WHERE id = :id")
    suspend fun deleteExerciseTemplateById(id: Long)
    
    /**
     * ユーザー作成のエクササイズテンプレートをすべて削除
     */
    @Query("DELETE FROM exercise_templates WHERE isUserCreated = 1")
    suspend fun deleteAllUserCreatedExerciseTemplates()
    
    /**
     * 全てのエクササイズテンプレートを削除（開発用）
     */
    @Query("DELETE FROM exercise_templates")
    suspend fun deleteAllExerciseTemplates()
}