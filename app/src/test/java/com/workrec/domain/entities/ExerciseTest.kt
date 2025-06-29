package com.workrec.domain.entities

import org.junit.Test
import org.junit.Assert.*
import kotlin.time.Duration.Companion.seconds

/**
 * Exerciseエンティティの単体テスト
 */
class ExerciseTest {

    @Test
    fun `maxWeightが正しく計算されること`() {
        // Given: 異なる重量のセットを持つエクササイズ
        val sets = listOf(
            ExerciseSet(reps = 10, weight = 60.0),
            ExerciseSet(reps = 8, weight = 70.0),
            ExerciseSet(reps = 6, weight = 80.0)
        )
        val exercise = createTestExercise(sets = sets)

        // When: 最大重量を取得
        val maxWeight = exercise.maxWeight

        // Then: 最大値が正しく計算される
        assertEquals(80.0, maxWeight, 0.01)
    }

    @Test
    fun `totalVolumeが正しく計算されること`() {
        // Given: 複数のセットを持つエクササイズ
        val sets = listOf(
            ExerciseSet(reps = 10, weight = 60.0),
            ExerciseSet(reps = 8, weight = 70.0),
            ExerciseSet(reps = 6, weight = 80.0)
        )
        val exercise = createTestExercise(sets = sets)

        // When: 総ボリュームを計算
        val totalVolume = exercise.totalVolume

        // Then: 正しい値が計算される
        // (10 * 60.0) + (8 * 70.0) + (6 * 80.0) = 600 + 560 + 480 = 1640
        assertEquals(1640.0, totalVolume, 0.01)
    }

    @Test
    fun `averageWeightが正しく計算されること`() {
        // Given: 複数のセットを持つエクササイズ
        val sets = listOf(
            ExerciseSet(reps = 10, weight = 60.0),
            ExerciseSet(reps = 8, weight = 80.0)
        )
        val exercise = createTestExercise(sets = sets)

        // When: 平均重量を計算
        val averageWeight = exercise.averageWeight

        // Then: 正しい値が計算される（(60.0 + 80.0) / 2 = 70.0）
        assertEquals(70.0, averageWeight, 0.01)
    }

    @Test
    fun `セットが空の場合のmaxWeightは0であること`() {
        // Given: セットが空のエクササイズ
        val exercise = createTestExercise(sets = emptyList())

        // When & Then: 最大重量が0
        assertEquals(0.0, exercise.maxWeight, 0.01)
    }

    @Test
    fun `セットが空の場合のtotalVolumeは0であること`() {
        // Given: セットが空のエクササイズ
        val exercise = createTestExercise(sets = emptyList())

        // When & Then: 総ボリュームが0
        assertEquals(0.0, exercise.totalVolume, 0.01)
    }

    @Test
    fun `セットが空の場合のaverageWeightは0であること`() {
        // Given: セットが空のエクササイズ
        val exercise = createTestExercise(sets = emptyList())

        // When & Then: 平均重量が0
        assertEquals(0.0, exercise.averageWeight, 0.01)
    }

    // テストヘルパーメソッド
    private fun createTestExercise(
        id: Long = 1L,
        name: String = "ベンチプレス",
        sets: List<ExerciseSet> = emptyList(),
        category: ExerciseCategory = ExerciseCategory.CHEST,
        notes: String? = null
    ) = Exercise(
        id = id,
        name = name,
        sets = sets,
        category = category,
        notes = notes
    )
}

/**
 * ExerciseSetエンティティの単体テスト
 */
class ExerciseSetTest {

    @Test
    fun `volumeが正しく計算されること`() {
        // Given: セット情報
        val exerciseSet = ExerciseSet(
            reps = 10,
            weight = 60.0,
            restTime = 90.seconds
        )

        // When: ボリュームを計算
        val volume = exerciseSet.volume

        // Then: 正しい値が計算される（10 * 60.0 = 600.0）
        assertEquals(600.0, volume, 0.01)
    }

    @Test
    fun `重量が0の場合のvolumeは0であること`() {
        // Given: 重量が0のセット
        val exerciseSet = ExerciseSet(reps = 10, weight = 0.0)

        // When & Then: ボリュームが0
        assertEquals(0.0, exerciseSet.volume, 0.01)
    }

    @Test
    fun `回数が0の場合のvolumeは0であること`() {
        // Given: 回数が0のセット
        val exerciseSet = ExerciseSet(reps = 0, weight = 60.0)

        // When & Then: ボリュームが0
        assertEquals(0.0, exerciseSet.volume, 0.01)
    }
}