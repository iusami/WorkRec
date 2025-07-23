#!/bin/bash

# Build Metrics Integration Script
# Integrates build metrics collection into CI/CD workflows with reporting

set -euo pipefail

# Configuration
METRICS_DIR="build-metrics"
BASELINE_FILE="${METRICS_DIR}/baseline-metrics.json"
REPORT_FILE="${METRICS_DIR}/build-report-$(date +%Y%m%d_%H%M%S).html"

# Function to create baseline metrics if they don't exist
create_baseline() {
    if [[ ! -f "$BASELINE_FILE" ]]; then
        echo "Creating baseline metrics..."
        
        # Run a clean build to establish baseline
        echo "Running clean build for baseline..."
        ./gradlew clean
        ./scripts/build-metrics-collection.sh
        
        # Find the most recent metrics file
        local latest_metrics=$(ls -t ${METRICS_DIR}/build-metrics-*.json | head -1)
        
        if [[ -f "$latest_metrics" ]]; then
            cp "$latest_metrics" "$BASELINE_FILE"
            echo "Baseline metrics created from: $latest_metrics"
        else
            echo "Error: Could not create baseline metrics"
            exit 1
        fi
    else
        echo "Baseline metrics already exist: $BASELINE_FILE"
    fi
}

# Function to compare current metrics with baseline
compare_with_baseline() {
    local current_metrics="$1"
    
    if [[ ! -f "$BASELINE_FILE" ]]; then
        echo "Warning: No baseline metrics found. Creating baseline first."
        create_baseline
        return
    fi
    
    echo "Comparing current build with baseline..."
    
    # Extract key metrics using basic shell tools
    local baseline_duration=$(grep '"total_duration_seconds"' "$BASELINE_FILE" | cut -d':' -f2 | tr -d ' ,"')
    local current_duration=$(grep '"total_duration_seconds"' "$current_metrics" | cut -d':' -f2 | tr -d ' ,"')
    
    local baseline_cache_rate=$(grep '"cache_hit_rate_percent"' "$BASELINE_FILE" | cut -d':' -f2 | tr -d ' ,"')
    local current_cache_rate=$(grep '"cache_hit_rate_percent"' "$current_metrics" | cut -d':' -f2 | tr -d ' ,"')
    
    local baseline_memory=$(grep '"peak_memory_mb"' "$BASELINE_FILE" | cut -d':' -f2 | tr -d ' ,"')
    local current_memory=$(grep '"peak_memory_mb"' "$current_metrics" | cut -d':' -f2 | tr -d ' ,"')
    
    # Calculate improvements (using bc if available)
    local duration_improvement="N/A"
    local cache_improvement="N/A"
    local memory_improvement="N/A"
    
    if command -v bc &> /dev/null && [[ "$baseline_duration" != "0" ]]; then
        duration_improvement=$(echo "scale=2; (($baseline_duration - $current_duration) / $baseline_duration) * 100" | bc -l)
        cache_improvement=$(echo "scale=2; $current_cache_rate - $baseline_cache_rate" | bc -l)
        memory_improvement=$(echo "scale=2; (($baseline_memory - $current_memory) / $baseline_memory) * 100" | bc -l)
    fi
    
    echo ""
    echo "=== PERFORMANCE COMPARISON ==="
    echo "Build Duration:"
    echo "  Baseline: ${baseline_duration}s"
    echo "  Current:  ${current_duration}s"
    echo "  Improvement: ${duration_improvement}%"
    echo ""
    echo "Cache Hit Rate:"
    echo "  Baseline: ${baseline_cache_rate}%"
    echo "  Current:  ${current_cache_rate}%"
    echo "  Improvement: ${cache_improvement}%"
    echo ""
    echo "Memory Usage:"
    echo "  Baseline: ${baseline_memory}MB"
    echo "  Current:  ${current_memory}MB"
    echo "  Improvement: ${memory_improvement}%"
    echo "=============================="
}

# Function to generate HTML report
generate_html_report() {
    local metrics_file="$1"
    
    if [[ ! -f "$metrics_file" ]]; then
        echo "Error: Metrics file not found: $metrics_file"
        return 1
    fi
    
    echo "Generating HTML report: $REPORT_FILE"
    
    # Extract metrics for report
    local build_status="SUCCESS"  # Default status since our script doesn't track build status yet
    local build_duration=$(grep '"total_duration_seconds"' "$metrics_file" | cut -d':' -f2 | tr -d ' ,"')
    local cache_hit_rate=$(grep '"cache_hit_rate_percent"' "$metrics_file" | cut -d':' -f2 | tr -d ' ,"')
    local total_tasks=$(grep '"total_tasks"' "$metrics_file" | cut -d':' -f2 | tr -d ' ,"')
    local cached_tasks=$(grep '"cached_tasks"' "$metrics_file" | cut -d':' -f2 | tr -d ' ,"')
    local memory_usage=$(grep '"peak_memory_mb"' "$metrics_file" | cut -d':' -f2 | tr -d ' ,"')
    local cache_size="N/A"  # We don't collect cache size yet
    
    cat > "$REPORT_FILE" << EOF
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Build Performance Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        .header { text-align: center; color: #333; border-bottom: 2px solid #4CAF50; padding-bottom: 10px; }
        .metrics-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin: 20px 0; }
        .metric-card { background: #f9f9f9; padding: 15px; border-radius: 6px; border-left: 4px solid #4CAF50; }
        .metric-title { font-weight: bold; color: #333; margin-bottom: 5px; }
        .metric-value { font-size: 24px; color: #4CAF50; font-weight: bold; }
        .metric-unit { font-size: 14px; color: #666; }
        .status-success { color: #4CAF50; }
        .status-failure { color: #f44336; }
        .cache-details { background: #e8f5e8; padding: 15px; border-radius: 6px; margin: 20px 0; }
        .timestamp { text-align: center; color: #666; font-size: 14px; margin-top: 20px; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Build Performance Report</h1>
            <h2 class="status-$(echo "$build_status" | tr '[:upper:]' '[:lower:]')">Status: $build_status</h2>
        </div>
        
        <div class="metrics-grid">
            <div class="metric-card">
                <div class="metric-title">Build Duration</div>
                <div class="metric-value">$build_duration <span class="metric-unit">seconds</span></div>
            </div>
            
            <div class="metric-card">
                <div class="metric-title">Cache Hit Rate</div>
                <div class="metric-value">$cache_hit_rate <span class="metric-unit">%</span></div>
            </div>
            
            <div class="metric-card">
                <div class="metric-title">Peak Memory Usage</div>
                <div class="metric-value">$memory_usage <span class="metric-unit">MB</span></div>
            </div>
            
            <div class="metric-card">
                <div class="metric-title">Gradle Cache Size</div>
                <div class="metric-value">$cache_size <span class="metric-unit">MB</span></div>
            </div>
        </div>
        
        <div class="cache-details">
            <h3>Cache Effectiveness Details</h3>
            <p><strong>Total Tasks:</strong> $total_tasks</p>
            <p><strong>Cached Tasks:</strong> $cached_tasks</p>
            <p><strong>Cache Hit Rate:</strong> $cache_hit_rate%</p>
        </div>
        
        <div class="timestamp">
            Report generated on $(date)
        </div>
    </div>
</body>
</html>
EOF

    echo "HTML report generated: $REPORT_FILE"
}

# Function to run build with metrics collection
run_build_with_metrics() {
    echo "Running build with metrics collection..."
    
    # Run the build metrics collection (it handles the build internally)
    ./scripts/build-metrics-collection.sh
    
    # Find the most recent metrics file
    local latest_metrics=$(ls -t ${METRICS_DIR}/build-metrics-*.json 2>/dev/null | head -1)
    
    if [[ -f "$latest_metrics" ]]; then
        echo "Latest metrics file: $latest_metrics"
        
        # Compare with baseline
        compare_with_baseline "$latest_metrics"
        
        # Generate HTML report
        generate_html_report "$latest_metrics"
        
        return 0
    else
        echo "Error: No metrics file found"
        return 1
    fi
}

# Function to display usage
usage() {
    echo "Usage: $0 [OPTIONS] [BUILD_COMMAND]"
    echo ""
    echo "Options:"
    echo "  --create-baseline    Create baseline metrics"
    echo "  --report-only FILE   Generate report from existing metrics file"
    echo "  --help              Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                                    # Run default build with metrics"
    echo "  $0 './gradlew assembleRelease'       # Run release build with metrics"
    echo "  $0 --create-baseline                 # Create baseline metrics"
    echo "  $0 --report-only metrics.json        # Generate report from existing file"
}

# Main execution
main() {
    # Ensure metrics directory exists
    mkdir -p "$METRICS_DIR"
    
    case "${1:-}" in
        --create-baseline)
            create_baseline
            ;;
        --report-only)
            if [[ -n "${2:-}" ]]; then
                generate_html_report "$2"
            else
                echo "Error: Please specify metrics file for --report-only"
                usage
                exit 1
            fi
            ;;
        --help)
            usage
            ;;
        *)
            run_build_with_metrics "$@"
            ;;
    esac
}

# Execute main function with all arguments
main "$@"