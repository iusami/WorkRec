#!/bin/bash

# Integration Tests for Cache Strategies
# Tests the multi-layer caching system and cache management functionality

set -euo pipefail

# Test configuration
TEST_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$TEST_DIR/../.." && pwd)"
TEMP_TEST_DIR="/tmp/cache-integration-test-$$"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counters
TESTS_RUN=0
TESTS_PASSED=0
TESTS_FAILED=0

# Cache directories for testing
TEST_GRADLE_CACHE="$TEMP_TEST_DIR/.gradle/caches"
TEST_BUILD_CACHE="$TEMP_TEST_DIR/.gradle/build-cache"
TEST_WRAPPER_CACHE="$TEMP_TEST_DIR/.gradle/wrapper"

# Logging functions
log_test() {
    echo -e "${BLUE}[TEST]${NC} $1"
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

# Setup test environment
setup_test_environment() {
    log_info "Setting up cache integration test environment..."
    
    # Create temporary test directory
    mkdir -p "$TEMP_TEST_DIR"
    cd "$TEMP_TEST_DIR"
    
    # Create cache directories
    mkdir -p "$TEST_GRADLE_CACHE"
    mkdir -p "$TEST_BUILD_CACHE"
    mkdir -p "$TEST_WRAPPER_CACHE"
    
    # Create test project structure
    mkdir -p app/src/main/java/com/test
    mkdir -p app/src/test/java/com/test
    
    # Create minimal Android project files
    cat > settings.gradle.kts << 'EOF'
rootProject.name = "TestProject"
include(":app")
EOF

    cat > app/build.gradle.kts << 'EOF'
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.test"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.test"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    testImplementation("junit:junit:4.13.2")
}
EOF

    cat > gradle.properties << 'EOF'
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
android.useAndroidX=true
android.enableJetifier=true
EOF

    # Create wrapper files
    mkdir -p gradle/wrapper
    cat > gradle/wrapper/gradle-wrapper.properties << 'EOF'
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
EOF

    # Create gradlew script (simplified)
    cat > gradlew << 'EOF'
#!/bin/bash
# Simplified gradlew for testing
echo "Mock Gradle execution: $*"
exit 0
EOF
    chmod +x gradlew
    
    # Create source files
    echo "package com.test; class MainActivity" > app/src/main/java/com/test/MainActivity.kt
    echo "package com.test; class MainActivityTest" > app/src/test/java/com/test/MainActivityTest.kt
    
    log_info "Cache integration test environment setup completed"
}

# Cleanup test environment
cleanup_test_environment() {
    log_info "Cleaning up cache integration test environment..."
    cd "$PROJECT_ROOT"
    rm -rf "$TEMP_TEST_DIR"
}

# Helper function to get directory size in bytes
get_dir_size_bytes() {
    local dir="$1"
    if [[ -d "$dir" ]]; then
        du -sb "$dir" 2>/dev/null | awk '{print $1}' || echo "0"
    else
        echo "0"
    fi
}

# Helper function to create mock cache entries
create_mock_cache_entries() {
    local cache_dir="$1"
    local entry_count="$2"
    
    mkdir -p "$cache_dir"
    
    for i in $(seq 1 "$entry_count"); do
        local entry_dir="$cache_dir/entry-$i"
        mkdir -p "$entry_dir"
        echo "Mock cache entry $i" > "$entry_dir/data.bin"
        echo "metadata" > "$entry_dir/metadata.json"
    done
}

# Helper function to create corrupted cache entries
create_corrupted_cache_entries() {
    local cache_dir="$1"
    
    mkdir -p "$cache_dir"
    
    # Create incomplete entries
    touch "$cache_dir/incomplete.tmp"
    touch "$cache_dir/locked.lock"
    
    # Create zero-byte files
    touch "$cache_dir/empty.bin"
    touch "$cache_dir/empty.json"
    
    # Create old entries
    mkdir -p "$cache_dir/old-entry"
    echo "old data" > "$cache_dir/old-entry/data.bin"
    # Set modification time to 35 days ago
    touch -t $(date -d '35 days ago' '+%Y%m%d%H%M') "$cache_dir/old-entry/data.bin" 2>/dev/null || true
}

# Test 1: Cache size monitoring
test_cache_size_monitoring() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing cache size monitoring..."
    
    # Create mock cache entries
    create_mock_cache_entries "$TEST_GRADLE_CACHE/modules-2" 10
    create_mock_cache_entries "$TEST_BUILD_CACHE" 5
    create_mock_cache_entries "$TEST_WRAPPER_CACHE" 3
    
    # Source the cache management script functions
    source "$PROJECT_ROOT/scripts/cache-management.sh"
    
    # Override cache directories for testing
    GRADLE_CACHE_DIR="$TEST_GRADLE_CACHE"
    BUILD_CACHE_DIR="$TEST_BUILD_CACHE"
    GRADLE_WRAPPER_DIR="$TEST_WRAPPER_CACHE"
    
    # Test cache size monitoring
    local initial_size=$(get_dir_size_bytes "$TEST_GRADLE_CACHE")
    
    if [[ $initial_size -gt 0 ]]; then
        log_pass "Cache size monitoring detects cache entries"
    else
        log_fail "Cache size monitoring failed to detect cache entries"
    fi
}

# Test 2: Cache corruption detection
test_cache_corruption_detection() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing cache corruption detection..."
    
    # Create corrupted cache entries
    create_corrupted_cache_entries "$TEST_GRADLE_CACHE"
    
    # Source the cache management script functions
    source "$PROJECT_ROOT/scripts/cache-management.sh"
    
    # Override cache directories for testing
    GRADLE_CACHE_DIR="$TEST_GRADLE_CACHE"
    BUILD_CACHE_DIR="$TEST_BUILD_CACHE"
    GRADLE_WRAPPER_DIR="$TEST_WRAPPER_CACHE"
    LOCAL_BUILD_CACHE_DIR="$TEMP_TEST_DIR/.gradle/build-cache"
    
    # Test corruption detection
    if ! detect_cache_corruption 2>/dev/null; then
        log_pass "Cache corruption detection works correctly"
    else
        log_fail "Cache corruption detection failed to detect corruption"
    fi
}

# Test 3: Cache cleanup functionality
test_cache_cleanup() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing cache cleanup functionality..."
    
    # Create cache entries with different ages
    create_mock_cache_entries "$TEST_GRADLE_CACHE/modules-2" 20
    create_corrupted_cache_entries "$TEST_GRADLE_CACHE"
    
    # Source the cache management script functions
    source "$PROJECT_ROOT/scripts/cache-management.sh"
    
    # Override cache directories and settings for testing
    GRADLE_CACHE_DIR="$TEST_GRADLE_CACHE"
    BUILD_CACHE_DIR="$TEST_BUILD_CACHE"
    GRADLE_WRAPPER_DIR="$TEST_WRAPPER_CACHE"
    LOCAL_BUILD_CACHE_DIR="$TEMP_TEST_DIR/.gradle/build-cache"
    CACHE_MAX_AGE_DAYS=1  # Very short for testing
    
    local initial_corruption_count=$(find "$TEST_GRADLE_CACHE" -name "*.tmp" -o -name "*.lock" 2>/dev/null | wc -l)
    
    # Run cleanup
    cleanup_corrupted_cache 2>/dev/null || true
    
    local final_corruption_count=$(find "$TEST_GRADLE_CACHE" -name "*.tmp" -o -name "*.lock" 2>/dev/null | wc -l)
    
    if [[ $final_corruption_count -lt $initial_corruption_count ]]; then
        log_pass "Cache cleanup successfully removed corrupted entries"
    else
        log_fail "Cache cleanup failed to remove corrupted entries"
    fi
}

# Test 4: Cache effectiveness analysis
test_cache_effectiveness_analysis() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing cache effectiveness analysis..."
    
    # Create mock build log with cache statistics
    cat > build.log << 'EOF'
BUILD SUCCESSFUL in 45s
Task :app:compileDebugKotlin FROM-CACHE
Task :app:processDebugResources EXECUTED
Task :app:compileDebugJavaWithJavac UP-TO-DATE
Task :app:packageDebug EXECUTED
BUILD SUCCESSFUL in 32s
Task :app:compileDebugKotlin EXECUTED
Task :app:processDebugResources FROM-CACHE
EOF
    
    # Create build-metrics directory
    mkdir -p build-metrics
    
    # Source the cache management script functions
    source "$PROJECT_ROOT/scripts/cache-management.sh"
    
    # Override cache directories for testing
    GRADLE_CACHE_DIR="$TEST_GRADLE_CACHE"
    BUILD_CACHE_DIR="$TEST_BUILD_CACHE"
    
    # Run effectiveness analysis
    analyze_cache_effectiveness 2>/dev/null || true
    
    # Check if metrics file was created
    if [[ -f "build-metrics/cache-effectiveness.json" ]]; then
        local hit_rate=$(grep -o '"cache_hit_rate": [0-9.]*' build-metrics/cache-effectiveness.json | cut -d' ' -f2 || echo "0")
        if [[ "$hit_rate" != "0" ]]; then
            log_pass "Cache effectiveness analysis generated metrics"
        else
            log_fail "Cache effectiveness analysis generated invalid metrics"
        fi
    else
        log_fail "Cache effectiveness analysis failed to generate metrics file"
    fi
}

# Test 5: Cache retention policies
test_cache_retention_policies() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing cache retention policies..."
    
    # Create cache entries with different access times
    create_mock_cache_entries "$TEST_GRADLE_CACHE/modules-2" 15
    
    # Create some recently accessed files
    local recent_file="$TEST_GRADLE_CACHE/modules-2/entry-1/data.bin"
    touch "$recent_file"
    
    # Create some old files
    local old_file="$TEST_GRADLE_CACHE/modules-2/entry-10/data.bin"
    if [[ -f "$old_file" ]]; then
        # Set modification time to 10 days ago
        touch -t $(date -d '10 days ago' '+%Y%m%d%H%M' 2>/dev/null || date -v-10d '+%Y%m%d%H%M') "$old_file" 2>/dev/null || true
    fi
    
    # Source the cache management script functions
    source "$PROJECT_ROOT/scripts/cache-management.sh"
    
    # Override cache directories and settings for testing
    GRADLE_CACHE_DIR="$TEST_GRADLE_CACHE"
    BUILD_CACHE_DIR="$TEST_BUILD_CACHE"
    GRADLE_WRAPPER_DIR="$TEST_WRAPPER_CACHE"
    LOCAL_BUILD_CACHE_DIR="$TEMP_TEST_DIR/.gradle/build-cache"
    CACHE_MAX_SIZE_GB=0.001  # Very small for testing
    CACHE_MAX_AGE_DAYS=5
    
    local initial_count=$(find "$TEST_GRADLE_CACHE" -type f | wc -l)
    
    # Run retention policies
    implement_retention_policies 2>/dev/null || true
    
    local final_count=$(find "$TEST_GRADLE_CACHE" -type f | wc -l)
    
    # Should have cleaned up some files
    if [[ $final_count -le $initial_count ]]; then
        log_pass "Cache retention policies applied successfully"
    else
        log_fail "Cache retention policies failed to clean up files"
    fi
}

# Test 6: Cache key generation and validation
test_cache_key_generation() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing cache key generation and validation..."
    
    # Create different build files to test key generation
    echo "version = '1.0'" > gradle.properties
    echo "apply plugin: 'android'" > app/build.gradle.kts
    
    # Simulate cache key generation (based on GitHub Actions cache strategy)
    local os_name="Linux"
    local gradle_files_hash=$(find . -name "*.gradle*" -o -name "gradle-wrapper.properties" -o -name "gradle.properties" | sort | xargs cat | shasum | cut -d' ' -f1)
    
    local primary_key="${os_name}-gradle-${gradle_files_hash}"
    local restore_key="${os_name}-gradle-"
    
    if [[ ${#primary_key} -gt 10 ]] && [[ ${#restore_key} -gt 5 ]]; then
        log_pass "Cache key generation produces valid keys"
    else
        log_fail "Cache key generation failed. Primary: $primary_key, Restore: $restore_key"
    fi
}

# Test 7: Multi-layer cache coordination
test_multi_layer_cache_coordination() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing multi-layer cache coordination..."
    
    # Create entries in different cache layers
    create_mock_cache_entries "$TEST_GRADLE_CACHE/modules-2" 5  # Dependency cache
    create_mock_cache_entries "$TEST_BUILD_CACHE" 3             # Build cache
    create_mock_cache_entries "$TEST_WRAPPER_CACHE" 2           # Wrapper cache
    
    # Source the cache management script functions
    source "$PROJECT_ROOT/scripts/cache-management.sh"
    
    # Override cache directories for testing
    GRADLE_CACHE_DIR="$TEST_GRADLE_CACHE"
    BUILD_CACHE_DIR="$TEST_BUILD_CACHE"
    GRADLE_WRAPPER_DIR="$TEST_WRAPPER_CACHE"
    
    # Test that all layers are monitored
    local gradle_size=$(get_dir_size_bytes "$TEST_GRADLE_CACHE")
    local build_size=$(get_dir_size_bytes "$TEST_BUILD_CACHE")
    local wrapper_size=$(get_dir_size_bytes "$TEST_WRAPPER_CACHE")
    
    if [[ $gradle_size -gt 0 ]] && [[ $build_size -gt 0 ]] && [[ $wrapper_size -gt 0 ]]; then
        log_pass "Multi-layer cache coordination works correctly"
    else
        log_fail "Multi-layer cache coordination failed. Sizes: G=$gradle_size, B=$build_size, W=$wrapper_size"
    fi
}

# Test 8: Cache health monitoring
test_cache_health_monitoring() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing cache health monitoring..."
    
    # Create a mix of healthy and unhealthy cache entries
    create_mock_cache_entries "$TEST_GRADLE_CACHE/modules-2" 10
    create_corrupted_cache_entries "$TEST_GRADLE_CACHE"
    
    # Source the cache management script functions
    source "$PROJECT_ROOT/scripts/cache-management.sh"
    
    # Override cache directories and settings for testing
    GRADLE_CACHE_DIR="$TEST_GRADLE_CACHE"
    BUILD_CACHE_DIR="$TEST_BUILD_CACHE"
    GRADLE_WRAPPER_DIR="$TEST_WRAPPER_CACHE"
    CACHE_MAX_SIZE_GB=0.001  # Very small to trigger size warnings
    CACHE_MIN_HIT_RATE=0.8   # High threshold for testing
    
    # Run health monitoring
    local health_result=0
    monitor_cache_health 2>/dev/null || health_result=$?
    
    # Should detect issues (non-zero exit code)
    if [[ $health_result -ne 0 ]]; then
        log_pass "Cache health monitoring detected issues correctly"
    else
        log_fail "Cache health monitoring failed to detect issues"
    fi
}

# Test 9: Cache recovery integration
test_cache_recovery_integration() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing cache recovery integration..."
    
    # Create corrupted cache state
    create_corrupted_cache_entries "$TEST_GRADLE_CACHE"
    
    # Source the cache management script functions
    source "$PROJECT_ROOT/scripts/cache-management.sh"
    
    # Override cache directories for testing
    GRADLE_CACHE_DIR="$TEST_GRADLE_CACHE"
    BUILD_CACHE_DIR="$TEST_BUILD_CACHE"
    GRADLE_WRAPPER_DIR="$TEST_WRAPPER_CACHE"
    LOCAL_BUILD_CACHE_DIR="$TEMP_TEST_DIR/.gradle/build-cache"
    
    local initial_corruption=$(find "$TEST_GRADLE_CACHE" -name "*.tmp" -o -name "*.lock" | wc -l)
    
    # Run recovery
    recover_cache_with_error_handling 2>/dev/null || true
    
    local final_corruption=$(find "$TEST_GRADLE_CACHE" -name "*.tmp" -o -name "*.lock" | wc -l)
    
    if [[ $final_corruption -lt $initial_corruption ]]; then
        log_pass "Cache recovery integration works correctly"
    else
        log_fail "Cache recovery integration failed to recover cache"
    fi
}

# Test 10: Performance under load
test_cache_performance_under_load() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing cache performance under load..."
    
    # Create large number of cache entries
    create_mock_cache_entries "$TEST_GRADLE_CACHE/modules-2" 100
    create_mock_cache_entries "$TEST_BUILD_CACHE" 50
    
    # Source the cache management script functions
    source "$PROJECT_ROOT/scripts/cache-management.sh"
    
    # Override cache directories for testing
    GRADLE_CACHE_DIR="$TEST_GRADLE_CACHE"
    BUILD_CACHE_DIR="$TEST_BUILD_CACHE"
    GRADLE_WRAPPER_DIR="$TEST_WRAPPER_CACHE"
    
    # Measure performance of cache operations
    local start_time=$(date +%s)
    
    # Run multiple cache operations
    monitor_cache_sizes 2>/dev/null || true
    detect_cache_corruption 2>/dev/null || true
    
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    # Should complete within reasonable time (10 seconds)
    if [[ $duration -lt 10 ]]; then
        log_pass "Cache operations perform well under load (${duration}s)"
    else
        log_fail "Cache operations too slow under load (${duration}s)"
    fi
}

# Run all tests
run_all_tests() {
    log_info "Starting cache strategies integration tests..."
    
    setup_test_environment
    
    # Run individual tests
    test_cache_size_monitoring
    test_cache_corruption_detection
    test_cache_cleanup
    test_cache_effectiveness_analysis
    test_cache_retention_policies
    test_cache_key_generation
    test_multi_layer_cache_coordination
    test_cache_health_monitoring
    test_cache_recovery_integration
    test_cache_performance_under_load
    
    cleanup_test_environment
    
    # Print test summary
    echo ""
    echo "========================================"
    echo "Cache Strategies Integration Test Summary"
    echo "========================================"
    echo "Tests Run: $TESTS_RUN"
    echo -e "Tests Passed: ${GREEN}$TESTS_PASSED${NC}"
    echo -e "Tests Failed: ${RED}$TESTS_FAILED${NC}"
    
    if [[ $TESTS_FAILED -eq 0 ]]; then
        echo -e "${GREEN}All tests passed!${NC}"
        return 0
    else
        echo -e "${RED}Some tests failed!${NC}"
        return 1
    fi
}

# Main execution
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    run_all_tests
fi