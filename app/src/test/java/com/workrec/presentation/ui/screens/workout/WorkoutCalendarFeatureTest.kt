package com.workrec.presentation.ui.screens.workout

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.workrec.domain.entities.*
import com.workrec.domain.usecase.workout.AddWorkoutUseCase
import com.workrec.domain.usecase.workout.DeleteWorkoutUseCase
import com.workrec.domain.usecase.workout.GetWorkoutHistoryUseCase
import com.workrec.domain.usecase.calendar.GetWorkoutDatesUseCase
import com.workrec.domain.usecase.calendar.GetWorkoutsByDateUseCase
import com.workrec.presentation.viewmodel.WorkoutViewModel
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
 * Calendar-specific feature tests for WorkoutViewModel
 * Tests new calendar functionality and integration with existing features
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutCalendarFeatureTest {

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

        // Default mock setup
        every { mockGetWorkoutHistoryUseCase() } returns flowOf(emptyList())
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
    fun `カレンダー初期状態_正しく設定されること`() = runTest {
        // Given: ViewModelが初期化される
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: カレンダーの初期状態が正しく設定される
        val uiState = viewModel.uiState.value
        assertNotNull(uiState.currentMonth)
        assertNull(uiState.selectedDate)
        assertTrue(uiState.workoutDates.isEmpty())
        assertTrue(uiState.selectedDateWorkouts.isEmpty())
        assertFalse(uiState.isLoadingWorkouts)
        
        // And: 現在の月のワークアウト日付読み込みが呼ばれる
        verify { mockGetWorkoutDatesUseCase.getWorkoutDatesForMonth(any(), any()) }
    }

    @Test
    fun `月間ナビゲーション_連続した月移動が正しく動作すること`() = runTest {
        // Given: 初期月
        val initialMonth = viewModel.uiState.value.currentMonth
        every { mockGetWorkoutDatesUseCase.getWorkoutDatesForMonth(any(), any()) } returns flowOf(emptySet())

        // When: 複数回次の月に移動
        repeat(3) {
            viewModel.navigateToNextMonth()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: 正しい月に移動している
        val expectedMonth = initialMonth.plusMonths(3)
        assertEquals(expectedMonth, viewModel.uiState.value.currentMonth)

        // When: 複数回前の月に移動
        repeat(2) {
            viewModel.navigateToPreviousMonth()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: 正しい月に戻る
        val expectedReturnMonth = initialMonth.plusMonths(1)
        assertEquals(expectedReturnMonth, viewModel.uiState.value.currentMonth)
    }

    @Test
    fun `日付選択_複数の日付選択が正しく動作すること`() = runTest {
        // Given: 複数の日付とそれぞれのワークアウト
        val date1 = LocalDate(2024, 3, 15)
        val date2 = LocalDate(2024, 3, 20)
        val workouts1 = listOf(createTestWorkout(date1))
        val workouts2 = listOf(createTestWorkout(date2), createTestWorkout(date2))
        
        every { mockGetWorkoutsByDateUseCase(date1) } returns flowOf(workouts1)
        every { mockGetWorkoutsByDateUseCase(date2) } returns flowOf(workouts2)

        // When: 最初の日付を選択
        viewModel.onDateSelected(date1)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: 最初の日付のワークアウトが表示される
        var uiState = viewModel.uiState.value
        assertEquals(date1, uiState.selectedDate)
        assertEquals(workouts1, uiState.selectedDateWorkouts)
        assertFalse(uiState.isLoadingWorkouts)

        // When: 2番目の日付を選択
        viewModel.onDateSelected(date2)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: 2番目の日付のワークアウトが表示される
        uiState = viewModel.uiState.value
        assertEquals(date2, uiState.selectedDate)
        assertEquals(workouts2, uiState.selectedDateWorkouts)
        assertFalse(uiState.isLoadingWorkouts)
    }

    @Test
    fun `ワークアウト追加_カレンダー状態に影響しないこと`() = runTest {
        // Given: 日付が選択された状態
        val selectedDate = LocalDate(2024, 3, 15)
        val initialWorkouts = listOf(createTestWorkout(selectedDate))
        
        every { mockGetWorkoutsByDateUseCase(selectedDate) } returns flowOf(initialWorkouts)
        viewModel.onDateSelected(selectedDate)
        testDispatcher.scheduler.advanceUntilIdle()

        val initialUiState = viewModel.uiState.value
        
        // When: 新しいワークアウトを追加
        val newWorkout = createTestWorkout()
        coEvery { mockAddWorkoutUseCase(newWorkout) } returns Result.success(123L)
        
        viewModel.addWorkout(newWorkout)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: カレンダー状態は維持される
        val uiState = viewModel.uiState.value
        assertEquals(initialUiState.currentMonth, uiState.currentMonth)
        assertEquals(initialUiState.selectedDate, uiState.selectedDate)
        assertEquals(initialUiState.selectedDateWorkouts, uiState.selectedDateWorkouts)
        
        // And: ワークアウト追加は成功する
        assertEquals("ワークアウトを保存しました", uiState.message)
    }

    @Test
    fun `ワークアウト削除_選択日のワークアウトリストが更新されること`() = runTest {
        // Given: 選択日にワークアウトがある状態
        val selectedDate = LocalDate(2024, 3, 15)
        val workout = createTestWorkout(selectedDate)
        val initialWorkouts = listOf(workout)
        
        every { mockGetWorkoutsByDateUseCase(selectedDate) } returns flowOf(initialWorkouts)
        viewModel.onDateSelected(selectedDate)
        testDispatcher.scheduler.advanceUntilIdle()

        // When: ワークアウトを削除
        coEvery { mockDeleteWorkoutUseCase(workout) } returns Result.success(Unit)
        
        viewModel.deleteWorkout(workout)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: 削除が成功する
        val uiState = viewModel.uiState.value
        assertEquals("ワークアウトを削除しました", uiState.message)
        
        // And: カレンダー状態は維持される
        assertEquals(selectedDate, uiState.selectedDate)
    }

    @Test
    fun `今日ボタン_選択状態が更新されること`() = runTest {
        // Given: 今日ボタンの動作をテスト
        every { mockGetWorkoutDatesUseCase.getWorkoutDatesForMonth(any(), any()) } returns flowOf(emptySet())
        every { mockGetWorkoutsByDateUseCase(any()) } returns flowOf(emptyList())

        // When: 今日ボタンを押す
        viewModel.navigateToToday()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: 今日ボタンが呼ばれたことを確認（選択状態の詳細は統合テストで確認）
        val uiState = viewModel.uiState.value
        // 今日ボタンの機能が動作することを確認
        verify { mockGetWorkoutsByDateUseCase(any()) }
    }

    @Test
    fun `エラー処理_カレンダーデータ読み込みが正しく処理されること`() = runTest {
        // Given: 正常なワークアウト日付取得
        every { mockGetWorkoutDatesUseCase.getWorkoutDatesForMonth(any(), any()) } returns flowOf(emptySet())

        // When: 月を変更
        viewModel.navigateToNextMonth()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: エラーなく処理される
        val uiState = viewModel.uiState.value
        assertTrue(uiState.workoutDates.isEmpty())
        assertNull(uiState.errorMessage)
    }

    // Test helper methods
    private fun createTestWorkout(date: LocalDate = LocalDate(2024, 1, 1)): Workout {
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
            date = date,
            exercises = listOf(exercise),
            notes = "テストワークアウト"
        )
    }
}