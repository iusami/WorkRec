#!/bin/bash

# Comprehensive Error Handling and Fallback Strategies Script
# Implements cache corruption recovery, build failure retry logic, and clean build fallbacks

set -euo pipefail

# Configuration
MAX_RETRY_ATTEMPTS=${MAX_RETRY_ATTEMPTS:-3}
RETRY_BASE_DELAY=${RETRY_BASE_DELAY:-5}
CACHE_CORRUPTION_THRESHOLD=${CACHE_CORRUPTION_THRESHOLD:-10}
BUILD_TIMEOUT=${BUILD_TIMEOUT:-1800}  # 30 minutes
ERROR_LOG_FILE="build-errors.log"
NOTIFICATION_WEBHOOK=${NOTIFICATION_WEBHOOK:-""}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1" | tee -a "$ERROR_LOG_FILE"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1" | tee -a "$ERROR_LOG_FILE"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "$ERROR_LOG_FILE"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1" | tee -a "$ERROR_LOG_FILE"
}

log_debug() {
    echo -e "${PURPLE}[DEBUG]${NC} $1" | tee -a "$ERROR_LOG_FILE"
}

# Initialize error tracking
initialize_error_tracking() {
    log_info "Initializing error tracking system..."
    
    # Create error log file with timestamp
    echo "=== Build Error Tracking Started at $(date) ===" > "$ERROR_LOG_FILE"
    
    # Set up trap for unexpected exits
    trap 'handle_unexpected_exit $?' EXIT
    trap 'handle_interrupt' INT TERM
    
    # Export error tracking variables with defaults
    export ERROR_COUNT=${ERROR_COUNT:-0}
    export CACHE_CORRUPTION_COUNT=${CACHE_CORRUPTION_COUNT:-0}
    export BUILD_FAILURE_COUNT=${BUILD_FAILURE_COUNT:-0}
    export LAST_ERROR_TYPE=${LAST_ERROR_TYPE:-""}
    export RECOVERY_ATTEMPTS=${RECOVERY_ATTEMPTS:-0}
    
    log_success "Error tracking system initialized"
}

# Handle unexpected script exits
handle_unexpected_exit() {
    local exit_code=$1
    if [[ $exit_code -ne 0 ]]; then
        log_error "Script exited unexpectedly with code $exit_code"
        generate_error_report "unexpected_exit" "$exit_code"
        send_error_notification "Script Exit" "Build script exited unexpectedly with code $exit_code"
    fi
}

# Handle interrupt signals
handle_interrupt() {
    log_warn "Build interrupted by user signal"
    generate_error_report "user_interrupt" "130"
    send_error_notification "Build Interrupted" "Build was interrupted by user signal"
    exit 130
}

# Cache corruption detection and recovery
detect_and_recover_cache_corruption() {
    log_info "Detecting cache corruption..."
    
    local corruption_indicators=0
    local corruption_details=""
    
    # Check for incomplete cache entries
    local incomplete_entries=$(find ~/.gradle/caches -name "*.tmp" -o -name "*.lock" 2>/dev/null | wc -l)
    if [[ $incomplete_entries -gt $CACHE_CORRUPTION_THRESHOLD ]]; then
        corruption_indicators=$((corruption_indicators + 1))
        corruption_details+="Incomplete entries: $incomplete_entries; "
    fi
    
    # Check for zero-byte cache files
    local zero_byte_files=$(find ~/.gradle/caches -type f -size 0 2>/dev/null | wc -l)
    if [[ $zero_byte_files -gt $CACHE_CORRUPTION_THRESHOLD ]]; then
        corruption_indicators=$((corruption_indicators + 1))
        corruption_details+="Zero-byte files: $zero_byte_files; "
    fi
    
    # Check for corrupted build cache entries
    if [[ -d ~/.gradle/caches/build-cache-1 ]]; then
        local corrupted_cache=$(find ~/.gradle/caches/build-cache-1 -name "*.bin" -exec file {} \; 2>/dev/null | grep -v "data" | wc -l)
        if [[ $corrupted_cache -gt 5 ]]; then
            corruption_indicators=$((corruption_indicators + 1))
            corruption_details+="Corrupted cache entries: $corrupted_cache; "
        fi
    fi
    
    # Check for permission issues
    local permission_errors=$(find ~/.gradle/caches -type f ! -readable 2>&1 | wc -l)
    if [[ $permission_errors -gt 0 ]]; then
        corruption_indicators=$((corruption_indicators + 1))
        corruption_details+="Permission errors: $permission_errors; "
    fi
    
    if [[ $corruption_indicators -gt 0 ]]; then
        log_warn "Cache corruption detected: $corruption_details"
        export CACHE_CORRUPTION_COUNT=$((CACHE_CORRUPTION_COUNT + 1))
        export LAST_ERROR_TYPE="cache_corruption"
        
        # Attempt cache recovery
        recover_corrupted_cache
        return 1
    else
        log_success "No cache corruption detected"
        return 0
    fi
}

# Recover from cache corruption
recover_corrupted_cache() {
    log_info "Attempting cache corruption recovery..."
    export RECOVERY_ATTEMPTS=$((RECOVERY_ATTEMPTS + 1))
    
    local recovery_success=false
    
    # Step 1: Clean up obvious corruption
    log_info "Step 1: Cleaning up corrupted cache entries..."
    
    # Remove incomplete entries
    find ~/.gradle/caches -name "*.tmp" -delete 2>/dev/null || true
    find ~/.gradle/caches -name "*.lock" -delete 2>/dev/null || true
    
    # Remove zero-byte files
    find ~/.gradle/caches -type f -size 0 -delete 2>/dev/null || true
    
    # Fix permissions
    chmod -R u+rw ~/.gradle/caches 2>/dev/null || true
    
    # Step 2: Validate critical cache directories
    log_info "Step 2: Validating cache directory structure..."
    
    local critical_dirs=(
        "~/.gradle/caches/modules-2"
        "~/.gradle/caches/transforms-3"
        "~/.gradle/caches/build-cache-1"
        "~/.gradle/wrapper"
    )
    
    for dir in "${critical_dirs[@]}"; do
        expanded_dir=$(eval echo "$dir")
        if [[ ! -d "$expanded_dir" ]]; then
            log_warn "Critical cache directory missing: $expanded_dir"
            mkdir -p "$expanded_dir" 2>/dev/null || true
        fi
    done
    
    # Step 3: Test cache functionality
    log_info "Step 3: Testing cache functionality..."
    
    if test_cache_functionality; then
        recovery_success=true
        log_success "Cache corruption recovery successful"
    else
        log_warn "Cache recovery failed, will attempt full cache reset"
        
        # Step 4: Full cache reset as last resort
        if [[ $RECOVERY_ATTEMPTS -lt 2 ]]; then
            log_info "Step 4: Performing full cache reset..."
            reset_gradle_cache
            
            if test_cache_functionality; then
                recovery_success=true
                log_success "Full cache reset successful"
            fi
        fi
    fi
    
    if [[ "$recovery_success" == true ]]; then
        log_success "Cache corruption recovery completed successfully"
        return 0
    else
        log_error "Cache corruption recovery failed after $RECOVERY_ATTEMPTS attempts"
        generate_error_report "cache_recovery_failed" "$RECOVERY_ATTEMPTS"
        return 1
    fi
}

# Test cache functionality
test_cache_functionality() {
    log_debug "Testing cache functionality..."
    
    # Test basic Gradle cache access
    if ! timeout 30 ./gradlew help --quiet --no-daemon 2>/dev/null; then
        log_debug "Basic Gradle cache test failed"
        return 1
    fi
    
    # Test dependency resolution
    if ! timeout 60 ./gradlew dependencies --configuration debugCompileClasspath --quiet --no-daemon 2>/dev/null; then
        log_debug "Dependency cache test failed"
        return 1
    fi
    
    log_debug "Cache functionality test passed"
    return 0
}

# Reset Gradle cache completely
reset_gradle_cache() {
    log_warn "Performing complete Gradle cache reset..."
    
    # Stop any running Gradle daemons
    ./gradlew --stop 2>/dev/null || true
    
    # Remove cache directories
    rm -rf ~/.gradle/caches 2>/dev/null || true
    rm -rf ~/.gradle/daemon 2>/dev/null || true
    rm -rf .gradle 2>/dev/null || true
    
    # Recreate basic structure
    mkdir -p ~/.gradle/caches
    mkdir -p .gradle
    
    log_warn "Gradle cache reset completed"
}

# Build failure retry logic with exponential backoff
retry_build_with_backoff() {
    local build_command="$1"
    local attempt=1
    local delay=$RETRY_BASE_DELAY
    
    log_info "Starting build with retry logic: $build_command"
    
    while [[ $attempt -le $MAX_RETRY_ATTEMPTS ]]; do
        log_info "Build attempt $attempt of $MAX_RETRY_ATTEMPTS"
        
        # Set timeout for build command (use gtimeout on macOS if available)
        local timeout_cmd="timeout"
        if ! command -v timeout >/dev/null 2>&1; then
            if command -v gtimeout >/dev/null 2>&1; then
                timeout_cmd="gtimeout"
            else
                # No timeout available, run without timeout
                timeout_cmd=""
            fi
        fi
        
        if [[ -n "$timeout_cmd" ]]; then
            if $timeout_cmd $BUILD_TIMEOUT bash -c "$build_command"; then
                log_success "Build succeeded on attempt $attempt"
                return 0
            else
                local exit_code=$?
                export BUILD_FAILURE_COUNT=$((${BUILD_FAILURE_COUNT:-0} + 1))
                export LAST_ERROR_TYPE="build_failure"
                
                log_error "Build failed on attempt $attempt with exit code $exit_code"
                
                # Analyze failure and determine recovery strategy
                analyze_build_failure "$exit_code" "$attempt"
                
                if [[ $attempt -lt $MAX_RETRY_ATTEMPTS ]]; then
                    log_info "Waiting ${delay}s before retry (exponential backoff)..."
                    sleep $delay
                    
                    # Apply recovery strategy based on failure analysis
                    apply_failure_recovery_strategy "$exit_code" "$attempt"
                    
                    # Exponential backoff
                    delay=$((delay * 2))
                    attempt=$((attempt + 1))
                else
                    log_error "Build failed after $MAX_RETRY_ATTEMPTS attempts"
                    generate_error_report "build_failure_max_retries" "$exit_code"
                    return $exit_code
                fi
            fi
        else
            # No timeout available, run command directly
            if bash -c "$build_command"; then
                log_success "Build succeeded on attempt $attempt"
                return 0
            else
                local exit_code=$?
                export BUILD_FAILURE_COUNT=$((${BUILD_FAILURE_COUNT:-0} + 1))
                export LAST_ERROR_TYPE="build_failure"
                
                log_error "Build failed on attempt $attempt with exit code $exit_code"
                
                # Analyze failure and determine recovery strategy
                analyze_build_failure "$exit_code" "$attempt"
                
                if [[ $attempt -lt $MAX_RETRY_ATTEMPTS ]]; then
                    log_info "Waiting ${delay}s before retry (exponential backoff)..."
                    sleep $delay
                    
                    # Apply recovery strategy based on failure analysis
                    apply_failure_recovery_strategy "$exit_code" "$attempt"
                    
                    # Exponential backoff
                    delay=$((delay * 2))
                    attempt=$((attempt + 1))
                else
                    log_error "Build failed after $MAX_RETRY_ATTEMPTS attempts"
                    generate_error_report "build_failure_max_retries" "$exit_code"
                    return $exit_code
                fi
            fi
        fi
    done
}

# Analyze build failure to determine cause
analyze_build_failure() {
    local exit_code=$1
    local attempt=$2
    
    log_info "Analyzing build failure (exit code: $exit_code, attempt: $attempt)..."
    
    # Check for common failure patterns in build logs
    local failure_analysis=""
    
    # Check for out of memory errors
    if grep -q "OutOfMemoryError\|GC overhead limit exceeded" build.log 2>/dev/null; then
        failure_analysis="memory_exhaustion"
        log_warn "Detected memory exhaustion in build"
    fi
    
    # Check for network/dependency issues
    if grep -q "Could not resolve\|Connection refused\|timeout" build.log 2>/dev/null; then
        failure_analysis="network_dependency_issue"
        log_warn "Detected network/dependency issues in build"
    fi
    
    # Check for cache corruption indicators
    if grep -q "Corrupt\|corrupted\|invalid cache" build.log 2>/dev/null; then
        failure_analysis="cache_corruption"
        log_warn "Detected cache corruption indicators in build"
    fi
    
    # Check for compilation errors
    if grep -q "Compilation failed\|cannot find symbol" build.log 2>/dev/null; then
        failure_analysis="compilation_error"
        log_warn "Detected compilation errors in build"
    fi
    
    # Check for permission issues
    if grep -q "Permission denied\|Access is denied" build.log 2>/dev/null; then
        failure_analysis="permission_issue"
        log_warn "Detected permission issues in build"
    fi
    
    # Export analysis for recovery strategy
    export FAILURE_ANALYSIS="$failure_analysis"
    
    log_info "Build failure analysis completed: $failure_analysis"
}

# Apply recovery strategy based on failure analysis
apply_failure_recovery_strategy() {
    local exit_code=$1
    local attempt=$2
    
    log_info "Applying recovery strategy for: $FAILURE_ANALYSIS"
    
    case "$FAILURE_ANALYSIS" in
        "memory_exhaustion")
            log_info "Applying memory exhaustion recovery..."
            # Reduce parallel workers and increase heap size
            export GRADLE_OPTS="-Xmx6g -XX:MaxMetaspaceSize=1g -Dorg.gradle.workers.max=1 -Dorg.gradle.daemon=false"
            # Clean up memory-intensive intermediate files
            rm -rf app/build/intermediates/transforms-* 2>/dev/null || true
            ;;
            
        "network_dependency_issue")
            log_info "Applying network/dependency recovery..."
            # Clear dependency cache and retry with offline mode disabled
            rm -rf ~/.gradle/caches/modules-2/metadata-* 2>/dev/null || true
            # Add network retry flags
            export GRADLE_OPTS="$GRADLE_OPTS -Dorg.gradle.internal.network.retry.max.attempts=5"
            ;;
            
        "cache_corruption")
            log_info "Applying cache corruption recovery..."
            # Trigger cache corruption recovery
            detect_and_recover_cache_corruption
            ;;
            
        "compilation_error")
            log_info "Applying compilation error recovery..."
            # Clean build outputs and retry
            ./gradlew clean --quiet --no-daemon 2>/dev/null || true
            rm -rf app/build/generated 2>/dev/null || true
            ;;
            
        "permission_issue")
            log_info "Applying permission issue recovery..."
            # Fix permissions on common directories
            chmod -R u+rw ~/.gradle 2>/dev/null || true
            chmod -R u+rw .gradle 2>/dev/null || true
            chmod +x ./gradlew 2>/dev/null || true
            ;;
            
        *)
            log_info "Applying generic recovery strategy..."
            # Generic recovery: clean intermediate files
            rm -rf app/build/tmp 2>/dev/null || true
            rm -rf build/tmp 2>/dev/null || true
            ;;
    esac
    
    log_info "Recovery strategy applied for attempt $((attempt + 1))"
}

# Fallback to clean build when optimizations fail
fallback_to_clean_build() {
    local original_command="$1"
    
    log_warn "Initiating fallback to clean build..."
    
    # Disable all optimizations
    export GRADLE_OPTS="-Xmx4g -Dorg.gradle.daemon=false -Dorg.gradle.caching=false -Dorg.gradle.parallel=false"
    
    # Clean everything
    log_info "Cleaning all build artifacts..."
    ./gradlew clean --no-daemon --quiet 2>/dev/null || true
    rm -rf app/build 2>/dev/null || true
    rm -rf build 2>/dev/null || true
    rm -rf .gradle 2>/dev/null || true
    
    # Clear caches
    log_info "Clearing all caches..."
    rm -rf ~/.gradle/caches 2>/dev/null || true
    
    # Modify command to disable optimizations
    local clean_command=$(echo "$original_command" | sed 's/--build-cache//g' | sed 's/--parallel//g')
    clean_command="$clean_command --no-build-cache --no-parallel --no-daemon"
    
    log_info "Executing clean build: $clean_command"
    
    # Execute clean build with extended timeout
    if timeout $((BUILD_TIMEOUT * 2)) bash -c "$clean_command"; then
        log_success "Clean build fallback succeeded"
        return 0
    else
        local exit_code=$?
        log_error "Clean build fallback failed with exit code $exit_code"
        generate_error_report "clean_build_fallback_failed" "$exit_code"
        return $exit_code
    fi
}

# Generate comprehensive error report
generate_error_report() {
    local error_type="$1"
    local error_code="$2"
    
    log_info "Generating error report for: $error_type"
    
    local report_file="error-report-$(date +%Y%m%d-%H%M%S).json"
    
    cat > "$report_file" << EOF
{
  "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
  "error_type": "$error_type",
  "error_code": "$error_code",
  "build_environment": {
    "os": "$(uname -s)",
    "arch": "$(uname -m)",
    "java_version": "$(java -version 2>&1 | head -n1)",
    "gradle_version": "$(./gradlew --version 2>/dev/null | grep Gradle | head -n1 || echo 'Unknown')",
    "available_memory": "$(free -h 2>/dev/null | grep Mem | awk '{print $2}' || echo 'Unknown')",
    "disk_space": "$(df -h . | tail -n1 | awk '{print $4}' || echo 'Unknown')"
  },
  "error_statistics": {
    "total_errors": ${ERROR_COUNT:-0},
    "cache_corruption_count": ${CACHE_CORRUPTION_COUNT:-0},
    "build_failure_count": ${BUILD_FAILURE_COUNT:-0},
    "recovery_attempts": ${RECOVERY_ATTEMPTS:-0},
    "last_error_type": "${LAST_ERROR_TYPE:-unknown}"
  },
  "system_state": {
    "gradle_daemon_running": $(pgrep -f "GradleDaemon" >/dev/null && echo "true" || echo "false"),
    "cache_size_mb": $(du -sm ~/.gradle/caches 2>/dev/null | cut -f1 || echo "0"),
    "build_dir_size_mb": $(du -sm app/build 2>/dev/null | cut -f1 || echo "0"),
    "temp_dir_size_mb": $(du -sm /tmp 2>/dev/null | cut -f1 || echo "0")
  },
  "recent_log_entries": [
$(tail -n 20 "$ERROR_LOG_FILE" 2>/dev/null | sed 's/"/\\"/g' | sed 's/^/    "/' | sed 's/$/",/' | sed '$ s/,$//')
  ]
}
EOF
    
    log_success "Error report generated: $report_file"
    
    # Upload to CI artifacts if in CI environment
    if [[ -n "${GITHUB_ACTIONS:-}" ]]; then
        echo "ERROR_REPORT_FILE=$report_file" >> "$GITHUB_ENV"
    fi
}

# Send error notifications
send_error_notification() {
    local title="$1"
    local message="$2"
    
    log_info "Sending error notification: $title"
    
    # Send to webhook if configured
    if [[ -n "$NOTIFICATION_WEBHOOK" ]]; then
        local payload=$(cat << EOF
{
  "title": "$title",
  "message": "$message",
  "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
  "build_info": {
    "repository": "${GITHUB_REPOSITORY:-unknown}",
    "branch": "${GITHUB_REF_NAME:-unknown}",
    "commit": "${GITHUB_SHA:-unknown}",
    "run_id": "${GITHUB_RUN_ID:-unknown}"
  }
}
EOF
        )
        
        if curl -s -X POST -H "Content-Type: application/json" -d "$payload" "$NOTIFICATION_WEBHOOK" >/dev/null; then
            log_success "Error notification sent successfully"
        else
            log_warn "Failed to send error notification to webhook"
        fi
    fi
    
    # Send to GitHub Actions annotations if in CI
    if [[ -n "${GITHUB_ACTIONS:-}" ]]; then
        echo "::error title=$title::$message"
    fi
    
    # Log to system log if available
    if command -v logger >/dev/null 2>&1; then
        logger -t "build-error" "$title: $message"
    fi
}

# Main error handling orchestrator
handle_build_with_error_recovery() {
    local build_command="$1"
    
    log_info "Starting build with comprehensive error handling..."
    
    # Initialize error tracking
    initialize_error_tracking
    
    # Pre-build cache corruption check
    if ! detect_and_recover_cache_corruption; then
        log_warn "Cache corruption detected and recovery attempted"
    fi
    
    # Attempt build with retry logic
    if retry_build_with_backoff "$build_command"; then
        log_success "Build completed successfully with error handling"
        return 0
    else
        local exit_code=$?
        log_error "Build failed after all retry attempts"
        
        # Attempt clean build fallback
        log_info "Attempting clean build fallback..."
        if fallback_to_clean_build "$build_command"; then
            log_success "Clean build fallback succeeded"
            send_error_notification "Build Recovery" "Build succeeded after clean fallback"
            return 0
        else
            local fallback_exit_code=$?
            log_error "All recovery attempts failed"
            
            # Generate final error report
            generate_error_report "all_recovery_failed" "$fallback_exit_code"
            send_error_notification "Build Failed" "All build and recovery attempts failed"
            
            return $fallback_exit_code
        fi
    fi
}

# Validate error handling system
validate_error_handling_system() {
    log_info "Validating error handling system..."
    
    local validation_passed=true
    
    # Test 1: Error logging
    if [[ ! -f "$ERROR_LOG_FILE" ]]; then
        log_error "Error log file not created"
        validation_passed=false
    fi
    
    # Test 2: Cache corruption detection
    if ! command -v find >/dev/null 2>&1; then
        log_error "Required command 'find' not available"
        validation_passed=false
    fi
    
    # Test 3: Gradle availability
    if [[ ! -f "./gradlew" ]]; then
        log_error "Gradle wrapper not found"
        validation_passed=false
    fi
    
    # Test 4: Required directories
    if [[ ! -d ~/.gradle ]]; then
        mkdir -p ~/.gradle
        log_warn "Created missing .gradle directory"
    fi
    
    if [[ "$validation_passed" == true ]]; then
        log_success "Error handling system validation passed"
        return 0
    else
        log_error "Error handling system validation failed"
        return 1
    fi
}

# Main execution function
main() {
    local command="${1:-help}"
    
    case "$command" in
        "validate")
            validate_error_handling_system
            ;;
        "test-corruption")
            detect_and_recover_cache_corruption
            ;;
        "test-retry")
            retry_build_with_backoff "${2:-./gradlew help}"
            ;;
        "clean-fallback")
            fallback_to_clean_build "${2:-./gradlew assembleDebug}"
            ;;
        "handle-build")
            handle_build_with_error_recovery "${2:-./gradlew assembleDebug}"
            ;;
        "help")
            echo "Error Handling Script Usage:"
            echo "  validate          - Validate error handling system"
            echo "  test-corruption   - Test cache corruption detection"
            echo "  test-retry        - Test retry logic with command"
            echo "  clean-fallback    - Test clean build fallback"
            echo "  handle-build      - Full error handling for build command"
            ;;
        *)
            log_error "Unknown command: $command"
            exit 1
            ;;
    esac
}

# Execute main function if script is run directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi