# CI Build Optimization Guide

## Overview

This guide provides comprehensive documentation for the CI build optimization strategies implemented in this project. The optimizations focus on reducing build times through advanced caching, parallel execution, and intelligent build selection.

## Optimization Strategies

### 1. Multi-Layer Caching Strategy

#### Gradle Build Cache
- **Purpose**: Reuse build outputs across builds
- **Configuration**: `gradle.properties`
- **Benefits**: 40-60% time reduction for incremental builds

```properties
# Core caching settings
org.gradle.caching=true
org.gradle.parallel=true
org.gradle.configureondemand=true
```

#### Dependency Cache
- **Purpose**: Avoid re-downloading dependencies
- **Implementation**: GitHub Actions cache with intelligent key generation
- **Cache Keys**:
  - Primary: `${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}`
  - Fallback: `${{ runner.os }}-gradle-`

#### Build Output Cache
- **Purpose**: Reuse compiled artifacts
- **Paths Cached**:
  - `app/build/intermediates`
  - `app/build/tmp`
  - `build/kotlin`

### 2. Parallel Execution Optimization

#### Gradle Parallel Settings
```properties
org.gradle.parallel=true
org.gradle.workers.max=4
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g
```

#### Test Parallelization
```kotlin
tasks.withType<Test> {
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    forkEvery = 100
}
```

### 3. Selective Build Execution

#### Change Detection
The system analyzes file changes to determine optimal build strategy:

- **Documentation Only**: Skip build entirely
- **Test Only**: Run tests without recompilation
- **Source Changes**: Incremental build
- **Build Script Changes**: Full rebuild

#### Build Strategies
- **SKIP_BUILD**: Documentation-only changes
- **TEST_ONLY_BUILD**: Test file changes only
- **INCREMENTAL_BUILD**: Source code changes
- **FULL_BUILD**: Build configuration changes

### 4. Build Task Ordering Optimization

#### KSP and KAPT Task Dependencies
To ensure reliable builds in CI environments while avoiding circular dependencies, a minimal task ordering configuration is enforced:

```kotlin
// KSP and KAPT configuration - minimal dependencies to avoid circular dependency
afterEvaluate {
    // Only ensure KAPT runs after KSP for the main source set
    tasks.named("kaptDebugKotlin") {
        mustRunAfter("kspDebugKotlin")
    }
    tasks.named("kaptReleaseKotlin") {
        mustRunAfter("kspReleaseKotlin")
    }
}
```

#### KSP Configuration for CI Reliability
The project implements environment-specific KSP configuration to ensure reliable Room DAO generation in CI environments:

```kotlin
// KSP Optimization for Symbol Processing (Room only)
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.expandProjection", "true")
    arg("room.generateKotlin", "true")
    arg("room.generateKotlinCodeByDefault", "true")
    
    // CI環境では確実にRoom DAOを生成するため、インクリメンタル処理を無効化
    if (System.getenv("CI") == "true") {
        arg("room.incremental", "false")
        arg("room.forceGenerateAll", "true")
    } else {
        arg("room.incremental", "true")
    }
}
```

**Key Features:**
- **Environment Detection**: Automatically detects CI environment using `CI` environment variable
- **Incremental Processing Control**: Disables incremental processing in CI for reliability
- **Force Generation**: Ensures all Room DAOs are generated in CI environments
- **Local Development Optimization**: Maintains incremental processing for faster local builds

**Benefits:**
- **Prevents Race Conditions**: Ensures Room DAOs are generated before KAPT processing
- **Avoids Circular Dependencies**: Minimal configuration prevents complex task dependency cycles
- **Improves CI Reliability**: Eliminates intermittent build failures in parallel environments
- **Maintains Build Determinism**: Consistent task execution order across all environments

**Implementation Details:**
- Uses `afterEvaluate` to configure tasks after project evaluation
- Applies `mustRunAfter` only for specific KAPT tasks (Debug and Release variants)
- Avoids broad task type matching that can create circular dependencies
- Focuses on essential task ordering without over-constraining the build graph

## Configuration Options

### Environment Variables

| Variable | Purpose | Default | Options |
|----------|---------|---------|---------|
| `GRADLE_OPTS` | JVM options for Gradle | `-Xmx4g` | Memory settings |
| `BUILD_STRATEGY` | Override build strategy | `auto` | `full`, `incremental`, `test`, `skip` |
| `CACHE_ENABLED` | Enable/disable caching | `true` | `true`, `false` |
| `PARALLEL_BUILDS` | Enable parallel execution | `true` | `true`, `false` |

### Gradle Properties

| Property | Purpose | Recommended Value |
|----------|---------|-------------------|
| `org.gradle.parallel` | Enable parallel builds | `true` |
| `org.gradle.workers.max` | Max parallel workers | `4` |
| `org.gradle.caching` | Enable build cache | `true` |
| `org.gradle.daemon` | Use Gradle daemon | `false` (CI only) |

### GitHub Actions Configuration

#### Cache Configuration
```yaml
- name: Cache Gradle dependencies
  uses: actions/cache@v3
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
      ~/.gradle/build-cache
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
    restore-keys: |
      ${{ runner.os }}-gradle-
```

#### Build Matrix
```yaml
strategy:
  matrix:
    build-type: [debug, release]
    include:
      - build-type: debug
        gradle-tasks: assembleDebug testDebugUnitTest
      - build-type: release
        gradle-tasks: assembleRelease
```

## Performance Targets

### Build Time Reduction
- **Incremental builds**: 50-70% reduction
- **Dependency-only changes**: 80-90% reduction
- **Test-only changes**: 60-80% reduction
- **Documentation changes**: Near-instant (skip)

### Cache Effectiveness
- **Cache hit rate**: >80% for dependencies
- **Build cache hit rate**: >60% for incremental changes
- **Cache size**: <2GB total
- **Cleanup frequency**: Weekly automated

### Resource Utilization
- **CPU utilization**: 80-90% during parallel execution
- **Memory usage**: <4GB peak
- **Network usage**: 90% reduction in downloads
- **Storage efficiency**: Optimal cache-to-benefit ratio

## Best Practices

### 1. Cache Key Design
- Include all relevant files in hash calculation
- Use hierarchical restore keys for fallback
- Avoid overly specific keys that reduce hit rates

### 2. Parallel Execution
- Balance worker count with available memory
- Use `forkEvery` to prevent memory leaks in tests
- Monitor resource usage to avoid thrashing

### 3. Build Strategy Selection
- Implement comprehensive change detection
- Use conservative fallbacks for uncertain cases
- Monitor build success rates by strategy

### 4. Cache Management
- Implement size-based cleanup policies
- Monitor cache hit rates and effectiveness
- Use cache corruption detection and recovery

## Monitoring and Metrics

### Key Metrics to Track
- Build duration by type
- Cache hit rates
- Resource utilization
- Build success rates
- Time savings compared to baseline

### Alerting Thresholds
- Build time >20% above baseline
- Cache hit rate <60%
- Build failure rate >5%
- Memory usage >90% of available

## Integration with Existing Workflows

### PR Validation
- Automatic change detection
- Selective build execution
- Performance comparison reporting

### Main Branch Builds
- Full optimization enabled
- Comprehensive metrics collection
- Cache warming for subsequent builds

### Release Builds
- Conservative optimization settings
- Full build verification
- Performance baseline establishment
## 
Troubleshooting Guide

### Common Issues and Solutions

#### 1. Cache Miss Issues

**Symptoms:**
- Low cache hit rates (<50%)
- Builds taking longer than expected
- Frequent dependency downloads

**Diagnosis:**
```bash
# Check cache hit rates
grep "cache hit" build.log | tail -10

# Verify cache key generation
echo "Cache key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}"
```

**Solutions:**
- Verify cache key includes all relevant files
- Check for unstable file timestamps
- Ensure cache paths are correct
- Review cache size limits

#### 2. Parallel Execution Problems

**Symptoms:**
- High memory usage (>4GB)
- Build failures with OutOfMemoryError
- Slower builds despite parallelization

**Diagnosis:**
```bash
# Check memory usage during build
./gradlew build --info | grep -i memory

# Verify parallel settings
grep -E "(parallel|workers)" gradle.properties
```

**Solutions:**
```properties
# Reduce parallel workers
org.gradle.workers.max=2

# Increase memory allocation
org.gradle.jvmargs=-Xmx6g -XX:MaxMetaspaceSize=1g

# Adjust test fork settings
# In build.gradle.kts:
maxParallelForks = 1
forkEvery = 50
```

#### 3. Build Strategy Selection Issues

**Symptoms:**
- Full builds when incremental should suffice
- Skipped builds when compilation needed
- Inconsistent build behavior

**Diagnosis:**
```bash
# Check change detection output
git diff --name-only origin/main...HEAD
./scripts/change-detection.sh --debug
```

**Solutions:**
- Review change detection patterns
- Add logging to build strategy selection
- Implement conservative fallbacks
- Test change detection with various scenarios

#### 4. KSP/KAPT Task Ordering Issues

**Symptoms:**
- Build failures with "Cannot find symbol" errors for Room DAOs
- Intermittent compilation failures in CI environments
- KAPT processing errors related to missing generated sources
- Race conditions between annotation processors

**Diagnosis:**
```bash
# Check task execution order
./gradlew build --dry-run | grep -E "(ksp|kapt|compile)"

# Verify KSP generated files exist
ls -la app/build/generated/ksp/

# Check for task dependency conflicts
./gradlew build --info | grep -E "(mustRunAfter|dependsOn)"
```

**Solutions:**
The project implements minimal task ordering configuration to avoid circular dependencies:
```kotlin
afterEvaluate {
    // Only ensure KAPT runs after KSP for the main source set
    tasks.named("kaptDebugKotlin") {
        mustRunAfter("kspDebugKotlin")
    }
    tasks.named("kaptReleaseKotlin") {
        mustRunAfter("kspReleaseKotlin")
    }
}
```

**KSP Incremental Processing Issues:**
The project automatically handles CI environment detection to disable incremental processing:
```kotlin
// Automatic CI detection in build.gradle.kts
if (System.getenv("CI") == "true") {
    arg("room.incremental", "false")
    arg("room.forceGenerateAll", "true")
} else {
    arg("room.incremental", "true")
}
```

**Manual Recovery:**
```bash
# Clean generated sources and rebuild
rm -rf app/build/generated/ksp
rm -rf app/build/generated/kapt
./gradlew clean kspDebugKotlin compileDebugKotlin

# Force full KSP generation (if needed)
CI=true ./gradlew clean kspDebugKotlin
```

#### 5. Cache Corruption

**Symptoms:**
- Sudden build failures after cache hits
- Inconsistent build outputs
- Gradle daemon crashes

**Diagnosis:**
```bash
# Check for cache corruption indicators
find ~/.gradle/caches -name "*.lock" -mtime +1
gradle --stop
```

**Solutions:**
```bash
# Clear corrupted cache
rm -rf ~/.gradle/caches
rm -rf .gradle/build-cache

# Implement cache validation
./scripts/cache-management.sh --validate
```

#### 5. Performance Regression

**Symptoms:**
- Builds slower than baseline
- Decreasing cache effectiveness
- Increased resource usage

**Diagnosis:**
```bash
# Compare with baseline metrics
./scripts/performance-comparison.sh --baseline

# Check recent changes impact
git log --oneline --since="1 week ago" -- "*.gradle*"
```

**Solutions:**
- Revert recent optimization changes
- Analyze build profile reports
- Adjust optimization parameters
- Implement performance monitoring alerts

### Debugging Commands

#### Cache Analysis
```bash
# Check cache size and contents
du -sh ~/.gradle/caches
find ~/.gradle/caches -type f -name "*.jar" | wc -l

# Analyze cache effectiveness
./scripts/cache-analysis.sh --detailed
```

#### Build Performance Analysis
```bash
# Generate build scan
./gradlew build --scan

# Profile build performance
./gradlew build --profile

# Analyze task execution times
./gradlew build --info | grep "Task.*took"
```

#### Memory and Resource Monitoring
```bash
# Monitor memory usage during build
top -p $(pgrep -f gradle) -d 1

# Check disk space usage
df -h
du -sh .gradle build
```

### Performance Tuning Checklist

#### Before Optimization
- [ ] Establish baseline build times
- [ ] Document current configuration
- [ ] Set up monitoring and metrics collection
- [ ] Create rollback plan

#### During Implementation
- [ ] Test changes in isolation
- [ ] Monitor resource usage
- [ ] Validate cache effectiveness
- [ ] Check build success rates

#### After Optimization
- [ ] Compare performance metrics
- [ ] Document configuration changes
- [ ] Set up automated monitoring
- [ ] Create maintenance procedures

### Emergency Procedures

#### Complete Cache Reset
```bash
#!/bin/bash
# Emergency cache reset script
echo "Performing emergency cache reset..."

# Stop Gradle daemon
./gradlew --stop

# Clear all caches
rm -rf ~/.gradle/caches
rm -rf ~/.gradle/build-cache
rm -rf .gradle

# Clear GitHub Actions cache (manual)
echo "Manually clear GitHub Actions cache in repository settings"

# Rebuild from clean state
./gradlew clean build
```

#### Fallback to Non-Optimized Build
```bash
#!/bin/bash
# Disable all optimizations for emergency builds
export GRADLE_OPTS="-Xmx2g"
export BUILD_STRATEGY="full"
export CACHE_ENABLED="false"
export PARALLEL_BUILDS="false"

./gradlew clean build --no-daemon --no-parallel --no-build-cache
```

### Monitoring and Alerting Setup

#### Key Metrics to Monitor
- Build duration trends
- Cache hit rate percentages
- Memory usage patterns
- Build failure rates
- Resource utilization

#### Alert Thresholds
```yaml
alerts:
  build_time:
    warning: ">20% above 7-day average"
    critical: ">50% above 7-day average"
  
  cache_hit_rate:
    warning: "<60%"
    critical: "<40%"
  
  memory_usage:
    warning: ">3.5GB"
    critical: ">4GB"
  
  build_failure_rate:
    warning: ">5%"
    critical: ">10%"
```

#### Automated Recovery Actions
- Cache cleanup on low hit rates
- Memory limit adjustment on OOM errors
- Fallback to sequential builds on parallel failures
- Automatic baseline recalculation on consistent improvements