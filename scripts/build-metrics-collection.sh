#!/bin/bash

# Build Metrics Collection Script
# Collects comprehensive build performance metrics for CI optimization analysis

set -euo pipefail

# Configuration
METRICS_DIR="build-metrics"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
METRICS_FILE="${METRICS_DIR}/build-metrics-${TIMESTAMP}.json"
BUILD_LOG_FILE="${METRICS_DIR}/build-${TIMESTAMP}.log"

# Ensure metrics directory exists
mkdir -p "$METRICS_DIR"

# Initialize metrics collection
echo "Starting build metrics collection at $(date)"

# Function to get current timestamp in milliseconds
get_timestamp_ms() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        python3 -c "import time; print(int(time.time() * 1000))"
    else
        # Linux
        date +%s%3N
    fi
}

# Function to calculate memory usage
get_memory_usage() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS - get memory usage in MB
        ps -o pid,rss,comm | grep -E "(gradle|java)" | awk '{sum+=$2} END {print sum/1024}' || echo "0"
    else
        # Linux - get memory usage in MB
        ps -o pid,rss,comm | grep -E "(gradle|java)" | awk '{sum+=$2} END {print sum/1024}' || echo "0"
    fi
}

# Function to get CPU core count
get_cpu_cores() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        sysctl -n hw.ncpu
    else
        nproc
    fi
}

# Function to analyze cache effectiveness
analyze_cache_effectiveness() {
    local build_log="$1"
    local total_tasks=0
    local cached_tasks=0
    local up_to_date_tasks=0
    
    if [[ -f "$build_log" ]]; then
        # Count total executed tasks
        total_tasks=$(grep -c "^> Task :" "$build_log" 2>/dev/null || echo "0")
        
        # Count cached tasks (FROM-CACHE)
        cached_tasks=$(grep -c "FROM-CACHE" "$build_log" 2>/dev/null || echo "0")
        
        # Count up-to-date tasks (UP-TO-DATE)
        up_to_date_tasks=$(grep -c "UP-TO-DATE" "$build_log" 2>/dev/null || echo "0")
    fi
    
    echo "$total_tasks,$cached_tasks,$up_to_date_tasks"
}

# Function to detect build type based on changed files
detect_build_type() {
    local build_type="unknown"
    
    if [[ -n "${GITHUB_BASE_REF:-}" ]]; then
        local changed_files
        changed_files=$(git diff --name-only "origin/${GITHUB_BASE_REF}...HEAD" 2>/dev/null || echo "")
        
        if [[ -z "$changed_files" ]]; then
            build_type="no-changes"
        elif echo "$changed_files" | grep -qE '\.(gradle|properties)$'; then
            build_type="build-config"
        elif echo "$changed_files" | grep -qE '\.(kt|java)$' && ! echo "$changed_files" | grep -q "src/test/"; then
            build_type="source-code"
        elif echo "$changed_files" | grep -q "src/test/"; then
            build_type="test-only"
        elif echo "$changed_files" | grep -qE '\.(md|txt)$'; then
            build_type="documentation"
        else
            build_type="mixed"
        fi
    fi
    
    echo "$build_type"
}

# Start timing
BUILD_START_TIME=$(get_timestamp_ms)
BUILD_TYPE=$(detect_build_type)
CPU_CORES=$(get_cpu_cores)

echo "Build type detected: $BUILD_TYPE"
echo "CPU cores available: $CPU_CORES"

# Execute the build with logging
echo "Executing Gradle build..."

# Determine appropriate build command based on build type
BUILD_COMMAND=""
case "$BUILD_TYPE" in
    "documentation")
        echo "Documentation-only changes detected, running lightweight build..."
        BUILD_COMMAND="assembleDebug"
        ;;
    "test-only")
        echo "Test-only changes detected, running tests..."
        BUILD_COMMAND="testDebugUnitTest"
        ;;
    "build-config")
        echo "Build configuration changes detected, running full build..."
        BUILD_COMMAND="clean assembleDebug testDebugUnitTest"
        ;;
    "source-code"|"mixed"|*)
        echo "Source code changes detected, running comprehensive build..."
        BUILD_COMMAND="assembleDebug testDebugUnitTest"
        ;;
esac

# Execute the build command (allow failure to continue metrics collection)
set +e
GRADLE_OPTS="-Dorg.gradle.logging.level=info" ./gradlew $BUILD_COMMAND --build-cache --parallel 2>&1 | tee "$BUILD_LOG_FILE"
BUILD_EXIT_CODE=$?
set -e

echo "Build command executed: ./gradlew $BUILD_COMMAND"
echo "Build exit code: $BUILD_EXIT_CODE"

# End timing
BUILD_END_TIME=$(get_timestamp_ms)
BUILD_DURATION=$((BUILD_END_TIME - BUILD_START_TIME))

# Check if build was successful
BUILD_SUCCESS="true"
if [[ $BUILD_EXIT_CODE -ne 0 ]]; then
    BUILD_SUCCESS="false"
    echo "Build failed with exit code $BUILD_EXIT_CODE, but continuing with metrics collection..."
fi

# Analyze cache effectiveness
CACHE_STATS=$(analyze_cache_effectiveness "$BUILD_LOG_FILE")
IFS=',' read -r TOTAL_TASKS CACHED_TASKS UP_TO_DATE_TASKS <<< "$CACHE_STATS"

# Calculate cache hit rate
if [[ $TOTAL_TASKS -gt 0 ]]; then
    CACHE_HIT_RATE=$(echo "scale=2; ($CACHED_TASKS + $UP_TO_DATE_TASKS) * 100 / $TOTAL_TASKS" | bc -l 2>/dev/null || echo "0")
else
    CACHE_HIT_RATE="0"
fi

# Get memory usage
PEAK_MEMORY_MB=$(get_memory_usage)

# Get Gradle daemon info
GRADLE_DAEMON_COUNT=$(./gradlew --status 2>/dev/null | grep -c "IDLE\|BUSY" || echo "0")

# Collect additional metrics
GRADLE_VERSION=$(./gradlew --version | grep "Gradle" | awk '{print $2}' || echo "unknown")
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}' || echo "unknown")

# Create comprehensive metrics JSON
cat > "$METRICS_FILE" << EOF
{
  "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
  "build_id": "${GITHUB_RUN_ID:-local-$(date +%s)}",
  "commit_sha": "${GITHUB_SHA:-$(git rev-parse HEAD 2>/dev/null || echo 'unknown')}",
  "branch": "${GITHUB_REF_NAME:-$(git branch --show-current 2>/dev/null || echo 'unknown')}",
  "build_type": "$BUILD_TYPE",
  "performance": {
    "total_duration_ms": $BUILD_DURATION,
    "total_duration_seconds": $(echo "scale=2; $BUILD_DURATION / 1000" | bc -l),
    "peak_memory_mb": $PEAK_MEMORY_MB,
    "cpu_cores_available": $CPU_CORES,
    "gradle_daemon_count": $GRADLE_DAEMON_COUNT,
    "build_success": $BUILD_SUCCESS
  },
  "cache_effectiveness": {
    "total_tasks": $TOTAL_TASKS,
    "cached_tasks": $CACHED_TASKS,
    "up_to_date_tasks": $UP_TO_DATE_TASKS,
    "executed_tasks": $((TOTAL_TASKS - CACHED_TASKS - UP_TO_DATE_TASKS)),
    "cache_hit_rate_percent": $CACHE_HIT_RATE
  },
  "environment": {
    "gradle_version": "$GRADLE_VERSION",
    "java_version": "$JAVA_VERSION",
    "os_type": "$OSTYPE",
    "ci_environment": "${CI:-false}",
    "github_actions": "${GITHUB_ACTIONS:-false}"
  },
  "build_configuration": {
    "parallel_enabled": true,
    "build_cache_enabled": true,
    "configuration_cache_enabled": false,
    "daemon_enabled": false
  }
}
EOF

# Display summary
echo ""
echo "=== Build Metrics Summary ==="
echo "Build Command: ./gradlew $BUILD_COMMAND"
echo "Build Success: $BUILD_SUCCESS"
echo "Build Duration: $(echo "scale=2; $BUILD_DURATION / 1000" | bc -l)s"
echo "Cache Hit Rate: ${CACHE_HIT_RATE}%"
echo "Total Tasks: $TOTAL_TASKS"
echo "Cached Tasks: $CACHED_TASKS"
echo "Up-to-Date Tasks: $UP_TO_DATE_TASKS"
echo "Peak Memory Usage: ${PEAK_MEMORY_MB}MB"
echo "Build Type: $BUILD_TYPE"
echo ""
echo "Metrics saved to: $METRICS_FILE"
echo "Build log saved to: $BUILD_LOG_FILE"

# Check if bc is available, install if needed
if ! command -v bc &> /dev/null; then
    echo "Warning: 'bc' calculator not found. Some calculations may be inaccurate."
    echo "Install with: brew install bc (macOS) or apt-get install bc (Ubuntu)"
fi