package com.workrec.domain.entities

import kotlinx.datetime.LocalDate

/**
 * 進捗データのコンテナ
 * 様々な進捗メトリクスを格納する
 */
data class ProgressData(
    val workoutStatistics: WorkoutStatistics,
    val goalProgress: List<GoalProgress>,
    val weeklyTrend: List<WeeklyData>,
    val personalRecords: List<PersonalRecord>
)

/**
 * ワークアウト統計データ
 */
data class WorkoutStatistics(
    val period: TimePeriod,
    val totalWorkouts: Int,
    val totalVolume: Double,
    val averageVolume: Double,
    val averageDuration: Int, // 分
    val totalSets: Int,
    val mostActiveDay: String,
    val workoutFrequency: Double, // 週あたりのワークアウト回数
    val volumeTrend: List<VolumeData>
)

/**
 * 目標進捗データ
 */
data class GoalProgress(
    val goal: Goal,
    val progressPercentage: Float,
    val remainingDays: Int?,
    val isOnTrack: Boolean,
    val projectedCompletion: LocalDate?
)

/**
 * 週間データ
 */
data class WeeklyData(
    val weekStart: LocalDate,
    val workoutCount: Int,
    val totalVolume: Double,
    val averageVolume: Double
)

/**
 * 個人記録
 */
data class PersonalRecord(
    val exerciseName: String,
    val recordType: RecordType,
    val value: Double,
    val unit: String,
    val achievedDate: LocalDate,
    val previousRecord: Double?
)

/**
 * ボリュームデータ（チャート用）
 */
data class VolumeData(
    val date: LocalDate,
    val volume: Double,
    val workoutCount: Int
)

/**
 * 記録のタイプ
 */
enum class RecordType(val displayName: String) {
    MAX_WEIGHT("最大重量"),
    MAX_REPS("最大回数"),
    MAX_VOLUME("最大ボリューム"),
    LONGEST_STREAK("最長連続記録")
}

/**
 * 時間期間の定義
 */
enum class TimePeriod(val displayName: String, val days: Int) {
    WEEK("週", 7),
    MONTH("月", 30),
    THREE_MONTHS("3ヶ月", 90),
    YEAR("年", 365)
}