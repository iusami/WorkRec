package com.workrec.data.repository

import com.workrec.data.database.dao.ExerciseDao
import com.workrec.data.database.dao.ExerciseSetDao
import com.workrec.data.database.dao.WorkoutDao
import com.workrec.data.database.entities.*
import com.workrec.domain.entities.Workout
import com.workrec.domain.repository.WorkoutRepository
import com.workrec.domain.repository.WorkoutStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
// import javax.inject.Inject
// import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes

/**
 * ワークアウトリポジトリの実装
 * Room DatabaseとドメインレイヤーのWorkoutRepositoryインターフェースを繋ぐ
 */
// @Singleton
class WorkoutRepositoryImpl constructor(
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
    private val exerciseSetDao: ExerciseSetDao
) : WorkoutRepository {

    override fun getAllWorkouts(): Flow<List<Workout>> {
        return workoutDao.getAllWorkouts().map { workoutEntities ->
            workoutEntities.map { workoutEntity ->
                workoutEntity.toDomainModelWithExercises()
            }
        }
    }

    override suspend fun getWorkoutById(id: Long): Workout? {
        val workoutEntity = workoutDao.getWorkoutById(id) ?: return null
        return workoutEntity.toDomainModelWithExercises()
    }

    override suspend fun getWorkoutsByDate(date: LocalDate): List<Workout> {
        return workoutDao.getWorkoutsByDate(date).map { workoutEntity ->
            workoutEntity.toDomainModelWithExercises()
        }
    }

    override suspend fun getWorkoutsByDateRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<Workout> {
        return workoutDao.getWorkoutsByDateRange(startDate, endDate).map { workoutEntity ->
            workoutEntity.toDomainModelWithExercises()
        }
    }

    override suspend fun saveWorkout(workout: Workout): Long {
        // ワークアウトを保存
        val workoutEntity = workout.toEntity()
        val workoutId = workoutDao.insertWorkout(workoutEntity)

        // 既存のエクササイズとセットを削除（更新の場合）
        if (workout.id != 0L) {
            exerciseDao.deleteExercisesByWorkoutId(workout.id)
        }

        // エクササイズとセットを保存
        workout.exercises.forEach { exercise ->
            val exerciseEntity = exercise.toEntity(workoutId)
            val exerciseId = exerciseDao.insertExercise(exerciseEntity)

            val exerciseSetEntities = exercise.sets.mapIndexed { index, set ->
                set.toEntity(exerciseId, index)
            }
            exerciseSetDao.insertSets(exerciseSetEntities)
        }

        return workoutId
    }

    override suspend fun deleteWorkout(workout: Workout) {
        workoutDao.deleteWorkout(workout.toEntity())
    }

    override suspend fun deleteWorkoutById(id: Long) {
        workoutDao.deleteWorkoutById(id)
    }

    override suspend fun searchWorkoutsByExerciseName(exerciseName: String): List<Workout> {
        val exerciseEntities = exerciseDao.searchExercisesByName(exerciseName)
        val workoutIds = exerciseEntities.map { it.workoutId }.distinct()
        
        return workoutIds.mapNotNull { workoutId ->
            getWorkoutById(workoutId)
        }
    }

    override suspend fun getWorkoutStats(
        startDate: LocalDate,
        endDate: LocalDate
    ): WorkoutStats {
        val workouts = getWorkoutsByDateRange(startDate, endDate)
        
        val totalWorkouts = workouts.size
        val totalVolume = workouts.sumOf { it.totalVolume }
        val totalSets = workouts.sumOf { it.totalSets }
        
        val averageWorkoutDuration = workouts
            .mapNotNull { it.totalDuration }
            .takeIf { it.isNotEmpty() }
            ?.let { durations ->
                val totalMillis = durations.sumOf { it.inWholeMilliseconds }
                (totalMillis / durations.size).minutes
            }
        
        val exerciseFrequency = workouts
            .flatMap { it.exercises }
            .groupingBy { it.name }
            .eachCount()
        
        val mostFrequentExercises = exerciseFrequency
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .map { it.first }
        
        return WorkoutStats(
            totalWorkouts = totalWorkouts,
            totalVolume = totalVolume,
            totalSets = totalSets,
            averageWorkoutDuration = averageWorkoutDuration,
            mostFrequentExercises = mostFrequentExercises
        )
    }

    /**
     * WorkoutEntityからエクササイズ付きのドメインモデルに変換
     */
    private suspend fun WorkoutEntity.toDomainModelWithExercises(): Workout {
        val exerciseEntities = exerciseDao.getExercisesByWorkoutId(this.id)
        val exercises = exerciseEntities.map { exerciseEntity ->
            val setEntities = exerciseSetDao.getSetsByExerciseId(exerciseEntity.id)
            val sets = setEntities.map { it.toDomainModel() }
            exerciseEntity.toDomainModel(sets)
        }
        return this.toDomainModel(exercises)
    }
}