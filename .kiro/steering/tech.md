# Technology Stack & Build System

## Build System
- **Gradle**: 8.5 with Kotlin DSL
- **Android Gradle Plugin**: 8.2.2
- **Kotlin**: 1.9.22
- **Target SDK**: 34 (Android 14)
- **Min SDK**: 24 (Android 7.0)

## Core Technologies

### UI & Navigation
- **Jetpack Compose**: 1.5.8 - Modern declarative UI toolkit
- **Material Design 3**: Latest Material Design components
- **Navigation Compose**: 2.7.6 - Type-safe navigation
- **Compose BOM**: 2024.02.00 - Version management for all Compose libraries

### Architecture & DI
- **Hilt**: 2.48 - Dependency injection framework
- **MVVM + Clean Architecture**: Separation of concerns across presentation, domain, and data layers
- **Repository Pattern**: Data access abstraction

### Data & Persistence
- **Room Database**: 2.7.0-alpha01 - Local SQLite database with type safety
- **Kotlin Coroutines**: 1.7.3 - Asynchronous programming
- **Kotlin Flow**: Reactive data streams
- **KSP**: Kotlin Symbol Processing for faster compilation

### Additional Libraries
- **Kotlinx DateTime**: 0.5.0 - Date/time handling
- **Kotlinx Serialization**: 1.6.2 - JSON serialization
- **Compose Charts**: 0.0.13 - Progress visualization
- **Core Library Desugaring**: Java 8+ API support for older Android versions

## Common Build Commands

### Development
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK  
./gradlew assembleRelease

# Install debug build to device
./gradlew installDebug

# Clean build
./gradlew clean
```

### Testing
```bash
# Run all unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run tests with coverage
./gradlew testDebugUnitTestCoverage
```

### Code Quality
```bash
# Run lint analysis
./gradlew lint

# Compile Kotlin (type checking)
./gradlew compileDebugKotlin
```

## Testing Framework
- **JUnit 4**: Core testing framework
- **Mockk**: Mocking library for Kotlin
- **Truth**: Fluent assertions by Google
- **Compose Testing**: UI testing for Jetpack Compose
- **Coroutines Test**: Testing utilities for async code

## Development Requirements
- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: 11
- **Android SDK**: API 24+