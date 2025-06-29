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
    val createdAt: LocalDate
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
enum class GoalType(val displayName: String) {
    WEIGHT("重量目標"),
    REPS("回数目標"),
    FREQUENCY("頻度目標"),
    DURATION("継続目標"),
    VOLUME("ボリューム目標"),
    BODY_WEIGHT("体重目標"),
    OTHER("その他")
}