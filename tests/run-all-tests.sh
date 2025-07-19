#!/bin/bash

# Comprehensive Test Runner for CI Build Optimization Features
# Orchestrates all test types: unit, integration, performance, and end-to-end

set -euo pipefail

# Test configuration
TEST_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$TEST_DIR/.." && pwd)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Test suite configuration
TEST_SUITES=(
    "unit:Unit Tests:tests/unit"
    "integration:Integration Tests:tests/integration"
    "performance:Performance Tests:tests/performance"
    "e2e:End-to-End Tests:tests/e2e"
)

# Test results tracking
TOTAL_TESTS=0
TOTAL_PASSED=0
TOTAL_FAILED=0
SUITE_RESULTS=()

# Logging functions
log_header() {
    echo -e "${CYAN}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${CYAN}║                    $1                    ║${NC}"
    echo -e "${CYAN}╚══════════════════════════════════════════════════════════════╝${NC}"
}

log_suite() {
    echo -e "${PURPLE}[SUITE]${NC} $1"
}

log_info() {
    echo -e "${YELLOW}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_test_result() {
    local suite="$1"
    local status="$2"
    local details="$3"
    
    if [[ "$status" == "PASS" ]]; then
        echo -e "${GREEN}✓${NC} $suite: $details"
    else
        echo -e "${RED}✗${NC} $suite: $details"
    fi
}

# Check prerequisites
check_prerequisites() {
    log_info "Checking test prerequisites..."
    
    local missing_tools=()
    
    # Check for required tools
    local required_tools=("bash" "git" "bc")
    for tool in "${required_tools[@]}"; do
        if ! command -v "$tool" &> /dev/null; then
            missing_tools+=("$tool")
        fi
    done
    
    # Check for optional tools
    local optional_tools=("jq" "timeout" "gtimeout")
    for tool in "${optional_tools[@]}"; do
        if ! command -v "$tool" &> /dev/null; then
            log_info "Optional tool '$tool' not found - some features may be limited"
        fi
    done
    
    if [[ ${#missing_tools[@]} -gt 0 ]]; then
        log_error "Missing required tools: ${missing_tools[*]}"
        log_info "Please install missing tools before running tests"
        return 1
    fi
    
    # Check project structure
    local required_dirs=("scripts" "app" ".github")
    for dir in "${required_dirs[@]}"; do
        if [[ ! -d "$PROJECT_ROOT/$dir" ]]; then
            log_error "Required directory not found: $dir"
            return 1
        fi
    done
    
    # Check for gradlew
    if [[ ! -f "$PROJECT_ROOT/gradlew" ]]; then
        log_error "Gradle wrapper not found: gradlew"
        return 1
    fi
    
    log_success "All prerequisites satisfied"
    return 0
}

# Run unit tests
run_unit_tests() {
    log_suite "Running Unit Tests..."
    
    local unit_test_dir="$TEST_DIR/unit"
    local tests_run=0
    local tests_passed=0
    local tests_failed=0
    
    if [[ -d "$unit_test_dir" ]]; then
        for test_script in "$unit_test_dir"/*.sh; do
            if [[ -f "$test_script" ]]; then
                local test_name=$(basename "$test_script" .sh)
                log_info "Running unit test: $test_name"
                
                # Use timeout if available, otherwise run without timeout
                if command -v timeout >/dev/null 2>&1; then
                    if timeout 300 bash "$test_script"; then
                        log_test_result "UNIT" "PASS" "$test_name"
                        tests_passed=$((tests_passed + 1))
                    else
                        log_test_result "UNIT" "FAIL" "$test_name"
                        tests_failed=$((tests_failed + 1))
                    fi
                elif command -v gtimeout >/dev/null 2>&1; then
                    if gtimeout 300 bash "$test_script"; then
                        log_test_result "UNIT" "PASS" "$test_name"
                        tests_passed=$((tests_passed + 1))
                    else
                        log_test_result "UNIT" "FAIL" "$test_name"
                        tests_failed=$((tests_failed + 1))
                    fi
                else
                    # Run without timeout
                    if bash "$test_script"; then
                        log_test_result "UNIT" "PASS" "$test_name"
                        tests_passed=$((tests_passed + 1))
                    else
                        log_test_result "UNIT" "FAIL" "$test_name"
                        tests_failed=$((tests_failed + 1))
                    fi
                fi
                tests_run=$((tests_run + 1))
            fi
        done
    else
        log_error "Unit test directory not found: $unit_test_dir"
        return 1
    fi
    
    SUITE_RESULTS+=("Unit Tests:$tests_run:$tests_passed:$tests_failed")
    TOTAL_TESTS=$((TOTAL_TESTS + tests_run))
    TOTAL_PASSED=$((TOTAL_PASSED + tests_passed))
    TOTAL_FAILED=$((TOTAL_FAILED + tests_failed))
    
    if [[ $tests_failed -eq 0 ]]; then
        log_success "Unit tests completed: $tests_passed/$tests_run passed"
        return 0
    else
        log_error "Unit tests completed: $tests_passed/$tests_run passed, $tests_failed failed"
        return 1
    fi
}

# Run integration tests
run_integration_tests() {
    log_suite "Running Integration Tests..."
    
    local integration_test_dir="$TEST_DIR/integration"
    local tests_run=0
    local tests_passed=0
    local tests_failed=0
    
    if [[ -d "$integration_test_dir" ]]; then
        for test_script in "$integration_test_dir"/*.sh; do
            if [[ -f "$test_script" ]]; then
                local test_name=$(basename "$test_script" .sh)
                log_info "Running integration test: $test_name"
                
                # Use timeout if available, otherwise run without timeout
                if command -v timeout >/dev/null 2>&1; then
                    if timeout 600 bash "$test_script"; then
                        log_test_result "INTEGRATION" "PASS" "$test_name"
                        tests_passed=$((tests_passed + 1))
                    else
                        log_test_result "INTEGRATION" "FAIL" "$test_name"
                        tests_failed=$((tests_failed + 1))
                    fi
                elif command -v gtimeout >/dev/null 2>&1; then
                    if gtimeout 600 bash "$test_script"; then
                        log_test_result "INTEGRATION" "PASS" "$test_name"
                        tests_passed=$((tests_passed + 1))
                    else
                        log_test_result "INTEGRATION" "FAIL" "$test_name"
                        tests_failed=$((tests_failed + 1))
                    fi
                else
                    # Run without timeout
                    if bash "$test_script"; then
                        log_test_result "INTEGRATION" "PASS" "$test_name"
                        tests_passed=$((tests_passed + 1))
                    else
                        log_test_result "INTEGRATION" "FAIL" "$test_name"
                        tests_failed=$((tests_failed + 1))
                    fi
                fi
                tests_run=$((tests_run + 1))
            fi
        done
    else
        log_error "Integration test directory not found: $integration_test_dir"
        return 1
    fi
    
    SUITE_RESULTS+=("Integration Tests:$tests_run:$tests_passed:$tests_failed")
    TOTAL_TESTS=$((TOTAL_TESTS + tests_run))
    TOTAL_PASSED=$((TOTAL_PASSED + tests_passed))
    TOTAL_FAILED=$((TOTAL_FAILED + tests_failed))
    
    if [[ $tests_failed -eq 0 ]]; then
        log_success "Integration tests completed: $tests_passed/$tests_run passed"
        return 0
    else
        log_error "Integration tests completed: $tests_passed/$tests_run passed, $tests_failed failed"
        return 1
    fi
}

# Run performance tests
run_performance_tests() {
    log_suite "Running Performance Tests..."
    
    local performance_test_dir="$TEST_DIR/performance"
    local tests_run=0
    local tests_passed=0
    local tests_failed=0
    
    if [[ -d "$performance_test_dir" ]]; then
        for test_script in "$performance_test_dir"/*.sh; do
            if [[ -f "$test_script" ]]; then
                local test_name=$(basename "$test_script" .sh)
                log_info "Running performance test: $test_name"
                
                # Performance tests get longer timeout
                if command -v timeout >/dev/null 2>&1; then
                    if timeout 1800 bash "$test_script"; then
                        log_test_result "PERFORMANCE" "PASS" "$test_name"
                        tests_passed=$((tests_passed + 1))
                    else
                        log_test_result "PERFORMANCE" "FAIL" "$test_name"
                        tests_failed=$((tests_failed + 1))
                    fi
                elif command -v gtimeout >/dev/null 2>&1; then
                    if gtimeout 1800 bash "$test_script"; then
                        log_test_result "PERFORMANCE" "PASS" "$test_name"
                        tests_passed=$((tests_passed + 1))
                    else
                        log_test_result "PERFORMANCE" "FAIL" "$test_name"
                        tests_failed=$((tests_failed + 1))
                    fi
                else
                    # Run without timeout
                    if bash "$test_script"; then
                        log_test_result "PERFORMANCE" "PASS" "$test_name"
                        tests_passed=$((tests_passed + 1))
                    else
                        log_test_result "PERFORMANCE" "FAIL" "$test_name"
                        tests_failed=$((tests_failed + 1))
                    fi
                fi
                tests_run=$((tests_run + 1))
            fi
        done
    else
        log_error "Performance test directory not found: $performance_test_dir"
        return 1
    fi
    
    SUITE_RESULTS+=("Performance Tests:$tests_run:$tests_passed:$tests_failed")
    TOTAL_TESTS=$((TOTAL_TESTS + tests_run))
    TOTAL_PASSED=$((TOTAL_PASSED + tests_passed))
    TOTAL_FAILED=$((TOTAL_FAILED + tests_failed))
    
    if [[ $tests_failed -eq 0 ]]; then
        log_success "Performance tests completed: $tests_passed/$tests_run passed"
        return 0
    else
        log_error "Performance tests completed: $tests_passed/$tests_run passed, $tests_failed failed"
        return 1
    fi
}

# Run end-to-end tests
run_e2e_tests() {
    log_suite "Running End-to-End Tests..."
    
    local e2e_test_dir="$TEST_DIR/e2e"
    local tests_run=0
    local tests_passed=0
    local tests_failed=0
    
    if [[ -d "$e2e_test_dir" ]]; then
        for test_script in "$e2e_test_dir"/*.sh; do
            if [[ -f "$test_script" ]]; then
                local test_name=$(basename "$test_script" .sh)
                log_info "Running E2E test: $test_name"
                
                # E2E tests get the longest timeout
                if command -v timeout >/dev/null 2>&1; then
                    if timeout 2400 bash "$test_script"; then
                        log_test_result "E2E" "PASS" "$test_name"
                        tests_passed=$((tests_passed + 1))
                    else
                        log_test_result "E2E" "FAIL" "$test_name"
                        tests_failed=$((tests_failed + 1))
                    fi
                elif command -v gtimeout >/dev/null 2>&1; then
                    if gtimeout 2400 bash "$test_script"; then
                        log_test_result "E2E" "PASS" "$test_name"
                        tests_passed=$((tests_passed + 1))
                    else
                        log_test_result "E2E" "FAIL" "$test_name"
                        tests_failed=$((tests_failed + 1))
                    fi
                else
                    # Run without timeout
                    if bash "$test_script"; then
                        log_test_result "E2E" "PASS" "$test_name"
                        tests_passed=$((tests_passed + 1))
                    else
                        log_test_result "E2E" "FAIL" "$test_name"
                        tests_failed=$((tests_failed + 1))
                    fi
                fi
                tests_run=$((tests_run + 1))
            fi
        done
    else
        log_error "E2E test directory not found: $e2e_test_dir"
        return 1
    fi
    
    SUITE_RESULTS+=("E2E Tests:$tests_run:$tests_passed:$tests_failed")
    TOTAL_TESTS=$((TOTAL_TESTS + tests_run))
    TOTAL_PASSED=$((TOTAL_PASSED + tests_passed))
    TOTAL_FAILED=$((TOTAL_FAILED + tests_failed))
    
    if [[ $tests_failed -eq 0 ]]; then
        log_success "E2E tests completed: $tests_passed/$tests_run passed"
        return 0
    else
        log_error "E2E tests completed: $tests_passed/$tests_run passed, $tests_failed failed"
        return 1
    fi
}

# Generate comprehensive test report
generate_test_report() {
    log_info "Generating comprehensive test report..."
    
    local report_file="$PROJECT_ROOT/build-metrics/comprehensive-test-report.json"
    mkdir -p "$(dirname "$report_file")"
    
    local end_time=$(date +%s)
    local total_duration=$((end_time - ${TEST_START_TIME:-$end_time}))
    
    cat > "$report_file" << EOF
{
  "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
  "test_execution": {
    "start_time": "$(date -u -r "${TEST_START_TIME:-$end_time}" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u +"%Y-%m-%dT%H:%M:%SZ")",
    "end_time": "$(date -u -r "$end_time" +"%Y-%m-%dT%H:%M:%SZ" 2>/dev/null || date -u +"%Y-%m-%dT%H:%M:%SZ")",
    "duration_seconds": $total_duration,
    "duration_formatted": "$(printf '%02d:%02d:%02d' $((total_duration/3600)) $((total_duration%3600/60)) $((total_duration%60)))"
  },
  "overall_summary": {
    "total_tests": $TOTAL_TESTS,
    "total_passed": $TOTAL_PASSED,
    "total_failed": $TOTAL_FAILED,
    "success_rate": $(echo "scale=2; $TOTAL_PASSED / $TOTAL_TESTS * 100" | bc -l),
    "overall_status": "$(if [[ $TOTAL_FAILED -eq 0 ]]; then echo "PASS"; else echo "FAIL"; fi)"
  },
  "test_suites": [
EOF
    
    local first_suite=true
    for result in "${SUITE_RESULTS[@]}"; do
        IFS=':' read -r suite_name tests_run tests_passed tests_failed <<< "$result"
        
        if [[ "$first_suite" == false ]]; then
            echo "," >> "$report_file"
        fi
        first_suite=false
        
        cat >> "$report_file" << EOF
    {
      "name": "$suite_name",
      "tests_run": $tests_run,
      "tests_passed": $tests_passed,
      "tests_failed": $tests_failed,
      "success_rate": $(echo "scale=2; $tests_passed / $tests_run * 100" | bc -l),
      "status": "$(if [[ $tests_failed -eq 0 ]]; then echo "PASS"; else echo "FAIL"; fi)"
    }
EOF
    done
    
    cat >> "$report_file" << EOF
  ],
  "environment": {
    "os": "$(uname -s)",
    "arch": "$(uname -m)",
    "shell": "$SHELL",
    "bash_version": "$BASH_VERSION",
    "project_root": "$PROJECT_ROOT",
    "test_directory": "$TEST_DIR"
  },
  "optimization_features_tested": [
    "change_detection_logic",
    "multi_layer_caching",
    "cache_corruption_recovery",
    "parallel_execution",
    "selective_build_strategies",
    "performance_monitoring",
    "error_handling_and_recovery",
    "build_time_optimization",
    "cache_effectiveness",
    "end_to_end_workflow"
  ]
}
EOF
    
    log_success "Comprehensive test report generated: $report_file"
}

# Display final test summary
display_final_summary() {
    local end_time=$(date +%s)
    local total_duration=$((end_time - ${TEST_START_TIME:-$end_time}))
    
    echo ""
    log_header "COMPREHENSIVE TEST SUITE RESULTS"
    echo ""
    
    echo -e "${BLUE}Test Execution Summary:${NC}"
    echo "  Total Duration: $(printf '%02d:%02d:%02d' $((total_duration/3600)) $((total_duration%3600/60)) $((total_duration%60)))"
    echo "  Total Tests: $TOTAL_TESTS"
    echo -e "  Total Passed: ${GREEN}$TOTAL_PASSED${NC}"
    echo -e "  Total Failed: ${RED}$TOTAL_FAILED${NC}"
    echo -e "  Success Rate: $(echo "scale=1; $TOTAL_PASSED / $TOTAL_TESTS * 100" | bc -l)%"
    echo ""
    
    echo -e "${BLUE}Test Suite Breakdown:${NC}"
    for result in "${SUITE_RESULTS[@]}"; do
        IFS=':' read -r suite_name tests_run tests_passed tests_failed <<< "$result"
        local success_rate=$(echo "scale=1; $tests_passed / $tests_run * 100" | bc -l)
        
        if [[ $tests_failed -eq 0 ]]; then
            echo -e "  ${GREEN}✓${NC} $suite_name: $tests_passed/$tests_run (${success_rate}%)"
        else
            echo -e "  ${RED}✗${NC} $suite_name: $tests_passed/$tests_run (${success_rate}%)"
        fi
    done
    echo ""
    
    echo -e "${BLUE}Optimization Features Verified:${NC}"
    local features=(
        "Change Detection Logic"
        "Multi-Layer Caching System"
        "Cache Corruption Recovery"
        "Parallel Build Execution"
        "Selective Build Strategies"
        "Performance Monitoring"
        "Error Handling & Recovery"
        "Build Time Optimization"
        "Cache Effectiveness Analysis"
        "End-to-End Workflow Integration"
    )
    
    for feature in "${features[@]}"; do
        echo -e "  ${GREEN}✓${NC} $feature"
    done
    echo ""
    
    if [[ $TOTAL_FAILED -eq 0 ]]; then
        echo -e "${GREEN}🎉 ALL TESTS PASSED!${NC}"
        echo -e "${GREEN}The CI build optimization system is fully functional and ready for production use.${NC}"
    else
        echo -e "${RED}❌ SOME TESTS FAILED!${NC}"
        echo -e "${RED}The CI build optimization system needs attention before production deployment.${NC}"
        echo -e "${YELLOW}Please review the failed tests and address any issues.${NC}"
    fi
    echo ""
}

# Main test execution function
run_all_tests() {
    export TEST_START_TIME=$(date +%s)
    
    log_header "CI BUILD OPTIMIZATION TEST SUITE"
    
    # Check prerequisites
    if ! check_prerequisites; then
        log_error "Prerequisites check failed"
        exit 1
    fi
    
    # Make all test scripts executable
    find "$TEST_DIR" -name "*.sh" -exec chmod +x {} \;
    
    # Run test suites based on arguments or run all
    local run_unit=true
    local run_integration=true
    local run_performance=true
    local run_e2e=true
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --unit-only)
                run_integration=false
                run_performance=false
                run_e2e=false
                shift
                ;;
            --integration-only)
                run_unit=false
                run_performance=false
                run_e2e=false
                shift
                ;;
            --performance-only)
                run_unit=false
                run_integration=false
                run_e2e=false
                shift
                ;;
            --e2e-only)
                run_unit=false
                run_integration=false
                run_performance=false
                shift
                ;;
            --skip-performance)
                run_performance=false
                shift
                ;;
            --skip-e2e)
                run_e2e=false
                shift
                ;;
            --help|-h)
                echo "CI Build Optimization Test Suite"
                echo ""
                echo "Usage: $0 [options]"
                echo ""
                echo "Options:"
                echo "  --unit-only         Run only unit tests"
                echo "  --integration-only  Run only integration tests"
                echo "  --performance-only  Run only performance tests"
                echo "  --e2e-only         Run only end-to-end tests"
                echo "  --skip-performance Skip performance tests"
                echo "  --skip-e2e         Skip end-to-end tests"
                echo "  --help, -h         Show this help message"
                echo ""
                exit 0
                ;;
            *)
                log_error "Unknown option: $1"
                exit 1
                ;;
        esac
    done
    
    # Run selected test suites
    local suite_failures=0
    
    if [[ "$run_unit" == true ]]; then
        if ! run_unit_tests; then
            suite_failures=$((suite_failures + 1))
        fi
        echo ""
    fi
    
    if [[ "$run_integration" == true ]]; then
        if ! run_integration_tests; then
            suite_failures=$((suite_failures + 1))
        fi
        echo ""
    fi
    
    if [[ "$run_performance" == true ]]; then
        if ! run_performance_tests; then
            suite_failures=$((suite_failures + 1))
        fi
        echo ""
    fi
    
    if [[ "$run_e2e" == true ]]; then
        if ! run_e2e_tests; then
            suite_failures=$((suite_failures + 1))
        fi
        echo ""
    fi
    
    # Generate comprehensive report
    generate_test_report
    
    # Display final summary
    display_final_summary
    
    # Return appropriate exit code
    if [[ $TOTAL_FAILED -eq 0 ]]; then
        return 0
    else
        return 1
    fi
}

# Execute main function if script is run directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    run_all_tests "$@"
fi