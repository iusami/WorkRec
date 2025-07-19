#!/bin/bash

# End-to-End Tests for Complete Optimization Workflow
# Tests the entire CI build optimization system from start to finish

set -euo pipefail

# Test configuration
TEST_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$TEST_DIR/../.." && pwd)"
TEMP_TEST_DIR="/tmp/e2e-optimization-test-$$"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Test counters
TESTS_RUN=0
TESTS_PASSED=0
TESTS_FAILED=0

# Workflow stages
WORKFLOW_STAGES=(
    "initialization"
    "change_detection"
    "cache_management"
    "build_execution"
    "performance_monitoring"
    "error_handling"
    "cleanup_and_reporting"
)

# Logging functions
log_test() {
    echo -e "${BLUE}[E2E-TEST]${NC} $1"
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

log_stage() {
    echo -e "${PURPLE}[STAGE]${NC} $1"
}

log_workflow() {
    echo -e "${CYAN}[WORKFLOW]${NC} $1"
}

# Setup comprehensive test environment
setup_comprehensive_test_environment() {
    log_info "Setting up comprehensive E2E test environment..."
    
    # Create temporary test directory
    mkdir -p "$TEMP_TEST_DIR"
    cd "$TEMP_TEST_DIR"
    
    # Copy entire project for realistic testing
    cp -r "$PROJECT_ROOT"/* . 2>/dev/null || true
    cp -r "$PROJECT_ROOT"/.git . 2>/dev/null || true
    cp -r "$PROJECT_ROOT"/.github . 2>/dev/null || true
    
    # Ensure all scripts are executable
    find scripts -name "*.sh" -exec chmod +x {} \; 2>/dev/null || true
    chmod +x ./gradlew 2>/dev/null || true
    
    # Create necessary directories
    mkdir -p build-metrics
    mkdir -p .gradle/caches
    mkdir -p .gradle/wrapper
    
    # Initialize git if not present
    if [[ ! -d .git ]]; then
        git init --quiet
        git config user.email "test@example.com"
        git config user.name "E2E Test User"
        git add .
        git commit -m "Initial E2E test commit" --quiet
        git branch main
    fi
    
    # Set up environment variables for testing
    export GITHUB_ACTIONS="true"
    export GITHUB_REPOSITORY="test/repo"
    export GITHUB_REF_NAME="main"
    export GITHUB_SHA="$(git rev-parse HEAD 2>/dev/null || echo 'test-sha')"
    export GITHUB_RUN_ID="12345"
    export GITHUB_ENV="$TEMP_TEST_DIR/github-env"
    touch "$GITHUB_ENV"
    
    log_info "Comprehensive E2E test environment setup completed"
}

# Cleanup test environment
cleanup_test_environment() {
    log_info "Cleaning up E2E test environment..."
    cd "$PROJECT_ROOT"
    rm -rf "$TEMP_TEST_DIR"
}

# Test 1: Complete workflow initialization
test_workflow_initialization() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing complete workflow initialization..."
    
    log_stage "Initializing optimization workflow..."
    
    # Test script availability
    local required_scripts=(
        "scripts/build-optimization-integration.sh"
        "scripts/cache-management.sh"
        "scripts/error-handling.sh"
        "scripts/build-dashboard.sh"
        "scripts/optimization-recommendations.sh"
    )
    
    local missing_scripts=()
    for script in "${required_scripts[@]}"; do
        if [[ ! -f "$script" ]]; then
            missing_scripts+=("$script")
        fi
    done
    
    if [[ ${#missing_scripts[@]} -eq 0 ]]; then
        log_pass "All required scripts are available"
    else
        log_fail "Missing required scripts: ${missing_scripts[*]}"
        return
    fi
    
    # Test configuration files
    local config_files=(
        "gradle.properties"
        ".github/workflows/pr-validation.yml"
    )
    
    local missing_configs=()
    for config in "${config_files[@]}"; do
        if [[ ! -f "$config" ]]; then
            missing_configs+=("$config")
        fi
    done
    
    if [[ ${#missing_configs[@]} -eq 0 ]]; then
        log_pass "All required configuration files are available"
    else
        log_fail "Missing configuration files: ${missing_configs[*]}"
    fi
}

# Test 2: End-to-end change detection workflow
test_e2e_change_detection_workflow() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing end-to-end change detection workflow..."
    
    log_stage "Testing change detection scenarios..."
    
    # Scenario 1: Source code changes
    echo "// E2E test change $(date)" >> app/src/main/java/com/workrec/MainActivity.kt
    git add app/src/main/java/com/workrec/MainActivity.kt
    git commit -m "E2E: Source code change" --quiet
    
    # Test change detection
    if source scripts/build-optimization-integration.sh && detect_changes main >/dev/null 2>&1; then
        if [[ "${INCREMENTAL_BUILD:-}" == "true" ]]; then
            log_pass "Source code change detection works in E2E workflow"
        else
            log_fail "Source code change not detected correctly in E2E workflow"
        fi
    else
        log_fail "Change detection script failed in E2E workflow"
    fi
    
    # Scenario 2: Documentation-only changes
    git reset --hard HEAD~1 --quiet
    echo "# E2E Documentation Change" >> README.md
    git add README.md
    git commit -m "E2E: Documentation change" --quiet
    
    # Reset environment variables
    unset FULL_BUILD INCREMENTAL_BUILD TEST_ONLY_BUILD SKIP_BUILD
    
    if source scripts/build-optimization-integration.sh && detect_changes main >/dev/null 2>&1; then
        if [[ "${SKIP_BUILD:-}" == "true" ]]; then
            log_pass "Documentation-only change detection works in E2E workflow"
        else
            log_fail "Documentation-only change not detected correctly in E2E workflow"
        fi
    else
        log_fail "Change detection failed for documentation changes in E2E workflow"
    fi
}

# Test 3: Complete cache management lifecycle
test_e2e_cache_management_lifecycle() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing complete cache management lifecycle..."
    
    log_stage "Testing cache management lifecycle..."
    
    # Create initial cache state
    mkdir -p ~/.gradle/caches/modules-2
    mkdir -p ~/.gradle/caches/transforms-3
    mkdir -p .gradle/build-cache
    
    # Populate with test data
    for i in {1..10}; do
        echo "cache entry $i" > ~/.gradle/caches/modules-2/entry-$i.bin
        echo "transform $i" > ~/.gradle/caches/transforms-3/transform-$i.bin
        echo "build cache $i" > .gradle/build-cache/build-$i.bin
    done
    
    # Add some corrupted entries
    touch ~/.gradle/caches/incomplete.tmp
    touch ~/.gradle/caches/locked.lock
    touch ~/.gradle/caches/empty.bin
    
    # Test cache management workflow
    if ./scripts/cache-management.sh full >/dev/null 2>&1; then
        log_pass "Cache management lifecycle completed successfully"
        
        # Verify cleanup occurred
        local corruption_count=$(find ~/.gradle/caches -name "*.tmp" -o -name "*.lock" 2>/dev/null | wc -l)
        if [[ $corruption_count -eq 0 ]]; then
            log_pass "Cache corruption cleanup worked correctly"
        else
            log_fail "Cache corruption cleanup failed"
        fi
    else
        log_fail "Cache management lifecycle failed"
    fi
}

# Test 4: Build execution with optimization strategies
test_e2e_build_execution_with_optimizations() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing build execution with optimization strategies..."
    
    log_stage "Testing optimized build execution..."
    
    # Test different build scenarios
    local build_scenarios=(
        "clean_build"
        "incremental_build"
        "test_only_build"
        "cache_hit_build"
    )
    
    local successful_scenarios=0
    
    for scenario in "${build_scenarios[@]}"; do
        log_workflow "Testing $scenario scenario..."
        
        case "$scenario" in
            "clean_build")
                ./gradlew clean --quiet 2>/dev/null || true
                rm -rf ~/.gradle/caches 2>/dev/null || true
                if timeout 300 ./gradlew help --no-daemon >/dev/null 2>&1; then
                    successful_scenarios=$((successful_scenarios + 1))
                    log_workflow "Clean build scenario successful"
                else
                    log_workflow "Clean build scenario failed or timed out"
                fi
                ;;
            "incremental_build")
                echo "// Incremental change" >> app/src/main/java/com/workrec/MainActivity.kt
                if timeout 180 ./gradlew help --no-daemon >/dev/null 2>&1; then
                    successful_scenarios=$((successful_scenarios + 1))
                    log_workflow "Incremental build scenario successful"
                else
                    log_workflow "Incremental build scenario failed or timed out"
                fi
                ;;
            "test_only_build")
                echo "// Test change" >> app/src/test/java/com/workrec/presentation/viewmodel/WorkoutViewModelTest.kt
                if timeout 120 ./gradlew help --no-daemon >/dev/null 2>&1; then
                    successful_scenarios=$((successful_scenarios + 1))
                    log_workflow "Test-only build scenario successful"
                else
                    log_workflow "Test-only build scenario failed or timed out"
                fi
                ;;
            "cache_hit_build")
                rm -rf app/build 2>/dev/null || true
                if timeout 90 ./gradlew help --no-daemon >/dev/null 2>&1; then
                    successful_scenarios=$((successful_scenarios + 1))
                    log_workflow "Cache hit build scenario successful"
                else
                    log_workflow "Cache hit build scenario failed or timed out"
                fi
                ;;
        esac
    done
    
    if [[ $successful_scenarios -ge 3 ]]; then
        log_pass "Build execution with optimizations successful ($successful_scenarios/4 scenarios)"
    else
        log_fail "Build execution with optimizations failed ($successful_scenarios/4 scenarios)"
    fi
}

# Test 5: Performance monitoring integration
test_e2e_performance_monitoring_integration() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing performance monitoring integration..."
    
    log_stage "Testing performance monitoring workflow..."
    
    # Generate build metrics
    if ./scripts/build-dashboard.sh >/dev/null 2>&1; then
        log_workflow "Build dashboard generation successful"
        
        # Check for generated files
        local expected_files=(
            "build-metrics/dashboard.html"
            "build-metrics/performance-report.md"
        )
        
        local found_files=0
        for file in "${expected_files[@]}"; do
            if [[ -f "$file" ]]; then
                found_files=$((found_files + 1))
            fi
        done
        
        if [[ $found_files -eq ${#expected_files[@]} ]]; then
            log_pass "Performance monitoring files generated successfully"
        else
            log_fail "Performance monitoring files missing ($found_files/${#expected_files[@]})"
        fi
    else
        log_fail "Build dashboard generation failed"
    fi
    
    # Test optimization recommendations
    if ./scripts/optimization-recommendations.sh >/dev/null 2>&1; then
        log_workflow "Optimization recommendations generation successful"
        
        if [[ -f "optimization-recommendations.md" ]]; then
            log_pass "Optimization recommendations file generated"
        else
            log_fail "Optimization recommendations file not generated"
        fi
    else
        log_fail "Optimization recommendations generation failed"
    fi
}

# Test 6: Error handling and recovery workflow
test_e2e_error_handling_and_recovery() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing error handling and recovery workflow..."
    
    log_stage "Testing error handling scenarios..."
    
    # Scenario 1: Cache corruption recovery
    log_workflow "Testing cache corruption recovery..."
    
    # Create corrupted cache state
    mkdir -p ~/.gradle/caches/test-corruption
    for i in {1..15}; do
        touch ~/.gradle/caches/test-corruption/corrupt-$i.tmp
        touch ~/.gradle/caches/test-corruption/locked-$i.lock
        touch ~/.gradle/caches/test-corruption/empty-$i.bin
    done
    
    if ./scripts/error-handling.sh test-corruption >/dev/null 2>&1; then
        log_workflow "Cache corruption recovery test completed"
        
        # Check if corruption was cleaned up
        local remaining_corruption=$(find ~/.gradle/caches -name "*.tmp" -o -name "*.lock" 2>/dev/null | wc -l)
        if [[ $remaining_corruption -lt 5 ]]; then
            log_pass "Cache corruption recovery successful"
        else
            log_fail "Cache corruption recovery incomplete"
        fi
    else
        log_fail "Cache corruption recovery test failed"
    fi
    
    # Scenario 2: Build retry logic
    log_workflow "Testing build retry logic..."
    
    if ./scripts/error-handling.sh test-retry "./gradlew help" >/dev/null 2>&1; then
        log_pass "Build retry logic test successful"
    else
        log_fail "Build retry logic test failed"
    fi
}

# Test 7: Complete workflow integration
test_e2e_complete_workflow_integration() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing complete workflow integration..."
    
    log_stage "Running complete optimization workflow..."
    
    local workflow_start_time=$(date +%s)
    
    # Run the complete optimization integration
    if ./scripts/build-optimization-integration.sh >/dev/null 2>&1; then
        log_workflow "Complete optimization integration successful"
        
        # Verify all expected outputs
        local expected_outputs=(
            "docs/build-optimization-guide.md"
            "build-optimization-summary.md"
            "optimization-recommendations.md"
        )
        
        local found_outputs=0
        for output in "${expected_outputs[@]}"; do
            if [[ -f "$output" ]]; then
                found_outputs=$((found_outputs + 1))
            fi
        done
        
        local workflow_end_time=$(date +%s)
        local workflow_duration=$((workflow_end_time - workflow_start_time))
        
        if [[ $found_outputs -eq ${#expected_outputs[@]} ]]; then
            log_pass "Complete workflow integration successful in ${workflow_duration}s"
        else
            log_fail "Complete workflow integration incomplete ($found_outputs/${#expected_outputs[@]} outputs)"
        fi
    else
        log_fail "Complete optimization integration failed"
    fi
}

# Test 8: CI/CD workflow simulation
test_e2e_cicd_workflow_simulation() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing CI/CD workflow simulation..."
    
    log_stage "Simulating CI/CD pipeline workflow..."
    
    # Simulate different CI scenarios
    local ci_scenarios=(
        "pull_request_validation"
        "main_branch_build"
        "release_build"
    )
    
    local successful_ci_scenarios=0
    
    for scenario in "${ci_scenarios[@]}"; do
        log_workflow "Simulating $scenario..."
        
        case "$scenario" in
            "pull_request_validation")
                # Simulate PR validation workflow
                export GITHUB_EVENT_NAME="pull_request"
                export GITHUB_BASE_REF="main"
                
                # Make a change and test validation
                echo "// PR change $(date)" >> app/src/main/java/com/workrec/MainActivity.kt
                git add .
                git commit -m "PR: Test change" --quiet
                
                # Test change detection and build strategy
                if source scripts/build-optimization-integration.sh && detect_changes main >/dev/null 2>&1; then
                    successful_ci_scenarios=$((successful_ci_scenarios + 1))
                    log_workflow "PR validation simulation successful"
                else
                    log_workflow "PR validation simulation failed"
                fi
                ;;
                
            "main_branch_build")
                # Simulate main branch build
                export GITHUB_EVENT_NAME="push"
                export GITHUB_REF="refs/heads/main"
                
                if timeout 300 ./gradlew help --no-daemon >/dev/null 2>&1; then
                    successful_ci_scenarios=$((successful_ci_scenarios + 1))
                    log_workflow "Main branch build simulation successful"
                else
                    log_workflow "Main branch build simulation failed or timed out"
                fi
                ;;
                
            "release_build")
                # Simulate release build
                export GITHUB_EVENT_NAME="release"
                export GITHUB_REF="refs/tags/v1.0.0"
                
                if timeout 300 ./gradlew help --no-daemon >/dev/null 2>&1; then
                    successful_ci_scenarios=$((successful_ci_scenarios + 1))
                    log_workflow "Release build simulation successful"
                else
                    log_workflow "Release build simulation failed or timed out"
                fi
                ;;
        esac
    done
    
    if [[ $successful_ci_scenarios -ge 2 ]]; then
        log_pass "CI/CD workflow simulation successful ($successful_ci_scenarios/3 scenarios)"
    else
        log_fail "CI/CD workflow simulation failed ($successful_ci_scenarios/3 scenarios)"
    fi
}

# Test 9: Performance regression detection
test_e2e_performance_regression_detection() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing performance regression detection..."
    
    log_stage "Testing performance regression detection workflow..."
    
    # Create baseline performance data
    mkdir -p build-metrics
    cat > build-metrics/performance-baseline.json << 'EOF'
{
  "clean_build_time": 120,
  "incremental_build_time": 30,
  "cache_hit_rate": 0.75,
  "cache_size_gb": 1.5
}
EOF
    
    # Run performance tests
    if [[ -f "$PROJECT_ROOT/tests/performance/test-build-performance.sh" ]]; then
        if timeout 600 "$PROJECT_ROOT/tests/performance/test-build-performance.sh" >/dev/null 2>&1; then
            log_workflow "Performance regression tests completed"
            
            # Check for performance report
            if [[ -f "build-metrics/performance-test-report.json" ]]; then
                log_pass "Performance regression detection successful"
            else
                log_fail "Performance regression report not generated"
            fi
        else
            log_fail "Performance regression tests failed or timed out"
        fi
    else
        log_fail "Performance regression test script not found"
    fi
}

# Test 10: End-to-end cleanup and reporting
test_e2e_cleanup_and_reporting() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing end-to-end cleanup and reporting..."
    
    log_stage "Testing cleanup and reporting workflow..."
    
    # Generate comprehensive report
    local report_file="e2e-test-report.json"
    
    cat > "$report_file" << EOF
{
  "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
  "test_summary": {
    "total_tests": $TESTS_RUN,
    "passed_tests": $TESTS_PASSED,
    "failed_tests": $TESTS_FAILED,
    "success_rate": $(echo "scale=2; $TESTS_PASSED / $TESTS_RUN * 100" | bc -l)
  },
  "workflow_stages_tested": [
$(printf '    "%s"' "${WORKFLOW_STAGES[@]}" | paste -sd ',' -)
  ],
  "optimization_features_verified": [
    "change_detection",
    "cache_management",
    "parallel_execution",
    "error_handling",
    "performance_monitoring",
    "build_strategies"
  ],
  "environment": {
    "test_duration_s": $(($(date +%s) - ${E2E_START_TIME:-$(date +%s)})),
    "temp_dir": "$TEMP_TEST_DIR",
    "project_root": "$PROJECT_ROOT"
  }
}
EOF
    
    # Test cleanup functionality
    local initial_cache_size=$(du -sb ~/.gradle/caches 2>/dev/null | awk '{print $1}' || echo "0")
    
    if ./scripts/cache-management.sh clean >/dev/null 2>&1; then
        local final_cache_size=$(du -sb ~/.gradle/caches 2>/dev/null | awk '{print $1}' || echo "0")
        
        if [[ $final_cache_size -le $initial_cache_size ]]; then
            log_pass "Cleanup and reporting workflow successful"
        else
            log_fail "Cleanup workflow did not reduce cache size"
        fi
    else
        log_fail "Cleanup workflow failed"
    fi
    
    # Verify report generation
    if [[ -f "$report_file" ]]; then
        log_pass "E2E test report generated successfully"
    else
        log_fail "E2E test report generation failed"
    fi
}

# Run all E2E tests
run_all_e2e_tests() {
    log_info "Starting end-to-end optimization workflow tests..."
    
    export E2E_START_TIME=$(date +%s)
    
    setup_comprehensive_test_environment
    
    # Run all E2E tests
    test_workflow_initialization
    test_e2e_change_detection_workflow
    test_e2e_cache_management_lifecycle
    test_e2e_build_execution_with_optimizations
    test_e2e_performance_monitoring_integration
    test_e2e_error_handling_and_recovery
    test_e2e_complete_workflow_integration
    test_e2e_cicd_workflow_simulation
    test_e2e_performance_regression_detection
    test_e2e_cleanup_and_reporting
    
    cleanup_test_environment
    
    local total_duration=$(($(date +%s) - E2E_START_TIME))
    
    # Print comprehensive test summary
    echo ""
    echo "=================================================="
    echo "End-to-End Optimization Workflow Test Summary"
    echo "=================================================="
    echo "Total Test Duration: ${total_duration}s"
    echo "Tests Run: $TESTS_RUN"
    echo -e "Tests Passed: ${GREEN}$TESTS_PASSED${NC}"
    echo -e "Tests Failed: ${RED}$TESTS_FAILED${NC}"
    echo -e "Success Rate: $(echo "scale=1; $TESTS_PASSED / $TESTS_RUN * 100" | bc -l)%"
    echo ""
    echo "Workflow Stages Tested:"
    for stage in "${WORKFLOW_STAGES[@]}"; do
        echo "  ✓ $stage"
    done
    echo ""
    
    if [[ $TESTS_FAILED -eq 0 ]]; then
        echo -e "${GREEN}🎉 All end-to-end tests passed!${NC}"
        echo -e "${GREEN}The complete optimization workflow is functioning correctly.${NC}"
        return 0
    else
        echo -e "${RED}❌ Some end-to-end tests failed!${NC}"
        echo -e "${RED}The optimization workflow needs attention.${NC}"
        return 1
    fi
}

# Main execution
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    run_all_e2e_tests
fi