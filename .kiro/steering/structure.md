# Project Structure & Architecture

## Clean Architecture Layers

The project follows Clean Architecture with MVVM pattern, organized into three distinct layers:

### 1. Presentation Layer (`presentation/`)
- **UI Components** (`ui/screens/`, `ui/components/`): Jetpack Compose screens and reusable components
- **ViewModels** (`viewmodel/`): State management and UI logic
- **Navigation** (`navigation/`): Screen routing and navigation logic
- **Theme** (`ui/theme/`): Material Design 3 theming and styling

### 2. Domain Layer (`domain/`)
- **Entities** (`entities/`): Core business objects (Workout, Exercise, Goal, etc.)
- **Use Cases** (`usecase/`): Business logic organized by feature (workout/, goal/, progress/, etc.)
- **Repository Interfaces** (`repository/`): Data access contracts

### 3. Data Layer (`data/`)
- **Database** (`database/`): Room entities, DAOs, converters, and migrations
- **Repository Implementations** (`repository/`): Concrete implementations of domain interfaces
- **Dependency Injection** (`di/`): Hilt modules for data layer

## Package Structure

```
com.workrec/
├── data/
│   ├── database/
│   │   ├── entities/          # Room database entities
│   │   ├── dao/              # Data Access Objects
│   │   ├── converters/       # Type converters for Room
│   │   ├── migrations/       # Database schema migrations
│   │   └── WorkoutDatabase.kt
│   ├── repository/           # Repository implementations
│   └── di/                   # Data layer DI modules
├── domain/
│   ├── entities/             # Business entities
│   ├── repository/           # Repository interfaces
│   └── usecase/             # Business logic by feature
│       ├── workout/
│       ├── goal/
│       ├── progress/
│       ├── calendar/
│       └── exercise/
├── presentation/
│   ├── ui/
│   │   ├── screens/         # Feature-based screen organization
│   │   │   ├── workout/
│   │   │   ├── goal/
│   │   │   ├── progress/
│   │   │   ├── calendar/
│   │   │   ├── exercise/
│   │   │   └── settings/
│   │   ├── components/      # Reusable UI components
│   │   ├── theme/          # Material Design 3 theming
│   │   └── utils/          # UI utilities
│   ├── viewmodel/          # ViewModels for each feature
│   └── navigation/         # Navigation setup
├── di/                     # App-level DI modules
├── MainActivity.kt
└── WorkRecApplication.kt
```

## Naming Conventions

### Files & Classes
- **Entities**: `Workout.kt`, `Exercise.kt`, `Goal.kt`
- **Use Cases**: `AddWorkoutUseCase.kt`, `GetGoalProgressUseCase.kt`
- **ViewModels**: `WorkoutViewModel.kt`, `GoalDetailViewModel.kt`
- **Screens**: `WorkoutListScreen.kt`, `AddGoalScreen.kt`
- **Components**: `WorkoutCard.kt`, `ProgressChart.kt`
- **DAOs**: `WorkoutDao.kt`, `ExerciseDao.kt`
- **Repositories**: `WorkoutRepositoryImpl.kt` (implementation), `WorkoutRepository.kt` (interface)

### Packages
- Feature-based organization within each layer
- Use lowercase with underscores for multi-word features: `exercise_template/`

## Architecture Rules

### Dependency Direction
- Presentation → Domain ← Data
- Inner layers should not depend on outer layers
- Use dependency inversion through interfaces

### Data Flow
1. **UI** triggers actions via ViewModels
2. **ViewModels** call Use Cases from Domain layer
3. **Use Cases** coordinate business logic and call Repository interfaces
4. **Repository implementations** handle data persistence via Room/API

### Testing Strategy
- **Unit Tests** (`src/test/`): Domain layer use cases and ViewModels
- **Integration Tests** (`src/androidTest/`): Repository implementations and database operations
- **UI Tests** (`src/androidTest/`): Compose UI components and user flows

## Key Architectural Patterns

### Repository Pattern
- Abstract data access through interfaces in domain layer
- Concrete implementations in data layer handle Room database operations

### Use Case Pattern
- Single responsibility business operations
- Coordinate between repositories and handle business rules
- Easy to test and maintain

### MVVM with Compose
- ViewModels manage UI state using StateFlow/LiveData
- Compose UI observes state changes reactively
- Unidirectional data flow from UI to ViewModel to Use Cases