package com.workrec.domain.entities

/**
 * エクササイズのドメインエンティティ
 */
data class Exercise(
    val id: Long = 0,
    val name: String,
    val sets: List<ExerciseSet>,
    val category: ExerciseCategory,
    val notes: String? = null
) {
    /**
     * エクササイズの最大重量を取得
     */
    val maxWeight: Double
        get() = sets.maxOfOrNull { it.weight } ?: 0.0

    /**
     * エクササイズの総ボリューム（重量×回数の合計）を計算
     */
    val totalVolume: Double
        get() = sets.sumOf { it.weight * it.reps }

    /**
     * エクササイズの平均重量を計算
     */
    val averageWeight: Double
        get() = if (sets.isNotEmpty()) {
            sets.map { it.weight }.average()
        } else 0.0
}

/**
 * エクササイズのセット情報
 */
data class ExerciseSet(
    val reps: Int,
    val weight: Double,
    val restTime: kotlin.time.Duration? = null
) {
    /**
     * セットのボリューム（重量×回数）を計算
     */
    val volume: Double
        get() = weight * reps
}

/**
 * エクササイズのカテゴリー分類
 */
enum class ExerciseCategory(val displayName: String) {
    CHEST("胸"),
    BACK("背中"),
    SHOULDERS("肩"),
    ARMS("腕"),
    LEGS("脚"),
    CORE("体幹"),
    CARDIO("有酸素"),
    OTHER("その他")
}