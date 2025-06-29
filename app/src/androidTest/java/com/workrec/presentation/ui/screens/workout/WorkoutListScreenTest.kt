package com.workrec.presentation.ui.screens.workout

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
 * WorkoutListScreenのUIテスト
 */
@RunWith(AndroidJUnit4::class)
class WorkoutListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun workoutListScreen_空の状態で適切なメッセージが表示されること() {
        // Given: 空のワークアウトリスト
        composeTestRule.setContent {
            WorkRecTheme {
                // TODO: ViewModelをモックしたテスト用のコンポーネントを作成
                // EmptyWorkoutStateの直接テストとして実装
            }
        }

        // Then: 空の状態メッセージが表示される
        composeTestRule
            .onNodeWithText("まだワークアウトが記録されていません")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("最初のワークアウトを追加しましょう！")
            .assertIsDisplayed()
    }

    @Test
    fun workoutCard_ワークアウト情報が正しく表示されること() {
        // Given: テスト用のワークアウト
        val workout = createTestWorkout()

        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.WorkoutCard(
                    workout = workout,
                    onClick = { },
                    onDelete = { }
                )
            }
        }

        // Then: ワークアウト情報が正しく表示される
        composeTestRule
            .onNodeWithText("2024年01月01日")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("2種目・3セット")
            .assertIsDisplayed()
            
        composeTestRule
            .onNodeWithText("総ボリューム: 1640.0kg")
            .assertIsDisplayed()
            
        composeTestRule
            .onNodeWithText("ベンチプレス, スクワット")
            .assertIsDisplayed()
    }

    @Test
    fun workoutCard_削除ボタンタップで確認ダイアログが表示されること() {
        // Given: テスト用のワークアウト
        val workout = createTestWorkout()

        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.WorkoutCard(
                    workout = workout,
                    onClick = { },
                    onDelete = { }
                )
            }
        }

        // When: 削除ボタンをタップ
        composeTestRule
            .onNodeWithContentDescription("削除")
            .performClick()

        // Then: 確認ダイアログが表示される
        composeTestRule
            .onNodeWithText("ワークアウトを削除")
            .assertIsDisplayed()
            
        composeTestRule
            .onNodeWithText("このワークアウトを削除しますか？この操作は取り消せません。")
            .assertIsDisplayed()
            
        composeTestRule
            .onNodeWithText("削除")
            .assertIsDisplayed()
            
        composeTestRule
            .onNodeWithText("キャンセル")
            .assertIsDisplayed()
    }

    @Test
    fun workoutCard_削除確認ダイアログでキャンセルできること() {
        // Given: テスト用のワークアウト
        val workout = createTestWorkout()

        composeTestRule.setContent {
            WorkRecTheme {
                com.workrec.presentation.ui.components.WorkoutCard(
                    workout = workout,
                    onClick = { },
                    onDelete = { }
                )
            }
        }

        // When: 削除ボタンをタップしてキャンセル
        composeTestRule
            .onNodeWithContentDescription("削除")
            .performClick()
            
        composeTestRule
            .onNodeWithText("キャンセル")
            .performClick()

        // Then: ダイアログが閉じられる
        composeTestRule
            .onNodeWithText("ワークアウトを削除")
            .assertDoesNotExist()
    }

    // テストヘルパーメソッド
    private fun createTestWorkout(): Workout {
        val benchPressSets = listOf(
            ExerciseSet(reps = 10, weight = 60.0),
            ExerciseSet(reps = 8, weight = 70.0)
        )
        val squatSets = listOf(
            ExerciseSet(reps = 12, weight = 80.0)
        )
        
        val exercises = listOf(
            Exercise(
                id = 1L,
                name = "ベンチプレス",
                sets = benchPressSets,
                category = ExerciseCategory.CHEST
            ),
            Exercise(
                id = 2L,
                name = "スクワット",
                sets = squatSets,
                category = ExerciseCategory.LEGS
            )
        )
        
        return Workout(
            id = 1L,
            date = LocalDate(2024, 1, 1),
            exercises = exercises,
            notes = "テストワークアウト"
        )
    }
}