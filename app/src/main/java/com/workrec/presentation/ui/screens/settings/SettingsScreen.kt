package com.workrec.presentation.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.workrec.presentation.viewmodel.SettingItem
import com.workrec.presentation.viewmodel.SettingsViewModel

/**
 * 設定画面
 * アプリの各種設定とエクササイズ管理への直接アクセスを提供
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToExerciseManager: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 設定項目リスト
    val settingItems = remember {
        listOf(
            SettingItemData(
                item = SettingItem.EXERCISE_MANAGER,
                title = "エクササイズ管理",
                subtitle = "カスタムエクササイズの追加・編集・削除",
                icon = Icons.Default.FitnessCenter,
                isEnabled = true
            ),
            SettingItemData(
                item = SettingItem.APP_INFO,
                title = "アプリについて",
                subtitle = "バージョン情報・ライセンス",
                icon = Icons.Default.Info,
                isEnabled = true
            ),
            SettingItemData(
                item = SettingItem.NOTIFICATION_SETTINGS,
                title = "通知設定",
                subtitle = "リマインダー設定（準備中）",
                icon = Icons.Default.Notifications,
                isEnabled = false
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ヘッダー
        Text(
            text = "設定",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 設定項目リスト
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(settingItems) { settingItem ->
                SettingItemCard(
                    settingItem = settingItem,
                    onClick = {
                        viewModel.onSettingItemClicked(settingItem.item)
                        when (settingItem.item) {
                            SettingItem.EXERCISE_MANAGER -> onNavigateToExerciseManager()
                            SettingItem.APP_INFO -> {
                                // TODO: アプリ情報ダイアログ表示
                            }
                            else -> {
                                // 準備中項目は何もしない
                            }
                        }
                    }
                )
            }
            
            // アプリバージョン情報
            item {
                Spacer(modifier = Modifier.height(32.dp))
                AppVersionInfo(viewModel = viewModel)
            }
        }
    }

    // エラーメッセージ表示
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // TODO: Snackbarでエラー表示
            viewModel.clearError()
        }
    }
}

/**
 * 設定項目カード
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingItemCard(
    settingItem: SettingItemData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = if (settingItem.isEnabled) onClick else { {} },
        colors = CardDefaults.cardColors(
            containerColor = if (settingItem.isEnabled) 
                MaterialTheme.colorScheme.surface 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // アイコン
            Icon(
                imageVector = settingItem.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (settingItem.isEnabled) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // テキスト部分
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = settingItem.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (settingItem.isEnabled) 
                        MaterialTheme.colorScheme.onSurface 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = settingItem.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 矢印アイコン（有効な項目のみ）
            if (settingItem.isEnabled) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "移動",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * アプリバージョン情報
 */
@Composable
private fun AppVersionInfo(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "WorkRec",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Version ${viewModel.getAppVersion()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Build ${viewModel.getBuildNumber()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 設定項目データクラス
 */
private data class SettingItemData(
    val item: SettingItem,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val isEnabled: Boolean
)