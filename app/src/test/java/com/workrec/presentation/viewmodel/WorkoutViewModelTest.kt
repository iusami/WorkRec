package com.workrec.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.workrec.domain.entities.*
import com.workrec.domain.usecase.workout.AddWorkoutUseCase
import com.workrec.domain.usecase.workout.DeleteWorkoutUseCase
import com.workrec.domain.usecase.workout.GetWorkoutHistoryUseCase
import com.workrec.domain.usecase.calendar.GetWorkoutDatesUseCase
import com.workrec.domain.usecase.calendar.GetWorkoutsByDateUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import kotlinx.datetime.LocalDate
import java.time.YearMonth
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

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var mockAddWorkoutUseCase: AddWorkoutUseCase
    private lateinit var mockGetWorkoutHistoryUseCase: GetWorkoutHistoryUseCase
    private lateinit var mockDeleteWorkoutUseCase: DeleteWorkoutUseCase
    private lateinit var mockGetWorkoutDatesUseCase: GetWorkoutDatesUseCase
    private lateinit var mockGetWorkoutsByDateUseCase: GetWorkoutsByDateUseCase
    private lateinit var viewModel: WorkoutViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        mockAddWorkoutUseCase = mockk()
        mockGetWorkoutHistoryUseCase = mockk()
        mockDeleteWorkoutUseCase = mockk()
        mockGetWorkoutDatesUseCase = mockk()
        mockGetWorkoutsByDateUseCase = mockk()

        // GetWorkoutHistoryUseCaseのinvoke演算子をモック
        every { mockGetWorkoutHistoryUseCase() } returns flowOf(emptyList())
        
        // Calendar use casesのモック設定
        every { mockGetWorkoutDatesUseCase.getWorkoutDatesForMonth(any(), any()) } returns flowOf(emptySet())
        every { mockGetWorkoutsByDateUseCase(any()) } returns flowOf(emptyList())

        viewModel = WorkoutViewModel(
            mockAddWorkoutUseCase,
            mockGetWorkoutHistoryUseCase,
            mockDeleteWorkoutUseCase,
            mockGetWorkoutDatesUseCase,
            mockGetWorkoutsByDateUseCase
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

        // Then: UnconfinedTestDispatcherにより即座に実行完了
        // ロード完了後の状態を確認
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals("ワークアウトを保存しました", viewModel.uiState.value.message)
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
    fun `カレンダー機能_月間ワークアウト日付が正しく読み込まれること`() = runTest {
        // Given: 特定の月のワークアウト日付
        val year = 2024
        val month = 3
        val workoutDates = setOf(LocalDate(2024, 3, 15), LocalDate(2024, 3, 20))
        
        every { mockGetWorkoutDatesUseCase.getWorkoutDatesForMonth(year, month) } returns flowOf(workoutDates)

        // When: ViewModelを初期化し、月を設定
        val testMonth = YearMonth.of(year, month)
        viewModel.navigateToNextMonth() // This will trigger loading for current month + 1
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: ワークアウト日付が読み込まれる
        verify { mockGetWorkoutDatesUseCase.getWorkoutDatesForMonth(any(), any()) }
    }

    @Test
    fun `カレンダー機能_選択日のワークアウトが正しく読み込まれること`() = runTest {
        // Given: 選択する日付とそのワークアウト
        val selectedDate = LocalDate(2024, 3, 15)
        val workouts = listOf(createTestWorkout())
        
        every { mockGetWorkoutsByDateUseCase(selectedDate) } returns flowOf(workouts)

        // When: 日付を選択
        viewModel.onDateSelected(selectedDate)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: 選択日とワークアウトが更新される
        val uiState = viewModel.uiState.value
        assertEquals(selectedDate, uiState.selectedDate)
        assertEquals(workouts, uiState.selectedDateWorkouts)
        assertFalse(uiState.isLoadingWorkouts)
        
        verify { mockGetWorkoutsByDateUseCase(selectedDate) }
    }

    @Test
    fun `カレンダー機能_月間ナビゲーションが正しく動作すること`() = runTest {
        // Given: 初期月
        val initialMonth = viewModel.uiState.value.currentMonth
        val nextMonth = initialMonth.plusMonths(1)
        val previousMonth = initialMonth.minusMonths(1)
        
        every { mockGetWorkoutDatesUseCase.getWorkoutDatesForMonth(any(), any()) } returns flowOf(emptySet())

        // When: 次の月に移動
        viewModel.navigateToNextMonth()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: 月が更新される
        assertEquals(nextMonth, viewModel.uiState.value.currentMonth)
        assertNull(viewModel.uiState.value.selectedDate) // Selection should be cleared

        // When: 前の月に移動
        viewModel.navigateToPreviousMonth()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: 月が戻る
        assertEquals(initialMonth, viewModel.uiState.value.currentMonth)
        assertNull(viewModel.uiState.value.selectedDate) // Selection should be cleared
    }

    @Test
    fun `カレンダー機能_今日ボタンが正しく動作すること`() = runTest {
        // Given: 今日の日付とワークアウト
        val todayWorkouts = listOf(createTestWorkout())
        
        every { mockGetWorkoutDatesUseCase.getWorkoutDatesForMonth(any(), any()) } returns flowOf(emptySet())
        every { mockGetWorkoutsByDateUseCase(any()) } returns flowOf(todayWorkouts)

        // When: 今日に移動
        viewModel.navigateToToday()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: 今日が選択され、ワークアウトが読み込まれる
        val uiState = viewModel.uiState.value
        assertNotNull(uiState.selectedDate) // Today should be selected
        assertEquals(todayWorkouts, uiState.selectedDateWorkouts)
        assertFalse(uiState.isLoadingWorkouts)
    }

    @Test
    fun `カレンダー機能_エラー処理が正しく動作すること`() = runTest {
        // Given: エラーが発生するワークアウト日付取得
        val exception = RuntimeException("カレンダーデータ取得エラー")
        every { mockGetWorkoutDatesUseCase.getWorkoutDatesForMonth(any(), any()) } returns flow { throw exception }

        // When: 月を変更してエラーを発生させる
        viewModel.navigateToNextMonth()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: エラーメッセージが設定される
        val uiState = viewModel.uiState.value
        assertEquals("カレンダーデータ取得エラー", uiState.errorMessage)
    }

    @Test
    fun `カレンダー機能_選択日ワークアウト読み込みエラーが正しく処理されること`() = runTest {
        // Given: エラーが発生する選択日ワークアウト取得
        val selectedDate = LocalDate(2024, 3, 15)
        val exception = RuntimeException("ワークアウトデータ取得エラー")
        
        every { mockGetWorkoutsByDateUseCase(selectedDate) } returns flow { throw exception }

        // When: 日付を選択
        viewModel.onDateSelected(selectedDate)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: エラーメッセージが設定される
        val uiState = viewModel.uiState.value
        assertEquals(selectedDate, uiState.selectedDate)
        assertFalse(uiState.isLoadingWorkouts)
        assertEquals("ワークアウトデータ取得エラー", uiState.errorMessage)
    }

    @Test
    fun `clearError_エラーメッセージがクリアされること`() = runTest {
        // Given: エラーメッセージが設定された状態
        coEvery { mockAddWorkoutUseCase(any()) } returns Result.failure(RuntimeException("エラー"))
        viewModel.addWorkout(createTestWorkout()) // エラーを発生させる

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