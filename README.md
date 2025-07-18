# WorkRec

A modern Android fitness tracking application built with Clean Architecture, MVVM, and Jetpack Compose.

## Overview

WorkRec is a comprehensive fitness tracking application designed for Android smartphones. It provides users with powerful tools to record daily strength training workouts, track progress over time, and manage fitness goals effectively.

### Key Features

**Test documentation change for CI optimization**

- **Workout Recording**: Log exercises, sets, weights, and repetitions
- **Progress Tracking**: Visualize fitness progress with charts and analytics  
- **Goal Management**: Set and track fitness goals with progress monitoring
- **Calendar Integration**: View workout history in calendar format
- **Clean UI**: Modern Material Design 3 interface built with Jetpack Compose

## Architecture

This project follows **Clean Architecture** principles combined with the **MVVM** pattern, organized into three distinct layers:

### Layer Structure

```
┌─────────────────────────────────────────┐
│            Presentation Layer           │
│  • Jetpack Compose UI                   │
│  • ViewModels                           │
│  • Navigation                           │
└─────────────────────────────────────────┘
                     │
┌─────────────────────────────────────────┐
│             Domain Layer                │
│  • Use Cases (Business Logic)           │
│  • Business Entities                    │
│  • Repository Interfaces                │
└─────────────────────────────────────────┘
                     │
┌─────────────────────────────────────────┐
│              Data Layer                 │
│  • Repository Implementations           │
│  • Room Database                        │
│  • Data Entities                        │
└─────────────────────────────────────────┘
```

### Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| **UI Framework** | Jetpack Compose | 1.5.8 |
| **Architecture** | MVVM + Clean Architecture | - |
| **Dependency Injection** | Hilt | 2.48 |
| **Database** | Room | 2.7.0-alpha01 |
| **Navigation** | Navigation Compose | 2.7.6 |
| **Async Programming** | Kotlin Coroutines + Flow | 1.7.3 |
| **Language** | Kotlin | 1.9.22 |
| **Build System** | Gradle | 8.5 |
| **Testing** | JUnit + Mockk + Truth | - |

## Prerequisites

Before setting up the project, ensure you have:

- **Android Studio** Hedgehog (2023.1.1) or later
- **Android SDK** API 24 (Android 7.0) or higher
- **Java Development Kit** JDK 11
- **Git** for version control

## Project Structure

```
app/
├── src/main/java/com/workrec/
│   ├── data/                    # Data Layer
│   │   ├── database/           # Room database, DAOs, entities
│   │   ├── repository/         # Repository implementations
│   │   └── di/                # Data layer Hilt modules
│   ├── domain/                 # Domain Layer
│   │   ├── entities/          # Business entities
│   │   ├── repository/        # Repository interfaces
│   │   └── usecase/           # Use cases (business logic)
│   ├── presentation/          # Presentation Layer
│   │   ├── ui/               # Jetpack Compose screens & components
│   │   ├── viewmodel/        # ViewModels
│   │   └── navigation/       # Navigation setup
│   └── di/                   # Application-level Hilt modules
├── src/test/                  # Unit tests
└── src/androidTest/          # Instrumented tests
```

## Setup & Installation

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/WorkRec.git
cd WorkRec
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Select "Open an Existing Project"
3. Navigate to the cloned WorkRec directory
4. Click "Open"

### 3. Sync Project

Android Studio will automatically prompt to sync the project. If not:
- Click "Sync Now" in the notification bar, or
- Select "File" → "Sync Project with Gradle Files"

### 4. Build the Project

```bash
./gradlew build
```

## Build Commands

### Development Builds

```bash
# Build the entire project (debug + release)
./gradlew build

# Build debug APK only
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

### Code Quality & Analysis

```bash
# Run lint analysis
./gradlew lint

# Compile Kotlin (type checking)
./gradlew compileDebugKotlin
```

### Testing

```bash
# Run all unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run tests with coverage report
./gradlew testDebugUnitTestCoverage

# Run specific test class
./gradlew test --tests "com.workrec.domain.usecase.WorkoutUseCaseTest"
```

### Installation

```bash
# Install debug APK to connected device
./gradlew installDebug

# Uninstall from device
./gradlew uninstallDebug
```

## Testing

The project maintains comprehensive test coverage with a robust testing strategy that separates concerns across different test types.

### Test Architecture

The testing strategy follows a layered approach that mirrors the application architecture:

#### Unit Tests (`src/test/`)
- **Domain Layer Tests**: Use cases and business logic validation
- **ViewModel Tests**: State management and UI logic (core functionality)
- **Feature-Specific Tests**: Dedicated test classes for complex features
- **Utility Tests**: Calendar utilities, responsive design helpers

#### Integration Tests (`src/androidTest/`)
- **UI Component Tests**: Individual Compose component behavior
- **Workflow Tests**: Complex user interaction scenarios
- **Screen Integration Tests**: Full screen functionality and navigation

### Calendar Feature Testing Strategy

The calendar functionality uses a **specialized testing approach** with dedicated test classes:

- **`WorkoutCalendarFeatureTest.kt`**: Calendar-specific business logic and ViewModel integration
- **`WorkoutCalendarWorkflowTest.kt`**: Complex calendar user workflows and interactions
- **`WorkoutCalendarIntegrationTest.kt`**: Calendar UI component integration testing
- **`CalendarAccessibilityTest.kt`**: Accessibility compliance and responsive design

This separation ensures:
- **Focused Testing**: Each test class has a single responsibility
- **Maintainability**: Calendar tests are isolated from core workout functionality
- **Performance**: Faster test execution through targeted test suites

### Testing Frameworks

- **JUnit 4**: Core testing framework
- **Mockk**: Mocking library for Kotlin with coroutines support
- **Truth**: Fluent assertion library by Google
- **Compose Testing**: UI testing for Jetpack Compose components
- **Coroutines Test**: Testing utilities for async operations

### Running Tests

```bash
# Run all unit tests
./gradlew test

# Run specific feature tests
./gradlew test --tests "*Calendar*"

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Generate detailed test report
./gradlew test --continue

# View test results
open app/build/reports/tests/testDebugUnitTest/index.html
```

## Development

### Code Style

This project follows Kotlin coding conventions and Clean Architecture principles. For detailed development guidelines, see [CLAUDE.md](CLAUDE.md).

### Commit Convention

We use conventional commit messages:

- `feat:` New features
- `fix:` Bug fixes  
- `refactor:` Code refactoring
- `test:` Test additions/modifications
- `docs:` Documentation updates
- `build:` Build system changes

### Dependencies

Key dependencies are managed through version catalogs in `build.gradle.kts`:

- **Compose BOM**: Manages all Compose library versions
- **Hilt**: Compile-time dependency injection
- **Room**: Type-safe database access
- **Navigation**: Type-safe navigation for Compose

## CI/CD Pipeline

This project uses an automated CI/CD pipeline powered by GitHub Actions for continuous integration and deployment.

### Continuous Integration

**Executed on every commit:**
- Unit test execution
- Lint analysis
- Build verification

**Executed on pull requests:**
- Code quality checks
- Test result reporting
- Build success validation

### Automated Releases

**Automatic execution on release branches (`release/*`):**
- Complete test suite execution
- Release APK build
- Automatic GitHub Release creation
- Version format: `YYYYMMDD` (e.g., `20240117`)

### Build Artifacts

Each build generates the following artifacts:
- **Debug APK**: For development and testing
- **Release APK**: For production distribution
- **Test Reports**: Coverage and detailed results
- **Lint Results**: Code quality analysis

### Manual Releases

Manual release creation available through GitHub UI:
- Custom release notes
- Version suffix support (`-hotfix`, `-beta`)
- Prerelease configuration

For detailed information, see [`.github/workflows/README.md`](.github/workflows/README.md).

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Write tests for your changes
4. Ensure all tests pass (`./gradlew test`)
5. Commit your changes (`git commit -m 'feat: add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

### Development Workflow

1. Make sure tests pass before committing
2. Follow the established architecture patterns
3. Add tests for new functionality
4. Update documentation as needed
5. CI/CD pipeline will automatically validate your changes

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Documentation

### Architecture & Design
- **[Design Document](docs/design.md)**: Comprehensive system architecture and design decisions
- **[Calendar Implementation Guide](docs/calendar-implementation.md)**: Detailed calendar feature architecture and testing strategy

### Development Guidelines
- **[CLAUDE.md](CLAUDE.md)**: Development guidance and coding standards

## テスト状況

### エクササイズ管理機能のテスト結果

2025年7月3日に実施したカスタムエクササイズ管理機能の包括的テスト結果：

#### ✅ 実装済み機能の確認
- **ExerciseManagerScreen** - 完全実装済み（検索・フィルタ・CRUD操作）
- **ExerciseManagerViewModel** - 高機能実装済み（キャッシュ・編集状態管理）
- **カスタムエクササイズ機能** - 追加・編集・削除機能完備
- **検索・フィルタリング機能** - リアルタイム検索・カテゴリー別フィルター

#### ✅ テスト実行結果
- **ビルド**: `./gradlew assembleDebug` - 成功
- **単体テスト**: `./gradlew test` - 成功（既存テスト69個実行）
- **エクササイズエンティティテスト**: 全て正常（maxWeight、totalVolume、averageWeight計算）

#### 📋 Playwrightテストシナリオ生成
- **基本機能テスト**: ExerciseManagerBasicFunctionality
- **CRUD操作テスト**: CustomExerciseCRUDOperations
- **実際のE2Eテスト**: Android UIテスト（Compose Testing）として実装可能

#### 🏗 アーキテクチャ品質
- Clean Architecture + MVVM実装
- Jetpack Compose による宣言的UI
- Hilt依存性注入
- Room Database永続化
- Kotlinコルーチン非同期処理

エクササイズ管理機能は本格的なフィットネスアプリに必要な全ての機能が高品質で実装されています。

---

Built with modern Android development best practices and Clean Architecture principles.