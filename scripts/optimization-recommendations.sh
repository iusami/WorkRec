#!/bin/bash

# Automated Build Optimization Recommendations
# Analyzes build performance and provides actionable optimization suggestions

set -euo pipefail

# Configuration
METRICS_DIR="build-metrics"
RECOMMENDATIONS_FILE="optimization-recommendations.md"
LOG_FILE="optimization-analysis.log"
BASELINE_FILE="build-baseline.json"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOG_FILE"
}

# Initialize analysis
initialize_analysis() {
    log "Starting automated optimization analysis..."
    
    # Create necessary directories
    mkdir -p "$METRICS_DIR"
    
    # Initialize recommendations file
    cat > "$RECOMMENDATIONS_FILE" << 'EOF'
# Build Optimization Recommendations

Generated: $(date '+%Y-%m-%d %H:%M:%S')

## Executive Summary

This report provides automated analysis of build performance and actionable optimization recommendations based on current metrics and industry best practices.

EOF
}

# Analyze cache performance
analyze_cache_performance() {
    log "Analyzing cache performance..."
    
    local cache_hit_rate=0
    local cache_size=0
    local recommendations=()
    
    # Calculate average cache hit rate from recent builds
    if [[ -d "$METRICS_DIR" ]]; then
        local total_rate=0
        local count=0
        
        for metrics_file in "$METRICS_DIR"/*.json; do
            if [[ -f "$metrics_file" ]]; then
                if command -v jq &> /dev/null; then
                    local rate=$(jq -r '.cache_hit_rate // 0' "$metrics_file")
                    if [[ "$rate" != "null" && "$rate" != "0" ]]; then
                        total_rate=$(echo "$total_rate + $rate" | bc -l 2>/dev/null || echo "$total_rate")
                        count=$((count + 1))
                    fi
                fi
            fi
        done
        
        if [[ $count -gt 0 ]] && command -v bc &> /dev/null; then
            cache_hit_rate=$(echo "scale=2; $total_rate / $count" | bc -l)
        fi
    fi
    
    # Check cache size
    if [[ -d ~/.gradle/caches ]]; then
        cache_size=$(du -sm ~/.gradle/caches 2>/dev/null | cut -f1 || echo "0")
    fi
    
    # Generate cache recommendations
    if (( $(echo "$cache_hit_rate < 60" | bc -l 2>/dev/null || echo "1") )); then
        recommendations+=("🔴 **Critical**: Cache hit rate is ${cache_hit_rate}% (target: >80%)")
        recommendations+=("   - Review cache key generation strategy")
        recommendations+=("   - Check for unstable file timestamps")
        recommendations+=("   - Verify cache paths configuration")
    elif (( $(echo "$cache_hit_rate < 80" | bc -l 2>/dev/null || echo "1") )); then
        recommendations+=("🟡 **Warning**: Cache hit rate is ${cache_hit_rate}% (target: >80%)")
        recommendations+=("   - Fine-tune cache key patterns")
        recommendations+=("   - Consider cache warming strategies")
    else
        recommendations+=("✅ **Good**: Cache hit rate is ${cache_hit_rate}%")
    fi
    
    if [[ $cache_size -gt 2048 ]]; then
        recommendations+=("🟡 **Warning**: Cache size is ${cache_size}MB (target: <2GB)")
        recommendations+=("   - Implement cache cleanup policies")
        recommendations+=("   - Review cache retention settings")
    fi
    
    # Write cache analysis to recommendations
    cat >> "$RECOMMENDATIONS_FILE" << EOF

## Cache Performance Analysis

### Current Metrics
- **Cache Hit Rate**: ${cache_hit_rate}%
- **Cache Size**: ${cache_size}MB
- **Target Hit Rate**: >80%
- **Target Size**: <2048MB

### Recommendations
$(printf '%s\n' "${recommendations[@]}")

EOF
}

# Analyze build time performance
analyze_build_performance() {
    log "Analyzing build time performance..."
    
    local avg_build_time=0
    local build_trend="stable"
    local recommendations=()
    
    # Calculate average build time and trend
    if [[ -d "$METRICS_DIR" ]]; then
        local times=()
        
        for metrics_file in "$METRICS_DIR"/*.json; do
            if [[ -f "$metrics_file" ]]; then
                if command -v jq &> /dev/null; then
                    local time=$(jq -r '.build_duration // 0' "$metrics_file")
                    if [[ "$time" != "null" && "$time" != "0" ]]; then
                        times+=("$time")
                    fi
                fi
            fi
        done
        
        if [[ ${#times[@]} -gt 0 ]]; then
            local total=0
            for time in "${times[@]}"; do
                total=$((total + time))
            done
            avg_build_time=$((total / ${#times[@]}))
            
            # Simple trend analysis (compare first half vs second half)
            if [[ ${#times[@]} -gt 4 ]]; then
                local mid=$((${#times[@]} / 2))
                local first_half_avg=0
                local second_half_avg=0
                
                # Calculate first half average
                local first_total=0
                for ((i=0; i<mid; i++)); do
                    first_total=$((first_total + times[i]))
                done
                first_half_avg=$((first_total / mid))
                
                # Calculate second half average
                local second_total=0
                for ((i=mid; i<${#times[@]}; i++)); do
                    second_total=$((second_total + times[i]))
                done
                second_half_avg=$((second_total / (${#times[@]} - mid)))
                
                # Determine trend
                if [[ $second_half_avg -gt $((first_half_avg + first_half_avg / 10)) ]]; then
                    build_trend="increasing"
                elif [[ $second_half_avg -lt $((first_half_avg - first_half_avg / 10)) ]]; then
                    build_trend="decreasing"
                fi
            fi
        fi
    fi
    
    # Generate build time recommendations
    if [[ $avg_build_time -gt 600 ]]; then  # 10 minutes
        recommendations+=("🔴 **Critical**: Average build time is ${avg_build_time}s (target: <300s)")
        recommendations+=("   - Enable parallel execution if not already active")
        recommendations+=("   - Implement incremental build strategies")
        recommendations+=("   - Consider build cache optimization")
    elif [[ $avg_build_time -gt 300 ]]; then  # 5 minutes
        recommendations+=("🟡 **Warning**: Average build time is ${avg_build_time}s (target: <300s)")
        recommendations+=("   - Fine-tune parallel worker configuration")
        recommendations+=("   - Optimize test execution parallelization")
    else
        recommendations+=("✅ **Good**: Average build time is ${avg_build_time}s")
    fi
    
    case $build_trend in
        "increasing")
            recommendations+=("🔴 **Alert**: Build times are trending upward")
            recommendations+=("   - Investigate recent changes causing performance regression")
            recommendations+=("   - Review dependency updates impact")
            ;;
        "decreasing")
            recommendations+=("✅ **Positive**: Build times are trending downward")
            ;;
        *)
            recommendations+=("ℹ️ **Info**: Build times are stable")
            ;;
    esac
    
    # Write build performance analysis
    cat >> "$RECOMMENDATIONS_FILE" << EOF

## Build Performance Analysis

### Current Metrics
- **Average Build Time**: ${avg_build_time}s
- **Performance Trend**: ${build_trend}
- **Target Build Time**: <300s

### Recommendations
$(printf '%s\n' "${recommendations[@]}")

EOF
}

# Analyze resource utilization
analyze_resource_utilization() {
    log "Analyzing resource utilization..."
    
    local recommendations=()
    
    # Check current Gradle configuration
    local parallel_enabled="false"
    local max_workers="1"
    local memory_allocation="2g"
    
    if [[ -f "gradle.properties" ]]; then
        parallel_enabled=$(grep -o "org.gradle.parallel=true" gradle.properties &>/dev/null && echo "true" || echo "false")
        max_workers=$(grep "org.gradle.workers.max" gradle.properties | cut -d'=' -f2 || echo "1")
        memory_allocation=$(grep "org.gradle.jvmargs" gradle.properties | grep -o "\-Xmx[0-9]*[gG]" | sed 's/-Xmx//g' || echo "2g")
    fi
    
    # Generate resource recommendations
    if [[ "$parallel_enabled" == "false" ]]; then
        recommendations+=("🔴 **Critical**: Parallel execution is disabled")
        recommendations+=("   - Enable parallel builds: org.gradle.parallel=true")
        recommendations+=("   - Set optimal worker count: org.gradle.workers.max=4")
    fi
    
    local memory_gb=$(echo "$memory_allocation" | sed 's/[gG]//g')
    if [[ "$memory_gb" -lt 4 ]]; then
        recommendations+=("🟡 **Warning**: Memory allocation is ${memory_allocation} (recommended: 4g+)")
        recommendations+=("   - Increase JVM memory: org.gradle.jvmargs=-Xmx4g")
    fi
    
    if [[ "$max_workers" -lt 2 ]]; then
        recommendations+=("🟡 **Warning**: Worker count is ${max_workers} (recommended: 2-4)")
        recommendations+=("   - Increase worker count: org.gradle.workers.max=4")
    fi
    
    # Check for test parallelization
    if [[ -f "app/build.gradle.kts" ]]; then
        if ! grep -q "maxParallelForks" app/build.gradle.kts; then
            recommendations+=("🟡 **Suggestion**: Test parallelization not configured")
            recommendations+=("   - Add test parallelization in build.gradle.kts")
        fi
    fi
    
    # Write resource analysis
    cat >> "$RECOMMENDATIONS_FILE" << EOF

## Resource Utilization Analysis

### Current Configuration
- **Parallel Execution**: ${parallel_enabled}
- **Max Workers**: ${max_workers}
- **Memory Allocation**: ${memory_allocation}

### Recommendations
$(printf '%s\n' "${recommendations[@]}")

EOF
}

# Generate optimization action plan
generate_action_plan() {
    log "Generating optimization action plan..."
    
    cat >> "$RECOMMENDATIONS_FILE" << 'EOF'

## Optimization Action Plan

### Immediate Actions (High Impact, Low Effort)

1. **Enable Basic Optimizations**
   ```properties
   # Add to gradle.properties
   org.gradle.parallel=true
   org.gradle.caching=true
   org.gradle.configureondemand=true
   org.gradle.workers.max=4
   org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g
   ```

2. **Implement Dependency Caching**
   ```yaml
   # Add to GitHub Actions workflow
   - name: Cache Gradle dependencies
     uses: actions/cache@v3
     with:
       path: |
         ~/.gradle/caches
         ~/.gradle/wrapper
       key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
   ```

### Short-term Actions (1-2 weeks)

1. **Optimize Test Execution**
   ```kotlin
   // Add to build.gradle.kts
   tasks.withType<Test> {
       maxParallelForks = Runtime.getRuntime().availableProcessors() / 2
       forkEvery = 100
   }
   ```

2. **Implement Change Detection**
   - Set up selective build execution based on file changes
   - Skip builds for documentation-only changes
   - Use incremental compilation for source-only changes

3. **Set Up Performance Monitoring**
   - Implement build metrics collection
   - Create performance dashboard
   - Set up automated alerts for regressions

### Long-term Actions (1+ months)

1. **Advanced Cache Strategies**
   - Implement build output caching
   - Set up cache cleanup automation
   - Optimize cache key generation

2. **Resource Optimization**
   - Fine-tune memory allocation based on usage patterns
   - Implement dynamic worker scaling
   - Optimize CI runner specifications

3. **Continuous Optimization**
   - Set up automated performance regression detection
   - Implement A/B testing for optimization strategies
   - Create feedback loop for continuous improvement

## Implementation Priority Matrix

| Action | Impact | Effort | Priority |
|--------|--------|--------|----------|
| Enable parallel builds | High | Low | 🔴 Critical |
| Implement dependency caching | High | Low | 🔴 Critical |
| Add test parallelization | Medium | Low | 🟡 High |
| Set up change detection | High | Medium | 🟡 High |
| Implement monitoring | Medium | Medium | 🟡 High |
| Advanced cache strategies | Medium | High | 🟢 Medium |
| Resource optimization | Low | High | 🟢 Low |

## Success Metrics

### Target Improvements
- **Build Time Reduction**: 50-70% for incremental builds
- **Cache Hit Rate**: >80% for dependencies
- **Resource Efficiency**: <4GB memory usage
- **Developer Experience**: <5 minute feedback cycles

### Monitoring KPIs
- Average build duration trend
- Cache effectiveness percentage
- Build success rate
- Resource utilization efficiency
- Developer satisfaction scores

EOF
}

# Generate configuration templates
generate_config_templates() {
    log "Generating configuration templates..."
    
    cat >> "$RECOMMENDATIONS_FILE" << 'EOF'

## Configuration Templates

### Optimized gradle.properties
```properties
# Build performance optimizations
org.gradle.parallel=true
org.gradle.workers.max=4
org.gradle.configureondemand=true
org.gradle.caching=true
org.gradle.daemon=false  # Disabled for CI environments

# JVM optimizations
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g -XX:+UseG1GC

# Kotlin compilation optimizations
kotlin.incremental=true
kotlin.incremental.useClasspathSnapshot=true
kotlin.build.report.output=file

# Android build optimizations
android.useAndroidX=true
android.enableJetifier=true
android.enableR8.fullMode=true
```

### GitHub Actions Workflow Template
```yaml
name: Optimized CI Build

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
      with:
        fetch-depth: 0  # For change detection
    
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
    
    - name: Cache Gradle dependencies
      uses: actions/cache@v3
      with:
        path: |
          ~/.gradle/caches
          ~/.gradle/wrapper
          ~/.gradle/build-cache
        key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
        restore-keys: |
          ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*') }}
          ${{ runner.os }}-gradle-
    
    - name: Detect changes and set build strategy
      run: ./scripts/change-detection.sh
    
    - name: Build with optimizations
      run: |
        if [[ "${{ env.SKIP_BUILD }}" == "true" ]]; then
          echo "Skipping build for documentation-only changes"
        elif [[ "${{ env.TEST_ONLY_BUILD }}" == "true" ]]; then
          ./gradlew testDebugUnitTest --parallel
        else
          ./gradlew assembleDebug testDebugUnitTest --parallel --build-cache
        fi
    
    - name: Collect build metrics
      if: always()
      run: ./scripts/build-metrics-collection.sh
```

### Test Parallelization Template
```kotlin
// build.gradle.kts (app module)
android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                // Optimize test execution
                maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
                forkEvery = 100
                
                // Memory optimization for tests
                minHeapSize = "128m"
                maxHeapSize = "1g"
                
                // JVM arguments for test execution
                jvmArgs("-XX:MaxMetaspaceSize=256m")
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    
    // Parallel test execution
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    forkEvery = 100
    
    // Test reporting
    testLogging {
        events("passed", "skipped", "failed")
        parallel = true
    }
}
```

EOF
}

# Main execution function
main() {
    echo -e "${BLUE}🔍 Starting Automated Optimization Analysis${NC}"
    
    initialize_analysis
    analyze_cache_performance
    analyze_build_performance
    analyze_resource_utilization
    generate_action_plan
    generate_config_templates
    
    echo -e "${GREEN}✅ Optimization analysis completed${NC}"
    echo -e "${YELLOW}📋 Recommendations available at: $RECOMMENDATIONS_FILE${NC}"
    
    # Display summary
    echo -e "\n${PURPLE}📊 Analysis Summary:${NC}"
    if [[ -f "$RECOMMENDATIONS_FILE" ]]; then
        echo -e "${BLUE}Critical issues found:${NC} $(grep -c "🔴" "$RECOMMENDATIONS_FILE" || echo "0")"
        echo -e "${YELLOW}Warnings found:${NC} $(grep -c "🟡" "$RECOMMENDATIONS_FILE" || echo "0")"
        echo -e "${GREEN}Good practices:${NC} $(grep -c "✅" "$RECOMMENDATIONS_FILE" || echo "0")"
    fi
    
    log "Optimization analysis completed successfully"
}

# Execute main function
main "$@"