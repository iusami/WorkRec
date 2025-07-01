package com.workrec.data.repository

import com.workrec.data.database.dao.GoalDao
import com.workrec.data.database.dao.GoalProgressDao
import com.workrec.data.database.entities.toDomainModel
import com.workrec.data.database.entities.toEntity
import com.workrec.domain.entities.Goal
import com.workrec.domain.entities.GoalType
import com.workrec.domain.entities.GoalProgressRecord
import com.workrec.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 目標リポジトリの実装
 * Room DatabaseとドメインレイヤーのGoalRepositoryインターフェースを繋ぐ
 */
// @Singleton
class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao,
    private val goalProgressDao: GoalProgressDao
) : GoalRepository {

    override fun getAllGoals(): Flow<List<Goal>> {
        return goalDao.getAllGoals().map { goalEntities ->
            goalEntities.map { it.toDomainModel() }
        }
    }

    override fun getActiveGoals(): Flow<List<Goal>> {
        return goalDao.getActiveGoals().map { goalEntities ->
            goalEntities.map { it.toDomainModel() }
        }
    }

    override fun getCompletedGoals(): Flow<List<Goal>> {
        return goalDao.getCompletedGoals().map { goalEntities ->
            goalEntities.map { it.toDomainModel() }
        }
    }

    override suspend fun getGoalById(id: Long): Goal? {
        return goalDao.getGoalById(id)?.toDomainModel()
    }

    override suspend fun getGoalsByType(type: GoalType): List<Goal> {
        return goalDao.getGoalsByType(type).map { it.toDomainModel() }
    }

    override suspend fun saveGoal(goal: Goal): Long {
        val goalEntity = goal.toEntity()
        return goalDao.insertGoal(goalEntity)
    }

    override suspend fun deleteGoal(goal: Goal) {
        goalDao.deleteGoal(goal.toEntity())
    }

    override suspend fun deleteGoalById(id: Long) {
        goalDao.deleteGoalById(id)
    }

    override suspend fun updateGoalProgress(id: Long, currentValue: Double) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        goalDao.updateGoalProgress(id, currentValue, today)
    }

    override suspend fun markGoalAsCompleted(id: Long) {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        goalDao.markGoalAsCompleted(id, today)
    }

    // GoalProgressRecord関連のメソッド実装
    
    override fun getProgressByGoalId(goalId: Long): Flow<List<GoalProgressRecord>> {
        return goalProgressDao.getProgressByGoalIdFlow(goalId).map { progressEntities ->
            progressEntities.map { it.toDomainModel() }
        }
    }

    override fun getLatestProgressByGoalId(goalId: Long): Flow<GoalProgressRecord?> {
        return goalProgressDao.getLatestProgressByGoalIdFlow(goalId).map { progressEntity ->
            progressEntity?.toDomainModel()
        }
    }

    override fun getProgressByGoalIdAndDateRange(goalId: Long, startDate: LocalDate, endDate: LocalDate): Flow<List<GoalProgressRecord>> {
        return goalProgressDao.getProgressByGoalIdAndDateRangeFlow(goalId, startDate, endDate).map { progressEntities ->
            progressEntities.map { it.toDomainModel() }
        }
    }

    override suspend fun saveProgressRecord(progress: GoalProgressRecord): Long {
        val progressEntity = progress.toEntity()
        return goalProgressDao.insertProgress(progressEntity)
    }

    override suspend fun deleteProgressRecord(progressId: Long) {
        goalProgressDao.deleteProgressById(progressId)
    }

    override suspend fun deleteAllProgressByGoalId(goalId: Long) {
        goalProgressDao.deleteProgressByGoalId(goalId)
    }
}