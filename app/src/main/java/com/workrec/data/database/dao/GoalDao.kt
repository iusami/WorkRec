package com.workrec.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.workrec.data.database.entities.GoalEntity
import com.workrec.data.database.entities.GoalWithProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: Long): GoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoalById(goalId: Long)

    @Transaction
    @Query("SELECT * FROM goals")
    fun getGoalsWithProgress(): Flow<List<GoalWithProgress>>

    @Transaction
    @Query("SELECT * FROM goals WHERE id = :goalId")
    fun getGoalWithProgressById(goalId: Long): Flow<GoalWithProgress?>

    // Optimized query methods for goal filtering
    @Query("SELECT * FROM goals WHERE isCompleted = 0")
    fun getActiveGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE isCompleted = 1")
    fun getCompletedGoals(): Flow<List<GoalEntity>>

    @Transaction
    @Query("SELECT * FROM goals WHERE isCompleted = 0")
    fun getActiveGoalsWithProgress(): Flow<List<GoalWithProgress>>

    @Transaction
    @Query("SELECT * FROM goals WHERE isCompleted = 1")
    fun getCompletedGoalsWithProgress(): Flow<List<GoalWithProgress>>
}