# Design Document

## Overview

This design optimizes the GoalRepository implementation by replacing inefficient in-memory filtering with database-level queries. The optimization focuses on adding specific DAO methods for active and completed goals, ensuring better performance and scalability while maintaining the existing public interface.

## Architecture

### Current Architecture Issues

The current implementation has performance bottlenecks:

```kotlin
// Current inefficient implementation
override fun getActiveGoals(): Flow<List<Goal>> {
    return goalDao.getAllGoals().map { goalEntities ->
        goalEntities.filter { !it.isCompleted }.map { it.toDomainModel() }
    }
}
```

**Problems:**
- Fetches ALL goals from database regardless of filter criteria
- Performs filtering in application memory
- Scales poorly with dataset size (O(n) memory usage)
- Unnecessary data transfer from database to application

### Optimized Architecture

The optimized implementation will use database-level filtering:

```kotlin
// Optimized implementation
override fun getActiveGoals(): Flow<List<Goal>> {
    return goalDao.getActiveGoals().map { goalEntities ->
        goalEntities.map { it.toDomainModel() }
    }
}
```

**Benefits:**
- Database performs filtering using indexed queries
- Reduced memory usage in application
- Better scalability (O(log n) query time with proper indexing)
- Reduced data transfer between database and application

## Components and Interfaces

### 1. GoalDao Interface Enhancement

**New Methods to Add:**
```kotlin
@Query("SELECT * FROM goals WHERE isCompleted = 0")
fun getActiveGoals(): Flow<List<GoalEntity>>

@Query("SELECT * FROM goals WHERE isCompleted = 1")
fun getCompletedGoals(): Flow<List<GoalEntity>>

@Transaction
@Query("SELECT * FROM goals WHERE isCompleted = 0")
fun getActiveGoalsWithProgress(): Flow<List<GoalWithProgress>>

@Transaction
@Query("SELECT * FROM goals WHERE isCompleted = 1")
fun getCompletedGoalsWithProgress(): Flow<List<GoalWithProgress>>
```

**Query Optimization Considerations:**
- Use `isCompleted = 0` and `isCompleted = 1` for boolean filtering (SQLite standard)
- Include `@Transaction` for methods that need goal progress data
- Maintain Flow return types for reactive data streams
- Consider adding database index on `isCompleted` column for better performance

### 2. GoalRepositoryImpl Optimization

**Method Updates:**
```kotlin
class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao
) : GoalRepository {
    
    // Optimized: Direct database filtering
    override fun getActiveGoals(): Flow<List<Goal>> {
        return goalDao.getActiveGoals().map { goalEntities ->
            goalEntities.map { it.toDomainModel() }
        }
    }
    
    // Optimized: Direct database filtering
    override fun getCompletedGoals(): Flow<List<Goal>> {
        return goalDao.getCompletedGoals().map { goalEntities ->
            goalEntities.map { it.toDomainModel() }
        }
    }
    
    // Keep existing methods unchanged for backward compatibility
    override fun getAllGoals(): Flow<List<Goal>> {
        return goalDao.getAllGoals().map { goalEntities ->
            goalEntities.map { it.toDomainModel() }
        }
    }
}
```

### 3. Database Schema Considerations

**Current Schema:**
```sql
CREATE TABLE goals (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    targetValue REAL NOT NULL,
    currentValue REAL NOT NULL DEFAULT 0,
    unit TEXT NOT NULL,
    goalType TEXT NOT NULL,
    targetDate INTEGER,
    isCompleted INTEGER NOT NULL DEFAULT 0,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
);
```

**Optimization Recommendation:**
```sql
-- Add index for better query performance
CREATE INDEX idx_goals_isCompleted ON goals(isCompleted);
```

## Data Models

### Entity Mapping

No changes required to existing entity models:
- `GoalEntity` remains unchanged
- `GoalWithProgress` remains unchanged
- Domain model `Goal` remains unchanged

### Data Flow

**Optimized Data Flow:**
1. **Request**: Repository method called (e.g., `getActiveGoals()`)
2. **Database Query**: DAO executes filtered query with WHERE clause
3. **Result Set**: Database returns only matching records
4. **Mapping**: Repository maps entities to domain models
5. **Response**: Filtered domain models returned to caller

**Performance Comparison:**
- **Before**: Database → All Goals → Memory Filter → Subset
- **After**: Database Filter → Subset → Domain Models

## Error Handling

### Database Query Errors

```kotlin
override fun getActiveGoals(): Flow<List<Goal>> {
    return goalDao.getActiveGoals()
        .map { goalEntities ->
            goalEntities.map { it.toDomainModel() }
        }
        .catch { exception ->
            // Log error and emit empty list or rethrow based on requirements
            Timber.e(exception, "Failed to fetch active goals")
            emit(emptyList())
        }
}
```

### Migration Considerations

- No database migration required (only adding new queries)
- Existing data remains compatible
- Gradual rollout possible (can implement one method at a time)

## Testing Strategy

### 1. Unit Tests for DAO

```kotlin
@Test
fun getActiveGoals_returnsOnlyNonCompletedGoals() {
    // Given: Mix of completed and active goals in database
    // When: getActiveGoals() called
    // Then: Only goals with isCompleted = false returned
}

@Test
fun getCompletedGoals_returnsOnlyCompletedGoals() {
    // Given: Mix of completed and active goals in database
    // When: getCompletedGoals() called
    // Then: Only goals with isCompleted = true returned
}
```

### 2. Unit Tests for Repository

```kotlin
@Test
fun getActiveGoals_mapsEntitiesToDomainModels() {
    // Given: DAO returns goal entities
    // When: Repository getActiveGoals() called
    // Then: Entities correctly mapped to domain models
}

@Test
fun getActiveGoals_maintainsSameInterfaceContract() {
    // Given: Existing test expectations
    // When: Optimized method called
    // Then: Same behavior as before optimization
}
```

### 3. Integration Tests

```kotlin
@Test
fun goalRepository_optimizedQueriesPerformBetter() {
    // Given: Large dataset of goals
    // When: Comparing old vs new implementation
    // Then: New implementation shows improved performance metrics
}
```

### 4. Performance Tests

```kotlin
@Test
fun getActiveGoals_performanceWithLargeDataset() {
    // Given: 10,000+ goals in database
    // When: getActiveGoals() called
    // Then: Query completes within acceptable time limits
}
```

## Performance Considerations

### Query Performance

**Database Indexing:**
- Add index on `isCompleted` column for O(log n) filtering
- Consider composite indexes if filtering by multiple columns becomes common

**Memory Usage:**
- Reduced memory footprint by avoiding full dataset loading
- Streaming results through Flow for better memory management

**Network/IO:**
- Reduced data transfer between database and application
- More efficient use of database connection resources

### Scalability

**Dataset Growth:**
- Linear performance degradation → Logarithmic performance with indexing
- Memory usage scales with result set size, not total dataset size
- Better resource utilization for mobile applications

## Migration Strategy

### Phase 1: Add New DAO Methods
- Implement new filtered query methods in GoalDao
- Add comprehensive unit tests for new methods
- Verify query performance with test data

### Phase 2: Update Repository Implementation
- Replace in-memory filtering with database queries
- Maintain existing public interface
- Add integration tests

### Phase 3: Performance Validation
- Benchmark performance improvements
- Validate memory usage reduction
- Ensure all existing functionality works correctly

### Phase 4: Cleanup and Documentation
- Remove any unused code
- Update code documentation
- Add performance notes for future developers