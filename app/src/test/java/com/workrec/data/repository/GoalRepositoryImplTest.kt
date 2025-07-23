package com.workrec.data.repository

import com.workrec.data.database.dao.GoalDao
import com.workrec.data.database.dao.GoalProgressDao
import com.workrec.data.database.entities.GoalEntity
import com.workrec.data.database.entities.GoalProgressEntity
import com.workrec.data.database.entities.toDomainModel
import com.workrec.data.database.entities.toEntity
import com.workrec.domain.entities.Goal
import com.workrec.domain.entities.GoalType
import com.workrec.domain.entities.GoalProgressRecord
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * GoalRepositoryImplの単体テスト
 * 最適化されたDAO呼び出しとドメインモデルマッピングをテスト
 */
class GoalRepositoryImplTest {

    private lateinit var mockGoalDao: GoalDao
    private lateinit var mockGoalProgressDao: GoalProgressDao
    private lateinit var repository: GoalRepositoryImpl

    @Before
    fun setup() {
        mockGoalDao = mockk()
        mockGoalProgressDao = mockk()
        repository = GoalRepositoryImpl(mockGoalDao, mockGoalProgressDao)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    // Test optimized getActiveGoals method
    @Test
    fun `getActiveGoals_正しくDAOのgetActiveGoalsメソッドを呼び出すこと`() = runTest {
        // Given: アクティブな目標エンティティが存在
        val activeGoalEntities = listOf(
            createTestGoalEntity(id = 1L, isCompleted = false),
            createTestGoalEntity(id = 2L, isCompleted = false)
        )
        
        every { mockGoalDao.getActiveGoals() } returns flowOf(activeGoalEntities)

        // When: アクティブな目標を取得
        val result = repository.getActiveGoals().first()

        // Then: 正しくDAOメソッドが呼ばれ、ドメインモデルに変換される
        verify(exactly = 1) { mockGoalDao.getActiveGoals() }
        assertEquals(2, result.size)
        assertFalse(result[0].isCompleted)
        assertFalse(result[1].isCompleted)
        assertEquals(1L, result[0].id)
        assertEquals(2L, result[1].id)
    }

    @Test
    fun `getActiveGoals_空のリストが正しく処理されること`() = runTest {
        // Given: アクティブな目標が存在しない
        every { mockGoalDao.getActiveGoals() } returns flowOf(emptyList())

        // When: アクティブな目標を取得
        val result = repository.getActiveGoals().first()

        // Then: 空のリストが返される
        verify(exactly = 1) { mockGoalDao.getActiveGoals() }
        assertTrue(result.isEmpty())
    }

    // Test optimized getCompletedGoals method
    @Test
    fun `getCompletedGoals_正しくDAOのgetCompletedGoalsメソッドを呼び出すこと`() = runTest {
        // Given: 完了した目標エンティティが存在
        val completedGoalEntities = listOf(
            createTestGoalEntity(id = 3L, isCompleted = true),
            createTestGoalEntity(id = 4L, isCompleted = true)
        )
        
        every { mockGoalDao.getCompletedGoals() } returns flowOf(completedGoalEntities)

        // When: 完了した目標を取得
        val result = repository.getCompletedGoals().first()

        // Then: 正しくDAOメソッドが呼ばれ、ドメインモデルに変換される
        verify(exactly = 1) { mockGoalDao.getCompletedGoals() }
        assertEquals(2, result.size)
        assertTrue(result[0].isCompleted)
        assertTrue(result[1].isCompleted)
        assertEquals(3L, result[0].id)
        assertEquals(4L, result[1].id)
    }

    @Test
    fun `getCompletedGoals_空のリストが正しく処理されること`() = runTest {
        // Given: 完了した目標が存在しない
        every { mockGoalDao.getCompletedGoals() } returns flowOf(emptyList())

        // When: 完了した目標を取得
        val result = repository.getCompletedGoals().first()

        // Then: 空のリストが返される
        verify(exactly = 1) { mockGoalDao.getCompletedGoals() }
        assertTrue(result.isEmpty())
    }

    // Test domain model mapping continues to work correctly
    @Test
    fun `getActiveGoals_ドメインモデルマッピングが正しく動作すること`() = runTest {
        // Given: 詳細なアクティブ目標エンティティ
        val goalEntity = GoalEntity(
            id = 100L,
            type = GoalType.STRENGTH,
            title = "ベンチプレス100kg",
            description = "ベンチプレスで100kg挙上を目指す",
            targetValue = 100.0,
            currentValue = 85.0,
            unit = "kg",
            deadline = LocalDate(2024, 12, 31),
            isCompleted = false,
            createdAt = LocalDate(2024, 1, 1),
            updatedAt = LocalDate(2024, 6, 15)
        )
        
        every { mockGoalDao.getActiveGoals() } returns flowOf(listOf(goalEntity))

        // When: アクティブな目標を取得
        val result = repository.getActiveGoals().first()

        // Then: すべてのフィールドが正しくマッピングされる
        assertEquals(1, result.size)
        val goal = result[0]
        assertEquals(100L, goal.id)
        assertEquals(GoalType.STRENGTH, goal.type)
        assertEquals("ベンチプレス100kg", goal.title)
        assertEquals("ベンチプレスで100kg挙上を目指す", goal.description)
        assertEquals(100.0, goal.targetValue, 0.001)
        assertEquals(85.0, goal.currentValue, 0.001)
        assertEquals("kg", goal.unit)
        assertEquals(LocalDate(2024, 12, 31), goal.deadline)
        assertFalse(goal.isCompleted)
        assertEquals(LocalDate(2024, 1, 1), goal.createdAt)
        assertEquals(LocalDate(2024, 6, 15), goal.updatedAt)
    }

    @Test
    fun `getCompletedGoals_ドメインモデルマッピングが正しく動作すること`() = runTest {
        // Given: 詳細な完了目標エンティティ
        val goalEntity = GoalEntity(
            id = 200L,
            type = GoalType.WEIGHT_LOSS,
            title = "5kg減量",
            description = "健康的に5kg減量する",
            targetValue = 5.0,
            currentValue = 5.0,
            unit = "kg",
            deadline = LocalDate(2024, 6, 30),
            isCompleted = true,
            createdAt = LocalDate(2024, 1, 1),
            updatedAt = LocalDate(2024, 6, 30)
        )
        
        every { mockGoalDao.getCompletedGoals() } returns flowOf(listOf(goalEntity))

        // When: 完了した目標を取得
        val result = repository.getCompletedGoals().first()

        // Then: すべてのフィールドが正しくマッピングされる
        assertEquals(1, result.size)
        val goal = result[0]
        assertEquals(200L, goal.id)
        assertEquals(GoalType.WEIGHT_LOSS, goal.type)
        assertEquals("5kg減量", goal.title)
        assertEquals("健康的に5kg減量する", goal.description)
        assertEquals(5.0, goal.targetValue, 0.001)
        assertEquals(5.0, goal.currentValue, 0.001)
        assertEquals("kg", goal.unit)
        assertEquals(LocalDate(2024, 6, 30), goal.deadline)
        assertTrue(goal.isCompleted)
        assertEquals(LocalDate(2024, 1, 1), goal.createdAt)
        assertEquals(LocalDate(2024, 6, 30), goal.updatedAt)
    }

    // Test existing repository functionality continues to work
    @Test
    fun `getAllGoals_既存の機能が正常に動作すること`() = runTest {
        // Given: 全ての目標エンティティが存在
        val allGoalEntities = listOf(
            createTestGoalEntity(id = 1L, isCompleted = false),
            createTestGoalEntity(id = 2L, isCompleted = true),
            createTestGoalEntity(id = 3L, isCompleted = false)
        )
        
        every { mockGoalDao.getAllGoals() } returns flowOf(allGoalEntities)

        // When: 全ての目標を取得
        val result = repository.getAllGoals().first()

        // Then: 正しく全ての目標が返される
        verify(exactly = 1) { mockGoalDao.getAllGoals() }
        assertEquals(3, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(2L, result[1].id)
        assertEquals(3L, result[2].id)
    }

    @Test
    fun `getGoalById_既存の機能が正常に動作すること`() = runTest {
        // Given: 特定のIDの目標エンティティが存在
        val goalId = 42L
        val goalEntity = createTestGoalEntity(id = goalId)
        
        coEvery { mockGoalDao.getGoalById(goalId) } returns goalEntity

        // When: 特定のIDの目標を取得
        val result = repository.getGoalById(goalId)

        // Then: 正しく目標が返される
        coVerify(exactly = 1) { mockGoalDao.getGoalById(goalId) }
        assertNotNull(result)
        assertEquals(goalId, result!!.id)
    }

    @Test
    fun `getGoalById_存在しないIDでnullが返されること`() = runTest {
        // Given: 存在しないID
        val nonExistentId = 999L
        
        coEvery { mockGoalDao.getGoalById(nonExistentId) } returns null

        // When: 存在しないIDの目標を取得
        val result = repository.getGoalById(nonExistentId)

        // Then: nullが返される
        coVerify(exactly = 1) { mockGoalDao.getGoalById(nonExistentId) }
        assertNull(result)
    }

    @Test
    fun `saveGoal_新しい目標が正常に保存されること`() = runTest {
        // Given: 新しい目標
        val goal = createTestGoal()
        val expectedId = 123L
        
        coEvery { mockGoalDao.insertGoal(any()) } returns expectedId

        // When: 目標を保存
        val result = repository.saveGoal(goal)

        // Then: 正しいIDが返され、DAOが呼ばれる
        assertEquals(expectedId, result)
        coVerify(exactly = 1) { mockGoalDao.insertGoal(any()) }
    }

    @Test
    fun `deleteGoal_目標が正常に削除されること`() = runTest {
        // Given: 削除対象の目標
        val goal = createTestGoal()
        
        coEvery { mockGoalDao.deleteGoal(any()) } returns Unit

        // When: 目標を削除
        repository.deleteGoal(goal)

        // Then: DAOの削除メソッドが呼ばれる
        coVerify(exactly = 1) { mockGoalDao.deleteGoal(any()) }
    }

    @Test
    fun `updateGoalProgress_目標の進捗が正常に更新されること`() = runTest {
        // Given: 既存の目標と新しい進捗値
        val goalId = 1L
        val newProgress = 75.0
        val existingGoal = createTestGoalEntity(id = goalId, currentValue = 50.0)
        
        coEvery { mockGoalDao.getGoalById(goalId) } returns existingGoal
        coEvery { mockGoalDao.updateGoal(any()) } returns Unit

        // When: 目標の進捗を更新
        repository.updateGoalProgress(goalId, newProgress)

        // Then: 正しく更新される
        coVerify(exactly = 1) { mockGoalDao.getGoalById(goalId) }
        coVerify(exactly = 1) { mockGoalDao.updateGoal(any()) }
    }

    @Test
    fun `markGoalAsCompleted_目標が完了としてマークされること`() = runTest {
        // Given: 未完了の目標
        val goalId = 1L
        val existingGoal = createTestGoalEntity(id = goalId, isCompleted = false)
        
        coEvery { mockGoalDao.getGoalById(goalId) } returns existingGoal
        coEvery { mockGoalDao.updateGoal(any()) } returns Unit

        // When: 目標を完了としてマーク
        repository.markGoalAsCompleted(goalId)

        // Then: 正しく完了状態に更新される
        coVerify(exactly = 1) { mockGoalDao.getGoalById(goalId) }
        coVerify(exactly = 1) { mockGoalDao.updateGoal(match { it.isCompleted }) }
    }

    // Performance-focused tests to verify database-level filtering
    @Test
    fun `getActiveGoals_データベースレベルフィルタリングが使用されること`() = runTest {
        // Given: 最適化されたDAOメソッドのモック
        every { mockGoalDao.getActiveGoals() } returns flowOf(emptyList())

        // When: アクティブな目標を取得
        repository.getActiveGoals().first()

        // Then: 最適化されたgetActiveGoalsメソッドが呼ばれ、getAllGoalsは呼ばれない
        verify(exactly = 1) { mockGoalDao.getActiveGoals() }
        verify(exactly = 0) { mockGoalDao.getAllGoals() }
    }

    @Test
    fun `getCompletedGoals_データベースレベルフィルタリングが使用されること`() = runTest {
        // Given: 最適化されたDAOメソッドのモック
        every { mockGoalDao.getCompletedGoals() } returns flowOf(emptyList())

        // When: 完了した目標を取得
        repository.getCompletedGoals().first()

        // Then: 最適化されたgetCompletedGoalsメソッドが呼ばれ、getAllGoalsは呼ばれない
        verify(exactly = 1) { mockGoalDao.getCompletedGoals() }
        verify(exactly = 0) { mockGoalDao.getAllGoals() }
    }

    @Test
    fun `getActiveGoals_大量データでも効率的に処理されること`() = runTest {
        // Given: 大量のアクティブ目標エンティティ（データベースレベルでフィルタリング済み）
        val largeActiveGoalList = (1..1000).map { id ->
            createTestGoalEntity(id = id.toLong(), isCompleted = false)
        }
        
        every { mockGoalDao.getActiveGoals() } returns flowOf(largeActiveGoalList)

        // When: アクティブな目標を取得
        val result = repository.getActiveGoals().first()

        // Then: 全てのデータが正しく処理され、メモリ内フィルタリングは行われない
        verify(exactly = 1) { mockGoalDao.getActiveGoals() }
        verify(exactly = 0) { mockGoalDao.getAllGoals() }
        assertEquals(1000, result.size)
        assertTrue(result.all { !it.isCompleted })
    }

    @Test
    fun `getCompletedGoals_大量データでも効率的に処理されること`() = runTest {
        // Given: 大量の完了目標エンティティ（データベースレベルでフィルタリング済み）
        val largeCompletedGoalList = (1..500).map { id ->
            createTestGoalEntity(id = id.toLong(), isCompleted = true)
        }
        
        every { mockGoalDao.getCompletedGoals() } returns flowOf(largeCompletedGoalList)

        // When: 完了した目標を取得
        val result = repository.getCompletedGoals().first()

        // Then: 全てのデータが正しく処理され、メモリ内フィルタリングは行われない
        verify(exactly = 1) { mockGoalDao.getCompletedGoals() }
        verify(exactly = 0) { mockGoalDao.getAllGoals() }
        assertEquals(500, result.size)
        assertTrue(result.all { it.isCompleted })
    }

    // Additional comprehensive tests for optimized implementation
    @Test
    fun `getActiveGoals_Flow動作が正しく機能すること`() = runTest {
        // Given: アクティブな目標エンティティのFlow
        val activeGoalEntities = listOf(
            createTestGoalEntity(id = 1L, isCompleted = false),
            createTestGoalEntity(id = 2L, isCompleted = false)
        )
        
        every { mockGoalDao.getActiveGoals() } returns flowOf(activeGoalEntities)

        // When: Flowを複数回収集
        val result1 = repository.getActiveGoals().first()
        val result2 = repository.getActiveGoals().first()

        // Then: 両方とも同じ結果が返され、DAOメソッドが適切に呼ばれる
        assertEquals(2, result1.size)
        assertEquals(2, result2.size)
        assertEquals(result1[0].id, result2[0].id)
        verify(atLeast = 2) { mockGoalDao.getActiveGoals() }
    }

    @Test
    fun `getCompletedGoals_Flow動作が正しく機能すること`() = runTest {
        // Given: 完了した目標エンティティのFlow
        val completedGoalEntities = listOf(
            createTestGoalEntity(id = 3L, isCompleted = true),
            createTestGoalEntity(id = 4L, isCompleted = true)
        )
        
        every { mockGoalDao.getCompletedGoals() } returns flowOf(completedGoalEntities)

        // When: Flowを複数回収集
        val result1 = repository.getCompletedGoals().first()
        val result2 = repository.getCompletedGoals().first()

        // Then: 両方とも同じ結果が返され、DAOメソッドが適切に呼ばれる
        assertEquals(2, result1.size)
        assertEquals(2, result2.size)
        assertEquals(result1[0].id, result2[0].id)
        verify(atLeast = 2) { mockGoalDao.getCompletedGoals() }
    }

    @Test
    fun `getActiveGoals_異なる目標タイプが正しく処理されること`() = runTest {
        // Given: 異なるタイプのアクティブ目標エンティティ
        val activeGoalEntities = listOf(
            createTestGoalEntity(id = 1L, isCompleted = false).copy(type = GoalType.STRENGTH),
            createTestGoalEntity(id = 2L, isCompleted = false).copy(type = GoalType.WEIGHT_LOSS),
            createTestGoalEntity(id = 3L, isCompleted = false).copy(type = GoalType.ENDURANCE)
        )
        
        every { mockGoalDao.getActiveGoals() } returns flowOf(activeGoalEntities)

        // When: アクティブな目標を取得
        val result = repository.getActiveGoals().first()

        // Then: 全ての目標タイプが正しく処理される
        assertEquals(3, result.size)
        assertEquals(GoalType.STRENGTH, result[0].type)
        assertEquals(GoalType.WEIGHT_LOSS, result[1].type)
        assertEquals(GoalType.ENDURANCE, result[2].type)
        assertTrue(result.all { !it.isCompleted })
    }

    @Test
    fun `getCompletedGoals_異なる目標タイプが正しく処理されること`() = runTest {
        // Given: 異なるタイプの完了目標エンティティ
        val completedGoalEntities = listOf(
            createTestGoalEntity(id = 4L, isCompleted = true).copy(type = GoalType.STRENGTH),
            createTestGoalEntity(id = 5L, isCompleted = true).copy(type = GoalType.WEIGHT_LOSS)
        )
        
        every { mockGoalDao.getCompletedGoals() } returns flowOf(completedGoalEntities)

        // When: 完了した目標を取得
        val result = repository.getCompletedGoals().first()

        // Then: 全ての目標タイプが正しく処理される
        assertEquals(2, result.size)
        assertEquals(GoalType.STRENGTH, result[0].type)
        assertEquals(GoalType.WEIGHT_LOSS, result[1].type)
        assertTrue(result.all { it.isCompleted })
    }

    @Test
    fun `optimized_methods_do_not_interfere_with_existing_functionality`() = runTest {
        // Given: 全ての目標、アクティブ目標、完了目標のモック
        val allGoals = listOf(
            createTestGoalEntity(id = 1L, isCompleted = false),
            createTestGoalEntity(id = 2L, isCompleted = true),
            createTestGoalEntity(id = 3L, isCompleted = false)
        )
        val activeGoals = allGoals.filter { !it.isCompleted }
        val completedGoals = allGoals.filter { it.isCompleted }
        
        every { mockGoalDao.getAllGoals() } returns flowOf(allGoals)
        every { mockGoalDao.getActiveGoals() } returns flowOf(activeGoals)
        every { mockGoalDao.getCompletedGoals() } returns flowOf(completedGoals)

        // When: 各メソッドを呼び出し
        val allResult = repository.getAllGoals().first()
        val activeResult = repository.getActiveGoals().first()
        val completedResult = repository.getCompletedGoals().first()

        // Then: 各メソッドが独立して正しく動作する
        assertEquals(3, allResult.size)
        assertEquals(2, activeResult.size)
        assertEquals(1, completedResult.size)
        
        // 最適化されたメソッドが既存機能に干渉しない
        verify(exactly = 1) { mockGoalDao.getAllGoals() }
        verify(exactly = 1) { mockGoalDao.getActiveGoals() }
        verify(exactly = 1) { mockGoalDao.getCompletedGoals() }
    }

    // Goal Progress related tests (existing functionality)
    @Test
    fun `getProgressByGoalId_目標の進捗記録が正しく取得されること`() = runTest {
        // Given: 目標の進捗記録エンティティが存在
        val goalId = 1L
        val progressEntities = listOf(
            createTestGoalProgressEntity(goalId = goalId, value = 10.0),
            createTestGoalProgressEntity(goalId = goalId, value = 20.0)
        )
        
        every { mockGoalProgressDao.getProgressByGoalIdFlow(goalId) } returns flowOf(progressEntities)

        // When: 目標の進捗記録を取得
        val result = repository.getProgressByGoalId(goalId).first()

        // Then: 正しく進捗記録が返される
        verify(exactly = 1) { mockGoalProgressDao.getProgressByGoalIdFlow(goalId) }
        assertEquals(2, result.size)
        assertEquals(10.0, result[0].progressValue, 0.001)
        assertEquals(20.0, result[1].progressValue, 0.001)
    }

    @Test
    fun `saveProgressRecord_進捗記録が正常に保存されること`() = runTest {
        // Given: 新しい進捗記録
        val progressRecord = createTestGoalProgressRecord()
        val expectedId = 456L
        
        coEvery { mockGoalProgressDao.insertProgress(any()) } returns expectedId

        // When: 進捗記録を保存
        val result = repository.saveProgressRecord(progressRecord)

        // Then: 正しいIDが返され、DAOが呼ばれる
        assertEquals(expectedId, result)
        coVerify(exactly = 1) { mockGoalProgressDao.insertProgress(any()) }
    }

    // Helper methods for creating test data
    private fun createTestGoalEntity(
        id: Long = 1L,
        isCompleted: Boolean = false,
        currentValue: Double = 50.0
    ): GoalEntity {
        return GoalEntity(
            id = id,
            type = GoalType.STRENGTH,
            title = "テスト目標",
            description = "テスト用の目標です",
            targetValue = 100.0,
            currentValue = currentValue,
            unit = "kg",
            deadline = LocalDate(2024, 12, 31),
            isCompleted = isCompleted,
            createdAt = LocalDate(2024, 1, 1),
            updatedAt = LocalDate(2024, 6, 15)
        )
    }

    private fun createTestGoal(): Goal {
        return Goal(
            id = 0L,
            type = GoalType.STRENGTH,
            title = "テスト目標",
            description = "テスト用の目標です",
            targetValue = 100.0,
            currentValue = 50.0,
            unit = "kg",
            deadline = LocalDate(2024, 12, 31),
            isCompleted = false,
            createdAt = LocalDate(2024, 1, 1),
            updatedAt = LocalDate(2024, 6, 15)
        )
    }

    private fun createTestGoalProgressEntity(
        goalId: Long = 1L,
        value: Double = 10.0
    ): GoalProgressEntity {
        return GoalProgressEntity(
            id = 0L,
            goalId = goalId,
            recordDate = LocalDate(2024, 6, 15),
            progressValue = value,
            notes = "テスト進捗",
            createdAt = LocalDate(2024, 6, 15)
        )
    }

    private fun createTestGoalProgressRecord(): GoalProgressRecord {
        return GoalProgressRecord(
            id = 0L,
            goalId = 1L,
            recordDate = LocalDate(2024, 6, 15),
            progressValue = 10.0,
            notes = "テスト進捗",
            createdAt = LocalDate(2024, 6, 15)
        )
    }
}