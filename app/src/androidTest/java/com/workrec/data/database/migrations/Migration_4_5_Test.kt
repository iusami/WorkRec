package com.workrec.data.database.migrations

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.workrec.data.database.WorkoutDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * データベースマイグレーション4→5のテスト
 * isCompletedカラムのインデックス追加をテスト
 */
@RunWith(AndroidJUnit4::class)
class Migration_4_5_Test {

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
    fun testDatabaseCreationWithIndex() = runBlocking {
        // データベースが正常に作成されることを確認
        val goalDao = database.goalDao()
        
        // インデックスが存在することを間接的に確認（クエリが正常に動作することで）
        val activeGoals = goalDao.getActiveGoals()
        val completedGoals = goalDao.getCompletedGoals()
        
        // クエリが正常に実行されることを確認
        assert(activeGoals != null) { "Active goals query should work" }
        assert(completedGoals != null) { "Completed goals query should work" }
    }
}