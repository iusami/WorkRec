# Design Document
## 概要
このドキュメントは、プロジェクトの設計に関する情報を提供します。目的、機能要件、非機能要件、システムアーキテクチャ、データベース設計、API設計、セキュリティ要件、テスト計画、運用計画、メンテナンス計画、リスク管理、参考文献を含みます。

## アプリケーションの概要
このアプリケーションは、ユーザーが日々の筋力トレーニングの記録を管理し、進捗を追跡するためのツールです。ユーザーはトレーニングの内容、回数、重量などを記録し、グラフで進捗を可視化できます。

## 機能要件
- ユーザーはトレーニングの内容、回数、重量を記録できる。
- 記録されたデータはグラフで可視化され、進捗を確認できる。
- ユーザーは過去のトレーニング履歴を参照できる。
- ユーザーはトレーニングの目標を設定し、達成度を追跡できる。
- ユーザーはカレンダー形式でトレーニングの記録を確認できる。
- カレンダーは日、週、月単位で表示できる。
- アプリケーションは使いやすいインターフェースを提供する。

## 非機能要件
- アプリケーションはスマートフォンでの使用を想定している。
- レスポンシブデザインを採用し、様々な画面サイズに対応する。
- アプリケーションのパフォーマンスは、トレーニングデータが増えても快適に動作すること。
- セキュリティは、ユーザーデータの保護を最優先とする。
- アプリケーションはオフラインでも基本的な機能を提供する。
- ユーザーインターフェースは直感的で、学習コストが低いこと。
- アプリケーションは多言語対応を考慮せず、日本語のみをサポートする。

## システムアーキテクチャ

### 基本アーキテクチャ: MVVM + Clean Architecture

本アプリケーションでは、2024年のAndroid開発のベストプラクティスに基づき、MVVM（Model-View-ViewModel）パターンとClean Architectureを組み合わせたアーキテクチャを採用します。

#### アーキテクチャの階層構造

1. **プレゼンテーション層（Presentation Layer）**
   - Jetpack Composeによる宣言的UI
   - ViewModelによる状態管理
   - Navigation Componentによる画面遷移

2. **ドメイン層（Domain Layer）**
   - ビジネスロジックとユースケースの実装
   - エンティティ（Business Objects）の定義
   - リポジトリインターフェースの定義

3. **データ層（Data Layer）**
   - Repositoryパターンによるデータアクセスの抽象化
   - Room Databaseによるローカルデータ永続化
   - 将来的なAPI統合への対応

### アーキテクチャの利点

- **保守性**: 関心事の分離により、各層の責任が明確
- **テスタビリティ**: 依存性注入により単体テストが容易
- **スケーラビリティ**: 機能追加時の影響範囲を限定
- **再利用性**: ドメイン層のビジネスロジックは他のプラットフォームでも利用可能

## 技術スタック

### UI・ナビゲーション
- **Jetpack Compose**: モダンな宣言的UIツールキット
- **Navigation Component 3**: 型安全なナビゲーション（2024年リリース）
- **Material Design 3**: 一貫したUIコンポーネント

### データ永続化・状態管理
- **Room Database**: オフライン優先のローカルデータストレージ
- **Kotlin Coroutines & Flow**: リアクティブなデータストリーム
- **Repository Pattern**: データアクセスの抽象化
- **ViewModel with SavedStateHandle**: 設定変更に対応した状態管理
- **StateFlow/LiveData**: UI状態管理

### 依存性注入
- **Hilt**: Daggerベースの簡素化された依存性注入フレームワーク

### 非同期処理・パフォーマンス
- **Kotlin Coroutines**: 非同期処理とスレッド管理
- **Flow**: リアクティブプログラミング
- **Paging 3**: 大量データの効率的な表示

### テスト
- **JUnit 5**: 単体テスト
- **Hilt Testing**: 依存性注入のテスト
- **Compose Testing**: UIテスト
- **Room Testing**: データベーステスト

### ビルド・開発ツール
- **Kotlin DSL**: Gradleビルドスクリプト
- **Version Catalog**: 依存関係の一元管理
- **KSP (Kotlin Symbol Processing)**: コード生成の高速化

## プロジェクト構造

### ディレクトリ構造
```
app/
├── src/main/java/com/workrec/
│   ├── data/                          # データ層
│   │   ├── database/                  # Room関連
│   │   │   ├── entities/              # データベースエンティティ
│   │   │   ├── dao/                   # Data Access Objects
│   │   │   └── WorkoutDatabase.kt     # データベース定義
│   │   ├── repository/                # リポジトリ実装
│   │   │   ├── WorkoutRepositoryImpl.kt
│   │   │   └── GoalRepositoryImpl.kt
│   │   └── di/                        # データ層のHiltモジュール
│   │       └── DatabaseModule.kt
│   ├── domain/                        # ドメイン層
│   │   ├── entities/                  # ビジネスエンティティ
│   │   │   ├── Workout.kt
│   │   │   ├── Exercise.kt
│   │   │   └── Goal.kt
│   │   ├── repository/                # リポジトリインターフェース
│   │   │   ├── WorkoutRepository.kt
│   │   │   └── GoalRepository.kt
│   │   └── usecase/                   # ユースケース（ビジネスロジック）
│   │       ├── workout/
│   │       │   ├── AddWorkoutUseCase.kt
│   │       │   ├── GetWorkoutHistoryUseCase.kt
│   │       │   └── DeleteWorkoutUseCase.kt
│   │       └── goal/
│   │           ├── SetGoalUseCase.kt
│   │           └── GetGoalProgressUseCase.kt
│   ├── presentation/                  # プレゼンテーション層
│   │   ├── ui/                        # Compose UI
│   │   │   ├── screens/               # 各画面のComposable
│   │   │   │   ├── workout/
│   │   │   │   │   ├── WorkoutListScreen.kt
│   │   │   │   │   ├── AddWorkoutScreen.kt
│   │   │   │   │   └── WorkoutDetailScreen.kt
│   │   │   │   ├── calendar/
│   │   │   │   │   └── CalendarScreen.kt
│   │   │   │   ├── progress/
│   │   │   │   │   └── ProgressScreen.kt
│   │   │   │   └── goal/
│   │   │   │       └── GoalScreen.kt
│   │   │   ├── components/            # 再利用可能なUIコンポーネント
│   │   │   │   ├── WorkoutCard.kt
│   │   │   │   ├── ProgressChart.kt
│   │   │   │   └── CalendarView.kt
│   │   │   └── theme/                 # テーマとスタイル
│   │   │       ├── Color.kt
│   │   │       ├── Theme.kt
│   │   │       └── Type.kt
│   │   ├── viewmodel/                 # ViewModels
│   │   │   ├── WorkoutViewModel.kt
│   │   │   ├── CalendarViewModel.kt
│   │   │   ├── ProgressViewModel.kt
│   │   │   └── GoalViewModel.kt
│   │   └── navigation/                # ナビゲーション
│   │       ├── WorkRecNavigation.kt
│   │       └── Screen.kt
│   ├── di/                            # アプリレベルのHiltモジュール
│   │   └── AppModule.kt
│   └── WorkRecApplication.kt          # Application クラス
├── src/test/                          # 単体テスト
├── src/androidTest/                   # インストゥルメンテッドテスト
└── build.gradle.kts                   # ビルド設定
```

### 各層の責任と相互作用

#### プレゼンテーション層
- **UI (Composable関数)**: ユーザーインターフェースの定義と表示
- **ViewModel**: UI状態の管理、ユーザーアクションの処理
- **Navigation**: 画面間の遷移ロジック

#### ドメイン層
- **Entities**: アプリのコアビジネスオブジェクト
- **Use Cases**: 特定のビジネス操作を実行
- **Repository Interfaces**: データアクセスの抽象化

#### データ層
- **Repository Implementations**: ドメイン層のインターフェースを実装
- **Database (Room)**: ローカルデータの永続化
- **Data Transfer Objects**: データベース層とドメイン層間のデータ変換

## 主要機能の実装詳細

### 1. ワークアウト記録機能

#### データモデル
```kotlin
// ドメインエンティティ
data class Workout(
    val id: Long = 0,
    val date: LocalDate,
    val exercises: List<Exercise>,
    val totalDuration: Duration,
    val notes: String? = null
)

data class Exercise(
    val id: Long = 0,
    val name: String,
    val sets: List<ExerciseSet>,
    val category: ExerciseCategory
)

data class ExerciseSet(
    val reps: Int,
    val weight: Double,
    val restTime: Duration? = null
)
```

#### ユースケース
- **AddWorkoutUseCase**: 新しいワークアウトの追加
- **GetWorkoutHistoryUseCase**: 過去のワークアウト履歴取得
- **UpdateWorkoutUseCase**: ワークアウト情報の更新
- **DeleteWorkoutUseCase**: ワークアウトの削除

### 2. 進捗可視化機能

#### グラフ表示
- **Jetpack Compose Canvas**: カスタムチャートの描画
- **MPAndroidChart**: 高機能なグラフライブラリ（検討中）
- **データの時系列表示**: 重量、回数、ボリュームの推移

#### 実装パターン
```kotlin
@Composable
fun ProgressChart(
    data: List<ProgressData>,
    chartType: ChartType = ChartType.LINE
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // チャート描画ロジック
        drawProgressLines(data)
        drawDataPoints(data)
        drawAxes()
    }
}
```

### 3. カレンダー表示機能

#### カスタムカレンダーコンポーネント
- **LazyColumn + LazyRow**: 効率的なスクロール実装
- **日/週/月表示切り替え**: ViewModelでの状態管理
- **ワークアウト実施日のハイライト**: データベースクエリと連携

#### 実装例
```kotlin
@Composable
fun WorkoutCalendar(
    selectedDate: LocalDate,
    workoutDates: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(7)
    ) {
        items(calendarDays) { date ->
            CalendarDay(
                date = date,
                hasWorkout = date in workoutDates,
                isSelected = date == selectedDate,
                onClick = { onDateSelected(date) }
            )
        }
    }
}
```

### 4. 目標設定・追跡機能

#### 目標タイプ
- **重量目標**: 特定のエクササイズでの目標重量
- **回数目標**: 総回数や頻度の目標
- **継続目標**: 連続実施日数の目標

#### Progress Tracking
```kotlin
data class Goal(
    val id: Long = 0,
    val type: GoalType,
    val targetValue: Double,
    val currentValue: Double,
    val deadline: LocalDate?,
    val isCompleted: Boolean = false
) {
    val progressPercentage: Float
        get() = (currentValue / targetValue).coerceAtMost(1.0).toFloat()
}
```

### 5. オフライン対応

#### Room Database設計
- **WorkoutEntity**: ワークアウトデータの永続化
- **ExerciseEntity**: エクササイズ情報
- **GoalEntity**: 目標データ
- **Relations**: エンティティ間の関係定義

#### データ同期戦略
- **オフライン優先**: すべてのデータはまずローカルに保存
- **バックグラウンド同期**: 将来的なクラウド同期への対応
- **コンフリクト解決**: データの整合性保持

### 6. セキュリティ・プライバシー

#### データ保護
- **ローカル暗号化**: 機密データの暗号化保存
- **データ最小化**: 必要最小限のデータのみ収集
- **ユーザー制御**: データのエクスポート・削除機能

#### 実装方針
- **Android Keystore**: 暗号化キーの安全な管理
- **Room暗号化**: SQLCipherを使用した（オプション）
- **GDPR準拠**: プライバシー設定の提供

## テスト戦略とアーキテクチャ

### テスト設計原則

本プロジェクトでは、**関心事の分離**に基づいたテスト戦略を採用し、機能の複雑さに応じて適切なテストレベルを選択しています。

#### 階層化テストアプローチ

```
┌─────────────────────────────────────────┐
│        Integration Tests                │
│  • UI Workflow Tests                    │
│  • Component Integration Tests          │
│  • End-to-End User Scenarios           │
└─────────────────────────────────────────┘
                     │
┌─────────────────────────────────────────┐
│        Feature-Specific Tests           │
│  • Calendar Feature Tests               │
│  • Exercise Management Tests            │
│  • Goal Tracking Tests                  │
└─────────────────────────────────────────┘
                     │
┌─────────────────────────────────────────┐
│           Unit Tests                    │
│  • Domain Use Cases                     │
│  • Core ViewModel Logic                 │
│  • Utility Functions                    │
└─────────────────────────────────────────┘
```

### カレンダー機能のテスト設計

カレンダー機能は複雑な状態管理と UI インタラクションを含むため、**専用テストクラス**による分離テスト戦略を採用：

#### 1. 機能別テストクラス分離

- **`WorkoutCalendarFeatureTest.kt`**
  - カレンダー固有のビジネスロジック
  - 月間ナビゲーション、日付選択、データ読み込み
  - ViewModelとUse Caseの統合テスト

- **`WorkoutCalendarWorkflowTest.kt`**
  - 複雑なユーザーワークフローのテスト
  - 複数月ナビゲーション、日付選択の連続操作
  - 状態遷移の検証

- **`WorkoutCalendarIntegrationTest.kt`**
  - UI コンポーネントの統合テスト
  - Compose コンポーネント間の相互作用
  - アクセシビリティとレスポンシブデザイン

#### 2. テスト分離の利点

- **保守性**: 各テストクラスが単一の責任を持つ
- **実行速度**: 必要なテストのみを選択実行可能
- **可読性**: テストの目的が明確
- **拡張性**: 新機能追加時の影響範囲を限定

#### 3. ViewModelテストの最適化

`WorkoutViewModel` の単体テストは**コア機能**に集中：
- 基本的なワークアウト CRUD 操作
- エラーハンドリング
- 状態管理の基本動作

複雑なカレンダー機能は専用テストクラスで包括的にテスト。

### テストカバレッジ戦略

#### 機能別テストマトリックス

| 機能領域 | Unit Tests | Feature Tests | Integration Tests |
|---------|------------|---------------|-------------------|
| **ワークアウト CRUD** | ✅ ViewModel | ✅ Use Cases | ✅ UI Components |
| **カレンダー表示** | ✅ Utilities | ✅ Feature Tests | ✅ UI Workflows |
| **進捗追跡** | ✅ Calculations | ✅ Chart Logic | ✅ Screen Tests |
| **目標管理** | ✅ Business Logic | ✅ Progress Tracking | ✅ User Flows |

#### アクセシビリティテスト

- **`CalendarAccessibilityTest.kt`**: カレンダーコンポーネントのアクセシビリティ準拠
- **レスポンシブデザインテスト**: 画面サイズ対応の検証
- **キーボードナビゲーション**: 支援技術対応

### 継続的品質保証

#### 自動化テスト実行

```bash
# 機能別テスト実行
./gradlew test --tests "*Calendar*"
./gradlew test --tests "*Exercise*"
./gradlew test --tests "*Goal*"

# 包括的テスト実行
./gradlew test
./gradlew connectedAndroidTest
```

#### テスト品質メトリクス

- **テストカバレッジ**: 各層で適切なカバレッジを維持
- **テスト実行時間**: 機能分離により高速実行を実現
- **テスト安定性**: モックとテストダブルによる安定したテスト環境