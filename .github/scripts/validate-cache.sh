#!/bin/bash

# Cache Validation Script
# Validates that the multi-layer caching strategy is working correctly

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if a cache directory exists and has content
validate_cache_layer() {
    local layer_name="$1"
    local cache_path="$2"
    local min_size_mb="${3:-1}"
    
    log_info "Validating $layer_name cache layer..."
    
    if [[ ! -d "$cache_path" ]]; then
        log_error "$layer_name cache directory does not exist: $cache_path"
        return 1
    fi
    
    local size_mb=$(du -sm "$cache_path" 2>/dev/null | cut -f1)
    if [[ $size_mb -lt $min_size_mb ]]; then
        log_warning "$layer_name cache is smaller than expected: ${size_mb}MB (min: ${min_size_mb}MB)"
        return 1
    fi
    
    local file_count=$(find "$cache_path" -type f 2>/dev/null | wc -l)
    if [[ $file_count -eq 0 ]]; then
        log_error "$layer_name cache has no files"
        return 1
    fi
    
    log_success "$layer_name cache validated: ${size_mb}MB, $file_count files"
    return 0
}

# Function to validate cache key generation
validate_cache_keys() {
    log_info "Validating cache key generation..."
    
    # Test dependency cache key
    local deps_key=$(find . -name "*.gradle*" -o -name "gradle-wrapper.properties" -o -name "gradle.properties" | sort | xargs sha256sum | sha256sum | cut -d' ' -f1)
    if [[ -n "$deps_key" ]]; then
        log_success "Dependency cache key generated: ${deps_key:0:12}..."
    else
        log_error "Failed to generate dependency cache key"
        return 1
    fi
    
    # Test source cache key
    local src_key=$(find app/src -name "*.kt" -o -name "*.java" | sort | xargs sha256sum 2>/dev/null | sha256sum | cut -d' ' -f1)
    if [[ -n "$src_key" ]]; then
        log_success "Source cache key generated: ${src_key:0:12}..."
    else
        log_warning "No source files found for cache key generation"
    fi
    
    # Test resource cache key
    local res_key=$(find app/src/main/res -type f 2>/dev/null | sort | xargs sha256sum 2>/dev/null | sha256sum | cut -d' ' -f1)
    if [[ -n "$res_key" ]]; then
        log_success "Resource cache key generated: ${res_key:0:12}..."
    else
        log_warning "No resource files found for cache key generation"
    fi
}

# Function to validate cache effectiveness
validate_cache_effectiveness() {
    log_info "Validating cache effectiveness..."
    
    # Check if cache statistics file exists
    if [[ -f "cache-stats.json" ]]; then
        local total_size=$(grep -o '"total_gb":[^,]*' cache-stats.json | cut -d':' -f2 | tr -d ' ')
        log_success "Cache statistics available, total size: ${total_size}GB"
        
        # Validate cache size is reasonable
        if (( $(echo "$total_size > 5.0" | bc -l 2>/dev/null || echo 0) )); then
            log_warning "Cache size is quite large: ${total_size}GB"
        fi
    else
        log_warning "Cache statistics file not found"
    fi
}

# Function to check cache permissions
validate_cache_permissions() {
    log_info "Validating cache permissions..."
    
    local cache_dirs=(
        "$HOME/.gradle/caches"
        ".gradle/build-cache"
        "app/build/intermediates"
        "app/build/generated/ksp"
    )
    
    for dir in "${cache_dirs[@]}"; do
        if [[ -d "$dir" ]]; then
            if [[ -r "$dir" && -w "$dir" ]]; then
                log_success "Cache directory permissions OK: $dir"
            else
                log_error "Cache directory permission issues: $dir"
                return 1
            fi
        fi
    done
}

# Function to validate cache structure
validate_cache_structure() {
    log_info "Validating cache directory structure..."
    
    local expected_dirs=(
        "$HOME/.gradle/caches/modules-2"
        "$HOME/.gradle/caches/jars-9"
        "$HOME/.gradle/caches/transforms-3"
        "$HOME/.gradle/wrapper"
    )
    
    for dir in "${expected_dirs[@]}"; do
        if [[ -d "$dir" ]]; then
            log_success "Expected cache directory exists: $(basename "$dir")"
        else
            log_warning "Expected cache directory missing: $(basename "$dir")"
        fi
    done
}

# Function to run comprehensive cache validation
run_full_validation() {
    log_info "Running comprehensive cache validation..."
    
    local validation_passed=0
    local validation_failed=0
    
    # Validate cache structure
    if validate_cache_structure; then
        ((validation_passed++))
    else
        ((validation_failed++))
    fi
    
    # Validate cache permissions
    if validate_cache_permissions; then
        ((validation_passed++))
    else
        ((validation_failed++))
    fi
    
    # Validate cache key generation
    if validate_cache_keys; then
        ((validation_passed++))
    else
        ((validation_failed++))
    fi
    
    # Validate individual cache layers
    local cache_layers=(
        "Gradle Dependencies:$HOME/.gradle/caches:10"
        "Gradle Wrapper:$HOME/.gradle/wrapper:1"
        "Build Cache:.gradle/build-cache:1"
        "Build Intermediates:app/build/intermediates:1"
        "KSP Generated:app/build/generated/ksp:1"
    )
    
    for layer in "${cache_layers[@]}"; do
        IFS=':' read -r name path min_size <<< "$layer"
        if validate_cache_layer "$name" "$path" "$min_size"; then
            ((validation_passed++))
        else
            ((validation_failed++))
        fi
    done
    
    # Validate cache effectiveness
    if validate_cache_effectiveness; then
        ((validation_passed++))
    else
        ((validation_failed++))
    fi
    
    # Summary
    log_info "Validation Summary:"
    echo "  Passed: $validation_passed"
    echo "  Failed: $validation_failed"
    echo "  Total:  $((validation_passed + validation_failed))"
    
    if [[ $validation_failed -eq 0 ]]; then
        log_success "All cache validations passed!"
        return 0
    else
        log_error "$validation_failed validation(s) failed"
        return 1
    fi
}

# Function to generate validation report
generate_validation_report() {
    log_info "Generating cache validation report..."
    
    local report_file="cache-validation-report.json"
    local timestamp=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
    
    cat > "$report_file" << EOF
{
  "timestamp": "$timestamp",
  "validation_results": {
    "cache_structure": "$(validate_cache_structure &>/dev/null && echo "passed" || echo "failed")",
    "cache_permissions": "$(validate_cache_permissions &>/dev/null && echo "passed" || echo "failed")",
    "cache_keys": "$(validate_cache_keys &>/dev/null && echo "passed" || echo "failed")",
    "cache_effectiveness": "$(validate_cache_effectiveness &>/dev/null && echo "passed" || echo "failed")"
  },
  "cache_layers": {
    "gradle_dependencies": "$(validate_cache_layer "Gradle Dependencies" "$HOME/.gradle/caches" 10 &>/dev/null && echo "passed" || echo "failed")",
    "gradle_wrapper": "$(validate_cache_layer "Gradle Wrapper" "$HOME/.gradle/wrapper" 1 &>/dev/null && echo "passed" || echo "failed")",
    "build_cache": "$(validate_cache_layer "Build Cache" ".gradle/build-cache" 1 &>/dev/null && echo "passed" || echo "failed")",
    "build_intermediates": "$(validate_cache_layer "Build Intermediates" "app/build/intermediates" 1 &>/dev/null && echo "passed" || echo "failed")",
    "ksp_generated": "$(validate_cache_layer "KSP Generated" "app/build/generated/ksp" 1 &>/dev/null && echo "passed" || echo "failed")"
  },
  "build_info": {
    "github_sha": "${GITHUB_SHA:-unknown}",
    "github_ref": "${GITHUB_REF:-unknown}",
    "runner_os": "${RUNNER_OS:-unknown}"
  }
}
EOF
    
    log_success "Validation report saved to $report_file"
}

# Main execution
main() {
    local command="${1:-validate}"
    
    case "$command" in
        "validate")
            run_full_validation
            ;;
        "structure")
            validate_cache_structure
            ;;
        "permissions")
            validate_cache_permissions
            ;;
        "keys")
            validate_cache_keys
            ;;
        "effectiveness")
            validate_cache_effectiveness
            ;;
        "report")
            generate_validation_report
            run_full_validation
            ;;
        *)
            echo "Usage: $0 {validate|structure|permissions|keys|effectiveness|report}"
            echo "  validate      - Run full cache validation"
            echo "  structure     - Validate cache directory structure"
            echo "  permissions   - Validate cache permissions"
            echo "  keys          - Validate cache key generation"
            echo "  effectiveness - Validate cache effectiveness"
            echo "  report        - Generate validation report and run full validation"
            exit 1
            ;;
    esac
}

# Install bc if not available (for floating point calculations)
if ! command -v bc &> /dev/null; then
    if command -v apt-get &> /dev/null; then
        sudo apt-get update && sudo apt-get install -y bc
    elif command -v yum &> /dev/null; then
        sudo yum install -y bc
    elif command -v brew &> /dev/null; then
        brew install bc
    fi
fi

main "$@"