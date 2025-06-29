package com.workrec.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.workrec.domain.entities.*
import com.workrec.domain.usecase.workout.AddWorkoutUseCase
import com.workrec.domain.usecase.workout.DeleteWorkoutUseCase
import com.workrec.domain.usecase.workout.GetWorkoutHistoryUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

/**
 * WorkoutViewModelの単体テスト
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockAddWorkoutUseCase: AddWorkoutUseCase
    private lateinit var mockGetWorkoutHistoryUseCase: GetWorkoutHistoryUseCase
    private lateinit var mockDeleteWorkoutUseCase: DeleteWorkoutUseCase
    private lateinit var viewModel: WorkoutViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        mockAddWorkoutUseCase = mockk()
        mockGetWorkoutHistoryUseCase = mockk()
        mockDeleteWorkoutUseCase = mockk()

        // GetWorkoutHistoryUseCaseのinvoke演算子をモック
        every { mockGetWorkoutHistoryUseCase() } returns flowOf(emptyList())

        viewModel = WorkoutViewModel(
            mockAddWorkoutUseCase,
            mockGetWorkoutHistoryUseCase,
            mockDeleteWorkoutUseCase
        )
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `addWorkout_成功時に適切な状態更新が行われること`() = runTest {
        // Given: 成功するワークアウト追加
        val workout = createTestWorkout()
        coEvery { mockAddWorkoutUseCase(workout) } returns Result.success(123L)

        // When: ワークアウトを追加
        viewModel.addWorkout(workout)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: UI状態が適切に更新される
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals("ワークアウトを保存しました", uiState.message)
        assertNull(uiState.errorMessage)
        
        coVerify { mockAddWorkoutUseCase(workout) }
    }

    @Test
    fun `addWorkout_失敗時にエラーメッセージが設定されること`() = runTest {
        // Given: 失敗するワークアウト追加
        val workout = createTestWorkout()
        val exception = IllegalArgumentException("エクササイズが必要です")
        coEvery { mockAddWorkoutUseCase(workout) } returns Result.failure(exception)

        // When: ワークアウトを追加
        viewModel.addWorkout(workout)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: エラーメッセージが設定される
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals("エクササイズが必要です", uiState.errorMessage)
        assertNull(uiState.message)
        
        coVerify { mockAddWorkoutUseCase(workout) }
    }

    @Test
    fun `addWorkout_ロード中状態が適切に管理されること`() = runTest {
        // Given: 長時間かかるワークアウト追加
        val workout = createTestWorkout()
        coEvery { mockAddWorkoutUseCase(workout) } returns Result.success(123L)

        // When: ワークアウト追加を開始
        viewModel.addWorkout(workout)

        // Then: ロード中状態になる
        assertTrue(viewModel.uiState.value.isLoading)

        // ロード完了
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `deleteWorkout_成功時に適切な状態更新が行われること`() = runTest {
        // Given: 成功するワークアウト削除
        val workout = createTestWorkout()
        coEvery { mockDeleteWorkoutUseCase(workout) } returns Result.success(Unit)

        // When: ワークアウトを削除
        viewModel.deleteWorkout(workout)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: UI状態が適切に更新される
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals("ワークアウトを削除しました", uiState.message)
        assertNull(uiState.errorMessage)
        
        coVerify { mockDeleteWorkoutUseCase(workout) }
    }

    @Test
    fun `deleteWorkout_失敗時にエラーメッセージが設定されること`() = runTest {
        // Given: 失敗するワークアウト削除
        val workout = createTestWorkout()
        val exception = RuntimeException("削除エラー")
        coEvery { mockDeleteWorkoutUseCase(workout) } returns Result.failure(exception)

        // When: ワークアウトを削除
        viewModel.deleteWorkout(workout)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: エラーメッセージが設定される
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals("削除エラー", uiState.errorMessage)
        assertNull(uiState.message)
        
        coVerify { mockDeleteWorkoutUseCase(workout) }
    }

    @Test
    fun `getWorkoutsByDate_成功時にselectedDateWorkoutsが更新されること`() = runTest {
        // Given: 特定の日付のワークアウト
        val date = LocalDate(2024, 1, 15)
        val workouts = listOf(createTestWorkout())
        coEvery { mockGetWorkoutHistoryUseCase.getWorkoutsByDate(date) } returns workouts

        // When: 特定の日付のワークアウトを取得
        viewModel.getWorkoutsByDate(date)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: selectedDateWorkoutsが更新される
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals(workouts, uiState.selectedDateWorkouts)
        assertNull(uiState.errorMessage)
        
        coVerify { mockGetWorkoutHistoryUseCase.getWorkoutsByDate(date) }
    }

    @Test
    fun `getWorkoutsByDate_失敗時にエラーメッセージが設定されること`() = runTest {
        // Given: エラーが発生する日付のワークアウト取得
        val date = LocalDate(2024, 1, 15)
        val exception = RuntimeException("データ取得エラー")
        coEvery { mockGetWorkoutHistoryUseCase.getWorkoutsByDate(date) } throws exception

        // When: 特定の日付のワークアウトを取得
        viewModel.getWorkoutsByDate(date)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: エラーメッセージが設定される
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals("データ取得エラー", uiState.errorMessage)
        assertTrue(uiState.selectedDateWorkouts.isEmpty())
        
        coVerify { mockGetWorkoutHistoryUseCase.getWorkoutsByDate(date) }
    }

    @Test
    fun `clearError_エラーメッセージがクリアされること`() = runTest {
        // Given: エラーメッセージが設定された状態
        viewModel.addWorkout(createTestWorkout()) // エラーを発生させる
        coEvery { mockAddWorkoutUseCase(any()) } returns Result.failure(RuntimeException("エラー"))
        testDispatcher.scheduler.advanceUntilIdle()

        // When: エラーをクリア
        viewModel.clearError()

        // Then: エラーメッセージがクリアされる
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `clearMessage_メッセージがクリアされること`() = runTest {
        // Given: メッセージが設定された状態
        coEvery { mockAddWorkoutUseCase(any()) } returns Result.success(123L)
        viewModel.addWorkout(createTestWorkout())
        testDispatcher.scheduler.advanceUntilIdle()

        // When: メッセージをクリア
        viewModel.clearMessage()

        // Then: メッセージがクリアされる
        assertNull(viewModel.uiState.value.message)
    }

    // テストヘルパーメソッド
    private fun createTestWorkout(): Workout {
        val sets = listOf(
            ExerciseSet(reps = 10, weight = 60.0)
        )
        val exercise = Exercise(
            id = 1L,
            name = "ベンチプレス",
            sets = sets,
            category = ExerciseCategory.CHEST
        )
        
        return Workout(
            id = 1L,
            date = LocalDate(2024, 1, 1),
            exercises = listOf(exercise),
            notes = "テストワークアウト"
        )
    }
}