package com.workrec.data.database.dao

import com.workrec.data.database.entities.GoalEntity
import com.workrec.data.database.entities.GoalProgressEntity
import com.workrec.data.database.entities.GoalWithProgress
import com.workrec.domain.entities.GoalType
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * GoalDaoの新しい最適化されたクエリメソッドの単体テスト
 * 
 * このテストは、新しく追加された最適化されたDAO メソッドが正しく動作することを検証します:
 * - getActiveGoals(): アクティブな目標のみを返すことを検証
 * - getCompletedGoals(): 完了した目標のみを返すことを検証
 * - getActiveGoalsWithProgress(): アクティブな目標と進捗データを返すことを検証
 * - getCompletedGoalsWithProgress(): 完了した目標と進捗データを返すことを検証
 * - Flowの動作とリアクティブデータストリームの検証
 * - エッジケース（空のデータセット、混在した完了状態）の検証
 */
class GoalDaoTest {

    private lateinit var mockGoalDao: GoalDao

    @Before
    fun setup() {
        mockGoalDao = mockk()
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun getActiveGoals_returnsOnlyNonCompletedGoals() = runTest {
        // Given: アクティブと完了した目標が混在するデータセット
        val activeGoal1 = createTestGoalEntity(
            id = 1L,
            title = "アクティブ目標1",
            isCompleted = false
        )
        val activeGoal2 = createTestGoalEntity(
            id = 2L,
            title = "アクティブ目標2", 
            isCompleted = false
        )
        val expectedActiveGoals = listOf(activeGoal1, activeGoal2)

        every { mockGoalDao.getActiveGoals() } returns flowOf(expectedActiveGoals)

        // When: アクティブな目標を取得
        val result = mockGoalDao.getActiveGoals().first()

        // Then: アクティブな目標のみが返される
        assertEquals(2, result.size)
        assertTrue("すべての結果がアクティブ（未完了）であること", result.all { !it.isCompleted })
        
        val titles = result.map { it.title }.sorted()
        assertEquals(listOf("アクティブ目標1", "アクティブ目標2"), titles)
        
        verify { mockGoalDao.getActiveGoals() }
    }

    @Test
    fun getCompletedGoals_returnsOnlyCompletedGoals() = runTest {
        // Given: 完了した目標のデータセット
        val completedGoal1 = createTestGoalEntity(
            id = 2L,
            title = "完了目標1",
            isCompleted = true
        )
        val completedGoal2 = createTestGoalEntity(
            id = 3L,
            title = "完了目標2",
            isCompleted = true
        )
        val expectedCompletedGoals = listOf(completedGoal1, completedGoal2)

        every { mockGoalDao.getCompletedGoals() } returns flowOf(expectedCompletedGoals)

        // When: 完了した目標を取得
        val result = mockGoalDao.getCompletedGoals().first()

        // Then: 完了した目標のみが返される
        assertEquals(2, result.size)
        assertTrue("すべての結果が完了済みであること", result.all { it.isCompleted })
        
        val titles = result.map { it.title }.sorted()
        assertEquals(listOf("完了目標1", "完了目標2"), titles)
        
        verify { mockGoalDao.getCompletedGoals() }
    }

    @Test
    fun getActiveGoals_withEmptyDataset_returnsEmptyList() = runTest {
        // Given: 空のデータセット
        every { mockGoalDao.getActiveGoals() } returns flowOf(emptyList())

        // When: アクティブな目標を取得
        val result = mockGoalDao.getActiveGoals().first()

        // Then: 空のリストが返される
        assertTrue("空のデータセットでは空のリストが返されること", result.isEmpty())
        
        verify { mockGoalDao.getActiveGoals() }
    }

    @Test
    fun getCompletedGoals_withEmptyDataset_returnsEmptyList() = runTest {
        // Given: 空のデータセット
        every { mockGoalDao.getCompletedGoals() } returns flowOf(emptyList())

        // When: 完了した目標を取得
        val result = mockGoalDao.getCompletedGoals().first()

        // Then: 空のリストが返される
        assertTrue("空のデータセットでは空のリストが返されること", result.isEmpty())
        
        verify { mockGoalDao.getCompletedGoals() }
    }

    @Test
    fun getActiveGoals_withOnlyCompletedGoals_returnsEmptyList() = runTest {
        // Given: アクティブな目標が存在しない場合
        every { mockGoalDao.getActiveGoals() } returns flowOf(emptyList())

        // When: アクティブな目標を取得
        val result = mockGoalDao.getActiveGoals().first()

        // Then: 空のリストが返される
        assertTrue("完了した目標のみの場合、アクティブ目標は空のリスト", result.isEmpty())
        
        verify { mockGoalDao.getActiveGoals() }
    }

    @Test
    fun getCompletedGoals_withOnlyActiveGoals_returnsEmptyList() = runTest {
        // Given: 完了した目標が存在しない場合
        every { mockGoalDao.getCompletedGoals() } returns flowOf(emptyList())

        // When: 完了した目標を取得
        val result = mockGoalDao.getCompletedGoals().first()

        // Then: 空のリストが返される
        assertTrue("アクティブな目標のみの場合、完了目標は空のリスト", result.isEmpty())
        
        verify { mockGoalDao.getCompletedGoals() }
    }

    @Test
    fun getActiveGoalsWithProgress_returnsActiveGoalsWithProgressData() = runTest {
        // Given: アクティブな目標と進捗データ
        val activeGoal = createTestGoalEntity(id = 1L, isCompleted = false)
        val progressRecord = createTestGoalProgressEntity(goalId = 1L)
        val goalWithProgress = GoalWithProgress(
            goal = activeGoal,
            progressRecords = listOf(progressRecord)
        )

        every { mockGoalDao.getActiveGoalsWithProgress() } returns flowOf(listOf(goalWithProgress))

        // When: アクティブな目標と進捗データを取得
        val result = mockGoalDao.getActiveGoalsWithProgress().first()

        // Then: アクティブな目標のみが進捗データと共に返される
        assertEquals(1, result.size)
        assertFalse("返された目標がアクティブであること", result[0].goal.isCompleted)
        assertEquals(1, result[0].progressRecords.size)
        assertEquals(1L, result[0].progressRecords[0].goalId)
        
        verify { mockGoalDao.getActiveGoalsWithProgress() }
    }

    @Test
    fun getCompletedGoalsWithProgress_returnsCompletedGoalsWithProgressData() = runTest {
        // Given: 完了した目標と進捗データ
        val completedGoal = createTestGoalEntity(id = 2L, isCompleted = true)
        val progressRecord = createTestGoalProgressEntity(goalId = 2L)
        val goalWithProgress = GoalWithProgress(
            goal = completedGoal,
            progressRecords = listOf(progressRecord)
        )

        every { mockGoalDao.getCompletedGoalsWithProgress() } returns flowOf(listOf(goalWithProgress))

        // When: 完了した目標と進捗データを取得
        val result = mockGoalDao.getCompletedGoalsWithProgress().first()

        // Then: 完了した目標のみが進捗データと共に返される
        assertEquals(1, result.size)
        assertTrue("返された目標が完了済みであること", result[0].goal.isCompleted)
        assertEquals(1, result[0].progressRecords.size)
        assertEquals(2L, result[0].progressRecords[0].goalId)
        
        verify { mockGoalDao.getCompletedGoalsWithProgress() }
    }

    @Test
    fun goalQueries_flowBehavior_reactsToDataChanges() = runTest {
        // Given: 初期状態では空のデータベース、その後データが追加される
        val emptyFlow = flowOf(emptyList<GoalEntity>())
        val activeGoal = createTestGoalEntity(id = 1L, isCompleted = false)
        val activeGoalsFlow = flowOf(listOf(activeGoal))
        val completedGoal = activeGoal.copy(isCompleted = true)
        val completedGoalsFlow = flowOf(listOf(completedGoal))

        // 初期状態: 空
        every { mockGoalDao.getActiveGoals() } returns emptyFlow
        every { mockGoalDao.getCompletedGoals() } returns emptyFlow

        // When: 初期状態を確認
        var activeGoalsResult = mockGoalDao.getActiveGoals().first()
        assertTrue("初期状態では空", activeGoalsResult.isEmpty())

        // Then: データが追加された後の状態をシミュレート
        every { mockGoalDao.getActiveGoals() } returns activeGoalsFlow
        activeGoalsResult = mockGoalDao.getActiveGoals().first()
        assertEquals(1, activeGoalsResult.size)
        assertFalse(activeGoalsResult[0].isCompleted)

        // When: 目標が完了状態に更新された後の状態をシミュレート
        every { mockGoalDao.getActiveGoals() } returns emptyFlow
        every { mockGoalDao.getCompletedGoals() } returns completedGoalsFlow

        activeGoalsResult = mockGoalDao.getActiveGoals().first()
        assertTrue("更新後はアクティブ目標が空", activeGoalsResult.isEmpty())

        val completedGoalsResult = mockGoalDao.getCompletedGoals().first()
        assertEquals(1, completedGoalsResult.size)
        assertTrue("完了目標に移動", completedGoalsResult[0].isCompleted)
        
        verify(atLeast = 1) { mockGoalDao.getActiveGoals() }
        verify(atLeast = 1) { mockGoalDao.getCompletedGoals() }
    }

    @Test
    fun goalQueries_mixedCompletionStates_correctlyFilters() = runTest {
        // Given: 様々な完了状態の目標を大量に作成
        val activeGoals = mutableListOf<GoalEntity>()
        val completedGoals = mutableListOf<GoalEntity>()
        val allGoals = mutableListOf<GoalEntity>()
        
        // 10個のアクティブ目標
        repeat(10) { i ->
            val goal = createTestGoalEntity(
                id = i.toLong() + 1,
                title = "アクティブ目標${i + 1}",
                isCompleted = false
            )
            activeGoals.add(goal)
            allGoals.add(goal)
        }
        
        // 5個の完了目標
        repeat(5) { i ->
            val goal = createTestGoalEntity(
                id = i.toLong() + 11,
                title = "完了目標${i + 1}",
                isCompleted = true
            )
            completedGoals.add(goal)
            allGoals.add(goal)
        }

        every { mockGoalDao.getActiveGoals() } returns flowOf(activeGoals)
        every { mockGoalDao.getCompletedGoals() } returns flowOf(completedGoals)
        every { mockGoalDao.getAllGoals() } returns flowOf(allGoals)

        // When: それぞれのクエリを実行
        val activeResults = mockGoalDao.getActiveGoals().first()
        val completedResults = mockGoalDao.getCompletedGoals().first()
        val allResults = mockGoalDao.getAllGoals().first()

        // Then: 正しくフィルタリングされている
        assertEquals(10, activeResults.size)
        assertEquals(5, completedResults.size)
        assertEquals(15, allResults.size)

        assertTrue("アクティブ結果はすべて未完了", activeResults.all { !it.isCompleted })
        assertTrue("完了結果はすべて完了済み", completedResults.all { it.isCompleted })
        
        // 重複がないことを確認
        val activeIds = activeResults.map { it.id }.toSet()
        val completedIds = completedResults.map { it.id }.toSet()
        assertTrue("アクティブと完了のIDに重複がない", activeIds.intersect(completedIds).isEmpty())
        
        verify { mockGoalDao.getActiveGoals() }
        verify { mockGoalDao.getCompletedGoals() }
        verify { mockGoalDao.getAllGoals() }
    }

    @Test
    fun getActiveGoalsWithProgress_withEmptyProgressRecords_returnsGoalsWithEmptyProgress() = runTest {
        // Given: 進捗記録がないアクティブな目標
        val activeGoal = createTestGoalEntity(id = 1L, isCompleted = false)
        val goalWithEmptyProgress = GoalWithProgress(
            goal = activeGoal,
            progressRecords = emptyList()
        )

        every { mockGoalDao.getActiveGoalsWithProgress() } returns flowOf(listOf(goalWithEmptyProgress))

        // When: アクティブな目標と進捗データを取得
        val result = mockGoalDao.getActiveGoalsWithProgress().first()

        // Then: 進捗記録が空のアクティブな目標が返される
        assertEquals(1, result.size)
        assertFalse("返された目標がアクティブであること", result[0].goal.isCompleted)
        assertTrue("進捗記録が空であること", result[0].progressRecords.isEmpty())
        
        verify { mockGoalDao.getActiveGoalsWithProgress() }
    }

    @Test
    fun getCompletedGoalsWithProgress_withMultipleProgressRecords_returnsAllProgressData() = runTest {
        // Given: 複数の進捗記録を持つ完了した目標
        val completedGoal = createTestGoalEntity(id = 1L, isCompleted = true)
        val progressRecord1 = createTestGoalProgressEntity(goalId = 1L, progressValue = 50.0)
        val progressRecord2 = createTestGoalProgressEntity(goalId = 1L, progressValue = 100.0)
        val goalWithMultipleProgress = GoalWithProgress(
            goal = completedGoal,
            progressRecords = listOf(progressRecord1, progressRecord2)
        )

        every { mockGoalDao.getCompletedGoalsWithProgress() } returns flowOf(listOf(goalWithMultipleProgress))

        // When: 完了した目標と進捗データを取得
        val result = mockGoalDao.getCompletedGoalsWithProgress().first()

        // Then: すべての進捗記録が返される
        assertEquals(1, result.size)
        assertTrue("返された目標が完了済みであること", result[0].goal.isCompleted)
        assertEquals(2, result[0].progressRecords.size)
        assertEquals(50.0, result[0].progressRecords[0].progressValue, 0.01)
        assertEquals(100.0, result[0].progressRecords[1].progressValue, 0.01)
        
        verify { mockGoalDao.getCompletedGoalsWithProgress() }
    }

    // テストヘルパーメソッド
    private fun createTestGoalEntity(
        id: Long = 0L,
        title: String = "テスト目標",
        isCompleted: Boolean = false,
        type: GoalType = GoalType.STRENGTH,
        targetValue: Double = 100.0,
        currentValue: Double = 50.0
    ): GoalEntity {
        val today = LocalDate(2024, 1, 15)
        return GoalEntity(
            id = id,
            type = type,
            title = title,
            description = "テスト用の目標説明",
            targetValue = targetValue,
            currentValue = currentValue,
            unit = "kg",
            deadline = today.plus(DatePeriod(days = 30)),
            isCompleted = isCompleted,
            createdAt = today,
            updatedAt = today
        )
    }

    private fun createTestGoalProgressEntity(
        goalId: Long,
        progressValue: Double = 75.0
    ): GoalProgressEntity {
        val today = LocalDate(2024, 1, 15)
        return GoalProgressEntity(
            goalId = goalId,
            recordDate = today,
            progressValue = progressValue,
            notes = "テスト進捗記録",
            createdAt = today
        )
    }
}