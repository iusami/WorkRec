package com.workrec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.workrec.presentation.ui.theme.WorkRecTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * メインアクティビティ
 * アプリケーションのエントリーポイント
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WorkRecTheme {
                // メインUIを将来的にここに配置
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // TODO: ナビゲーション設定を追加
                }
            }
        }
    }
}