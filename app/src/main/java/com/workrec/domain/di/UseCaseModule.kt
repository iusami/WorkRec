package com.workrec.domain.di

import com.workrec.domain.usecase.goal.*
import com.workrec.domain.usecase.workout.*
import com.workrec.domain.usecase.progress.*
import com.workrec.domain.usecase.calendar.*
import com.workrec.domain.usecase.exercise.*
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * UseCase関連の依存性注入モジュール
 * UseCaseクラスは@Injectコンストラクタを持つため、
 * このモジュールでHiltに認識させる
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    // UseCaseクラスは@Injectコンストラクタを持つため、
    // 明示的な@Providesメソッドは不要
    // このモジュールはHiltにUseCaseパッケージを認識させるために存在
}