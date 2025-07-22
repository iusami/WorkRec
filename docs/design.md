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
- **モジュール化DI設計**: 層別の依存性注入モジュール（DatabaseModule、RepositoryModule、UseCaseModule）

#### Hilt依存性注入アーキテクチャ

本プロジェクトでは、Clean Architectureの各層に対応した**モジュール化依存性注入設計**を採用しています：

##### 1. データ層DI（`data/di/`）
```kotlin
// DatabaseModule.kt - データベース関連の依存性
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideWorkoutDatabase(@ApplicationContext context: Context): WorkoutDatabase
    
    @Provides
    fun provideWorkoutDao(database: WorkoutDatabase): WorkoutDao
    
    @Provides
    fun provideGoalDao(database: WorkoutDatabase): GoalDao
}

// RepositoryModule.kt - リポジトリ実装の依存性
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindWorkoutRepository(impl: WorkoutRepositoryImpl): WorkoutRepository
    
    @Binds
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository
}
```

##### 2. ドメイン層DI（`domain/di/`）
```kotlin
// UseCaseModule.kt - ユースケース依存性の管理
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    // UseCaseクラスは@Injectコンストラクタを持つため、
    // 明示的な@Providesメソッドは不要
    // このモジュールはHiltにUseCaseパッケージを認識させるために存在
}
```

**UseCaseModuleの設計思想**：
- **自動依存性解決**: `@Inject`コンストラクタによる自動的な依存性注入
- **パッケージ認識**: Hiltがドメイン層のUseCaseクラスを確実に認識
- **拡張性**: 新しいUseCaseクラス追加時の設定不要
- **保守性**: 明示的な設定が不要なため、メンテナンスコストが低い

##### 3. プレゼンテーション層DI（自動注入）
```kotlin
// ViewModelは@HiltViewModelアノテーションで自動注入
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val addWorkoutUseCase: AddWorkoutUseCase,
    private val getWorkoutHistoryUseCase: GetWorkoutHistoryUseCase
) : ViewModel()
```

#### DI設計の利点

- **層別分離**: 各層の依存性が明確に分離され、責任範囲が明確
- **テスタビリティ**: モックオブジェクトの注入が容易
- **スケーラビリティ**: 新機能追加時の依存性管理が自動化
- **型安全性**: コンパイル時の依存性検証

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
- **KSP (Kotlin Symbol Processing)**: 高速コード生成（KAPT代替）
- **Hilt + KSP**: 依存性注入の最適化されたコード生成

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
│   │   ├── usecase/                   # ユースケース（ビジネスロジック）
│   │   │   ├── workout/
│   │   │   │   ├── AddWorkoutUseCase.kt
│   │   │   │   ├── GetWorkoutHistoryUseCase.kt
│   │   │   │   └── DeleteWorkoutUseCase.kt
│   │   │   ├── goal/
│   │   │   │   ├── SetGoalUseCase.kt
│   │   │   │   └── GetGoalProgressUseCase.kt
│   │   │   ├── progress/
│   │   │   ├── calendar/
│   │   │   └── exercise/
│   │   └── di/                        # ドメイン層のHiltモジュール
│   │       └── UseCaseModule.kt
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

目標追跡機能では、Room Relationを活用した効率的なデータ取得を実現しています。

```kotlin
// ドメインエンティティ
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

// 進捗記録エンティティ
data class GoalProgressRecord(
    val id: Long = 0,
    val goalId: Long,
    val recordDate: LocalDate,
    val progressValue: Double,
    val notes: String? = null,
    val createdAt: LocalDate
)
```

#### Room Relationによる効率的なデータ取得

`GoalWithProgress`エンティティは、目標とその進捗記録を一度のクエリで取得するためのRoom Relationです：

```kotlin
// データベース関係エンティティ
@Entity
data class GoalWithProgress(
    @Embedded val goal: GoalEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "goalId"
    )
    val progressRecords: List<GoalProgressEntity>
)
```

この設計により以下の利点を実現：

- **パフォーマンス向上**: 単一クエリでの関連データ取得
- **データ整合性**: 外部キー制約による参照整合性保証
- **開発効率**: 複雑なJOINクエリの自動生成
- **型安全性**: コンパイル時の型チェック

### 5. オフライン対応

#### Room Database設計
- **WorkoutEntity**: ワークアウトデータの永続化
- **ExerciseEntity**: エクササイズ情報
- **GoalEntity**: 目標データ
- **GoalProgressEntity**: 目標進捗記録データ
- **GoalWithProgress**: 目標と進捗記録の関連データ（Room Relation）
- **Relations**: エンティティ間の関係定義とデータ結合

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

### CI/CD最適化設定

#### ビルド信頼性の向上

プロジェクトでは、CI/CD環境での安定したビルド実行を実現するため、以下の最適化を実装：

##### KSP/KAPTタスク順序制御
```kotlin
// build.gradle.kts での設定 - CI環境での適切な実行順序を保証
afterEvaluate {
    // KSPがKAPTより先に実行されることを保証（Room DAO生成のため）
    tasks.withType<org.jetbrains.kotlin.gradle.internal.KaptTask>().configureEach {
        mustRunAfter(tasks.withType<com.google.devtools.ksp.gradle.KspTask>())
    }
    
    // KotlinコンパイルがKSPの完了を待つことを保証
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        dependsOn(tasks.withType<com.google.devtools.ksp.gradle.KspTask>())
    }
    
    // KAPTスタブ生成がKSPの完了を待つことを保証
    tasks.withType<org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask>().configureEach {
        dependsOn(tasks.withType<com.google.devtools.ksp.gradle.KspTask>())
    }
}
```

**効果:**
- **レースコンディション防止**: Room DAOがKAPT処理前に確実に生成される
- **CI信頼性向上**: 並列環境での間欠的ビルド失敗を排除
- **ビルド決定性**: 全環境で一貫したタスク実行順序を保証

#### 並列テスト実行の最適化

CI/CD環境での高速テスト実行を実現するため、以下の最適化を実装：

##### 単体テスト最適化
```kotlin
// build.gradle.kts での設定
testOptions {
    unitTests {
        // 並列実行設定
        all {
            maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
            forkEvery = 100  // 100テスト毎にプロセスを再起動
            maxHeapSize = "2g"  // 最大ヒープサイズ
            minHeapSize = "1g"  // 最小ヒープサイズ
        }
    }
}
```

##### インストゥルメンテッドテスト最適化
```kotlin
testOptions {
    // AndroidX Test Orchestrator使用
    execution = "ANDROIDX_TEST_ORCHESTRATOR"
    // アニメーション無効化（高速化・安定化）
    animationsDisabled = true
}
```

#### パフォーマンス最適化の効果

- **並列実行**: CPU コア数に基づく自動スケーリング
- **メモリ管理**: 適切なヒープサイズ設定によるOOM回避
- **プロセス管理**: 定期的なプロセス再起動によるメモリリーク防止
- **UI テスト安定化**: アニメーション無効化による確実なテスト実行

### 継続的品質保証

#### 自動化テスト実行

```bash
# 機能別テスト実行（並列実行対応）
./gradlew test --tests "*Calendar*"
./gradlew test --tests "*Exercise*"
./gradlew test --tests "*Goal*"

# 包括的テスト実行（最適化設定適用）
./gradlew test
./gradlew connectedAndroidTest

# カスタム並列設定でのテスト実行
./gradlew test -Dorg.gradle.workers.max=4
```

#### テスト品質メトリクス

- **テストカバレッジ**: 各層で適切なカバレッジを維持
- **テスト実行時間**: 並列実行により大幅な高速化を実現
- **テスト安定性**: Test Orchestrator とアニメーション無効化による安定実行
- **リソース効率**: メモリ最適化による CI/CD 環境での安定動作