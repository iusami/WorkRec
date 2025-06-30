package com.workrec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.workrec.presentation.navigation.WorkRecNavigation
import com.workrec.presentation.ui.theme.WorkRecTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * メインアクティビティ
 * アプリケーションのエントリーポイント - Manual DI対応
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Hilt DI - ViewModelFactoryは不要
        
        setContent {
            WorkRecTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WorkRecNavigation()
                }
            }
        }
    }
}