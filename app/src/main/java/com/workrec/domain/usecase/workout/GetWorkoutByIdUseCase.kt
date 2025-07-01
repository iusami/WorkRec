package com.workrec.domain.usecase.workout

import com.workrec.domain.entities.Workout
import com.workrec.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 指定IDのワークアウト詳細を取得するUseCase
 * ワークアウト詳細画面でのリアルタイムデータ更新をサポート
 */
class GetWorkoutByIdUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    /**
     * 指定IDのワークアウトを取得
     * @param workoutId ワークアウトID
     * @return ワークアウト詳細を返すFlow（リアルタイム更新対応）
     */
    operator fun invoke(workoutId: Long): Flow<Workout?> {
        return workoutRepository.getWorkoutByIdFlow(workoutId)
    }
    
    /**
     * 指定IDのワークアウトを一度だけ取得（suspend版）
     * @param workoutId ワークアウトID
     * @return ワークアウト詳細（nullの場合は存在しない）
     */
    suspend fun getOnce(workoutId: Long): Workout? {
        return workoutRepository.getWorkoutById(workoutId)
    }
}