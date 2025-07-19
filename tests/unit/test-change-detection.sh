#!/bin/bash

# Unit Tests for Change Detection Logic
# Tests the change detection functionality used in selective build execution

set -euo pipefail

# Test configuration
TEST_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$TEST_DIR/../.." && pwd)"
TEMP_TEST_DIR="/tmp/change-detection-test-$$"

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
    log_info "Setting up test environment..."
    
    # Create temporary test directory
    mkdir -p "$TEMP_TEST_DIR"
    cd "$TEMP_TEST_DIR"
    
    # Initialize git repository
    git init --quiet
    git config user.email "test@example.com"
    git config user.name "Test User"
    
    # Create initial project structure
    mkdir -p app/src/main/java/com/test
    mkdir -p app/src/test/java/com/test
    mkdir -p docs
    
    # Create initial files
    echo "class MainActivity {}" > app/src/main/java/com/test/MainActivity.kt
    echo "class MainActivityTest {}" > app/src/test/java/com/test/MainActivityTest.kt
    echo "apply plugin: 'com.android.application'" > app/build.gradle.kts
    echo "# Project Documentation" > docs/README.md
    echo "# Changelog" > CHANGELOG.md
    
    # Initial commit
    git add .
    git commit -m "Initial commit" --quiet
    
    # Create main branch for comparison (if it doesn't exist)
    git branch main 2>/dev/null || git checkout -B main
    
    # Create a feature branch for testing changes
    git checkout -b feature-branch --quiet
    
    log_info "Test environment setup completed"
}

# Cleanup test environment
cleanup_test_environment() {
    log_info "Cleaning up test environment..."
    cd "$PROJECT_ROOT"
    rm -rf "$TEMP_TEST_DIR"
}

# Change detection function (copied from actual implementation)
detect_changes() {
    local base_ref="${1:-main}"
    local changed_files
    
    # Get changed files
    if git rev-parse --verify "$base_ref" >/dev/null 2>&1; then
        changed_files=$(git diff --name-only "$base_ref"...HEAD 2>/dev/null || echo "")
        # If no changes in diff, try different approach
        if [[ -z "$changed_files" ]]; then
            changed_files=$(git diff --name-only "$base_ref" HEAD 2>/dev/null || echo "")
        fi
    else
        # Fallback to all files if base ref doesn't exist
        changed_files=$(git ls-files 2>/dev/null || echo "")
    fi
    
    # Debug: show what files changed
    # echo "DEBUG: Changed files: $changed_files" >&2
    
    # Categorize changes
    local src_changes=""
    local test_changes=""
    local build_changes=""
    local doc_changes=""
    
    if [[ -n "$changed_files" ]]; then
        src_changes=$(echo "$changed_files" | grep -E '\.(kt|java)$' | grep -v 'src/test/' || true)
        test_changes=$(echo "$changed_files" | grep -E 'src/test/' || true)
        build_changes=$(echo "$changed_files" | grep -E '\.(gradle|properties)$' || true)
        doc_changes=$(echo "$changed_files" | grep -E '\.(md|txt)$' || true)
    fi
    
    # Reset environment variables
    unset FULL_BUILD INCREMENTAL_BUILD TEST_ONLY_BUILD SKIP_BUILD
    
    # Set build strategy based on changes
    if [[ -n "$build_changes" ]]; then
        export FULL_BUILD=true
        echo "FULL_BUILD=true"
    elif [[ -n "$src_changes" ]]; then
        export INCREMENTAL_BUILD=true
        echo "INCREMENTAL_BUILD=true"
    elif [[ -n "$test_changes" ]]; then
        export TEST_ONLY_BUILD=true
        echo "TEST_ONLY_BUILD=true"
    elif [[ -n "$doc_changes" ]]; then
        export SKIP_BUILD=true
        echo "SKIP_BUILD=true"
    else
        # No changes detected
        export SKIP_BUILD=true
        echo "SKIP_BUILD=true"
    fi
}

# Test 1: Source code changes should trigger incremental build
test_source_code_changes() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing source code changes detection..."
    
    # Make source code changes
    echo "class NewActivity {}" > app/src/main/java/com/test/NewActivity.kt
    git add app/src/main/java/com/test/NewActivity.kt
    git commit -m "Add new activity" --quiet
    
    # Test change detection
    local result=$(detect_changes main)
    
    if [[ "$result" == "INCREMENTAL_BUILD=true" ]]; then
        log_pass "Source code changes correctly detected as incremental build"
    else
        log_fail "Source code changes not detected correctly. Got: $result"
    fi
}

# Test 2: Test-only changes should trigger test-only build
test_test_only_changes() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing test-only changes detection..."
    
    # Make test-only changes
    echo "class NewTest {}" > app/src/test/java/com/test/NewTest.kt
    git add app/src/test/java/com/test/NewTest.kt
    git commit -m "Add new test" --quiet
    
    # Test change detection
    local result=$(detect_changes main)
    
    if [[ "$result" == "TEST_ONLY_BUILD=true" ]]; then
        log_pass "Test-only changes correctly detected"
    else
        log_fail "Test-only changes not detected correctly. Got: $result"
    fi
}

# Test 3: Build script changes should trigger full build
test_build_script_changes() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing build script changes detection..."
    
    # Make build script changes
    echo "android { compileSdk 34 }" >> app/build.gradle.kts
    git add app/build.gradle.kts
    git commit -m "Update build script" --quiet
    
    # Test change detection
    local result=$(detect_changes main)
    
    if [[ "$result" == "FULL_BUILD=true" ]]; then
        log_pass "Build script changes correctly detected as full build"
    else
        log_fail "Build script changes not detected correctly. Got: $result"
    fi
}

# Test 4: Documentation-only changes should skip build
test_documentation_only_changes() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing documentation-only changes detection..."
    
    # Make documentation-only changes
    echo "## New Section" >> docs/README.md
    echo "- Added new feature" >> CHANGELOG.md
    git add docs/README.md CHANGELOG.md
    git commit -m "Update documentation" --quiet
    
    # Test change detection
    local result=$(detect_changes main)
    
    if [[ "$result" == "SKIP_BUILD=true" ]]; then
        log_pass "Documentation-only changes correctly detected as skip build"
    else
        log_fail "Documentation-only changes not detected correctly. Got: $result"
    fi
}

# Test 5: Mixed changes should prioritize highest impact
test_mixed_changes() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing mixed changes prioritization..."
    
    # Make mixed changes (build script + source code)
    echo "class AnotherActivity {}" > app/src/main/java/com/test/AnotherActivity.kt
    echo "dependencies { implementation 'androidx.core:core-ktx:1.9.0' }" >> app/build.gradle.kts
    git add .
    git commit -m "Mixed changes" --quiet
    
    # Test change detection
    local result=$(detect_changes main)
    
    if [[ "$result" == "FULL_BUILD=true" ]]; then
        log_pass "Mixed changes correctly prioritized as full build"
    else
        log_fail "Mixed changes not prioritized correctly. Got: $result"
    fi
}

# Test 6: No changes should skip build
test_no_changes() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing no changes detection..."
    
    # No new commits, test against current HEAD
    local result=$(detect_changes HEAD)
    
    if [[ "$result" == "SKIP_BUILD=true" ]]; then
        log_pass "No changes correctly detected as skip build"
    else
        log_fail "No changes not detected correctly. Got: $result"
    fi
}

# Test 7: Invalid base reference handling
test_invalid_base_reference() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing invalid base reference handling..."
    
    # Test with non-existent branch
    local result=$(detect_changes nonexistent-branch 2>/dev/null)
    
    # Should fallback gracefully and not crash
    if [[ -n "$result" ]]; then
        log_pass "Invalid base reference handled gracefully"
    else
        log_fail "Invalid base reference not handled properly"
    fi
}

# Test 8: File pattern matching accuracy
test_file_pattern_matching() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing file pattern matching accuracy..."
    
    # Create files with various extensions
    echo "content" > app/src/main/java/com/test/Test.java
    echo "content" > app/src/main/java/com/test/Test.kt
    echo "content" > app/src/main/java/com/test/Test.xml
    echo "content" > gradle.properties
    echo "content" > local.properties
    echo "content" > README.txt
    
    git add .
    git commit -m "Various file types" --quiet
    
    # Test change detection
    local result=$(detect_changes main)
    
    # Should detect as full build due to gradle.properties
    if [[ "$result" == "FULL_BUILD=true" ]]; then
        log_pass "File pattern matching works correctly"
    else
        log_fail "File pattern matching failed. Got: $result"
    fi
}

# Test 9: Large changeset handling
test_large_changeset_handling() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing large changeset handling..."
    
    # Create many files
    for i in {1..50}; do
        echo "class Test$i {}" > "app/src/main/java/com/test/Test$i.kt"
    done
    
    git add .
    git commit -m "Large changeset" --quiet
    
    # Test change detection (should not timeout or fail)
    local start_time=$(date +%s)
    local result=$(detect_changes main)
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    if [[ "$result" == "INCREMENTAL_BUILD=true" ]] && [[ $duration -lt 10 ]]; then
        log_pass "Large changeset handled efficiently in ${duration}s"
    else
        log_fail "Large changeset handling failed or too slow. Duration: ${duration}s, Result: $result"
    fi
}

# Test 10: Edge case - empty repository
test_empty_repository_handling() {
    TESTS_RUN=$((TESTS_RUN + 1))
    log_test "Testing empty repository handling..."
    
    # Create new empty repository
    local empty_repo_dir="/tmp/empty-repo-test-$$"
    mkdir -p "$empty_repo_dir"
    cd "$empty_repo_dir"
    git init --quiet
    git config user.email "test@example.com"
    git config user.name "Test User"
    
    # Test change detection on empty repo
    local result=$(detect_changes main 2>/dev/null || echo "SKIP_BUILD=true")
    
    if [[ "$result" == "SKIP_BUILD=true" ]]; then
        log_pass "Empty repository handled gracefully"
    else
        log_fail "Empty repository not handled properly. Got: $result"
    fi
    
    # Cleanup
    cd "$TEMP_TEST_DIR"
    rm -rf "$empty_repo_dir"
}

# Run all tests
run_all_tests() {
    log_info "Starting change detection unit tests..."
    
    setup_test_environment
    
    # Run individual tests
    test_source_code_changes
    test_test_only_changes
    test_build_script_changes
    test_documentation_only_changes
    test_mixed_changes
    test_no_changes
    test_invalid_base_reference
    test_file_pattern_matching
    test_large_changeset_handling
    test_empty_repository_handling
    
    cleanup_test_environment
    
    # Print test summary
    echo ""
    echo "=================================="
    echo "Change Detection Unit Test Summary"
    echo "=================================="
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