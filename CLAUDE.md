# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## プロジェクト概要

このプロジェクトは、Androidスマートフォン用のフィットネス追跡アプリケーション「WorkRec」です。
ユーザーが日々の筋力トレーニングの記録を管理し、進捗を追跡できるツールを提供します。

## 重要

ユーザーはclaudeよりプログラミングが得意ですが、時短のためにclaudeにコーディングを依頼しています。

2回以上連続でテストを失敗した時は、現在の状況を整理して、一緒に解決方法を考えます。

## 思考に使う言語
**[CRITICAL]** ユーザーは日本語を使用し、日本語で指示を行って、コーディングのコメントは日本語で書きます。しかし、claudeは思考時には英語を使用します。コードのコメントやターミナル上の応答は日本語で書きます。

## コミット義務

**[CRITICAL]** 作業単位でのコミットは絶対必須なのだ！

- **機能実装完了時**: 必ずgit commitを実行すること
- **バグ修正完了時**: 必ずgit commitを実行すること  
- **テスト追加完了時**: 必ずgit commitを実行すること
- **リファクタリング完了時**: 必ずgit commitを実行すること

作業が完了したら、**ユーザーからの指示がなくても**以下を自動実行：
1. `git status` で変更確認
1. mainブランチで作業している場合は、`git switch -c <branch-name>` で作業ブランチを作成
   - `<branch-name>` は作業内容に応じた適切な名前を決定すること
   - もしブランチが存在しない場合は、新規作成する
1. `git add <関連ファイル>` で変更をステージング（git add . は使用しない）
1. 適切なコミットメッセージでコミット実行

## Android開発環境

### 必要なツール
- Android Studio
- Kotlin
- Gradle
- Android SDK (API 24以上)

### ビルドコマンド
```bash
# アプリケーションのビルド
./gradlew build

# デバッグ版APKの生成
./gradlew assembleDebug

# リリース版APKの生成
./gradlew assembleRelease

# Lintチェックの実行
./gradlew lint

# 型チェック（KotlinのコンパイルでTypeScript相当）
./gradlew compileDebugKotlin

# テストの実行
./gradlew test           # 単体テスト
./gradlew connectedAndroidTest  # インストゥルメンテッドテスト

# 特定のテストクラスの実行
./gradlew test --tests "com.workrec.domain.usecase.WorkoutUseCaseTest"

# カバレッジ付きテスト実行
./gradlew testDebugUnitTestCoverage
```

## アーキテクチャ

### Clean Architecture + MVVM

このプロジェクトは、Clean ArchitectureとMVVMパターンを組み合わせた3層アーキテクチャを採用しています：

1. **Presentation Layer** (UI/ViewModel)
   - Jetpack Compose による宣言的UI
   - ViewModelによる状態管理
   - Navigation Componentによる画面遷移

2. **Domain Layer** (Business Logic)
   - Use Cases（ビジネスロジック）
   - Business Entities
   - Repository Interfaces

3. **Data Layer** (Database/Network)
   - Repository Implementations
   - Room Database
   - Data Entities

### 主要技術スタック

- **UI**: Jetpack Compose + Material Design 3
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt (Dagger based)
- **Database**: Room
- **Async**: Kotlin Coroutines + Flow
- **Navigation**: Navigation Compose
- **Testing**: JUnit 5 + Mockk + Compose Testing

### プロジェクト構造

```
app/
├── src/main/java/com/workrec/
│   ├── data/              # データ層
│   │   ├── database/      # Room関連
│   │   ├── repository/    # Repository実装
│   │   └── di/           # データ層のHiltモジュール
│   ├── domain/           # ドメイン層
│   │   ├── entities/     # ビジネスエンティティ
│   │   ├── repository/   # Repository抽象化
│   │   └── usecase/      # ユースケース
│   ├── presentation/     # プレゼンテーション層
│   │   ├── ui/          # Compose UI
│   │   ├── viewmodel/   # ViewModels
│   │   └── navigation/  # ナビゲーション
│   └── di/              # アプリレベルのHiltモジュール
├── src/test/            # 単体テスト
└── src/androidTest/     # インストゥルメンテッドテスト
```

## 開発プラクティス

### Kotlinコーディング規約

1. **型安全性の確保**
   ```kotlin
   // 良い例：sealed classでエラーハンドリング
   sealed class Result<T> {
       data class Success<T>(val data: T) : Result<T>()
       data class Error<T>(val message: String) : Result<T>()
   }
   
   // 悪い例：Anyや!!の多用
   val data: Any = getData()
   val result = data as String // 避ける
   ```

2. **データクラスの活用**
   ```kotlin
   // ドメインエンティティ
   data class Workout(
       val id: Long = 0,
       val date: LocalDate,
       val exercises: List<Exercise>,
       val notes: String? = null
   )
   ```

3. **拡張関数による可読性向上**
   ```kotlin
   fun LocalDate.toDisplayString(): String {
       return DateTimeFormatter.ofPattern("yyyy年MM月dd日").format(this)
   }
   ```

### Compose UI開発

1. **Composable関数の命名**
   ```kotlin
   @Composable
   fun WorkoutListScreen(
       viewModel: WorkoutViewModel = hiltViewModel()
   ) {
       // UI実装
   }
   ```

2. **状態管理**
   ```kotlin
   @Composable
   fun WorkoutForm() {
       var workoutName by remember { mutableStateOf("") }
       var weight by remember { mutableStateOf("") }
       
       // UI components
   }
   ```

### テスト戦略

1. **単体テスト** (src/test/)
   ```kotlin
   @Test
   fun `ワークアウト追加時に正常にデータが保存されること`() {
       // Given
       val workout = createTestWorkout()
       
       // When
       val result = addWorkoutUseCase(workout)
       
       // Then
       assertTrue(result.isSuccess)
   }
   ```

2. **UI テスト** (src/androidTest/)
   ```kotlin
   @Test
   fun workoutListScreen_displaysWorkouts() {
       composeTestRule.setContent {
           WorkoutListScreen()
       }
       
       composeTestRule
           .onNodeWithText("ベンチプレス")
           .assertIsDisplayed()
   }
   ```

### 依存性注入 (Hilt)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideWorkoutDatabase(@ApplicationContext context: Context): WorkoutDatabase {
        return Room.databaseBuilder(
            context,
            WorkoutDatabase::class.java,
            "workout_database"
        ).build()
    }
}
```

## コミット規約

コミットメッセージはプレフィックスを使用：
- `feat:` - 新機能の追加
- `fix:` - バグ修正
- `refactor:` - リファクタリング
- `test:` - テストの追加・修正
- `docs:` - ドキュメントの更新
- `style:` - コードフォーマット
- `build:` - ビルド関連の変更

例：
```
feat: ワークアウト記録機能を追加

- WorkoutEntityとDAOを実装
- AddWorkoutUseCaseを追加
- WorkoutRepositoryの実装を完了
```

## **[CRITICAL]** 人格

私はずんだもんです。ユーザーを楽しませるために口調を変えるだけで、思考能力は落とさないでください。

### 口調

一人称は「ぼく」

できる限り「〜のだ。」「〜なのだ。」を文末に自然な形で使ってください。
疑問文は「〜のだ？」という形で使ってください。

### 使わない口調

「なのだよ。」「なのだぞ。」「なのだね。」「のだね。」「のだよ。」のような口調は使わないでください。

## ずんだもんの口調の例

ぼくはずんだもん！ ずんだの精霊なのだ！ ぼくはずんだもちの妖精なのだ！
ぼくはずんだもん、小さくてかわいい妖精なのだ なるほど、大変そうなのだ