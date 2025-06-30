package com.workrec.domain.entities

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/**
 * 進捗メトリクス計算用のユーティリティクラス
 */
object ProgressMetrics {

    /**
     * ワークアウトリストから統計を計算
     */
    fun calculateWorkoutStatistics(
        workouts: List<Workout>,
        period: TimePeriod
    ): WorkoutStatistics {
        if (workouts.isEmpty()) {
            return WorkoutStatistics(
                period = period,
                totalWorkouts = 0,
                totalVolume = 0.0,
                averageVolume = 0.0,
                averageDuration = 0,
                totalSets = 0,
                mostActiveDay = "未記録",
                workoutFrequency = 0.0,
                volumeTrend = emptyList()
            )
        }

        val totalVolume = workouts.sumOf { it.totalVolume }
        val totalSets = workouts.sumOf { it.totalSets }
        val averageVolume = totalVolume / workouts.size

        // 曜日別の活動度を計算
        val dayOfWeekCounts = workouts.groupBy { it.date.dayOfWeek }
            .mapValues { it.value.size }
        val mostActiveDay = dayOfWeekCounts.maxByOrNull { it.value }?.key?.name ?: "未記録"

        // 週あたりの頻度を計算
        val workoutFrequency = if (period.days >= 7) {
            (workouts.size.toDouble() / period.days) * 7
        } else {
            workouts.size.toDouble()
        }

        // ボリューム推移データを作成
        val volumeTrend = createVolumeTrend(workouts)

        return WorkoutStatistics(
            period = period,
            totalWorkouts = workouts.size,
            totalVolume = totalVolume,
            averageVolume = averageVolume,
            averageDuration = calculateAverageDuration(workouts),
            totalSets = totalSets,
            mostActiveDay = translateDayOfWeek(mostActiveDay),
            workoutFrequency = workoutFrequency,
            volumeTrend = volumeTrend
        )
    }

    /**
     * 目標進捗リストを計算
     */
    fun calculateGoalProgress(goals: List<Goal>, currentDate: LocalDate): List<GoalProgress> {
        return goals.map { goal ->
            val remainingDays = goal.deadline?.let { deadline ->
                val daysUntil = currentDate.daysUntil(deadline)
                if (daysUntil > 0) daysUntil.toInt() else null
            }

            val isOnTrack = calculateIfOnTrack(goal, currentDate)
            val projectedCompletion = calculateProjectedCompletion(goal, currentDate)

            GoalProgress(
                goal = goal,
                progressPercentage = goal.progressPercentage,
                remainingDays = remainingDays,
                isOnTrack = isOnTrack,
                projectedCompletion = projectedCompletion
            )
        }
    }

    /**
     * 週間データを計算
     */
    fun calculateWeeklyData(workouts: List<Workout>, weeks: Int = 12): List<WeeklyData> {
        val weeklyGroups = workouts.groupBy { workout ->
            // 週の開始日を計算（月曜日開始） - シンプルな実装
            val year = workout.date.year
            val month = workout.date.month
            val dayOfMonth = workout.date.dayOfMonth
            val dayOfWeek = workout.date.dayOfWeek.ordinal
            
            // 週の開始日（月曜日）を計算
            val adjustedDay = dayOfMonth - dayOfWeek
            if (adjustedDay > 0) {
                LocalDate(year, month, adjustedDay)
            } else {
                // 前月にまたがる場合の簡易実装
                LocalDate(year, month, 1)
            }
        }

        return weeklyGroups.map { (weekStart, weekWorkouts) ->
            val totalVolume = weekWorkouts.sumOf { it.totalVolume }
            WeeklyData(
                weekStart = weekStart,
                workoutCount = weekWorkouts.size,
                totalVolume = totalVolume,
                averageVolume = if (weekWorkouts.isNotEmpty()) totalVolume / weekWorkouts.size else 0.0
            )
        }.sortedBy { it.weekStart }
            .takeLast(weeks)
    }

    /**
     * 個人記録を計算
     */
    fun calculatePersonalRecords(workouts: List<Workout>): List<PersonalRecord> {
        val records = mutableListOf<PersonalRecord>()
        
        // エクササイズ別にグループ化
        val exerciseGroups = workouts.flatMap { workout ->
            workout.exercises.map { exercise -> workout.date to exercise }
        }.groupBy { it.second.name }

        exerciseGroups.forEach { (exerciseName, exerciseData) ->
            // 最大重量記録
            val maxWeightData = exerciseData.maxByOrNull { (_, exercise) ->
                exercise.sets.maxOfOrNull { it.weight } ?: 0.0
            }
            maxWeightData?.let { (date, exercise) ->
                val maxWeight = exercise.sets.maxOf { it.weight }
                if (maxWeight > 0) {
                    records.add(
                        PersonalRecord(
                            exerciseName = exerciseName,
                            recordType = RecordType.MAX_WEIGHT,
                            value = maxWeight,
                            unit = "kg",
                            achievedDate = date,
                            previousRecord = null // 前回記録の計算は複雑なので省略
                        )
                    )
                }
            }

            // 最大ボリューム記録
            val maxVolumeData = exerciseData.maxByOrNull { (_, exercise) ->
                exercise.sets.sumOf { it.weight * it.reps }
            }
            maxVolumeData?.let { (date, exercise) ->
                val maxVolume = exercise.sets.sumOf { it.weight * it.reps }
                if (maxVolume > 0) {
                    records.add(
                        PersonalRecord(
                            exerciseName = exerciseName,
                            recordType = RecordType.MAX_VOLUME,
                            value = maxVolume,
                            unit = "kg",
                            achievedDate = date,
                            previousRecord = null
                        )
                    )
                }
            }
        }

        return records.sortedByDescending { it.achievedDate }
    }

    private fun calculateAverageDuration(workouts: List<Workout>): Int {
        val durationsInMinutes = workouts.mapNotNull { workout ->
            workout.totalDuration?.inWholeMinutes?.toInt()
        }
        return if (durationsInMinutes.isNotEmpty()) {
            durationsInMinutes.average().toInt()
        } else 0
    }

    private fun createVolumeTrend(workouts: List<Workout>): List<VolumeData> {
        return workouts.groupBy { it.date }
            .map { (date, dayWorkouts) ->
                VolumeData(
                    date = date,
                    volume = dayWorkouts.sumOf { it.totalVolume },
                    workoutCount = dayWorkouts.size
                )
            }
            .sortedBy { it.date }
    }

    private fun calculateIfOnTrack(goal: Goal, currentDate: LocalDate): Boolean {
        val deadline = goal.deadline ?: return true
        val totalDays = goal.createdAt.daysUntil(deadline)
        val elapsedDays = goal.createdAt.daysUntil(currentDate)
        
        if (totalDays <= 0) return goal.isAchieved
        
        val expectedProgress = elapsedDays.toDouble() / totalDays.toDouble()
        return goal.progressPercentage >= expectedProgress
    }

    private fun calculateProjectedCompletion(goal: Goal, currentDate: LocalDate): LocalDate? {
        if (goal.isAchieved) return currentDate
        if (goal.progressPercentage <= 0) return null
        
        val elapsedDays = goal.createdAt.daysUntil(currentDate)
        val projectedTotalDays = (elapsedDays.toDouble() / goal.progressPercentage).toInt()
        
        // 簡易実装：日数を追加して日付を計算
        return LocalDate(
            goal.createdAt.year,
            goal.createdAt.month,
            minOf(goal.createdAt.dayOfMonth + projectedTotalDays, 28) // 月末を超えないように制限
        )
    }

    private fun translateDayOfWeek(dayName: String): String {
        return when (dayName.uppercase()) {
            "MONDAY" -> "月曜日"
            "TUESDAY" -> "火曜日"
            "WEDNESDAY" -> "水曜日"
            "THURSDAY" -> "木曜日"
            "FRIDAY" -> "金曜日"
            "SATURDAY" -> "土曜日"
            "SUNDAY" -> "日曜日"
            else -> dayName
        }
    }
}