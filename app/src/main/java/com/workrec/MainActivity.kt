package com.workrec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.workrec.presentation.navigation.WorkRecNavigation
import com.workrec.presentation.ui.theme.WorkRecTheme
import com.workrec.presentation.viewmodel.ViewModelFactory
// import dagger.hilt.android.AndroidEntryPoint  // 一時的に無効化

/**
 * メインアクティビティ
 * アプリケーションのエントリーポイント - Manual DI対応
 */
// @AndroidEntryPoint  // 一時的に無効化
class MainActivity : ComponentActivity() {
    
    private lateinit var viewModelFactory: ViewModelFactory
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Manual DI setup
        val app = application as WorkRecApplication
        viewModelFactory = ViewModelFactory(
            addWorkoutUseCase = app.addWorkoutUseCase,
            getWorkoutHistoryUseCase = app.getWorkoutHistoryUseCase,
            deleteWorkoutUseCase = app.deleteWorkoutUseCase,
            setGoalUseCase = app.setGoalUseCase,
            getGoalProgressUseCase = app.getGoalProgressUseCase
        )
        
        setContent {
            WorkRecTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WorkRecNavigation(viewModelFactory = viewModelFactory)
                }
            }
        }
    }
}