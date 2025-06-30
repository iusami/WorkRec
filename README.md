# WorkRec

A modern Android fitness tracking application built with Clean Architecture, MVVM, and Jetpack Compose.

## Overview

WorkRec is a comprehensive fitness tracking application designed for Android smartphones. It provides users with powerful tools to record daily strength training workouts, track progress over time, and manage fitness goals effectively.

### Key Features

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

The project maintains comprehensive test coverage with **57 tests** achieving **100% success rate**.

### Test Structure

- **Unit Tests** (`src/test/`): Business logic and ViewModels
- **Instrumented Tests** (`src/androidTest/`): UI components and integration tests

### Testing Frameworks

- **JUnit 4**: Core testing framework
- **Mockk**: Mocking library for Kotlin
- **Truth**: Fluent assertion library by Google
- **Compose Testing**: UI testing for Jetpack Compose
- **Coroutines Test**: Testing utilities for coroutines

### Running Tests

```bash
# Quick test run
./gradlew test

# Detailed test report (generates HTML report)
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

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For detailed development guidance and architecture decisions, refer to [CLAUDE.md](CLAUDE.md).

---

Built with modern Android development best practices and Clean Architecture principles.