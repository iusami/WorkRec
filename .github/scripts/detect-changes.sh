#!/bin/bash

# Change Detection and Build Strategy Script
# This script analyzes file changes and determines the appropriate build strategy

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to set environment variable (handles both CI and local environments)
set_env_var() {
    local var_name="$1"
    local var_value="$2"
    
    if [[ -n "$GITHUB_ENV" ]]; then
        echo "$var_name=$var_value" >> "$GITHUB_ENV"
    else
        export "$var_name"="$var_value"
    fi
}

# Function to detect file changes and categorize them
detect_changes() {
    local base_ref="${GITHUB_BASE_REF:-main}"
    local head_ref="${GITHUB_SHA:-HEAD}"
    
    print_info "Analyzing changes between $base_ref and $head_ref"
    
    # Get list of changed files
    local changed_files
    if [[ -n "$GITHUB_BASE_REF" ]]; then
        # For pull requests
        print_info "Detecting changes for pull request (base: $base_ref)"
        changed_files=$(git diff --name-only "origin/$base_ref...HEAD" 2>/dev/null || echo "")
    else
        # For push events, compare with previous commit
        print_info "Detecting changes for push event"
        changed_files=$(git diff --name-only HEAD~1 HEAD 2>/dev/null || echo "")
    fi
    
    if [[ -z "$changed_files" ]]; then
        print_warning "No changed files detected, defaulting to incremental build"
        set_env_var "BUILD_STRATEGY" "incremental"
        set_env_var "INCREMENTAL_BUILD" "true"
        return 0
    fi
    
    print_info "Changed files:"
    echo "$changed_files" | while read -r file; do
        [[ -n "$file" ]] && echo "  - $file"
    done
    
    # Categorize changes with comprehensive patterns
    local src_changes=$(echo "$changed_files" | grep -E '\.(kt|java)$' | grep -v 'src/test/' | grep -v 'src/androidTest/' || true)
    local test_changes=$(echo "$changed_files" | grep -E 'src/(test|androidTest)/.*\.(kt|java)$' || true)
    local build_changes=$(echo "$changed_files" | grep -E '\.(gradle|properties)$|gradle/|gradlew|build\.gradle\.kts|settings\.gradle\.kts|libs\.versions\.toml' || true)
    local doc_changes=$(echo "$changed_files" | grep -E '\.(md|txt|rst|adoc)$|docs/|README|CHANGELOG|LICENSE' || true)
    local config_changes=$(echo "$changed_files" | grep -E '\.github/|\.kiro/|proguard-rules\.pro|\.gitignore|\.editorconfig' || true)
    local resource_changes=$(echo "$changed_files" | grep -E 'src/main/res/|src/main/AndroidManifest\.xml' || true)
    local dependency_changes=$(echo "$changed_files" | grep -E 'gradle/wrapper/|gradle\.properties|libs\.versions\.toml' || true)
    
    # Count changes by category (handle empty strings properly)
    local src_count=0
    local test_count=0
    local build_count=0
    local doc_count=0
    local config_count=0
    local resource_count=0
    local dependency_count=0
    
    [[ -n "$src_changes" ]] && src_count=$(echo "$src_changes" | wc -l)
    [[ -n "$test_changes" ]] && test_count=$(echo "$test_changes" | wc -l)
    [[ -n "$build_changes" ]] && build_count=$(echo "$build_changes" | wc -l)
    [[ -n "$doc_changes" ]] && doc_count=$(echo "$doc_changes" | wc -l)
    [[ -n "$config_changes" ]] && config_count=$(echo "$config_changes" | wc -l)
    [[ -n "$resource_changes" ]] && resource_count=$(echo "$resource_changes" | wc -l)
    [[ -n "$dependency_changes" ]] && dependency_count=$(echo "$dependency_changes" | wc -l)
    
    print_info "Change analysis:"
    echo "  - Source files: $src_count"
    echo "  - Test files: $test_count"
    echo "  - Build files: $build_count"
    echo "  - Documentation: $doc_count"
    echo "  - Config files: $config_count"
    echo "  - Resource files: $resource_count"
    echo "  - Dependency files: $dependency_count"
    
    # Determine build strategy based on change types
    determine_build_strategy "$src_count" "$test_count" "$build_count" "$doc_count" "$config_count" "$resource_count" "$dependency_count"
}

# Function to determine build strategy based on change counts
determine_build_strategy() {
    local src_count=$1
    local test_count=$2
    local build_count=$3
    local doc_count=$4
    local config_count=$5
    local resource_count=$6
    local dependency_count=$7
    
    local total_non_doc=$((src_count + test_count + build_count + config_count + resource_count + dependency_count))
    
    # Priority order for build strategy determination:
    # 1. Skip build for documentation-only changes
    # 2. Full build for build system/dependency changes
    # 3. Incremental build for source/resource changes
    # 4. Test-only build for test-only changes
    # 5. Config-only build for configuration changes
    
    if [[ $total_non_doc -eq 0 && $doc_count -gt 0 ]]; then
        # Only documentation changes - skip build
        print_success "Documentation-only changes detected - skipping build"
        set_env_var "SKIP_BUILD" "true"
        set_env_var "BUILD_STRATEGY" "skip"
        set_env_var "SKIP_REASON" "documentation-only"
        
    elif [[ $build_count -gt 0 || $dependency_count -gt 0 ]]; then
        # Build system or dependency changes - full build required
        if [[ $dependency_count -gt 0 ]]; then
            print_warning "Dependency changes detected - full build required for cache invalidation"
        else
            print_warning "Build system changes detected - full build required"
        fi
        set_env_var "FULL_BUILD" "true"
        set_env_var "BUILD_STRATEGY" "full"
        set_env_var "CACHE_INVALIDATION" "true"
        
    elif [[ $src_count -gt 0 || $resource_count -gt 0 ]]; then
        # Source or resource changes - incremental build
        print_info "Source/resource changes detected - incremental build"
        set_env_var "INCREMENTAL_BUILD" "true"
        set_env_var "BUILD_STRATEGY" "incremental"
        
    elif [[ $test_count -gt 0 ]]; then
        # Only test changes - test-only build
        print_info "Test-only changes detected - test-only build"
        set_env_var "TEST_ONLY_BUILD" "true"
        set_env_var "BUILD_STRATEGY" "test-only"
        
    elif [[ $config_count -gt 0 ]]; then
        # Config changes only - lightweight incremental build
        print_info "Configuration changes detected - lightweight incremental build"
        set_env_var "INCREMENTAL_BUILD" "true"
        set_env_var "BUILD_STRATEGY" "config-only"
        
    else
        # Default to incremental build
        print_info "No specific change pattern detected - defaulting to incremental build"
        set_env_var "INCREMENTAL_BUILD" "true"
        set_env_var "BUILD_STRATEGY" "incremental"
    fi
}

# Function to set additional environment variables for build optimization
setup_build_environment() {
    local strategy="${BUILD_STRATEGY:-incremental}"
    
    print_info "Setting up build environment for strategy: $strategy"
    
    # Use GITHUB_ENV if available (in CI), otherwise create a temporary file for local testing
    local env_file="${GITHUB_ENV:-/tmp/github_env_$$}"
    if [[ -z "$GITHUB_ENV" ]]; then
        print_info "Running locally - environment variables will be written to $env_file"
    fi
    
    # Set Gradle optimization flags based on strategy
    case "$strategy" in
        "skip")
            set_env_var "GRADLE_OPTS" "-Dorg.gradle.daemon=false"
            set_env_var "GRADLE_TASKS" ""
            ;;
        "full")
            set_env_var "GRADLE_OPTS" "-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true -Dorg.gradle.workers.max=4 -Xmx4g -Dorg.gradle.configureondemand=true"
            set_env_var "GRADLE_TASKS" "clean assembleDebug assembleRelease testDebugUnitTest"
            ;;
        "incremental")
            set_env_var "GRADLE_OPTS" "-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true -Dorg.gradle.workers.max=4 -Xmx4g -Dorg.gradle.caching=true -Dorg.gradle.configureondemand=true"
            set_env_var "GRADLE_TASKS" "assembleDebug testDebugUnitTest"
            ;;
        "test-only")
            set_env_var "GRADLE_OPTS" "-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true -Xmx2g -Dorg.gradle.caching=true"
            set_env_var "GRADLE_TASKS" "testDebugUnitTest"
            ;;
        "config-only")
            set_env_var "GRADLE_OPTS" "-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true -Dorg.gradle.workers.max=2 -Xmx3g -Dorg.gradle.caching=true"
            set_env_var "GRADLE_TASKS" "assembleDebug"
            ;;
        *)
            print_warning "Unknown build strategy: $strategy, using incremental defaults"
            set_env_var "GRADLE_OPTS" "-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true -Dorg.gradle.workers.max=4 -Xmx4g -Dorg.gradle.caching=true -Dorg.gradle.configureondemand=true"
            set_env_var "GRADLE_TASKS" "assembleDebug testDebugUnitTest"
            ;;
    esac
    
    # Set cache strategy based on build type
    if [[ "$strategy" == "full" ]]; then
        set_env_var "CACHE_STRATEGY" "full"
    else
        set_env_var "CACHE_STRATEGY" "incremental"
    fi
    
    print_success "Build environment configured for $strategy strategy"
}

# Function to output build strategy summary
output_strategy_summary() {
    local strategy="${BUILD_STRATEGY:-incremental}"
    
    echo ""
    print_info "=== BUILD STRATEGY SUMMARY ==="
    echo "Strategy: $strategy"
    
    case "$strategy" in
        "skip")
            echo "Actions: Build will be skipped (documentation-only changes)"
            echo "Reason: ${SKIP_REASON:-unknown}"
            ;;
        "full")
            echo "Actions: Clean build + All tasks (build system/dependency changes detected)"
            echo "Cache Invalidation: ${CACHE_INVALIDATION:-false}"
            ;;
        "incremental")
            echo "Actions: Incremental build + Tests (source/resource changes)"
            ;;
        "test-only")
            echo "Actions: Tests only (test-only changes)"
            ;;
        "config-only")
            echo "Actions: Lightweight build (configuration changes only)"
            ;;
    esac
    
    echo "Gradle Options: ${GRADLE_OPTS:-default}"
    echo "Gradle Tasks: ${GRADLE_TASKS:-default}"
    echo "Cache Strategy: ${CACHE_STRATEGY:-default}"
    print_info "================================"
    echo ""
}

# Main execution
main() {
    print_info "Starting change detection and build strategy analysis..."
    
    # Ensure we're in a git repository
    if ! git rev-parse --git-dir > /dev/null 2>&1; then
        print_error "Not in a git repository"
        exit 1
    fi
    
    # Detect and categorize changes
    detect_changes
    
    # Setup build environment based on detected strategy
    setup_build_environment
    
    # Output summary
    output_strategy_summary
    
    print_success "Change detection and build strategy setup completed"
}

# Execute main function if script is run directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi