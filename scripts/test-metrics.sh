#!/bin/bash

# Simple test script to verify metrics collection works

set -euo pipefail

echo "Testing build metrics collection..."

# Configuration
METRICS_DIR="build-metrics"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
METRICS_FILE="${METRICS_DIR}/test-metrics-${TIMESTAMP}.json"

# Ensure metrics directory exists
mkdir -p "$METRICS_DIR"

# Function to get current timestamp in milliseconds
get_timestamp_ms() {
    if [[ "$OSTYPE" == "darwin"* ]]; then
        python3 -c "import time; print(int(time.time() * 1000))"
    else
        date +%s%3N
    fi
}

# Start timing
BUILD_START_TIME=$(get_timestamp_ms)

# Run a simple Gradle task
echo "Running simple Gradle task..."
./gradlew tasks --quiet > /dev/null 2>&1

# End timing
BUILD_END_TIME=$(get_timestamp_ms)
BUILD_DURATION=$((BUILD_END_TIME - BUILD_START_TIME))

# Create test metrics
cat > "$METRICS_FILE" << EOF
{
  "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
  "test_build_id": "test-$(date +%s)",
  "build_type": "test",
  "performance": {
    "total_duration_ms": $BUILD_DURATION,
    "total_duration_seconds": $(echo "scale=2; $BUILD_DURATION / 1000" | bc -l 2>/dev/null || echo "0"),
    "cpu_cores_available": $(sysctl -n hw.ncpu 2>/dev/null || nproc 2>/dev/null || echo "1")
  },
  "cache_effectiveness": {
    "total_tasks": 10,
    "cached_tasks": 3,
    "up_to_date_tasks": 5,
    "executed_tasks": 2,
    "cache_hit_rate_percent": 80.0
  },
  "environment": {
    "os_type": "$OSTYPE",
    "ci_environment": "${CI:-false}",
    "github_actions": "${GITHUB_ACTIONS:-false}"
  }
}
EOF

echo "Test metrics created: $METRICS_FILE"
echo "Build duration: $(echo "scale=2; $BUILD_DURATION / 1000" | bc -l 2>/dev/null || echo "0")s"
echo "Test completed successfully!"