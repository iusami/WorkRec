package com.workrec.presentation.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.workrec.domain.entities.*
import com.workrec.presentation.ui.theme.WorkRecTheme
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * GoalCardのUIテスト
 */
@RunWith(AndroidJUnit4::class)
class GoalCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun goalCard_目標情報が正しく表示されること() {
        // Given: テスト用の目標
        val goal = createTestGoal()

        composeTestRule.setContent {
            WorkRecTheme {
                GoalCard(
                    goal = goal,
                    onClick = { }
                )
            }
        }

        // Then: 目標情報が正しく表示される
        composeTestRule
            .onNodeWithText("ベンチプレス100kg")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("重量目標")
            .assertIsDisplayed()
            
        composeTestRule
            .onNodeWithText("50.0 / 100.0 kg")
            .assertIsDisplayed()
            
        composeTestRule
            .onNodeWithText("50%")
            .assertIsDisplayed()
            
        composeTestRule
            .onNodeWithText("目標説明テスト")
            .assertIsDisplayed()
            
        composeTestRule
            .onNodeWithText("期限: 2024-12-31")
            .assertIsDisplayed()
    }

    @Test
    fun goalCard_完了した目標で完了バッジが表示されること() {
        // Given: 完了した目標
        val completedGoal = createTestGoal().copy(
            isCompleted = true,
            currentValue = 100.0
        )

        composeTestRule.setContent {
            WorkRecTheme {
                GoalCard(
                    goal = completedGoal,
                    onClick = { }
                )
            }
        }

        // Then: 完了バッジが表示される
        composeTestRule
            .onNodeWithText("完了")
            .assertIsDisplayed()
            
        // 進捗率は100%
        composeTestRule
            .onNodeWithText("100%")
            .assertIsDisplayed()
    }

    @Test
    fun goalCard_進捗率が正しく表示されること() {
        // Given: 75%進捗の目標
        val goal = createTestGoal().copy(
            targetValue = 100.0,
            currentValue = 75.0
        )

        composeTestRule.setContent {
            WorkRecTheme {
                GoalCard(
                    goal = goal,
                    onClick = { }
                )
            }
        }

        // Then: 正しい進捗率が表示される
        composeTestRule
            .onNodeWithText("75.0 / 100.0 kg")
            .assertIsDisplayed()
            
        composeTestRule
            .onNodeWithText("75%")
            .assertIsDisplayed()
    }

    @Test
    fun goalCard_説明がない目標で説明が表示されないこと() {
        // Given: 説明がない目標
        val goalWithoutDescription = createTestGoal().copy(description = null)

        composeTestRule.setContent {
            WorkRecTheme {
                GoalCard(
                    goal = goalWithoutDescription,
                    onClick = { }
                )
            }
        }

        // Then: タイトルは表示されるが説明は表示されない
        composeTestRule
            .onNodeWithText("ベンチプレス100kg")
            .assertIsDisplayed()
            
        composeTestRule
            .onNodeWithText("目標説明テスト")
            .assertDoesNotExist()
    }

    @Test
    fun goalCard_期限がない目標で期限が表示されないこと() {
        // Given: 期限がない目標
        val goalWithoutDeadline = createTestGoal().copy(deadline = null)

        composeTestRule.setContent {
            WorkRecTheme {
                GoalCard(
                    goal = goalWithoutDeadline,
                    onClick = { }
                )
            }
        }

        // Then: タイトルは表示されるが期限は表示されない
        composeTestRule
            .onNodeWithText("ベンチプレス100kg")
            .assertIsDisplayed()
            
        composeTestRule
            .onNodeWithText("期限:")
            .assertDoesNotExist()
    }

    @Test
    fun goalCard_クリック可能であること() {
        // Given: テスト用の目標
        val goal = createTestGoal()
        var clickCount = 0

        composeTestRule.setContent {
            WorkRecTheme {
                GoalCard(
                    goal = goal,
                    onClick = { clickCount++ }
                )
            }
        }

        // When: カードをクリック
        composeTestRule
            .onNodeWithText("ベンチプレス100kg")
            .performClick()

        // Then: クリックイベントが発生する
        assert(clickCount == 1)
    }

    // テストヘルパーメソッド
    private fun createTestGoal(): Goal {
        return Goal(
            id = 1L,
            type = GoalType.WEIGHT,
            title = "ベンチプレス100kg",
            description = "目標説明テスト",
            targetValue = 100.0,
            currentValue = 50.0,
            unit = "kg",
            deadline = LocalDate(2024, 12, 31),
            isCompleted = false,
            createdAt = LocalDate(2024, 1, 1)
        )
    }
}