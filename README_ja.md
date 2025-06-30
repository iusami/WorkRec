# WorkRec

Clean Architecture、MVVM、Jetpack Composeで構築されたモダンなAndroidフィットネス追跡アプリケーション

## 概要

WorkRecは、Androidスマートフォン向けの包括的なフィットネス追跡アプリケーションです。ユーザーが日々の筋力トレーニングのワークアウトを記録し、時間の経過とともに進捗を追跡し、フィットネス目標を効果的に管理するための強力なツールを提供します。

### 主要機能

- **ワークアウト記録**: エクササイズ、セット、重量、レップ数のログ記録
- **進捗追跡**: チャートと分析によるフィットネス進捗の可視化
- **目標管理**: 進捗モニタリング付きのフィットネス目標の設定と追跡
- **カレンダー統合**: カレンダー形式でのワークアウト履歴表示
- **クリーンなUI**: Jetpack Composeで構築されたモダンなMaterial Design 3インターフェース

## アーキテクチャ

このプロジェクトは**Clean Architecture**の原則と**MVVM**パターンを組み合わせ、3つの明確なレイヤーに整理されています：

### レイヤー構造

```
┌─────────────────────────────────────────┐
│          プレゼンテーション層             │
│  • Jetpack Compose UI                   │
│  • ViewModels                           │
│  • Navigation                           │
└─────────────────────────────────────────┘
                     │
┌─────────────────────────────────────────┐
│            ドメイン層                    │
│  • Use Cases（ビジネスロジック）         │
│  • ビジネスエンティティ                  │
│  • Repositoryインターフェース            │
└─────────────────────────────────────────┘
                     │
┌─────────────────────────────────────────┐
│             データ層                     │
│  • Repository実装                       │
│  • Room Database                        │
│  • データエンティティ                    │
└─────────────────────────────────────────┘
```

### 技術スタック

| コンポーネント | 技術 | バージョン |
|---------------|------|-----------|
| **UIフレームワーク** | Jetpack Compose | 1.5.8 |
| **アーキテクチャ** | MVVM + Clean Architecture | - |
| **依存性注入** | Hilt | 2.48 |
| **データベース** | Room | 2.7.0-alpha01 |
| **ナビゲーション** | Navigation Compose | 2.7.6 |
| **非同期プログラミング** | Kotlin Coroutines + Flow | 1.7.3 |
| **言語** | Kotlin | 1.9.22 |
| **ビルドシステム** | Gradle | 8.5 |
| **テスト** | JUnit + Mockk + Truth | - |

## 前提条件

プロジェクトをセットアップする前に、以下が必要です：

- **Android Studio** Hedgehog (2023.1.1) 以降
- **Android SDK** API 24 (Android 7.0) 以上
- **Java Development Kit** JDK 11
- **Git** バージョン管理用

## プロジェクト構造

```
app/
├── src/main/java/com/workrec/
│   ├── data/                    # データ層
│   │   ├── database/           # Roomデータベース、DAO、エンティティ
│   │   ├── repository/         # Repository実装
│   │   └── di/                # データ層Hiltモジュール
│   ├── domain/                 # ドメイン層
│   │   ├── entities/          # ビジネスエンティティ
│   │   ├── repository/        # Repositoryインターフェース
│   │   └── usecase/           # Use Cases（ビジネスロジック）
│   ├── presentation/          # プレゼンテーション層
│   │   ├── ui/               # Jetpack Compose画面とコンポーネント
│   │   ├── viewmodel/        # ViewModels
│   │   └── navigation/       # ナビゲーション設定
│   └── di/                   # アプリケーションレベルHiltモジュール
├── src/test/                  # 単体テスト
└── src/androidTest/          # インストゥルメンテッドテスト
```

## セットアップとインストール

### 1. リポジトリのクローン

```bash
git clone https://github.com/your-username/WorkRec.git
cd WorkRec
```

### 2. Android Studioで開く

1. Android Studioを起動
2. 「既存のプロジェクトを開く」を選択
3. クローンしたWorkRecディレクトリに移動
4. 「開く」をクリック

### 3. プロジェクトの同期

Android Studioが自動的にプロジェクトの同期を促します。表示されない場合：
- 通知バーの「今すぐ同期」をクリック、または
- 「ファイル」→「プロジェクトをGradleファイルと同期」を選択

### 4. プロジェクトのビルド

```bash
./gradlew build
```

## ビルドコマンド

### 開発ビルド

```bash
# プロジェクト全体をビルド（デバッグ + リリース）
./gradlew build

# デバッグAPKのみビルド
./gradlew assembleDebug

# リリースAPKをビルド
./gradlew assembleRelease
```

### コード品質と解析

```bash
# Lint解析を実行
./gradlew lint

# Kotlinをコンパイル（型チェック）
./gradlew compileDebugKotlin
```

### テスト

```bash
# 全ての単体テストを実行
./gradlew test

# インストゥルメンテッドテストを実行（接続されたデバイス/エミュレータが必要）
./gradlew connectedAndroidTest

# カバレッジレポート付きでテストを実行
./gradlew testDebugUnitTestCoverage

# 特定のテストクラスを実行
./gradlew test --tests "com.workrec.domain.usecase.WorkoutUseCaseTest"
```

### インストール

```bash
# 接続されたデバイスにデバッグAPKをインストール
./gradlew installDebug

# デバイスからアンインストール
./gradlew uninstallDebug
```

## テスト

プロジェクトは**57のテスト**で**100%の成功率**を達成する包括的なテストカバレッジを維持しています。

### テスト構造

- **単体テスト** (`src/test/`): ビジネスロジックとViewModels
- **インストゥルメンテッドテスト** (`src/androidTest/`): UIコンポーネントと統合テスト

### テストフレームワーク

- **JUnit 4**: コアテストフレームワーク
- **Mockk**: Kotlin用モックライブラリ
- **Truth**: Googleによる流暢なアサーションライブラリ
- **Compose Testing**: Jetpack Compose用UIテスト
- **Coroutines Test**: コルーチン用テストユーティリティ

### テストの実行

```bash
# クイックテスト実行
./gradlew test

# 詳細テストレポート（HTMLレポートを生成）
./gradlew test --continue

# テスト結果の表示
open app/build/reports/tests/testDebugUnitTest/index.html
```

## 開発

### コードスタイル

このプロジェクトはKotlinコーディング規約とClean Architectureの原則に従っています。詳細な開発ガイドラインについては、[CLAUDE.md](CLAUDE.md)を参照してください。

### コミット規約

コンベンショナルコミットメッセージを使用します：

- `feat:` 新機能
- `fix:` バグ修正
- `refactor:` コードリファクタリング
- `test:` テストの追加/修正
- `docs:` ドキュメントの更新
- `build:` ビルドシステムの変更

### 依存関係

主要な依存関係は`build.gradle.kts`でバージョンカタログを通じて管理されています：

- **Compose BOM**: 全てのComposeライブラリのバージョンを管理
- **Hilt**: コンパイル時依存性注入
- **Room**: 型安全なデータベースアクセス
- **Navigation**: Compose用の型安全なナビゲーション

## 貢献

1. リポジトリをフォーク
2. 機能ブランチを作成（`git checkout -b feature/amazing-feature`）
3. 変更に対するテストを作成
4. 全てのテストが通ることを確認（`./gradlew test`）
5. 変更をコミット（`git commit -m 'feat: 素晴らしい機能を追加'`）
6. ブランチにプッシュ（`git push origin feature/amazing-feature`）
7. Pull Requestを開く

### 開発ワークフロー

1. コミット前にテストが通ることを確認
2. 確立されたアーキテクチャパターンに従う
3. 新機能にはテストを追加
4. 必要に応じてドキュメントを更新

## ライセンス

このプロジェクトはMITライセンスの下でライセンスされています - 詳細は[LICENSE](LICENSE)ファイルを参照してください。

## サポート

詳細な開発ガイダンスとアーキテクチャの決定については、[CLAUDE.md](CLAUDE.md)を参照してください。

---

モダンなAndroid開発のベストプラクティスとClean Architectureの原則で構築されています。