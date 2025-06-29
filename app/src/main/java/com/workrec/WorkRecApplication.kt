package com.workrec

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * WorkRecアプリケーションクラス
 * Hiltの依存性注入を有効にする
 */
@HiltAndroidApp
class WorkRecApplication : Application()