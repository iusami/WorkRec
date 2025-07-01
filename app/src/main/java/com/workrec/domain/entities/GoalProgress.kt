package com.workrec.domain.entities

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/**
 * 目標進捗記録のドメインエンティティ
 * 目標の進捗履歴を記録・追跡するためのエンティティ
 */
data class GoalProgressRecord(
    val id: Long = 0,
    val goalId: Long,
    val recordDate: LocalDate,
    val progressValue: Double,
    val notes: String? = null,
    val createdAt: LocalDate
) {
    /**
     * 進捗が目標の期待値に達しているかを判定
     * @param goal 対象の目標
     * @return 期待値に達している場合true
     */
    fun isOnTrack(goal: Goal): Boolean {
        if (goal.deadline == null) return true
        
        val totalDays = goal.createdAt.daysUntil(goal.deadline)
        val elapsedDays = goal.createdAt.daysUntil(recordDate)
        
        if (totalDays <= 0) return progressValue >= goal.targetValue
        
        val expectedProgress = (goal.targetValue * elapsedDays.toDouble()) / totalDays.toDouble()
        return progressValue >= expectedProgress
    }
    
    /**
     * 前回の進捗からの変化量を計算
     * @param previousProgress 前回の進捗記録
     * @return 変化量（正の値は増加、負の値は減少）
     */
    fun getDelta(previousProgress: GoalProgressRecord?): Double {
        return if (previousProgress != null) {
            progressValue - previousProgress.progressValue
        } else {
            progressValue
        }
    }
}

/**
 * 目標の進捗統計情報
 */
data class GoalProgressStats(
    val goalId: Long,
    val totalRecords: Int,
    val averageProgress: Double,
    val bestProgress: Double,
    val latestProgress: Double?,
    val progressTrend: ProgressTrend,
    val daysActive: Int,
    val lastRecordDate: LocalDate?
)

/**
 * 進捗トレンド
 */
enum class ProgressTrend {
    IMPROVING,    // 改善中
    STABLE,       // 安定
    DECLINING,    // 低下
    UNKNOWN       // 不明（データ不足）
}