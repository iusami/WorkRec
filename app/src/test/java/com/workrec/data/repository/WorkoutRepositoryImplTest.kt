package com.workrec.data.repository

import com.workrec.data.database.dao.ExerciseDao
import com.workrec.data.database.dao.ExerciseSetDao
import com.workrec.data.database.dao.WorkoutDao
import com.workrec.data.database.entities.*
import com.workrec.domain.entities.*
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
 * WorkoutRepositoryImplの単体テスト
 */
class WorkoutRepositoryImplTest {

    private lateinit var mockWorkoutDao: WorkoutDao
    private lateinit var mockExerciseDao: ExerciseDao
    private lateinit var mockExerciseSetDao: ExerciseSetDao
    private lateinit var repository: WorkoutRepositoryImpl

    @Before
    fun setup() {
        mockWorkoutDao = mockk()
        mockExerciseDao = mockk()
        mockExerciseSetDao = mockk()
        repository = WorkoutRepositoryImpl(mockWorkoutDao, mockExerciseDao, mockExerciseSetDao)
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `getAllWorkouts_データベースからワークアウト一覧を正しく取得できること`() = runTest {
        // Given: データベースにワークアウトエンティティが存在
        val workoutEntities = listOf(
            WorkoutEntity(
                id = 1L,
                date = LocalDate(2024, 1, 1),
                notes = "テストワークアウト1"
            ),
            WorkoutEntity(
                id = 2L,
                date = LocalDate(2024, 1, 2),
                notes = "テストワークアウト2"
            )
        )
        
        every { mockWorkoutDao.getAllWorkouts() } returns flowOf(workoutEntities)
        coEvery { mockExerciseDao.getExercisesByWorkoutId(any()) } returns emptyList()

        // When: すべてのワークアウトを取得
        val result = repository.getAllWorkouts().first()

        // Then: 正しくドメインエンティティに変換されて返される
        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(LocalDate(2024, 1, 1), result[0].date)
        assertEquals("テストワークアウト1", result[0].notes)
        assertEquals(2L, result[1].id)
        assertEquals(LocalDate(2024, 1, 2), result[1].date)
        assertEquals("テストワークアウト2", result[1].notes)
        
        verify { mockWorkoutDao.getAllWorkouts() }
        coVerify { mockExerciseDao.getExercisesByWorkoutId(1L) }
        coVerify { mockExerciseDao.getExercisesByWorkoutId(2L) }
    }

    @Test
    fun `getWorkoutById_存在するIDで正しくワークアウトを取得できること`() = runTest {
        // Given: 特定のIDのワークアウトエンティティが存在
        val workoutId = 1L
        val workoutEntity = WorkoutEntity(
            id = workoutId,
            date = LocalDate(2024, 1, 1),
            notes = "テストワークアウト"
        )
        
        coEvery { mockWorkoutDao.getWorkoutById(workoutId) } returns workoutEntity
        coEvery { mockExerciseDao.getExercisesByWorkoutId(workoutId) } returns emptyList()

        // When: 特定のIDのワークアウトを取得
        val result = repository.getWorkoutById(workoutId)

        // Then: 正しくドメインエンティティが返される
        assertNotNull(result)
        assertEquals(workoutId, result!!.id)
        assertEquals(LocalDate(2024, 1, 1), result.date)
        assertEquals("テストワークアウト", result.notes)
        
        coVerify { mockWorkoutDao.getWorkoutById(workoutId) }
        coVerify { mockExerciseDao.getExercisesByWorkoutId(workoutId) }
    }

    @Test
    fun `getWorkoutById_存在しないIDでnullが返されること`() = runTest {
        // Given: 存在しないID
        val nonExistentId = 999L
        
        coEvery { mockWorkoutDao.getWorkoutById(nonExistentId) } returns null

        // When: 存在しないIDのワークアウトを取得
        val result = repository.getWorkoutById(nonExistentId)

        // Then: nullが返される
        assertNull(result)
        
        coVerify { mockWorkoutDao.getWorkoutById(nonExistentId) }
        coVerify(exactly = 0) { mockExerciseDao.getExercisesByWorkoutId(any()) }
    }

    @Test
    fun `saveWorkout_新しいワークアウトが正常に保存されること`() = runTest {
        // Given: 新しいワークアウト
        val workout = createTestWorkout()
        val expectedWorkoutId = 123L
        val expectedExerciseId = 456L
        
        coEvery { mockWorkoutDao.insertWorkout(any()) } returns expectedWorkoutId
        coEvery { mockExerciseDao.insertExercise(any()) } returns expectedExerciseId
        coEvery { mockExerciseSetDao.insertSets(any()) } returns listOf(789L)

        // When: ワークアウトを保存
        val result = repository.saveWorkout(workout)

        // Then: 正しいIDが返され、適切にDAOが呼ばれる
        assertEquals(expectedWorkoutId, result)
        
        coVerify { mockWorkoutDao.insertWorkout(any()) }
        coVerify { mockExerciseDao.insertExercise(any()) }
        coVerify { mockExerciseSetDao.insertSets(any()) }
    }

    @Test
    fun `saveWorkout_既存ワークアウトの更新時に古いエクササイズが削除されること`() = runTest {
        // Given: 既存のワークアウト（IDが0でない）
        val existingWorkout = createTestWorkout().copy(id = 100L)
        val expectedWorkoutId = 123L
        val expectedExerciseId = 456L
        
        coEvery { mockWorkoutDao.insertWorkout(any()) } returns expectedWorkoutId
        coEvery { mockExerciseDao.deleteExercisesByWorkoutId(existingWorkout.id) } returns Unit
        coEvery { mockExerciseDao.insertExercise(any()) } returns expectedExerciseId
        coEvery { mockExerciseSetDao.insertSets(any()) } returns listOf(789L)

        // When: 既存ワークアウトを更新
        val result = repository.saveWorkout(existingWorkout)

        // Then: 古いエクササイズが削除された後、新しいデータが保存される
        assertEquals(expectedWorkoutId, result)
        
        coVerify { mockExerciseDao.deleteExercisesByWorkoutId(existingWorkout.id) }
        coVerify { mockWorkoutDao.insertWorkout(any()) }
        coVerify { mockExerciseDao.insertExercise(any()) }
        coVerify { mockExerciseSetDao.insertSets(any()) }
    }

    @Test
    fun `deleteWorkout_ワークアウトが正常に削除されること`() = runTest {
        // Given: 削除対象のワークアウト
        val workout = createTestWorkout()
        
        coEvery { mockWorkoutDao.deleteWorkout(any()) } returns Unit

        // When: ワークアウトを削除
        repository.deleteWorkout(workout)

        // Then: DAOの削除メソッドが呼ばれる
        coVerify { mockWorkoutDao.deleteWorkout(any()) }
    }

    @Test
    fun `getWorkoutsByDate_指定日のワークアウトが正しく取得されること`() = runTest {
        // Given: 特定の日付のワークアウトエンティティ
        val targetDate = LocalDate(2024, 1, 15)
        val workoutEntities = listOf(
            WorkoutEntity(
                id = 1L,
                date = targetDate,
                notes = "その日のワークアウト"
            )
        )
        
        coEvery { mockWorkoutDao.getWorkoutsByDate(targetDate) } returns workoutEntities
        coEvery { mockExerciseDao.getExercisesByWorkoutId(1L) } returns emptyList()

        // When: 特定の日付のワークアウトを取得
        val result = repository.getWorkoutsByDate(targetDate)

        // Then: 正しく返される
        assertEquals(1, result.size)
        assertEquals(targetDate, result[0].date)
        
        coVerify { mockWorkoutDao.getWorkoutsByDate(targetDate) }
    }

    // テストヘルパーメソッド
    private fun createTestWorkout(): Workout {
        val sets = listOf(
            ExerciseSet(reps = 10, weight = 60.0),
            ExerciseSet(reps = 8, weight = 65.0)
        )
        val exercise = Exercise(
            id = 1L,
            name = "ベンチプレス",
            sets = sets,
            category = ExerciseCategory.CHEST
        )
        
        return Workout(
            id = 0L,
            date = LocalDate(2024, 1, 1),
            exercises = listOf(exercise),
            notes = "テストワークアウト"
        )
    }
}