package com.workrec

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * WorkRecアプリケーションクラス
 * Hilt DI - 依存関係注入はHiltにより自動管理される
 */
@HiltAndroidApp
class WorkRecApplication : Application()