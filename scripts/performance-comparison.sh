#!/bin/bash

# Performance Comparison Script
# Compares build performance across different configurations and time periods

set -euo pipefail

# Configuration
METRICS_DIR="build-metrics"
COMPARISON_REPORT="build-metrics/performance-comparison-$(date +%Y%m%d_%H%M%S).html"

# Function to find metrics files by pattern
find_metrics_files() {
    local pattern="$1"
    find "$METRICS_DIR" -name "build-metrics-*.json" -type f | grep -E "$pattern" | sort
}

# Function to extract metric value from JSON file
extract_metric() {
    local file="$1"
    local metric="$2"
    
    if [[ -f "$file" ]]; then
        grep "\"$metric\"" "$file" | head -1 | cut -d':' -f2 | tr -d ' ,"' || echo "0"
    else
        echo "0"
    fi
}

# Function to calculate statistics
calculate_stats() {
    local values=("$@")
    local count=${#values[@]}
    
    if [[ $count -eq 0 ]]; then
        echo "0 0 0 0"
        return
    fi
    
    # Calculate sum
    local sum=0
    for value in "${values[@]}"; do
        if command -v bc &> /dev/null; then
            sum=$(echo "$sum + $value" | bc -l)
        else
            sum=$((sum + ${value%.*})) # Simple integer addition fallback
        fi
    done
    
    # Calculate average
    local avg=0
    if command -v bc &> /dev/null; then
        avg=$(echo "scale=2; $sum / $count" | bc -l)
    else
        avg=$((sum / count))
    fi
    
    # Find min and max
    local min=${values[0]}
    local max=${values[0]}
    
    for value in "${values[@]}"; do
        if command -v bc &> /dev/null; then
            if (( $(echo "$value < $min" | bc -l) )); then
                min=$value
            fi
            if (( $(echo "$value > $max" | bc -l) )); then
                max=$value
            fi
        else
            if [[ ${value%.*} -lt ${min%.*} ]]; then
                min=$value
            fi
            if [[ ${value%.*} -gt ${max%.*} ]]; then
                max=$value
            fi
        fi
    done
    
    echo "$avg $min $max $count"
}

# Function to compare two sets of metrics
compare_metrics() {
    local baseline_files=("$@")
    local current_files=()
    
    # Split arguments into baseline and current files
    local split_index=0
    for ((i=0; i<${#baseline_files[@]}; i++)); do
        if [[ "${baseline_files[i]}" == "--" ]]; then
            split_index=$((i+1))
            break
        fi
    done
    
    if [[ $split_index -gt 0 ]]; then
        current_files=("${baseline_files[@]:$split_index}")
        baseline_files=("${baseline_files[@]:0:$((split_index-1))}")
    fi
    
    echo "Comparing ${#baseline_files[@]} baseline builds with ${#current_files[@]} current builds"
    
    # Extract metrics from baseline files
    local baseline_durations=()
    local baseline_cache_rates=()
    local baseline_memory=()
    
    for file in "${baseline_files[@]}"; do
        if [[ -f "$file" ]]; then
            baseline_durations+=($(extract_metric "$file" "total_duration_seconds"))
            baseline_cache_rates+=($(extract_metric "$file" "cache_hit_rate_percent"))
            baseline_memory+=($(extract_metric "$file" "peak_memory_mb"))
        fi
    done
    
    # Extract metrics from current files
    local current_durations=()
    local current_cache_rates=()
    local current_memory=()
    
    for file in "${current_files[@]}"; do
        if [[ -f "$file" ]]; then
            current_durations+=($(extract_metric "$file" "total_duration_seconds"))
            current_cache_rates+=($(extract_metric "$file" "cache_hit_rate_percent"))
            current_memory+=($(extract_metric "$file" "peak_memory_mb"))
        fi
    done
    
    # Calculate statistics
    local baseline_duration_stats=($(calculate_stats "${baseline_durations[@]}"))
    local current_duration_stats=($(calculate_stats "${current_durations[@]}"))
    
    local baseline_cache_stats=($(calculate_stats "${baseline_cache_rates[@]}"))
    local current_cache_stats=($(calculate_stats "${current_cache_rates[@]}"))
    
    local baseline_memory_stats=($(calculate_stats "${baseline_memory[@]}"))
    local current_memory_stats=($(calculate_stats "${current_memory[@]}"))
    
    # Display comparison results
    echo ""
    echo "=== PERFORMANCE COMPARISON RESULTS ==="
    echo ""
    echo "Build Duration (seconds):"
    echo "  Baseline - Avg: ${baseline_duration_stats[0]}, Min: ${baseline_duration_stats[1]}, Max: ${baseline_duration_stats[2]}"
    echo "  Current  - Avg: ${current_duration_stats[0]}, Min: ${current_duration_stats[1]}, Max: ${current_duration_stats[2]}"
    
    echo ""
    echo "Cache Hit Rate (%):"
    echo "  Baseline - Avg: ${baseline_cache_stats[0]}, Min: ${baseline_cache_stats[1]}, Max: ${baseline_cache_stats[2]}"
    echo "  Current  - Avg: ${current_cache_stats[0]}, Min: ${current_cache_stats[1]}, Max: ${current_cache_stats[2]}"
    
    echo ""
    echo "Memory Usage (MB):"
    echo "  Baseline - Avg: ${baseline_memory_stats[0]}, Min: ${baseline_memory_stats[1]}, Max: ${baseline_memory_stats[2]}"
    echo "  Current  - Avg: ${current_memory_stats[0]}, Min: ${current_memory_stats[1]}, Max: ${current_memory_stats[2]}"
    echo ""
    echo "======================================"
    
    # Generate detailed comparison report
    generate_comparison_report "${baseline_files[@]}" "--" "${current_files[@]}"
}

# Function to generate detailed HTML comparison report
generate_comparison_report() {
    local all_files=("$@")
    local baseline_files=()
    local current_files=()
    
    # Split files into baseline and current
    local split_index=0
    for ((i=0; i<${#all_files[@]}; i++)); do
        if [[ "${all_files[i]}" == "--" ]]; then
            split_index=$((i+1))
            break
        fi
    done
    
    if [[ $split_index -gt 0 ]]; then
        baseline_files=("${all_files[@]:0:$((split_index-1))}")
        current_files=("${all_files[@]:$split_index}")
    else
        baseline_files=("${all_files[@]}")
    fi
    
    echo "Generating detailed comparison report: $COMPARISON_REPORT"
    
    cat > "$COMPARISON_REPORT" << 'EOF'
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Build Performance Comparison</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
        .container { max-width: 1400px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        .header { text-align: center; color: #333; border-bottom: 2px solid #2196F3; padding-bottom: 10px; }
        .comparison-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin: 20px 0; }
        .metric-section { background: #f9f9f9; padding: 15px; border-radius: 6px; }
        .metric-title { font-weight: bold; color: #333; margin-bottom: 10px; font-size: 18px; }
        .stats-table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        .stats-table th, .stats-table td { padding: 8px; text-align: left; border-bottom: 1px solid #ddd; }
        .stats-table th { background-color: #f2f2f2; font-weight: bold; }
        .improvement { color: #4CAF50; font-weight: bold; }
        .regression { color: #f44336; font-weight: bold; }
        .neutral { color: #666; }
        .chart-placeholder { height: 200px; background: #e8e8e8; border-radius: 4px; display: flex; align-items: center; justify-content: center; color: #666; margin: 10px 0; }
        .summary-cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; margin: 20px 0; }
        .summary-card { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px; border-radius: 6px; text-align: center; }
        .summary-value { font-size: 24px; font-weight: bold; margin-bottom: 5px; }
        .summary-label { font-size: 14px; opacity: 0.9; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Build Performance Comparison Report</h1>
            <p>Comprehensive analysis of build performance metrics</p>
        </div>
        
        <div class="summary-cards">
            <div class="summary-card">
                <div class="summary-value" id="avg-duration-improvement">--%</div>
                <div class="summary-label">Avg Duration Improvement</div>
            </div>
            <div class="summary-card">
                <div class="summary-value" id="cache-rate-improvement">--%</div>
                <div class="summary-label">Cache Rate Improvement</div>
            </div>
            <div class="summary-card">
                <div class="summary-value" id="memory-improvement">--%</div>
                <div class="summary-label">Memory Usage Improvement</div>
            </div>
        </div>
        
        <div class="comparison-grid">
            <div class="metric-section">
                <div class="metric-title">Build Duration Analysis</div>
                <table class="stats-table">
                    <tr><th>Metric</th><th>Baseline</th><th>Current</th><th>Change</th></tr>
                    <tr><td>Average</td><td id="baseline-duration-avg">--</td><td id="current-duration-avg">--</td><td id="duration-change">--</td></tr>
                    <tr><td>Minimum</td><td id="baseline-duration-min">--</td><td id="current-duration-min">--</td><td id="duration-min-change">--</td></tr>
                    <tr><td>Maximum</td><td id="baseline-duration-max">--</td><td id="current-duration-max">--</td><td id="duration-max-change">--</td></tr>
                </table>
                <div class="chart-placeholder">Duration Trend Chart</div>
            </div>
            
            <div class="metric-section">
                <div class="metric-title">Cache Hit Rate Analysis</div>
                <table class="stats-table">
                    <tr><th>Metric</th><th>Baseline</th><th>Current</th><th>Change</th></tr>
                    <tr><td>Average</td><td id="baseline-cache-avg">--</td><td id="current-cache-avg">--</td><td id="cache-change">--</td></tr>
                    <tr><td>Minimum</td><td id="baseline-cache-min">--</td><td id="current-cache-min">--</td><td id="cache-min-change">--</td></tr>
                    <tr><td>Maximum</td><td id="baseline-cache-max">--</td><td id="current-cache-max">--</td><td id="cache-max-change">--</td></tr>
                </table>
                <div class="chart-placeholder">Cache Rate Trend Chart</div>
            </div>
        </div>
        
        <div class="metric-section">
            <div class="metric-title">Memory Usage Analysis</div>
            <table class="stats-table">
                <tr><th>Metric</th><th>Baseline (MB)</th><th>Current (MB)</th><th>Change</th></tr>
                <tr><td>Average</td><td id="baseline-memory-avg">--</td><td id="current-memory-avg">--</td><td id="memory-change">--</td></tr>
                <tr><td>Minimum</td><td id="baseline-memory-min">--</td><td id="current-memory-min">--</td><td id="memory-min-change">--</td></tr>
                <tr><td>Maximum</td><td id="baseline-memory-max">--</td><td id="current-memory-max">--</td><td id="memory-max-change">--</td></tr>
            </table>
        </div>
        
        <div style="text-align: center; color: #666; font-size: 14px; margin-top: 30px;">
            Report generated on $(date)
        </div>
    </div>
</body>
</html>
EOF

    echo "Comparison report generated: $COMPARISON_REPORT"
}

# Function to analyze trends over time
analyze_trends() {
    local days="${1:-7}"
    
    echo "Analyzing build performance trends over the last $days days..."
    
    # Find metrics files from the last N days
    local cutoff_date=$(date -d "$days days ago" +%Y%m%d 2>/dev/null || date -v-${days}d +%Y%m%d 2>/dev/null || echo "20240101")
    local recent_files=()
    
    while IFS= read -r -d '' file; do
        local file_date=$(basename "$file" | grep -o '[0-9]\{8\}' | head -1)
        if [[ "$file_date" -ge "$cutoff_date" ]]; then
            recent_files+=("$file")
        fi
    done < <(find "$METRICS_DIR" -name "build-metrics-*.json" -type f -print0 | sort -z)
    
    if [[ ${#recent_files[@]} -eq 0 ]]; then
        echo "No metrics files found for the last $days days"
        return 1
    fi
    
    echo "Found ${#recent_files[@]} builds in the last $days days"
    
    # Analyze trends
    local durations=()
    local cache_rates=()
    local dates=()
    
    for file in "${recent_files[@]}"; do
        durations+=($(extract_metric "$file" "total_duration_seconds"))
        cache_rates+=($(extract_metric "$file" "cache_hit_rate_percent"))
        dates+=($(basename "$file" | grep -o '[0-9]\{8\}_[0-9]\{6\}' | head -1))
    done
    
    # Display trend analysis
    echo ""
    echo "=== TREND ANALYSIS (Last $days days) ==="
    echo "Build Count: ${#recent_files[@]}"
    echo "Duration Range: $(calculate_stats "${durations[@]}" | cut -d' ' -f2-3) seconds"
    echo "Cache Rate Range: $(calculate_stats "${cache_rates[@]}" | cut -d' ' -f2-3)%"
    echo "========================================"
}

# Function to display usage
usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --compare PATTERN1 PATTERN2    Compare builds matching two patterns"
    echo "  --trends [DAYS]                Analyze trends over last N days (default: 7)"
    echo "  --baseline-vs-recent           Compare baseline with recent builds"
    echo "  --help                         Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 --trends 14                           # Analyze last 14 days"
    echo "  $0 --compare '202401' '202402'           # Compare January vs February 2024"
    echo "  $0 --baseline-vs-recent                  # Compare baseline with recent builds"
}

# Main execution
main() {
    # Ensure metrics directory exists
    mkdir -p "$METRICS_DIR"
    
    case "${1:-}" in
        --compare)
            if [[ -n "${2:-}" && -n "${3:-}" ]]; then
                local baseline_files=($(find_metrics_files "$2"))
                local current_files=($(find_metrics_files "$3"))
                
                if [[ ${#baseline_files[@]} -eq 0 ]]; then
                    echo "No baseline files found matching pattern: $2"
                    exit 1
                fi
                
                if [[ ${#current_files[@]} -eq 0 ]]; then
                    echo "No current files found matching pattern: $3"
                    exit 1
                fi
                
                compare_metrics "${baseline_files[@]}" "--" "${current_files[@]}"
            else
                echo "Error: --compare requires two patterns"
                usage
                exit 1
            fi
            ;;
        --trends)
            analyze_trends "${2:-7}"
            ;;
        --baseline-vs-recent)
            if [[ -f "$METRICS_DIR/baseline-metrics.json" ]]; then
                local recent_files=($(ls -t ${METRICS_DIR}/build-metrics-*.json 2>/dev/null | head -5))
                if [[ ${#recent_files[@]} -gt 0 ]]; then
                    compare_metrics "$METRICS_DIR/baseline-metrics.json" "--" "${recent_files[@]}"
                else
                    echo "No recent metrics files found"
                    exit 1
                fi
            else
                echo "No baseline metrics found. Run with --create-baseline first."
                exit 1
            fi
            ;;
        --help)
            usage
            ;;
        *)
            echo "Error: Unknown option or missing arguments"
            usage
            exit 1
            ;;
    esac
}

# Execute main function with all arguments
main "$@"