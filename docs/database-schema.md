# Database Schema & Data Model

## Overview

WorkRec uses Room Database for local data persistence with a well-structured schema that supports complex relationships between entities. The database design follows Clean Architecture principles with clear separation between data entities and domain models.

## Entity Relationship Diagram

```
┌─────────────────┐    ┌─────────────────────┐
│   GoalEntity    │    │ GoalProgressEntity  │
│                 │    │                     │
│ • id (PK)       │◄───┤ • id (PK)           │
│ • type          │    │ • goalId (FK)       │
│ • title         │    │ • recordDate        │
│ • targetValue   │    │ • progressValue     │
│ • currentValue  │    │ • notes             │
│ • deadline      │    │ • createdAt         │
│ • isCompleted   │    └─────────────────────┘
│ • createdAt     │
│ • updatedAt     │
└─────────────────┘
        │
        │ (Room Relation)
        ▼
┌─────────────────────────────────────────┐
│         GoalWithProgress                │
│                                         │
│ • goal: GoalEntity (@Embedded)          │
│ • progressRecords: List<GoalProgress>   │
│   (@Relation)                           │
└─────────────────────────────────────────┘
```

## Core Entities

### GoalEntity

The primary entity for storing fitness goals with comprehensive tracking capabilities.

```kotlin
@Entity(tableName = "goals")
@TypeConverters(DateConverters::class, GoalTypeConverters::class)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: GoalType,                    // Weight, Reps, Frequency, etc.
    val title: String,                     // User-defined goal name
    val description: String? = null,       // Optional detailed description
    val targetValue: Double,               // Target value to achieve
    val currentValue: Double = 0.0,        // Current progress value
    val unit: String,                      // Unit of measurement (kg, reps, days)
    val deadline: LocalDate? = null,       // Optional deadline
    val isCompleted: Boolean = false,      // Completion status
    val createdAt: LocalDate,             // Creation timestamp
    val updatedAt: LocalDate              // Last update timestamp
)
```

**Key Features:**
- Auto-generated primary key for unique identification
- Flexible goal types supporting various fitness metrics
- Optional deadline for time-bound goals
- Completion tracking with boolean flag
- Audit trail with creation and update timestamps

### GoalProgressEntity

Tracks individual progress records for goals, enabling detailed progress history.

```kotlin
@Entity(
    tableName = "goal_progress",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE    // Cascade delete for data integrity
        )
    ]
)
@TypeConverters(DateConverters::class)
data class GoalProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalId: Long,                      // Foreign key to GoalEntity
    val recordDate: LocalDate,             // Date of progress record
    val progressValue: Double,             // Progress value at this date
    val notes: String? = null,             // Optional progress notes
    val createdAt: LocalDate              // Record creation timestamp
)
```

**Key Features:**
- Foreign key relationship with CASCADE delete for data integrity
- Date-based progress tracking for timeline visualization
- Optional notes for contextual information
- Immutable progress records for historical accuracy

### GoalWithProgress (Room Relation)

A powerful Room Relation entity that combines goals with their progress records in a single query.

```kotlin
data class GoalWithProgress(
    @Embedded val goal: GoalEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "goalId"
    )
    val progressRecords: List<GoalProgressEntity>
)
```

**Architectural Benefits:**
- **Performance Optimization**: Single query retrieves related data
- **Data Consistency**: Automatic relationship management
- **Type Safety**: Compile-time relationship validation
- **Developer Productivity**: Eliminates complex JOIN queries

## Domain Model Mapping

### Entity-to-Domain Conversion

The data layer provides seamless conversion between database entities and domain models:

```kotlin
// GoalEntity to Domain Goal
fun GoalEntity.toDomainModel(): Goal {
    return Goal(
        id = id,
        type = type,
        title = title,
        description = description,
        targetValue = targetValue,
        currentValue = currentValue,
        unit = unit,
        deadline = deadline,
        isCompleted = isCompleted,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// Domain Goal to GoalEntity
fun Goal.toEntity(): GoalEntity {
    return GoalEntity(
        id = id,
        type = type,
        title = title,
        description = description,
        targetValue = targetValue,
        currentValue = currentValue,
        unit = unit,
        deadline = deadline,
        isCompleted = isCompleted,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
```

### Progress Record Conversion

```kotlin
// GoalProgressEntity to Domain GoalProgressRecord
fun GoalProgressEntity.toDomainModel(): GoalProgressRecord {
    return GoalProgressRecord(
        id = id,
        goalId = goalId,
        recordDate = recordDate,
        progressValue = progressValue,
        notes = notes,
        createdAt = createdAt
    )
}

// Domain GoalProgressRecord to GoalProgressEntity
fun GoalProgressRecord.toEntity(): GoalProgressEntity {
    return GoalProgressEntity(
        id = id,
        goalId = goalId,
        recordDate = recordDate,
        progressValue = progressValue,
        notes = notes,
        createdAt = createdAt
    )
}
```

## Repository Implementation

### GoalRepositoryImpl Usage

The repository leverages the `GoalWithProgress` relation for efficient data operations:

```kotlin
class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao,
    private val goalProgressDao: GoalProgressDao
) : GoalRepository {

    // Efficient goal retrieval with progress data
    override fun getGoalWithProgress(goalId: Long): Flow<GoalWithProgress?> {
        return goalDao.getGoalWithProgressById(goalId)
            .map { it?.let { goalWithProgress ->
                // Convert to domain model if needed
                goalWithProgress
            }}
    }

    // Progress tracking operations
    override suspend fun saveProgressRecord(progress: GoalProgressRecord): Long {
        val progressEntity = progress.toEntity()
        return goalProgressDao.insertProgress(progressEntity)
    }
}
```

## Database Queries

### DAO Operations

The `GoalDao` provides optimized queries using the relation:

```kotlin
@Dao
interface GoalDao {
    
    @Transaction
    @Query("SELECT * FROM goals WHERE id = :goalId")
    fun getGoalWithProgressById(goalId: Long): Flow<GoalWithProgress?>
    
    @Transaction
    @Query("SELECT * FROM goals WHERE isCompleted = 0")
    fun getActiveGoalsWithProgress(): Flow<List<GoalWithProgress>>
    
    @Transaction
    @Query("SELECT * FROM goals ORDER BY updatedAt DESC")
    fun getAllGoalsWithProgress(): Flow<List<GoalWithProgress>>
}
```

**Query Optimization Features:**
- `@Transaction` ensures atomic operations
- `Flow` provides reactive data streams
- Efficient JOIN operations handled by Room
- Automatic relationship resolution

## Type Converters

### Date Handling

```kotlin
@TypeConverter
class DateConverters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }
}
```

### Goal Type Conversion

```kotlin
@TypeConverter
class GoalTypeConverters {
    @TypeConverter
    fun fromGoalType(type: GoalType): String {
        return type.name
    }

    @TypeConverter
    fun toGoalType(typeName: String): GoalType {
        return GoalType.valueOf(typeName)
    }
}
```

## Performance Considerations

### Indexing Strategy

```kotlin
@Entity(
    tableName = "goal_progress",
    indices = [
        Index(value = ["goalId"]),           // Foreign key index
        Index(value = ["recordDate"]),       // Date-based queries
        Index(value = ["goalId", "recordDate"]) // Composite index
    ]
)
```

### Query Optimization

- **Lazy Loading**: Progress records loaded only when accessed
- **Batch Operations**: Multiple progress records inserted efficiently
- **Caching**: Repository-level caching for frequently accessed data
- **Pagination**: Large datasets handled with Paging 3 library

## Migration Strategy

### Future Schema Changes

The database design supports evolution through Room migrations:

```kotlin
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Example: Adding new column to GoalEntity
        database.execSQL(
            "ALTER TABLE goals ADD COLUMN priority INTEGER NOT NULL DEFAULT 0"
        )
    }
}
```

## Data Integrity

### Constraints and Validation

- **Foreign Key Constraints**: Ensure referential integrity
- **CASCADE Delete**: Automatic cleanup of related records
- **NOT NULL Constraints**: Prevent invalid data states
- **Check Constraints**: Validate business rules at database level

### Error Handling

```kotlin
try {
    goalDao.insertGoal(goalEntity)
} catch (e: SQLiteConstraintException) {
    // Handle constraint violations
    throw GoalValidationException("Invalid goal data", e)
}
```

## Testing Strategy

### Database Testing

```kotlin
@RunWith(AndroidJUnit4::class)
class GoalDatabaseTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private lateinit var database: WorkoutDatabase
    private lateinit var goalDao: GoalDao
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WorkoutDatabase::class.java
        ).allowMainThreadQueries().build()
        
        goalDao = database.goalDao()
    }
    
    @Test
    fun insertGoalWithProgress_retrievesCorrectly() = runTest {
        // Test GoalWithProgress relationship
        val goal = createTestGoal()
        val goalId = goalDao.insertGoal(goal)
        
        val progressRecord = createTestProgress(goalId)
        goalDao.insertProgress(progressRecord)
        
        val goalWithProgress = goalDao.getGoalWithProgressById(goalId).first()
        
        assertThat(goalWithProgress).isNotNull()
        assertThat(goalWithProgress!!.progressRecords).hasSize(1)
    }
}
```

## Best Practices

### Entity Design
- Use meaningful table and column names
- Implement proper foreign key relationships
- Add appropriate indexes for query performance
- Use type converters for complex data types

### Repository Pattern
- Abstract database operations behind repository interfaces
- Provide domain model conversion at repository level
- Implement proper error handling and logging
- Use Flow for reactive data streams

### Performance
- Minimize database queries through efficient relations
- Use appropriate caching strategies
- Implement pagination for large datasets
- Profile query performance regularly

This database schema provides a robust foundation for the WorkRec fitness tracking application, with the `GoalWithProgress` relation being a key architectural component that enables efficient goal tracking and progress visualization.