package com.workrec.domain.entities

import kotlinx.datetime.LocalDate

/**
 * 目標のドメインエンティティ
 */
data class Goal(
    val id: Long = 0,
    val type: GoalType,
    val title: String,
    val description: String? = null,
    val targetValue: Double,
    val currentValue: Double = 0.0,
    val unit: String,
    val deadline: LocalDate? = null,
    val isCompleted: Boolean = false,
    val createdAt: LocalDate,
    val updatedAt: LocalDate
) {
    /**
     * 目標の達成率を計算（0.0 〜 1.0）
     */
    val progressPercentage: Float
        get() = (currentValue / targetValue).coerceIn(0.0, 1.0).toFloat()

    /**
     * 目標達成まで必要な値を計算
     */
    val remainingValue: Double
        get() = (targetValue - currentValue).coerceAtLeast(0.0)

    /**
     * 目標が達成されているかを判定
     */
    val isAchieved: Boolean
        get() = currentValue >= targetValue

    /**
     * 期限切れかどうかを判定
     */
    fun isOverdue(currentDate: LocalDate): Boolean {
        return deadline?.let { it < currentDate } ?: false
    }
}

/**
 * 目標のタイプ
 */
enum class GoalType(val displayName: String, val defaultUnit: String) {
    // 基本目標
    WEIGHT_LOSS("体重減少", "kg"),
    WEIGHT_GAIN("体重増加", "kg"),
    MUSCLE_GAIN("筋肉量増加", "kg"),
    
    // パフォーマンス目標
    STRENGTH("筋力向上", "kg"),        // 最大重量
    ENDURANCE("持久力向上", "回"),      // 連続回数
    VOLUME("総ボリューム", "kg"),       // 累計重量
    
    // 頻度・継続目標
    FREQUENCY("頻度目標", "回/週"),     // 週間頻度
    DURATION("継続目標", "日"),        // 継続日数
    
    // 体組成目標  
    BODY_FAT("体脂肪率", "%"),         // 体脂肪率
    MUSCLE_MASS("筋肉量", "kg"),       // 筋肉量
    
    // カスタム目標
    CUSTOM("カスタム目標", ""),        // ユーザー定義
    OTHER("その他", "")               // その他
}