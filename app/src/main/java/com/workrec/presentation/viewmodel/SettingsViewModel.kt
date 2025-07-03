package com.workrec.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * 設定画面ViewModel
 * アプリケーション設定の管理
 */
@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // アプリ情報を取得（将来的にリポジトリから）
    fun getAppVersion(): String {
        return "1.0.0" // TODO: BuildConfigから取得
    }

    fun getBuildNumber(): String {
        return "1" // TODO: BuildConfigから取得
    }

    // 設定項目のクリック処理
    fun onSettingItemClicked(item: SettingItem) {
        _uiState.value = _uiState.value.copy(
            lastClickedItem = item
        )
    }

    // エラーハンドリング
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

/**
 * 設定画面のUI状態
 */
data class SettingsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastClickedItem: SettingItem? = null
)

/**
 * 設定項目の種類
 */
enum class SettingItem {
    EXERCISE_MANAGER,      // エクササイズ管理
    APP_INFO,             // アプリについて
    DATA_MANAGEMENT,      // データ管理（将来実装）
    NOTIFICATION_SETTINGS // 通知設定（将来実装）
}