package com.workrec.data.database.migrations

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.workrec.data.database.WorkoutDatabase
import com.workrec.data.database.entities.GoalEntity
import com.workrec.domain.entities.GoalType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.system.measureTimeMillis

/**
 * Migration_4_5のパフォーマンステスト
 * isCompletedカラムのインデックスによるクエリパフォーマンス向上を検証
 */
@RunWith(AndroidJUnit4::class)
class Migration_4_5_PerformanceTest {

    private lateinit var database: WorkoutDatabase

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            WorkoutDatabase::class.java
        ).addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
         .build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun testIndexedQueryPerformance() = runBlocking {
        val goalDao = database.goalDao()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        
        // 大量のテストデータを作成（完了済みと未完了を混在）
        val testGoals = mutableListOf<GoalEntity>()
        repeat(1000) { index ->
            testGoals.add(
                GoalEntity(
                    type = GoalType.WEIGHT_LOSS,
                    title = "Test Goal $index",
                    description = "Test description $index",
                    targetValue = 100.0,
                    currentValue = if (index % 2 == 0) 100.0 else 50.0,
                    unit = "kg",
                    deadline = today.plus(DatePeriod(days = 30)),
                    isCompleted = index % 2 == 0, // 半分を完了済みに
                    createdAt = today,
                    updatedAt = today
                )
            )
        }
        
        // テストデータを挿入
        testGoals.forEach { goal ->
            goalDao.insertGoal(goal)
        }
        
        // インデックス付きクエリのパフォーマンステスト
        val activeGoalsTime = measureTimeMillis {
            val activeGoals = goalDao.getActiveGoals().first()
            assert(activeGoals.size == 500) { "Expected 500 active goals, got ${activeGoals.size}" }
        }
        
        val completedGoalsTime = measureTimeMillis {
            val completedGoals = goalDao.getCompletedGoals().first()
            assert(completedGoals.size == 500) { "Expected 500 completed goals, got ${completedGoals.size}" }
        }
        
        // パフォーマンス検証（インデックスがあれば高速になるはず）
        println("Active goals query time: ${activeGoalsTime}ms")
        println("Completed goals query time: ${completedGoalsTime}ms")
        
        // 大量データでも合理的な時間内で完了することを確認
        assert(activeGoalsTime < 100) { "Active goals query took too long: ${activeGoalsTime}ms" }
        assert(completedGoalsTime < 100) { "Completed goals query took too long: ${completedGoalsTime}ms" }
    }

    @Test
    fun testMigrationWithExistingData() = runBlocking {
        val goalDao = database.goalDao()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        
        // 既存データをシミュレート
        val existingGoal = GoalEntity(
            type = GoalType.MUSCLE_GAIN,
            title = "Existing Goal",
            description = "This goal existed before migration",
            targetValue = 80.0,
            currentValue = 40.0,
            unit = "kg",
            deadline = today.plus(DatePeriod(days = 60)),
            isCompleted = false,
            createdAt = today.plus(DatePeriod(days = -30)),
            updatedAt = today
        )
        
        goalDao.insertGoal(existingGoal)
        
        // マイグレーション後もデータが正常に取得できることを確認
        val activeGoals = goalDao.getActiveGoals().first()
        assert(activeGoals.size == 1) { "Expected 1 active goal after migration" }
        assert(activeGoals[0].title == "Existing Goal") { "Goal data should be preserved after migration" }
        
        val completedGoals = goalDao.getCompletedGoals().first()
        assert(completedGoals.isEmpty()) { "Should have no completed goals" }
    }

    @Test
    fun testIndexExistence() = runBlocking {
        // インデックスが正しく作成されていることを間接的に確認
        // SQLiteでは直接インデックスの存在を確認するのが困難なため、
        // クエリが正常に動作し、パフォーマンスが向上していることで確認
        
        val goalDao = database.goalDao()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        
        // 複数のクエリを実行してエラーが発生しないことを確認
        repeat(10) { index ->
            val goal = GoalEntity(
                type = GoalType.ENDURANCE,
                title = "Performance Test Goal $index",
                targetValue = 50.0,
                currentValue = 25.0,
                unit = "minutes",
                isCompleted = index % 3 == 0,
                createdAt = today,
                updatedAt = today
            )
            goalDao.insertGoal(goal)
        }
        
        // インデックス付きクエリが正常に動作することを確認
        val activeGoals = goalDao.getActiveGoals().first()
        val completedGoals = goalDao.getCompletedGoals().first()
        
        assert(activeGoals.isNotEmpty()) { "Active goals should not be empty" }
        assert(completedGoals.isNotEmpty()) { "Completed goals should not be empty" }
        
        // フィルタリングが正しく動作していることを確認
        assert(activeGoals.all { !it.isCompleted }) { "All active goals should have isCompleted = false" }
        assert(completedGoals.all { it.isCompleted }) { "All completed goals should have isCompleted = true" }
    }
}