package com.workrec.domain.usecase.workout

import com.workrec.domain.entities.*
import com.workrec.domain.repository.WorkoutRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * AddWorkoutUseCaseの単体テスト
 */
class AddWorkoutUseCaseTest {

    private lateinit var mockRepository: WorkoutRepository
    private lateinit var addWorkoutUseCase: AddWorkoutUseCase

    @Before
    fun setup() {
        mockRepository = mockk()
        addWorkoutUseCase = AddWorkoutUseCase(mockRepository)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `正常なワークアウトが正常に保存されること`() = runTest {
        // Given: 有効なワークアウト
        val validWorkout = createValidTestWorkout()
        val expectedWorkoutId = 123L
        
        coEvery { mockRepository.saveWorkout(validWorkout) } returns expectedWorkoutId

        // When: ワークアウトを追加
        val result = addWorkoutUseCase(validWorkout)

        // Then: 成功し、正しいIDが返される
        assertTrue(result.isSuccess)
        assertEquals(expectedWorkoutId, result.getOrNull())
        coVerify(exactly = 1) { mockRepository.saveWorkout(validWorkout) }
    }

    @Test
    fun `エクササイズが空の場合にエラーが返されること`() = runTest {
        // Given: エクササイズが空のワークアウト
        val invalidWorkout = createValidTestWorkout().copy(exercises = emptyList())

        // When: ワークアウトを追加
        val result = addWorkoutUseCase(invalidWorkout)

        // Then: 失敗し、適切なエラーメッセージが返される
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is IllegalArgumentException)
        assertEquals("ワークアウトには少なくとも1つのエクササイズが必要です", exception?.message)
        
        // リポジトリは呼ばれない
        coVerify(exactly = 0) { mockRepository.saveWorkout(any()) }
    }

    @Test
    fun `エクササイズのセットが空の場合にエラーが返されること`() = runTest {
        // Given: セットが空のエクササイズを持つワークアウト
        val exerciseWithoutSets = Exercise(
            id = 1L,
            name = "ベンチプレス",
            sets = emptyList(),
            category = ExerciseCategory.CHEST
        )
        val invalidWorkout = createValidTestWorkout().copy(exercises = listOf(exerciseWithoutSets))

        // When: ワークアウトを追加
        val result = addWorkoutUseCase(invalidWorkout)

        // Then: 失敗し、適切なエラーメッセージが返される
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is IllegalArgumentException)
        assertEquals("エクササイズ「ベンチプレス」にはセットが必要です", exception?.message)
        
        coVerify(exactly = 0) { mockRepository.saveWorkout(any()) }
    }

    @Test
    fun `回数が0以下の場合にエラーが返されること`() = runTest {
        // Given: 無効な回数のセットを持つワークアウト
        val invalidSet = ExerciseSet(reps = 0, weight = 60.0)
        val exerciseWithInvalidSet = Exercise(
            id = 1L,
            name = "ベンチプレス",
            sets = listOf(invalidSet),
            category = ExerciseCategory.CHEST
        )
        val invalidWorkout = createValidTestWorkout().copy(exercises = listOf(exerciseWithInvalidSet))

        // When: ワークアウトを追加
        val result = addWorkoutUseCase(invalidWorkout)

        // Then: 失敗し、適切なエラーメッセージが返される
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is IllegalArgumentException)
        assertEquals("回数は1以上である必要があります", exception?.message)
        
        coVerify(exactly = 0) { mockRepository.saveWorkout(any()) }
    }

    @Test
    fun `重量が負の値の場合にエラーが返されること`() = runTest {
        // Given: 負の重量のセットを持つワークアウト
        val invalidSet = ExerciseSet(reps = 10, weight = -10.0)
        val exerciseWithInvalidSet = Exercise(
            id = 1L,
            name = "ベンチプレス",
            sets = listOf(invalidSet),
            category = ExerciseCategory.CHEST
        )
        val invalidWorkout = createValidTestWorkout().copy(exercises = listOf(exerciseWithInvalidSet))

        // When: ワークアウトを追加
        val result = addWorkoutUseCase(invalidWorkout)

        // Then: 失敗し、適切なエラーメッセージが返される
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is IllegalArgumentException)
        assertEquals("重量は0以上である必要があります", exception?.message)
        
        coVerify(exactly = 0) { mockRepository.saveWorkout(any()) }
    }

    @Test
    fun `リポジトリで例外が発生した場合にエラーが返されること`() = runTest {
        // Given: リポジトリで例外が発生
        val validWorkout = createValidTestWorkout()
        val repositoryException = RuntimeException("データベースエラー")
        
        coEvery { mockRepository.saveWorkout(validWorkout) } throws repositoryException

        // When: ワークアウトを追加
        val result = addWorkoutUseCase(validWorkout)

        // Then: 失敗し、例外が返される
        assertTrue(result.isFailure)
        assertEquals(repositoryException, result.exceptionOrNull())
        
        coVerify(exactly = 1) { mockRepository.saveWorkout(validWorkout) }
    }

    // テストヘルパーメソッド
    private fun createValidTestWorkout(): Workout {
        val validSets = listOf(
            ExerciseSet(reps = 10, weight = 60.0),
            ExerciseSet(reps = 8, weight = 65.0)
        )
        val validExercise = Exercise(
            id = 1L,
            name = "ベンチプレス",
            sets = validSets,
            category = ExerciseCategory.CHEST
        )
        
        return Workout(
            id = 0L,
            date = LocalDate(2024, 1, 1),
            exercises = listOf(validExercise),
            notes = "テストワークアウト"
        )
    }
}