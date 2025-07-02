package com.workrec.domain.repository

import com.workrec.domain.entities.ExerciseTemplate
import com.workrec.domain.entities.ExerciseCategory
import com.workrec.domain.entities.ExerciseEquipment
import com.workrec.domain.entities.ExerciseDifficulty
import com.workrec.domain.entities.ExerciseFilter
import com.workrec.domain.entities.ExerciseSortOrder
import kotlinx.coroutines.flow.Flow

/**
 * エクササイズテンプレートデータアクセスの抽象化インターフェース
 * データ層の実装詳細をドメイン層から隠蔽する
 */
interface ExerciseTemplateRepository {
    
    /**
     * すべてのエクササイズテンプレートを取得（Flow版）
     */
    fun getAllExerciseTemplatesFlow(): Flow<List<ExerciseTemplate>>
    
    /**
     * すべてのエクササイズテンプレートを取得
     */
    suspend fun getAllExerciseTemplates(): List<ExerciseTemplate>
    
    /**
     * 指定したIDのエクササイズテンプレートを取得
     */
    suspend fun getExerciseTemplateById(id: Long): ExerciseTemplate?
    
    /**
     * エクササイズテンプレート名で検索
     */
    suspend fun searchExerciseTemplatesByName(name: String): List<ExerciseTemplate>
    
    /**
     * カテゴリー別のエクササイズテンプレートを取得
     */
    suspend fun getExerciseTemplatesByCategory(category: ExerciseCategory): List<ExerciseTemplate>
    
    /**
     * 器具別のエクササイズテンプレートを取得
     */
    suspend fun getExerciseTemplatesByEquipment(equipment: ExerciseEquipment): List<ExerciseTemplate>
    
    /**
     * 難易度別のエクササイズテンプレートを取得
     */
    suspend fun getExerciseTemplatesByDifficulty(difficulty: ExerciseDifficulty): List<ExerciseTemplate>
    
    /**
     * 主要筋肉別のエクササイズテンプレートを取得
     */
    suspend fun getExerciseTemplatesByMuscle(muscle: String): List<ExerciseTemplate>
    
    /**
     * フィルターとソート条件でエクササイズテンプレートを検索
     */
    suspend fun searchExerciseTemplates(
        filter: ExerciseFilter,
        sortOrder: ExerciseSortOrder = ExerciseSortOrder.NAME_ASC
    ): List<ExerciseTemplate>
    
    /**
     * ユーザー作成のエクササイズテンプレートのみ取得
     */
    suspend fun getUserCreatedExerciseTemplates(): List<ExerciseTemplate>
    
    /**
     * 事前定義されたエクササイズテンプレートのみ取得
     */
    suspend fun getPredefinedExerciseTemplates(): List<ExerciseTemplate>
    
    /**
     * ユニークなカテゴリー一覧を取得
     */
    suspend fun getUniqueCategories(): List<ExerciseCategory>
    
    /**
     * ユニークな器具一覧を取得
     */
    suspend fun getUniqueEquipment(): List<ExerciseEquipment>
    
    /**
     * ユニークな主要筋肉一覧を取得
     */
    suspend fun getUniqueMuscles(): List<String>
    
    /**
     * エクササイズテンプレートを保存
     */
    suspend fun saveExerciseTemplate(exerciseTemplate: ExerciseTemplate): Long
    
    /**
     * 複数のエクササイズテンプレートを一括保存
     */
    suspend fun saveExerciseTemplates(exerciseTemplates: List<ExerciseTemplate>): List<Long>
    
    /**
     * エクササイズテンプレートを更新
     */
    suspend fun updateExerciseTemplate(exerciseTemplate: ExerciseTemplate)
    
    /**
     * エクササイズテンプレートを削除
     */
    suspend fun deleteExerciseTemplate(exerciseTemplate: ExerciseTemplate)
    
    /**
     * 指定したIDのエクササイズテンプレートを削除
     */
    suspend fun deleteExerciseTemplateById(id: Long)
    
    /**
     * ユーザー作成のエクササイズテンプレートをすべて削除
     */
    suspend fun deleteAllUserCreatedExerciseTemplates()
    
    /**
     * 事前定義エクササイズデータをシードする
     */
    suspend fun seedPredefinedExercises()
    
    /**
     * データベースにシードデータが存在するかチェック
     */
    suspend fun hasSeedData(): Boolean
}