#!/bin/bash

# Build Retry Wrapper Script
# Integrates error handling, cache management, and build retry logic

set -euo pipefail

# Source the error handling script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/error-handling.sh"
source "$SCRIPT_DIR/cache-management.sh"

# Configuration
BUILD_RETRY_ENABLED=${BUILD_RETRY_ENABLED:-true}
CACHE_RECOVERY_ENABLED=${CACHE_RECOVERY_ENABLED:-true}
CLEAN_FALLBACK_ENABLED=${CLEAN_FALLBACK_ENABLED:-true}
ERROR_REPORTING_ENABLED=${ERROR_REPORTING_ENABLED:-true}

# Validate build environment
validate_build_environment() {
    log_info "Validating build environment..."
    
    local validation_errors=0
    
    # Check Java installation
    if ! command -v java >/dev/null 2>&1; then
        log_error "Java not found in PATH"
        validation_errors=$((validation_errors + 1))
    else
        local java_version=$(java -version 2>&1 | head -n1)
        log_info "Java version: $java_version"
    fi
    
    # Check Gradle wrapper
    if [[ ! -f "./gradlew" ]]; then
        log_error "Gradle wrapper not found"
        validation_errors=$((validation_errors + 1))
    elif [[ ! -x "./gradlew" ]]; then
        log_warn "Gradle wrapper not executable, fixing permissions..."
        chmod +x ./gradlew
    fi
    
    # Check disk space
    local available_space=$(df . | tail -n1 | awk '{print $4}')
    local available_gb=$(echo "scale=2; $available_space / 1024 / 1024" | bc -l 2>/dev/null || echo "0")
    if command -v bc >/dev/null 2>&1 && (( $(echo "$available_gb < 2" | bc -l 2>/dev/null || echo "0") )); then
        log_error "Insufficient disk space: ${available_gb}GB available (minimum 2GB required)"
        validation_errors=$((validation_errors + 1))
    fi
    
    # Check memory availability
    if command -v free >/dev/null 2>&1; then
        local available_memory=$(free -m | grep "^Mem:" | awk '{print $7}')
        if [[ $available_memory -lt 1024 ]]; then
            log_warn "Low available memory: ${available_memory}MB (recommended minimum 1GB)"
        fi
    fi
    
    # Check for required directories
    local required_dirs=("app/src" "gradle")
    for dir in "${required_dirs[@]}"; do
        if [[ ! -d "$dir" ]]; then
            log_error "Required directory not found: $dir"
            validation_errors=$((validation_errors + 1))
        fi
    done
    
    if [[ $validation_errors -eq 0 ]]; then
        log_success "Build environment validation passed"
        return 0
    else
        log_error "Build environment validation failed with $validation_errors errors"
        return 1
    fi
}

# Build wrapper with comprehensive error handling
execute_build_with_error_handling() {
    local build_command="$1"
    local build_type="${2:-default}"
    
    log_info "Starting build execution with error handling: $build_type"
    log_info "Build command: $build_command"
    
    # Initialize error tracking
    initialize_error_tracking
    
    # Pre-build system validation
    if ! validate_build_environment; then
        log_error "Build environment validation failed"
        return 1
    fi
    
    # Pre-build cache health check
    if [[ "$CACHE_RECOVERY_ENABLED" == "true" ]]; then
        log_info "Performing pre-build cache health check..."
        if ! monitor_cache_health; then
            log_warn "Cache health issues detected, attempting recovery..."
            if ! recover_cache_with_error_handling; then
                log_warn "Cache recovery failed, continuing with degraded cache"
            fi
        fi
    fi
    
    # Execute build with retry logic
    local build_success=false
    local final_exit_code=1
    
    if [[ "$BUILD_RETRY_ENABLED" == "true" ]]; then
        log_info "Executing build with retry logic..."
        if retry_build_with_backoff "$build_command"; then
            build_success=true
            final_exit_code=0
        else
            final_exit_code=$?
            log_warn "Build with retry failed, exit code: $final_exit_code"
        fi
    else
        log_info "Executing build without retry logic..."
        if timeout $BUILD_TIMEOUT bash -c "$build_command"; then
            build_success=true
            final_exit_code=0
        else
            final_exit_code=$?
            log_warn "Build failed, exit code: $final_exit_code"
        fi
    fi
    
    # If build failed and clean fallback is enabled, attempt clean build
    if [[ "$build_success" == false ]] && [[ "$CLEAN_FALLBACK_ENABLED" == "true" ]]; then
        log_info "Attempting clean build fallback..."
        if fallback_to_clean_build "$build_command"; then
            build_success=true
            final_exit_code=0
            log_success "Clean build fallback succeeded"
        else
            final_exit_code=$?
            log_error "Clean build fallback failed, exit code: $final_exit_code"
        fi
    fi
    
    # Post-build analysis and reporting
    if [[ "$build_success" == true ]]; then
        log_success "Build completed successfully"
        
        # Generate success metrics
        generate_build_success_report "$build_type"
        
        # Post-build cache optimization
        if [[ "$CACHE_RECOVERY_ENABLED" == "true" ]]; then
            log_info "Performing post-build cache optimization..."
            scripts/cache-management.sh optimize
        fi
    else
        log_error "All build attempts failed"
        
        # Generate comprehensive error report
        if [[ "$ERROR_REPORTING_ENABLED" == "true" ]]; then
            generate_error_report "build_all_attempts_failed" "$final_exit_code"
            send_error_notification "Build Failed" "All build attempts failed for $build_type build"
        fi
    fi
    
    return $final_exit_code
}



# Generate build success report
generate_build_success_report() {
    local build_type="$1"
    
    local success_report="build-success-$(date +%Y%m%d-%H%M%S).json"
    
    cat > "$success_report" << EOF
{
  "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
  "build_type": "$build_type",
  "success": true,
  "build_duration_seconds": $(($(date +%s) - ${BUILD_START_TIME:-$(date +%s)})),
  "retry_attempts": ${BUILD_FAILURE_COUNT:-0},
  "cache_recovery_attempts": ${RECOVERY_ATTEMPTS:-0},
  "optimizations_used": {
    "retry_logic": "$BUILD_RETRY_ENABLED",
    "cache_recovery": "$CACHE_RECOVERY_ENABLED",
    "clean_fallback": "$CLEAN_FALLBACK_ENABLED"
  },
  "system_metrics": {
    "final_cache_size_gb": $(get_dir_size_gb ~/.gradle/caches),
    "build_output_size_mb": $(du -sm app/build 2>/dev/null | cut -f1 || echo "0"),
    "java_version": "$(java -version 2>&1 | head -n1 | sed 's/"//g')"
  }
}
EOF
    
    log_info "Build success report generated: $success_report"
    
    # Export for CI integration
    if [[ -n "${GITHUB_ACTIONS:-}" ]]; then
        echo "BUILD_SUCCESS_REPORT_FILE=$success_report" >> "$GITHUB_ENV"
    fi
}

# Specialized build commands with error handling
build_debug_with_error_handling() {
    log_info "Starting debug build with error handling..."
    export BUILD_START_TIME=$(date +%s)
    execute_build_with_error_handling "./gradlew assembleDebug --stacktrace" "debug"
}

build_release_with_error_handling() {
    log_info "Starting release build with error handling..."
    export BUILD_START_TIME=$(date +%s)
    execute_build_with_error_handling "./gradlew assembleRelease --stacktrace" "release"
}

test_with_error_handling() {
    log_info "Starting tests with error handling..."
    export BUILD_START_TIME=$(date +%s)
    execute_build_with_error_handling "./gradlew testDebugUnitTest --stacktrace" "test"
}

lint_with_error_handling() {
    log_info "Starting lint analysis with error handling..."
    export BUILD_START_TIME=$(date +%s)
    execute_build_with_error_handling "./gradlew lint --stacktrace" "lint"
}

# CI-specific build execution with strategy-based error handling
execute_ci_build_with_strategy() {
    local build_strategy="${1:-incremental}"
    local gradle_tasks="${2:-assembleDebug testDebugUnitTest}"
    
    log_info "Executing CI build with strategy: $build_strategy"
    log_info "Gradle tasks: $gradle_tasks"
    
    # Adjust error handling based on build strategy
    case "$build_strategy" in
        "full")
            # Full builds get maximum error handling
            export MAX_RETRY_ATTEMPTS=3
            export BUILD_RETRY_ENABLED=true
            export CACHE_RECOVERY_ENABLED=true
            export CLEAN_FALLBACK_ENABLED=true
            ;;
        "incremental")
            # Incremental builds get moderate error handling
            export MAX_RETRY_ATTEMPTS=2
            export BUILD_RETRY_ENABLED=true
            export CACHE_RECOVERY_ENABLED=true
            export CLEAN_FALLBACK_ENABLED=false
            ;;
        "test-only")
            # Test-only builds get lightweight error handling
            export MAX_RETRY_ATTEMPTS=2
            export BUILD_RETRY_ENABLED=true
            export CACHE_RECOVERY_ENABLED=false
            export CLEAN_FALLBACK_ENABLED=false
            ;;
        "skip")
            log_info "Build strategy is skip, exiting successfully"
            return 0
            ;;
        *)
            # Default strategy
            export MAX_RETRY_ATTEMPTS=2
            export BUILD_RETRY_ENABLED=true
            export CACHE_RECOVERY_ENABLED=true
            export CLEAN_FALLBACK_ENABLED=false
            ;;
    esac
    
    # Execute build with strategy-specific configuration
    execute_build_with_error_handling "./gradlew $gradle_tasks --stacktrace" "$build_strategy"
}

# Health check for error handling system
health_check() {
    log_info "Performing error handling system health check..."
    
    local health_issues=0
    
    # Check if required scripts are available
    if [[ ! -f "$SCRIPT_DIR/error-handling.sh" ]]; then
        log_error "Error handling script not found"
        health_issues=$((health_issues + 1))
    fi
    
    if [[ ! -f "$SCRIPT_DIR/cache-management.sh" ]]; then
        log_error "Cache management script not found"
        health_issues=$((health_issues + 1))
    fi
    
    # Check if error handling functions are available
    if ! declare -f initialize_error_tracking >/dev/null; then
        log_error "Error tracking functions not available"
        health_issues=$((health_issues + 1))
    fi
    
    # Check if cache management functions are available
    if ! declare -f monitor_cache_health >/dev/null; then
        log_error "Cache management functions not available"
        health_issues=$((health_issues + 1))
    fi
    
    # Test basic functionality
    if ! validate_build_environment; then
        log_warn "Build environment validation failed (non-critical for health check)"
    fi
    
    if [[ $health_issues -eq 0 ]]; then
        log_success "Error handling system health check passed"
        return 0
    else
        log_error "Error handling system health check failed with $health_issues issues"
        return 1
    fi
}

# Main execution function
main() {
    local command="${1:-help}"
    shift || true
    
    case "$command" in
        "debug")
            build_debug_with_error_handling
            ;;
        "release")
            build_release_with_error_handling
            ;;
        "test")
            test_with_error_handling
            ;;
        "lint")
            lint_with_error_handling
            ;;
        "ci-build")
            execute_ci_build_with_strategy "$@"
            ;;
        "health-check")
            health_check
            ;;
        "validate")
            validate_build_environment
            ;;
        "help")
            echo "Build Retry Wrapper Usage:"
            echo "  debug                    - Build debug APK with error handling"
            echo "  release                  - Build release APK with error handling"
            echo "  test                     - Run tests with error handling"
            echo "  lint                     - Run lint analysis with error handling"
            echo "  ci-build <strategy> <tasks> - Execute CI build with strategy"
            echo "  health-check             - Check error handling system health"
            echo "  validate                 - Validate build environment"
            echo ""
            echo "Environment Variables:"
            echo "  BUILD_RETRY_ENABLED      - Enable/disable retry logic (default: true)"
            echo "  CACHE_RECOVERY_ENABLED   - Enable/disable cache recovery (default: true)"
            echo "  CLEAN_FALLBACK_ENABLED   - Enable/disable clean fallback (default: true)"
            echo "  ERROR_REPORTING_ENABLED  - Enable/disable error reporting (default: true)"
            echo "  MAX_RETRY_ATTEMPTS       - Maximum retry attempts (default: 3)"
            ;;
        *)
            log_error "Unknown command: $command"
            echo "Use '$0 help' for usage information"
            exit 1
            ;;
    esac
}

# Execute main function if script is run directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi