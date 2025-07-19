#!/bin/bash

# Performance Regression Tests for Build Optimization
# Tests build performance improvements and detects regressions

set -euo pipefail

# Test configuration
TEST_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$TEST_DIR/../.." && pwd)"
TEMP_TEST_DIR="/tmp/build-performance-test-$$"
BASELINE_FILE="$PROJECT_ROOT/build-metrics/performance-baseline.json"
RESULTS_FILE="$PROJECT_ROOT/build-metrics/performance-test-results.json"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Test counters
TESTS_RUN=0
TESTS_PASSED=0
TESTS_FAILED=0

# Performance thresholds (in seconds)
CLEAN_BUILD_THRESHOLD=300    # 5 minutes
INCREMENTAL_BUILD_THRESHOLD=60  # 1 minute
CACHE_HIT_BUILD_THRESHOLD=30    # 30 seconds
TEST_ONLY_BUILD_THRESHOLD=45    # 45 seconds

# Cache effectiveness thresholds
MIN_CACHE_HIT_RATE=0.6      # 60%
MAX_CACHE_SIZE_GB=2         # 2GB
MIN_PARALLEL_EFFICIENCY=0.7  # 70%

# Logging functions
log_test() {
    echo -e "${BLUE}[PERF-TEST]${NC} $1"
}

log_pass() {
    echo -e "${GREEN}[PASS]${NC} $1"
    TESTS_PASSED=$((TESTS_PASSED + 1))
}

log_fail() {
    echo -e "${RED}[FAIL]${NC} $1"
    TESTS_FAILED=$((TESTS_FAILED + 1))
}

log_info() {
    echo -e "${YELLOW}[INFO]${NC} $1"
}

log_perf() {
    echo -e "${PURPLE}[PERF]${NC} $1"
}

# Setup test environment
setup_test_environment() {
    log_info "Setting up performance test environment..."
    
    # Create temporary test directory
    mkdir -p "$TEMP_TEST_DIR"
    cd "$TEMP_TEST_DIR"
    
    # Copy project structure for testing
    cp -r "$PROJECT_ROOT"/* . 2>/dev/null || true
    
    # Ensure gradlew is executable
    chmod +x ./gradlew 2>/dev/null || true
    
    # Create build-metrics directory
    mkdir -p build-metrics
    
    # Initialize git for change detection tests
    if [[ ! -d .git ]]; then
        git init --quiet
        git config user.email "test@example.com"
        git config user.name "Test User"
        git add .
        git commit -m "Initial commit" --quiet
    fi
    
    log_info "Performance test environment setup completed"
}

# Cleanup test environment
cleanup_test_environment() {
    log_info "Cleaning up performance test environment..."
    cd "$PROJECT_ROOT"
    rm -rf "$TEMP_TEST_DIR"
}

# Measure build time
measure_build_time() {
    local build_command="$1"
    local build_type="$2"
    
    log_perf "Measuring $build_type build time..."
    
    local start_time=$(date +%s.%N)
    
    # Execute build command
    if eval "$build_command" > build-output.log 2>&1; then
        local end_time=$(date +%s.%N)
        local duration=$(echo "$end_time - $start_time" | bc -l)
        local duration_int=$(printf "%.0f" "$duration")
        
        log_perf "$build_type completed in ${duration}s"
        echo "$duration_int"
        return 0
    else
        local end_time=$(date +%s.%N)
        local duration=$(echo "$end_time - $start_time" | bc -l)
        local duration_int=$(printf "%.0f" "$duration")
        
        log_fail "$build_type failed after ${duration}s"
        echo "$duration_int"
        return 1
    fi
}

# Analyze cache effectiveness
analyze_cache_effectiveness() {
    local build_log="$1"
    
    if [[ ! -f "$build_log" ]]; then
        echo "0"
        return
    fi
    
    local total_tasks=$(grep -c "Task :" "$build_log" 2>/dev/null || echo "0")
    local cached_tasks=$(grep -c "FROM-CACHE\|UP-TO-DATE" "$build_log" 2>/dev/null || echo "0")
    
    if [[ $total_tasks -gt 0 ]]; then
        local hit_rate=$(echo "scale=2; $cached_tasks / $total_tasks" | bc -l)
        echo "$hit_rate"
    else
        echo "0"
    fi
}

# Get cache size in GB
get_cache_size_gb() {
    local cache_dirs=(
        "$HOME/.gradle/caches"
        "$HOME/.gradle/wrapper"
        ".gradle/build-cache"
    )
    
    local total_size=0
    for dir in "${cache_dirs[@]}"; do
        if [[ -d "$dir" ]]; then
            local size=$(du -sb "$dir" 2>/dev/null | awk '{print $1}' || echo "0")
            total_size=$((total_size + size))
        fi
    done
    
    echo "scale=2; $total_size / 1024 / 1024 / 1024" | bc -l
}

# Test 1: Clean build performance
test_clean_build_performance() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing clean build performance..."
    
    # Clean everything first
    ./gradlew clean --quiet 2>/dev/null || true
    rm -rf ~/.gradle/caches 2>/dev/null || true
    rm -rf .gradle 2>/dev/null || true
    
    # Measure clean build time
    local build_time=$(measure_build_time "./gradlew assembleDebug --no-daemon" "clean build")
    local build_success=$?
    
    if [[ $build_success -eq 0 ]] && [[ $build_time -lt $CLEAN_BUILD_THRESHOLD ]]; then
        log_pass "Clean build completed in ${build_time}s (threshold: ${CLEAN_BUILD_THRESHOLD}s)"
    else
        log_fail "Clean build took ${build_time}s (threshold: ${CLEAN_BUILD_THRESHOLD}s) or failed"
    fi
    
    # Store baseline if this is the first run
    if [[ ! -f "$BASELINE_FILE" ]]; then
        echo "{\"clean_build_time\": $build_time}" > "$BASELINE_FILE"
    fi
}

# Test 2: Incremental build performance
test_incremental_build_performance() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing incremental build performance..."
    
    # First build to populate cache
    ./gradlew assembleDebug --quiet 2>/dev/null || true
    
    # Make small change
    echo "// Small change $(date)" >> app/src/main/java/com/workrec/MainActivity.kt
    
    # Measure incremental build time
    local build_time=$(measure_build_time "./gradlew assembleDebug --no-daemon" "incremental build")
    local build_success=$?
    
    if [[ $build_success -eq 0 ]] && [[ $build_time -lt $INCREMENTAL_BUILD_THRESHOLD ]]; then
        log_pass "Incremental build completed in ${build_time}s (threshold: ${INCREMENTAL_BUILD_THRESHOLD}s)"
    else
        log_fail "Incremental build took ${build_time}s (threshold: ${INCREMENTAL_BUILD_THRESHOLD}s) or failed"
    fi
}

# Test 3: Cache hit build performance
test_cache_hit_build_performance() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing cache hit build performance..."
    
    # Build once to populate cache
    ./gradlew assembleDebug --quiet 2>/dev/null || true
    
    # Clean build outputs but keep cache
    rm -rf app/build 2>/dev/null || true
    
    # Measure cache hit build time
    local build_time=$(measure_build_time "./gradlew assembleDebug --no-daemon" "cache hit build")
    local build_success=$?
    
    # Analyze cache effectiveness
    local cache_hit_rate=$(analyze_cache_effectiveness "build-output.log")
    
    if [[ $build_success -eq 0 ]] && [[ $build_time -lt $CACHE_HIT_BUILD_THRESHOLD ]]; then
        log_pass "Cache hit build completed in ${build_time}s (threshold: ${CACHE_HIT_BUILD_THRESHOLD}s)"
        log_perf "Cache hit rate: $(echo "$cache_hit_rate * 100" | bc -l)%"
    else
        log_fail "Cache hit build took ${build_time}s (threshold: ${CACHE_HIT_BUILD_THRESHOLD}s) or failed"
    fi
}

# Test 4: Test-only build performance
test_test_only_build_performance() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing test-only build performance..."
    
    # Build once to populate cache
    ./gradlew testDebugUnitTest --quiet 2>/dev/null || true
    
    # Make test-only change
    echo "// Test change $(date)" >> app/src/test/java/com/workrec/presentation/viewmodel/WorkoutViewModelTest.kt
    
    # Measure test-only build time
    local build_time=$(measure_build_time "./gradlew testDebugUnitTest --no-daemon" "test-only build")
    local build_success=$?
    
    if [[ $build_success -eq 0 ]] && [[ $build_time -lt $TEST_ONLY_BUILD_THRESHOLD ]]; then
        log_pass "Test-only build completed in ${build_time}s (threshold: ${TEST_ONLY_BUILD_THRESHOLD}s)"
    else
        log_fail "Test-only build took ${build_time}s (threshold: ${TEST_ONLY_BUILD_THRESHOLD}s) or failed"
    fi
}

# Test 5: Parallel execution efficiency
test_parallel_execution_efficiency() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing parallel execution efficiency..."
    
    # Clean build
    ./gradlew clean --quiet 2>/dev/null || true
    
    # Measure serial build time
    local serial_time=$(measure_build_time "GRADLE_OPTS='-Dorg.gradle.parallel=false' ./gradlew assembleDebug --no-daemon" "serial build")
    
    # Clean again
    ./gradlew clean --quiet 2>/dev/null || true
    
    # Measure parallel build time
    local parallel_time=$(measure_build_time "GRADLE_OPTS='-Dorg.gradle.parallel=true -Dorg.gradle.workers.max=4' ./gradlew assembleDebug --no-daemon" "parallel build")
    
    # Calculate efficiency
    if [[ $serial_time -gt 0 ]] && [[ $parallel_time -gt 0 ]]; then
        local efficiency=$(echo "scale=2; ($serial_time - $parallel_time) / $serial_time" | bc -l)
        local efficiency_percent=$(echo "$efficiency * 100" | bc -l)
        
        if (( $(echo "$efficiency >= $MIN_PARALLEL_EFFICIENCY" | bc -l) )); then
            log_pass "Parallel execution efficiency: ${efficiency_percent}% (threshold: $(echo "$MIN_PARALLEL_EFFICIENCY * 100" | bc -l)%)"
        else
            log_fail "Parallel execution efficiency: ${efficiency_percent}% (threshold: $(echo "$MIN_PARALLEL_EFFICIENCY * 100" | bc -l)%)"
        fi
    else
        log_fail "Could not measure parallel execution efficiency"
    fi
}

# Test 6: Cache size efficiency
test_cache_size_efficiency() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing cache size efficiency..."
    
    # Build to populate cache
    ./gradlew assembleDebug testDebugUnitTest --quiet 2>/dev/null || true
    
    # Measure cache size
    local cache_size=$(get_cache_size_gb)
    
    if (( $(echo "$cache_size <= $MAX_CACHE_SIZE_GB" | bc -l) )); then
        log_pass "Cache size: ${cache_size}GB (threshold: ${MAX_CACHE_SIZE_GB}GB)"
    else
        log_fail "Cache size: ${cache_size}GB exceeds threshold: ${MAX_CACHE_SIZE_GB}GB"
    fi
}

# Test 7: Build time consistency
test_build_time_consistency() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing build time consistency..."
    
    local build_times=()
    local iterations=3
    
    for i in $(seq 1 $iterations); do
        # Clean build outputs but keep cache
        rm -rf app/build 2>/dev/null || true
        
        local build_time=$(measure_build_time "./gradlew assembleDebug --no-daemon" "consistency test $i")
        build_times+=("$build_time")
    done
    
    # Calculate average and standard deviation
    local sum=0
    for time in "${build_times[@]}"; do
        sum=$((sum + time))
    done
    local average=$((sum / iterations))
    
    # Calculate standard deviation
    local variance_sum=0
    for time in "${build_times[@]}"; do
        local diff=$((time - average))
        variance_sum=$((variance_sum + diff * diff))
    done
    local variance=$((variance_sum / iterations))
    local std_dev=$(echo "sqrt($variance)" | bc -l)
    local coefficient_of_variation=$(echo "scale=2; $std_dev / $average" | bc -l)
    
    # Consistency is good if coefficient of variation < 0.2 (20%)
    if (( $(echo "$coefficient_of_variation < 0.2" | bc -l) )); then
        log_pass "Build time consistency: CV=${coefficient_of_variation} (avg: ${average}s, std: ${std_dev}s)"
    else
        log_fail "Build time inconsistent: CV=${coefficient_of_variation} (avg: ${average}s, std: ${std_dev}s)"
    fi
}

# Test 8: Memory usage efficiency
test_memory_usage_efficiency() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing memory usage efficiency..."
    
    # Clean build
    ./gradlew clean --quiet 2>/dev/null || true
    
    # Monitor memory usage during build
    local max_memory_mb=0
    
    # Start build in background and monitor memory
    ./gradlew assembleDebug --no-daemon > build-output.log 2>&1 &
    local build_pid=$!
    
    while kill -0 $build_pid 2>/dev/null; do
        if command -v ps >/dev/null 2>&1; then
            local current_memory=$(ps -o rss= -p $build_pid 2>/dev/null | awk '{print int($1/1024)}' || echo "0")
            if [[ $current_memory -gt $max_memory_mb ]]; then
                max_memory_mb=$current_memory
            fi
        fi
        sleep 1
    done
    
    wait $build_pid
    local build_success=$?
    
    # Memory threshold: 4GB (4096MB)
    local memory_threshold_mb=4096
    
    if [[ $build_success -eq 0 ]] && [[ $max_memory_mb -lt $memory_threshold_mb ]]; then
        log_pass "Memory usage: ${max_memory_mb}MB (threshold: ${memory_threshold_mb}MB)"
    else
        log_fail "Memory usage: ${max_memory_mb}MB (threshold: ${memory_threshold_mb}MB) or build failed"
    fi
}

# Test 9: Regression detection
test_regression_detection() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing regression detection..."
    
    if [[ ! -f "$BASELINE_FILE" ]]; then
        log_info "No baseline file found, skipping regression test"
        return
    fi
    
    # Get baseline clean build time
    local baseline_time=$(jq -r '.clean_build_time // 0' "$BASELINE_FILE" 2>/dev/null || echo "0")
    
    if [[ "$baseline_time" == "0" ]]; then
        log_info "No baseline clean build time found, skipping regression test"
        return
    fi
    
    # Perform current clean build
    ./gradlew clean --quiet 2>/dev/null || true
    rm -rf ~/.gradle/caches 2>/dev/null || true
    
    local current_time=$(measure_build_time "./gradlew assembleDebug --no-daemon" "regression test build")
    
    # Calculate regression percentage
    local regression_percent=$(echo "scale=2; ($current_time - $baseline_time) / $baseline_time * 100" | bc -l)
    
    # Regression threshold: 20% slower than baseline
    if (( $(echo "$regression_percent <= 20" | bc -l) )); then
        log_pass "No significant regression detected: ${regression_percent}% change from baseline"
    else
        log_fail "Performance regression detected: ${regression_percent}% slower than baseline"
    fi
}

# Test 10: End-to-end optimization workflow
test_end_to_end_optimization_workflow() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing end-to-end optimization workflow..."
    
    local workflow_start_time=$(date +%s)
    
    # Step 1: Clean build (cold start)
    ./gradlew clean --quiet 2>/dev/null || true
    rm -rf ~/.gradle/caches 2>/dev/null || true
    
    local clean_time=$(measure_build_time "./gradlew assembleDebug --no-daemon" "E2E clean build")
    
    # Step 2: Incremental build (warm cache)
    echo "// E2E change $(date)" >> app/src/main/java/com/workrec/MainActivity.kt
    local incremental_time=$(measure_build_time "./gradlew assembleDebug --no-daemon" "E2E incremental build")
    
    # Step 3: Test-only build
    echo "// E2E test change $(date)" >> app/src/test/java/com/workrec/presentation/viewmodel/WorkoutViewModelTest.kt
    local test_time=$(measure_build_time "./gradlew testDebugUnitTest --no-daemon" "E2E test build")
    
    # Step 4: Cache hit build
    rm -rf app/build 2>/dev/null || true
    local cache_hit_time=$(measure_build_time "./gradlew assembleDebug --no-daemon" "E2E cache hit build")
    
    local workflow_end_time=$(date +%s)
    local total_workflow_time=$((workflow_end_time - workflow_start_time))
    
    # Analyze overall performance
    local cache_effectiveness=$(analyze_cache_effectiveness "build-output.log")
    local cache_size=$(get_cache_size_gb)
    
    # Success criteria: all builds complete within thresholds
    local workflow_success=true
    
    if [[ $clean_time -ge $CLEAN_BUILD_THRESHOLD ]]; then
        workflow_success=false
    fi
    if [[ $incremental_time -ge $INCREMENTAL_BUILD_THRESHOLD ]]; then
        workflow_success=false
    fi
    if [[ $test_time -ge $TEST_ONLY_BUILD_THRESHOLD ]]; then
        workflow_success=false
    fi
    if [[ $cache_hit_time -ge $CACHE_HIT_BUILD_THRESHOLD ]]; then
        workflow_success=false
    fi
    
    if [[ "$workflow_success" == true ]]; then
        log_pass "End-to-end optimization workflow successful"
        log_perf "Clean: ${clean_time}s, Incremental: ${incremental_time}s, Test: ${test_time}s, Cache Hit: ${cache_hit_time}s"
        log_perf "Total workflow time: ${total_workflow_time}s, Cache size: ${cache_size}GB"
    else
        log_fail "End-to-end optimization workflow failed performance thresholds"
        log_perf "Clean: ${clean_time}s, Incremental: ${incremental_time}s, Test: ${test_time}s, Cache Hit: ${cache_hit_time}s"
    fi
}

# Generate performance report
generate_performance_report() {
    log_info "Generating performance test report..."
    
    local report_file="$PROJECT_ROOT/build-metrics/performance-test-report.json"
    
    cat > "$report_file" << EOF
{
  "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
  "test_summary": {
    "tests_run": $TESTS_RUN,
    "tests_passed": $TESTS_PASSED,
    "tests_failed": $TESTS_FAILED,
    "success_rate": $(echo "scale=2; $TESTS_PASSED / $TESTS_RUN * 100" | bc -l)
  },
  "performance_thresholds": {
    "clean_build_threshold_s": $CLEAN_BUILD_THRESHOLD,
    "incremental_build_threshold_s": $INCREMENTAL_BUILD_THRESHOLD,
    "cache_hit_build_threshold_s": $CACHE_HIT_BUILD_THRESHOLD,
    "test_only_build_threshold_s": $TEST_ONLY_BUILD_THRESHOLD,
    "min_cache_hit_rate": $MIN_CACHE_HIT_RATE,
    "max_cache_size_gb": $MAX_CACHE_SIZE_GB,
    "min_parallel_efficiency": $MIN_PARALLEL_EFFICIENCY
  },
  "environment": {
    "os": "$(uname -s)",
    "arch": "$(uname -m)",
    "cpu_cores": $(nproc 2>/dev/null || sysctl -n hw.ncpu 2>/dev/null || echo "unknown"),
    "available_memory_gb": $(free -g 2>/dev/null | awk '/^Mem:/{print $2}' || echo "unknown"),
    "java_version": "$(java -version 2>&1 | head -n1 | cut -d'"' -f2 || echo "unknown")",
    "gradle_version": "$(./gradlew --version 2>/dev/null | grep Gradle | head -n1 | cut -d' ' -f2 || echo "unknown")"
  }
}
EOF
    
    log_info "Performance test report generated: $report_file"
}

# Run all performance tests
run_all_tests() {
    log_info "Starting build performance regression tests..."
    
    setup_test_environment
    
    # Run individual tests
    test_clean_build_performance
    test_incremental_build_performance
    test_cache_hit_build_performance
    test_test_only_build_performance
    test_parallel_execution_efficiency
    test_cache_size_efficiency
    test_build_time_consistency
    test_memory_usage_efficiency
    test_regression_detection
    test_end_to_end_optimization_workflow
    
    generate_performance_report
    cleanup_test_environment
    
    # Print test summary
    echo ""
    echo "============================================="
    echo "Build Performance Regression Test Summary"
    echo "============================================="
    echo "Tests Run: $TESTS_RUN"
    echo -e "Tests Passed: ${GREEN}$TESTS_PASSED${NC}"
    echo -e "Tests Failed: ${RED}$TESTS_FAILED${NC}"
    echo -e "Success Rate: $(echo "scale=1; $TESTS_PASSED / $TESTS_RUN * 100" | bc -l)%"
    
    if [[ $TESTS_FAILED -eq 0 ]]; then
        echo -e "${GREEN}All performance tests passed!${NC}"
        return 0
    else
        echo -e "${RED}Some performance tests failed!${NC}"
        return 1
    fi
}

# Main execution
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    run_all_tests
fi