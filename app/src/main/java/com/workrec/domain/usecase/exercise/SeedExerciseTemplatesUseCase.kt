package com.workrec.domain.usecase.exercise

import com.workrec.domain.repository.ExerciseTemplateRepository
import javax.inject.Inject

/**
 * 事前定義されたエクササイズテンプレートをデータベースにシードするユースケース
 * アプリ初回起動時に実行される
 */
class SeedExerciseTemplatesUseCase @Inject constructor(
    private val exerciseTemplateRepository: ExerciseTemplateRepository
) {
    
    /**
     * 事前定義エクササイズをシードする
     * 既にデータが存在する場合はスキップする
     */
    suspend operator fun invoke() {
        exerciseTemplateRepository.seedPredefinedExercises()
    }
    
    /**
     * シードデータが存在するかチェック
     */
    suspend fun hasSeedData(): Boolean {
        return exerciseTemplateRepository.hasSeedData()
    }
}