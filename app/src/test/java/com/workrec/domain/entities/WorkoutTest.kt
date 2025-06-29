package com.workrec.domain.entities

import kotlinx.datetime.LocalDate
import org.junit.Test
import org.junit.Assert.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Workoutエンティティの単体テスト
 */
class WorkoutTest {

    @Test
    fun `totalVolumeが正しく計算されること`() {
        // Given: 2つのエクササイズを持つワークアウト
        val exercises = listOf(
            createTestExercise(
                name = "ベンチプレス",
                sets = listOf(
                    ExerciseSet(reps = 10, weight = 60.0),
                    ExerciseSet(reps = 8, weight = 65.0)
                )
            ),
            createTestExercise(
                name = "スクワット", 
                sets = listOf(
                    ExerciseSet(reps = 12, weight = 80.0)
                )
            )
        )
        
        val workout = createTestWorkout(exercises = exercises)

        // When: 総ボリュームを計算
        val totalVolume = workout.totalVolume

        // Then: 正しい値が計算される
        // (10 * 60.0) + (8 * 65.0) + (12 * 80.0) = 600 + 520 + 960 = 2080
        assertEquals(2080.0, totalVolume, 0.01)
    }

    @Test
    fun `totalSetsが正しく計算されること`() {
        // Given: 複数のエクササイズを持つワークアウト
        val exercises = listOf(
            createTestExercise(sets = listOf(
                ExerciseSet(reps = 10, weight = 60.0),
                ExerciseSet(reps = 8, weight = 65.0)
            )),
            createTestExercise(sets = listOf(
                ExerciseSet(reps = 12, weight = 80.0),
                ExerciseSet(reps = 10, weight = 85.0),
                ExerciseSet(reps = 8, weight = 90.0)
            ))
        )
        
        val workout = createTestWorkout(exercises = exercises)

        // When: 総セット数を計算
        val totalSets = workout.totalSets

        // Then: 正しい値が計算される（2 + 3 = 5）
        assertEquals(5, totalSets)
    }

    @Test
    fun `isEmpty_エクササイズがない場合にtrueを返すこと`() {
        // Given: エクササイズが空のワークアウト
        val workout = createTestWorkout(exercises = emptyList())

        // When & Then: 空であることが判定される
        assertTrue(workout.isEmpty)
    }

    @Test
    fun `isEmpty_エクササイズがある場合にfalseを返すこと`() {
        // Given: エクササイズがあるワークアウト
        val exercises = listOf(
            createTestExercise(sets = listOf(ExerciseSet(reps = 10, weight = 60.0)))
        )
        val workout = createTestWorkout(exercises = exercises)

        // When & Then: 空でないことが判定される
        assertFalse(workout.isEmpty)
    }

    @Test
    fun `エクササイズがない場合の総ボリュームは0であること`() {
        // Given: エクササイズが空のワークアウト
        val workout = createTestWorkout(exercises = emptyList())

        // When & Then: 総ボリュームが0
        assertEquals(0.0, workout.totalVolume, 0.01)
    }

    @Test
    fun `エクササイズがない場合の総セット数は0であること`() {
        // Given: エクササイズが空のワークアウト
        val workout = createTestWorkout(exercises = emptyList())

        // When & Then: 総セット数が0
        assertEquals(0, workout.totalSets)
    }

    // テストヘルパーメソッド
    private fun createTestWorkout(
        id: Long = 1L,
        date: LocalDate = LocalDate(2024, 1, 1),
        exercises: List<Exercise> = emptyList(),
        totalDuration: kotlin.time.Duration? = 1.hours,
        notes: String? = "テストワークアウト"
    ) = Workout(
        id = id,
        date = date,
        exercises = exercises,
        totalDuration = totalDuration,
        notes = notes
    )

    private fun createTestExercise(
        id: Long = 1L,
        name: String = "テストエクササイズ",
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