package com.workrec.domain.entities

import kotlinx.datetime.LocalDate
import kotlin.time.Duration

/**
 * ワークアウトのドメインエンティティ
 * ビジネスロジックの中核となるデータ構造
 */
data class Workout(
    val id: Long = 0,
    val date: LocalDate,
    val exercises: List<Exercise>,
    val totalDuration: Duration? = null,
    val notes: String? = null
) {
    /**
     * ワークアウトの総ボリューム（重量×回数の合計）を計算
     */
    val totalVolume: Double
        get() = exercises.sumOf { exercise ->
            exercise.sets.sumOf { set ->
                set.weight * set.reps
            }
        }

    /**
     * ワークアウトの総セット数を計算
     */
    val totalSets: Int
        get() = exercises.sumOf { it.sets.size }

    /**
     * ワークアウトが空かどうかを判定
     */
    val isEmpty: Boolean
        get() = exercises.isEmpty()
}