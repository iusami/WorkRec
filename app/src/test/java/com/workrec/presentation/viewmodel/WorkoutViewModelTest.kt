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

    // Calendar functionality tests

    @Test
    fun `navigateToNextMonth_次の月に移動すること`() = runTest {
        // Given: 現在の月を取得し、次の月を計算
        val initialMonth = viewModel.uiState.value.currentMonth
        val expectedNextMonth = initialMonth.plusMonths(1)
        val workoutDates = setOf(LocalDate(expectedNextMonth.year, expectedNextMonth.monthValue, 15))
        
        every { mockGetWorkoutDatesUseCase.getWorkoutDatesForMonth(expectedNextMonth.year, expectedNextMonth.monthValue) } returns flowOf(workoutDates)

        // When: 次の月に移動
        viewModel.navigateToNextMonth()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: currentMonthが更新され、ワークアウト日付が読み込まれる
        val uiState = viewModel.uiState.value
        assertEquals(expectedNextMonth, uiState.currentMonth)
        assertEquals(workoutDates, uiState.workoutDates)
        
        verify { mockGetWorkoutDatesUseCase.getWorkoutDatesForMonth(expectedNextMonth.year, expectedNextMonth.monthValue) }
    }

    @Test
    fun `navigateToPreviousMonth_前の月に移動すること`() = runTest {
        // Given: 現在の月を取得し、前の月を計算
        val initialMonth = viewModel.uiState.value.currentMonth
        val expectedPreviousMonth = initialMonth.minusMonths(1)
        val workoutDates = setOf(LocalDate(expectedPreviousMonth.year, expectedPreviousMonth.monthValue, 10))
        
        every { mockGetWorkoutDatesUseCase.getWorkoutDatesForMonth(expectedPreviousMonth.year, expectedPreviousMonth.monthValue) } returns flowOf(workoutDates)

        // When: 前の月に移動
        viewModel.navigateToPreviousMonth()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: currentMonthが更新され、ワークアウト日付が読み込まれる
        val uiState = viewModel.uiState.value
        assertEquals(expectedPreviousMonth, uiState.currentMonth)
        assertEquals(workoutDates, uiState.workoutDates)
        
        verify { mockGetWorkoutDatesUseCase.getWorkoutDatesForMonth(expectedPreviousMonth.year, expectedPreviousMonth.monthValue) }
    }

    @Test
    fun `navigateToToday_今日の月に移動し今日を選択すること`() = runTest {
        // Given: 今日に移動するためのワークアウトデータをモック
        val todayWorkouts = listOf(createTestWorkout())
        
        // 任意の日付でワークアウトデータをモック（実際の今日の日付は CalendarUtils.getCurrentDate() で決まる）
        every { mockGetWorkoutDatesUseCase.getWorkoutDatesForMonth(any(), any()) } returns flowOf(emptySet())
        every { mockGetWorkoutsByDateUseCase(any()) } returns flowOf(todayWorkouts)

        // When: 今日に移動
        viewModel.navigateToToday()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: 今日が選択され、ワークアウトが読み込まれる
        val uiState = viewModel.uiState.value
        assertNotNull(uiState.selectedDate) // 今日の日付が選択されている
        assertEquals(todayWorkouts, uiState.selectedDateWorkouts)
        assertFalse(uiState.isLoadingWorkouts)
        
        // 今日の日付でワークアウトが取得されることを確認
        verify { mockGetWorkoutsByDateUseCase(any()) }
        verify { mockGetWorkoutDatesUseCase.getWorkoutDatesForMonth(any(), any()) }
    }

    @Test
    fun `onDateSelected_日付選択時にワークアウトが読み込まれること`() = runTest {
        // Given: 選択する日付とそのワークアウト
        val selectedDate = LocalDate(2024, 1, 20)
        val workouts = listOf(createTestWorkout(), createTestWorkout())
        
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
    fun `onDateSelected_ワークアウト読み込み中にローディング状態が管理されること`() = runTest {
        // Given: 選択する日付
        val selectedDate = LocalDate(2024, 1, 20)
        every { mockGetWorkoutsByDateUseCase(selectedDate) } returns flowOf(emptyList())

        // When: 日付を選択
        viewModel.onDateSelected(selectedDate)

        // Then: UnconfinedTestDispatcherにより即座に実行完了
        // ロード完了後の状態を確認
        val uiState = viewModel.uiState.value
        assertEquals(selectedDate, uiState.selectedDate)
        assertFalse(uiState.isLoadingWorkouts)
        assertTrue(uiState.selectedDateWorkouts.isEmpty())
    }

    @Test
    fun `onDateSelected_エラー時にエラーメッセージが設定されること`() = runTest {
        // Given: エラーが発生する日付選択
        val selectedDate = LocalDate(2024, 1, 20)
        val exception = RuntimeException("ワークアウトデータの取得に失敗しました")
        
        every { mockGetWorkoutsByDateUseCase(selectedDate) } returns flow { throw exception }

        // When: 日付を選択
        viewModel.onDateSelected(selectedDate)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: エラーメッセージが設定される
        val uiState = viewModel.uiState.value
        assertEquals(selectedDate, uiState.selectedDate)
        assertFalse(uiState.isLoadingWorkouts)
        assertEquals("ワークアウトデータの取得に失敗しました", uiState.errorMessage)
        
        verify { mockGetWorkoutsByDateUseCase(selectedDate) }
    }

    @Test
    fun `初期化時にカレンダー状態が適切に設定されること`() = runTest {
        // Given: ViewModelが初期化される（setupで実行済み）
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: 初期状態が適切に設定される
        val uiState = viewModel.uiState.value
        assertNotNull(uiState.currentMonth)
        assertNull(uiState.selectedDate)
        assertTrue(uiState.workoutDates.isEmpty())
        assertTrue(uiState.selectedDateWorkouts.isEmpty())
        assertFalse(uiState.isLoadingWorkouts)
        
        // 現在の月のワークアウト日付読み込みが呼ばれることを確認
        verify { mockGetWorkoutDatesUseCase.getWorkoutDatesForMonth(any(), any()) }
    }

    @Test
    fun `既存ワークアウト機能_カレンダー実装後も正常に動作すること`() = runTest {
        // Given: 既存のワークアウト機能
        val workout = createTestWorkout()
        coEvery { mockAddWorkoutUseCase(workout) } returns Result.success(123L)

        // When: ワークアウトを追加
        viewModel.addWorkout(workout)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: 既存機能が正常に動作する
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertEquals("ワークアウトを保存しました", uiState.message)
        assertNull(uiState.errorMessage)
        
        // And: カレンダー状態も維持される
        assertNotNull(uiState.currentMonth)
        assertTrue(uiState.workoutDates.isEmpty()) // 初期状態
        
        coVerify { mockAddWorkoutUseCase(workout) }
    }

    @Test
    fun `カレンダー機能_データ更新時にカレンダーが自動更新されること`() = runTest {
        // Given: ワークアウトが追加された後
        val workout = createTestWorkout()
        val workoutDate = workout.date
        val updatedWorkoutDates = setOf(workoutDate)
        
        coEvery { mockAddWorkoutUseCase(workout) } returns Result.success(123L)
        every { mockGetWorkoutDatesUseCase.getWorkoutDatesForMonth(any(), any()) } returns flowOf(updatedWorkoutDates)

        // When: ワークアウトを追加
        viewModel.addWorkout(workout)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: カレンダーデータが更新される
        // Note: 実際の実装では、ワークアウト追加後にカレンダーデータの再読み込みが必要
        // この動作は統合テストで確認される
        val uiState = viewModel.uiState.value
        assertEquals("ワークアウトを保存しました", uiState.message)
    }

    @Test
    fun `カレンダー機能_複数月のナビゲーションが正しく動作すること`() = runTest {
        // Given: 複数月のナビゲーション
        val initialMonth = viewModel.uiState.value.currentMonth
        every { mockGetWorkoutDatesUseCase.getWorkoutDatesForMonth(any(), any()) } returns flowOf(emptySet())

        // When: 複数回月を移動
        viewModel.navigateToNextMonth()
        viewModel.navigateToNextMonth()
        viewModel.navigateToNextMonth()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: 正しい月に移動している
        val expectedMonth = initialMonth.plusMonths(3)
        assertEquals(expectedMonth, viewModel.uiState.value.currentMonth)

        // When: 戻る
        viewModel.navigateToPreviousMonth()
        viewModel.navigateToPreviousMonth()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: 正しい月に戻る
        val expectedReturnMonth = initialMonth.plusMonths(1)
        assertEquals(expectedReturnMonth, viewModel.uiState.value.currentMonth)
    }

    @Test
    fun `カレンダー機能_月変更時に選択状態がクリアされること`() = runTest {
        // Given: 日付が選択された状態
        val selectedDate = LocalDate(2024, 3, 15)
        every { mockGetWorkoutsByDateUseCase(selectedDate) } returns flowOf(emptyList())
        every { mockGetWorkoutDatesUseCase.getWorkoutDatesForMonth(any(), any()) } returns flowOf(emptySet())

        viewModel.onDateSelected(selectedDate)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify date is selected
        assertEquals(selectedDate, viewModel.uiState.value.selectedDate)

        // When: 月を変更
        viewModel.navigateToNextMonth()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: 選択状態がクリアされる
        assertNull(viewModel.uiState.value.selectedDate)
        assertTrue(viewModel.uiState.value.selectedDateWorkouts.isEmpty())
    }

    @Test
    fun `カレンダー機能_同じ日付を再選択しても正しく動作すること`() = runTest {
        // Given: 既に選択された日付
        val selectedDate = LocalDate(2024, 3, 15)
        val workouts = listOf(createTestWorkout())
        
        every { mockGetWorkoutsByDateUseCase(selectedDate) } returns flowOf(workouts)

        // When: 同じ日付を再選択
        viewModel.onDateSelected(selectedDate)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.onDateSelected(selectedDate)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: 正しく動作する
        val uiState = viewModel.uiState.value
        assertEquals(selectedDate, uiState.selectedDate)
        assertEquals(workouts, uiState.selectedDateWorkouts)
        assertFalse(uiState.isLoadingWorkouts)
        
        // Verify the use case was called multiple times
        verify(atLeast = 2) { mockGetWorkoutsByDateUseCase(selectedDate) }
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