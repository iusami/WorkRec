package com.workrec.domain.entities

import kotlinx.datetime.LocalDate
import org.junit.Test
import org.junit.Assert.*

/**
 * Goalエンティティの単体テスト
 */
class GoalTest {

    @Test
    fun `progressPercentageが正しく計算されること`() {
        // Given: 進捗50%の目標
        val goal = createTestGoal(
            targetValue = 100.0,
            currentValue = 50.0
        )

        // When: 進捗率を計算
        val progress = goal.progressPercentage

        // Then: 正しい値が計算される
        assertEquals(0.5f, progress, 0.01f)
    }

    @Test
    fun `progressPercentageが100%を超えないこと`() {
        // Given: 目標を超過達成した場合
        val goal = createTestGoal(
            targetValue = 100.0,
            currentValue = 150.0
        )

        // When: 進捗率を計算
        val progress = goal.progressPercentage

        // Then: 100%でキャップされる
        assertEquals(1.0f, progress, 0.01f)
    }

    @Test
    fun `remainingValueが正しく計算されること`() {
        // Given: 目標の一部を達成した場合
        val goal = createTestGoal(
            targetValue = 100.0,
            currentValue = 30.0
        )

        // When: 残り必要値を計算
        val remaining = goal.remainingValue

        // Then: 正しい値が計算される
        assertEquals(70.0, remaining, 0.01)
    }

    @Test
    fun `remainingValueが負の値にならないこと`() {
        // Given: 目標を超過達成した場合
        val goal = createTestGoal(
            targetValue = 100.0,
            currentValue = 120.0
        )

        // When: 残り必要値を計算
        val remaining = goal.remainingValue

        // Then: 0以上の値になる
        assertEquals(0.0, remaining, 0.01)
    }

    @Test
    fun `isAchieved_目標達成時にtrueを返すこと`() {
        // Given: 目標達成した場合
        val goal = createTestGoal(
            targetValue = 100.0,
            currentValue = 100.0
        )

        // When & Then: 達成判定がtrue
        assertTrue(goal.isAchieved)
    }

    @Test
    fun `isAchieved_目標未達成時にfalseを返すこと`() {
        // Given: 目標未達成の場合
        val goal = createTestGoal(
            targetValue = 100.0,
            currentValue = 80.0
        )

        // When & Then: 達成判定がfalse
        assertFalse(goal.isAchieved)
    }

    @Test
    fun `isAchieved_目標超過達成時にtrueを返すこと`() {
        // Given: 目標を超過達成した場合
        val goal = createTestGoal(
            targetValue = 100.0,
            currentValue = 120.0
        )

        // When & Then: 達成判定がtrue
        assertTrue(goal.isAchieved)
    }

    @Test
    fun `isOverdue_期限切れの場合にtrueを返すこと`() {
        // Given: 期限切れの目標
        val goal = createTestGoal(
            deadline = LocalDate(2024, 1, 1)
        )
        val currentDate = LocalDate(2024, 1, 2)

        // When & Then: 期限切れ判定がtrue
        assertTrue(goal.isOverdue(currentDate))
    }

    @Test
    fun `isOverdue_期限内の場合にfalseを返すこと`() {
        // Given: 期限内の目標
        val goal = createTestGoal(
            deadline = LocalDate(2024, 1, 10)
        )
        val currentDate = LocalDate(2024, 1, 5)

        // When & Then: 期限切れ判定がfalse
        assertFalse(goal.isOverdue(currentDate))
    }

    @Test
    fun `isOverdue_期限が設定されていない場合にfalseを返すこと`() {
        // Given: 期限が設定されていない目標
        val goal = createTestGoal(deadline = null)
        val currentDate = LocalDate(2024, 1, 5)

        // When & Then: 期限切れ判定がfalse
        assertFalse(goal.isOverdue(currentDate))
    }

    @Test
    fun `isOverdue_期限当日の場合にfalseを返すこと`() {
        // Given: 期限当日の目標
        val deadline = LocalDate(2024, 1, 1)
        val goal = createTestGoal(deadline = deadline)

        // When & Then: 期限切れ判定がfalse（当日はまだ期限内）
        assertFalse(goal.isOverdue(deadline))
    }

    // テストヘルパーメソッド
    private fun createTestGoal(
        id: Long = 1L,
        type: GoalType = GoalType.WEIGHT,
        title: String = "ベンチプレス100kg",
        description: String? = "テスト目標",
        targetValue: Double = 100.0,
        currentValue: Double = 0.0,
        unit: String = "kg",
        deadline: LocalDate? = LocalDate(2024, 12, 31),
        isCompleted: Boolean = false,
        createdAt: LocalDate = LocalDate(2024, 1, 1)
    ) = Goal(
        id = id,
        type = type,
        title = title,
        description = description,
        targetValue = targetValue,
        currentValue = currentValue,
        unit = unit,
        deadline = deadline,
        isCompleted = isCompleted,
        createdAt = createdAt
    )
}