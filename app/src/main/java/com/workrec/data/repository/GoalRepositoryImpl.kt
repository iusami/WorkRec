package com.workrec.data.repository

import com.workrec.data.database.dao.GoalDao
import com.workrec.data.database.entities.toDomainModel
import com.workrec.data.database.entities.toEntity
import com.workrec.domain.entities.Goal
import com.workrec.domain.entities.GoalType
import com.workrec.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 目標リポジトリの実装
 * Room DatabaseとドメインレイヤーのGoalRepositoryインターフェースを繋ぐ
 */
// @Singleton
class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao
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
        goalDao.updateGoalProgress(id, currentValue)
    }

    override suspend fun markGoalAsCompleted(id: Long) {
        goalDao.markGoalAsCompleted(id)
    }
}