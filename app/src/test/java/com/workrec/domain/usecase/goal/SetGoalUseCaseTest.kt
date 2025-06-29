package com.workrec.domain.usecase.goal

import com.workrec.domain.entities.*
import com.workrec.domain.repository.GoalRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * SetGoalUseCaseの単体テスト
 */
class SetGoalUseCaseTest {

    private lateinit var mockRepository: GoalRepository
    private lateinit var setGoalUseCase: SetGoalUseCase

    @Before
    fun setup() {
        mockRepository = mockk()
        setGoalUseCase = SetGoalUseCase(mockRepository)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `正常な目標が正常に保存されること`() = runTest {
        // Given: 有効な目標
        val validGoal = createValidTestGoal()
        val expectedGoalId = 456L
        
        coEvery { mockRepository.saveGoal(validGoal) } returns expectedGoalId

        // When: 目標を設定
        val result = setGoalUseCase(validGoal)

        // Then: 成功し、正しいIDが返される
        assertTrue(result.isSuccess)
        assertEquals(expectedGoalId, result.getOrNull())
        coVerify(exactly = 1) { mockRepository.saveGoal(validGoal) }
    }

    @Test
    fun `タイトルが空の場合にエラーが返されること`() = runTest {
        // Given: タイトルが空の目標
        val invalidGoal = createValidTestGoal().copy(title = "")

        // When: 目標を設定
        val result = setGoalUseCase(invalidGoal)

        // Then: 失敗し、適切なエラーメッセージが返される
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is IllegalArgumentException)
        assertEquals("目標のタイトルが必要です", exception?.message)
        
        coVerify(exactly = 0) { mockRepository.saveGoal(any()) }
    }

    @Test
    fun `タイトルがブランクの場合にエラーが返されること`() = runTest {
        // Given: タイトルがブランクの目標
        val invalidGoal = createValidTestGoal().copy(title = "   ")

        // When: 目標を設定
        val result = setGoalUseCase(invalidGoal)

        // Then: 失敗し、適切なエラーメッセージが返される
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is IllegalArgumentException)
        assertEquals("目標のタイトルが必要です", exception?.message)
        
        coVerify(exactly = 0) { mockRepository.saveGoal(any()) }
    }

    @Test
    fun `目標値が0以下の場合にエラーが返されること`() = runTest {
        // Given: 目標値が0の目標
        val invalidGoal = createValidTestGoal().copy(targetValue = 0.0)

        // When: 目標を設定
        val result = setGoalUseCase(invalidGoal)

        // Then: 失敗し、適切なエラーメッセージが返される
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is IllegalArgumentException)
        assertEquals("目標値は0より大きい値である必要があります", exception?.message)
        
        coVerify(exactly = 0) { mockRepository.saveGoal(any()) }
    }

    @Test
    fun `目標値が負の値の場合にエラーが返されること`() = runTest {
        // Given: 目標値が負の目標
        val invalidGoal = createValidTestGoal().copy(targetValue = -10.0)

        // When: 目標を設定
        val result = setGoalUseCase(invalidGoal)

        // Then: 失敗し、適切なエラーメッセージが返される
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is IllegalArgumentException)
        assertEquals("目標値は0より大きい値である必要があります", exception?.message)
        
        coVerify(exactly = 0) { mockRepository.saveGoal(any()) }
    }

    @Test
    fun `現在値が負の値の場合にエラーが返されること`() = runTest {
        // Given: 現在値が負の目標
        val invalidGoal = createValidTestGoal().copy(currentValue = -5.0)

        // When: 目標を設定
        val result = setGoalUseCase(invalidGoal)

        // Then: 失敗し、適切なエラーメッセージが返される
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is IllegalArgumentException)
        assertEquals("現在値は0以上である必要があります", exception?.message)
        
        coVerify(exactly = 0) { mockRepository.saveGoal(any()) }
    }

    @Test
    fun `単位が空の場合にエラーが返されること`() = runTest {
        // Given: 単位が空の目標
        val invalidGoal = createValidTestGoal().copy(unit = "")

        // When: 目標を設定
        val result = setGoalUseCase(invalidGoal)

        // Then: 失敗し、適切なエラーメッセージが返される
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is IllegalArgumentException)
        assertEquals("目標の単位が必要です", exception?.message)
        
        coVerify(exactly = 0) { mockRepository.saveGoal(any()) }
    }

    @Test
    fun `単位がブランクの場合にエラーが返されること`() = runTest {
        // Given: 単位がブランクの目標
        val invalidGoal = createValidTestGoal().copy(unit = "   ")

        // When: 目標を設定
        val result = setGoalUseCase(invalidGoal)

        // Then: 失敗し、適切なエラーメッセージが返される
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is IllegalArgumentException)
        assertEquals("目標の単位が必要です", exception?.message)
        
        coVerify(exactly = 0) { mockRepository.saveGoal(any()) }
    }

    @Test
    fun `リポジトリで例外が発生した場合にエラーが返されること`() = runTest {
        // Given: リポジトリで例外が発生
        val validGoal = createValidTestGoal()
        val repositoryException = RuntimeException("データベースエラー")
        
        coEvery { mockRepository.saveGoal(validGoal) } throws repositoryException

        // When: 目標を設定
        val result = setGoalUseCase(validGoal)

        // Then: 失敗し、例外が返される
        assertTrue(result.isFailure)
        assertEquals(repositoryException, result.exceptionOrNull())
        
        coVerify(exactly = 1) { mockRepository.saveGoal(validGoal) }
    }

    // テストヘルパーメソッド
    private fun createValidTestGoal(): Goal {
        return Goal(
            id = 0L,
            type = GoalType.WEIGHT,
            title = "ベンチプレス100kg",
            description = "目標説明",
            targetValue = 100.0,
            currentValue = 50.0,
            unit = "kg",
            deadline = LocalDate(2024, 12, 31),
            isCompleted = false,
            createdAt = LocalDate(2024, 1, 1)
        )
    }
}