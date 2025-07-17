# Design Document

## Overview

ワークアウト一覧画面を従来のリスト形式からカレンダー形式に変更する機能の設計文書です。既存のCalendarScreenの実装を参考にしながら、WorkoutListScreenをカレンダーベースのインターフェースに変更し、ユーザーが日付を選択することでその日のワークアウトを表示できるようにします。

この変更により、ユーザーはワークアウトの履歴をより直感的に把握でき、トレーニングの頻度やパターンを視覚的に確認できるようになります。

## Architecture

### 既存アーキテクチャとの統合

現在のアプリケーションは以下のアーキテクチャを採用しており、この設計もそれに従います：

- **Clean Architecture**: Presentation → Domain ← Data
- **MVVM Pattern**: ViewModelによる状態管理
- **Jetpack Compose**: 宣言的UI
- **Hilt**: 依存性注入

### 変更対象コンポーネント

1. **WorkoutListScreen**: カレンダーベースのUIに変更
2. **WorkoutViewModel**: カレンダー機能に必要な状態管理を追加
3. **既存のCalendar UseCase**: 再利用とカスタマイズ

## Components and Interfaces

### 1. UI Layer (Presentation)

#### WorkoutListScreen (変更)
```kotlin
@Composable
fun WorkoutListScreen(
    onNavigateToAddWorkout: () -> Unit,
    onNavigateToWorkoutDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: WorkoutViewModel
)
```

**主要な変更点:**
- リスト表示からカレンダー表示に変更
- 月間カレンダーグリッドの実装
- 日付選択機能の追加
- 選択日のワークアウト詳細表示

**新しいコンポーネント:**
- `MonthlyCalendarGrid`: 月間カレンダーのグリッド表示
- `CalendarDayCell`: 各日付のセル（ワークアウト有無の視覚的表示）
- `SelectedDateWorkoutList`: 選択日のワークアウト一覧
- `CalendarNavigationHeader`: 月間ナビゲーション

#### WorkoutViewModel (拡張)
```kotlin
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val getWorkoutDatesUseCase: GetWorkoutDatesUseCase,
    private val getWorkoutsByDateUseCase: GetWorkoutsByDateUseCase
) : ViewModel()
```

**新しい状態:**
```kotlin
data class WorkoutUiState(
    // 既存の状態
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val message: String? = null,
    
    // 新しいカレンダー関連の状態
    val currentMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate? = null,
    val workoutDates: Set<LocalDate> = emptySet(),
    val selectedDateWorkouts: List<Workout> = emptyList(),
    val isLoadingWorkouts: Boolean = false
)
```

### 2. Domain Layer

#### 既存UseCaseの活用
- `GetWorkoutDatesUseCase`: ワークアウト実施日の取得
- `GetWorkoutsByDateUseCase`: 特定日のワークアウト取得

#### 新しいUseCase (必要に応じて)
```kotlin
class GetWorkoutsForMonthUseCase @Inject constructor(
    private val workoutRepository: WorkoutRepository
) {
    operator fun invoke(yearMonth: YearMonth): Flow<List<Workout>>
}
```

### 3. Data Layer

既存のWorkoutRepositoryとRoom実装をそのまま活用します。

## Data Models

### カレンダー表示用のデータモデル

```kotlin
data class CalendarDay(
    val date: LocalDate,
    val hasWorkout: Boolean,
    val workoutCount: Int,
    val isToday: Boolean,
    val isSelected: Boolean,
    val isCurrentMonth: Boolean
)

data class MonthData(
    val yearMonth: YearMonth,
    val days: List<CalendarDay>,
    val workoutDates: Set<LocalDate>
)
```

## Error Handling

### エラーケース
1. **データ読み込みエラー**: ワークアウトデータの取得失敗
2. **日付選択エラー**: 無効な日付の選択
3. **ネットワークエラー**: オフライン時の対応（Room使用のため基本的に発生しない）

### エラー処理戦略
```kotlin
sealed class WorkoutCalendarError {
    object LoadingWorkoutsError : WorkoutCalendarError()
    object LoadingDatesError : WorkoutCalendarError()
    data class UnknownError(val message: String) : WorkoutCalendarError()
}
```

## Testing Strategy

### Unit Tests
1. **WorkoutViewModel**: カレンダー状態管理のテスト
2. **GetWorkoutsForMonthUseCase**: 月間データ取得のテスト
3. **CalendarDay**: データモデルのテスト

### Integration Tests
1. **WorkoutListScreen**: カレンダーUIの統合テスト
2. **日付選択フロー**: ユーザーインタラクションのテスト

### UI Tests
1. **カレンダーナビゲーション**: 月間移動のテスト
2. **日付選択**: タップ操作とワークアウト表示のテスト
3. **ワークアウト詳細表示**: 選択日のワークアウト一覧表示のテスト

## Implementation Details

### カレンダーグリッドの実装

```kotlin
@Composable
private fun MonthlyCalendarGrid(
    monthData: MonthData,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = modifier
    ) {
        // 曜日ヘッダー
        items(7) { dayOfWeek ->
            CalendarDayHeader(dayOfWeek)
        }
        
        // カレンダー日付
        items(monthData.days) { calendarDay ->
            CalendarDayCell(
                calendarDay = calendarDay,
                isSelected = calendarDay.date == selectedDate,
                onClick = { onDateSelected(calendarDay.date) }
            )
        }
    }
}
```

### 日付セルの視覚的表現

```kotlin
@Composable
private fun CalendarDayCell(
    calendarDay: CalendarDay,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onClick() }
            .background(
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    calendarDay.isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                },
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = calendarDay.date.dayOfMonth.toString(),
                color = when {
                    !calendarDay.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            
            if (calendarDay.hasWorkout) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
```

### 月間ナビゲーション

```kotlin
@Composable
private fun CalendarNavigationHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "前の月")
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${currentMonth.year}年${currentMonth.monthValue}月",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onTodayClick) {
                Text("今日")
            }
        }
        
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ChevronRight, contentDescription = "次の月")
        }
    }
}
```

## Performance Considerations

### 最適化戦略
1. **LazyVerticalGrid**: 大量の日付データの効率的な表示
2. **State Hoisting**: 不要な再コンポーズの回避
3. **Flow**: リアクティブなデータ更新
4. **Caching**: 月間データのキャッシュ

### メモリ管理
- 表示月の前後1ヶ月分のデータのみメモリに保持
- 古い月のデータは自動的にガベージコレクション対象

## Accessibility

### アクセシビリティ対応
1. **Content Description**: 各日付セルに適切な説明を追加
2. **Semantic Properties**: カレンダーの構造を明確に
3. **Focus Management**: キーボードナビゲーションの対応
4. **Screen Reader**: 日付とワークアウト情報の読み上げ対応

```kotlin
CalendarDayCell(
    modifier = Modifier.semantics {
        contentDescription = buildString {
            append("${calendarDay.date.monthNumber}月${calendarDay.date.dayOfMonth}日")
            if (calendarDay.hasWorkout) {
                append(", ワークアウト${calendarDay.workoutCount}件")
            }
            if (calendarDay.isToday) {
                append(", 今日")
            }
        }
        role = Role.Button
    }
)
```

## Migration Strategy

### 段階的な移行
1. **Phase 1**: 新しいカレンダーUIの実装
2. **Phase 2**: 既存のリスト表示からカレンダー表示への切り替え
3. **Phase 3**: 不要なコードの削除とリファクタリング

### 後方互換性
- 既存のWorkoutViewModelの機能は保持
- ナビゲーション構造は変更なし
- データモデルは既存のものを活用