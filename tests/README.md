# CI Build Optimization Test Suite

This comprehensive test suite validates all aspects of the CI build optimization system, ensuring that performance improvements work correctly and don't introduce regressions.

## Test Structure

The test suite is organized into four main categories:

### 1. Unit Tests (`tests/unit/`)
Tests individual components and functions in isolation.

- **`test-change-detection.sh`**: Tests the change detection logic used for selective build execution
  - Source code change detection
  - Test-only change detection
  - Build script change detection
  - Documentation-only change detection
  - Mixed change prioritization
  - Edge cases and error handling

### 2. Integration Tests (`tests/integration/`)
Tests the interaction between different optimization components.

- **`test-cache-strategies.sh`**: Tests the multi-layer caching system
  - Cache size monitoring
  - Cache corruption detection and recovery
  - Cache cleanup functionality
  - Cache effectiveness analysis
  - Cache retention policies
  - Multi-layer cache coordination
  - Performance under load

### 3. Performance Tests (`tests/performance/`)
Tests build performance improvements and detects regressions.

- **`test-build-performance.sh`**: Tests build time optimizations
  - Clean build performance
  - Incremental build performance
  - Cache hit build performance
  - Test-only build performance
  - Parallel execution efficiency
  - Cache size efficiency
  - Build time consistency
  - Memory usage efficiency
  - Regression detection

### 4. End-to-End Tests (`tests/e2e/`)
Tests the complete optimization workflow from start to finish.

- **`test-optimization-workflow.sh`**: Tests the entire optimization system
  - Workflow initialization
  - Change detection workflow
  - Cache management lifecycle
  - Build execution with optimizations
  - Performance monitoring integration
  - Error handling and recovery
  - Complete workflow integration
  - CI/CD workflow simulation
  - Performance regression detection
  - Cleanup and reporting

## Running Tests

### Prerequisites

Ensure you have the following tools installed:
- `bash` (version 4.0+)
- `git`
- `bc` (basic calculator)
- `timeout` or `gtimeout` (optional, for test timeouts)
- `jq` (optional, for JSON processing)

### Running All Tests

```bash
# Run the complete test suite
./tests/run-all-tests.sh
```

### Running Specific Test Categories

```bash
# Run only unit tests
./tests/run-all-tests.sh --unit-only

# Run only integration tests
./tests/run-all-tests.sh --integration-only

# Run only performance tests
./tests/run-all-tests.sh --performance-only

# Run only end-to-end tests
./tests/run-all-tests.sh --e2e-only
```

### Skipping Specific Test Categories

```bash
# Skip performance tests (useful for quick validation)
./tests/run-all-tests.sh --skip-performance

# Skip end-to-end tests (useful for faster feedback)
./tests/run-all-tests.sh --skip-e2e
```

### Running Individual Test Scripts

```bash
# Run a specific test script directly
./tests/unit/test-change-detection.sh
./tests/integration/test-cache-strategies.sh
./tests/performance/test-build-performance.sh
./tests/e2e/test-optimization-workflow.sh
```

## Test Configuration

### Performance Thresholds

The performance tests use the following thresholds:

- **Clean Build**: 300 seconds (5 minutes)
- **Incremental Build**: 60 seconds (1 minute)
- **Cache Hit Build**: 30 seconds
- **Test-Only Build**: 45 seconds
- **Cache Hit Rate**: Minimum 60%
- **Cache Size**: Maximum 2GB
- **Parallel Efficiency**: Minimum 70%

### Environment Variables

Tests can be configured using environment variables:

```bash
# Cache management settings
export CACHE_MAX_SIZE_GB=2
export CACHE_MAX_AGE_DAYS=7
export CACHE_MIN_HIT_RATE=0.6

# Build timeout settings
export BUILD_TIMEOUT=1800
export MAX_RETRY_ATTEMPTS=3

# Test-specific settings
export CLEAN_BUILD_THRESHOLD=300
export INCREMENTAL_BUILD_THRESHOLD=60
```

## Test Reports

### Comprehensive Test Report

After running tests, a comprehensive report is generated at:
```
build-metrics/comprehensive-test-report.json
```

This report includes:
- Overall test summary
- Individual test suite results
- Performance metrics
- Environment information
- Optimization features verified

### Individual Test Reports

Each test category generates its own detailed reports:

- **Unit Tests**: Basic pass/fail results with detailed logging
- **Integration Tests**: Cache effectiveness metrics and system state
- **Performance Tests**: Build time measurements and regression analysis
- **End-to-End Tests**: Complete workflow validation and feature verification

## Continuous Integration Integration

### GitHub Actions Integration

Add the test suite to your GitHub Actions workflow:

```yaml
- name: Run Optimization Tests
  run: |
    chmod +x tests/run-all-tests.sh
    ./tests/run-all-tests.sh --skip-e2e
```

### Test Results in CI

The test suite provides CI-friendly output:
- Exit codes (0 for success, 1 for failure)
- GitHub Actions annotations for failures
- JSON reports for automated processing
- Performance metrics for trend analysis

## Troubleshooting

### Common Issues

1. **Permission Denied Errors**
   ```bash
   chmod +x tests/run-all-tests.sh
   find tests -name "*.sh" -exec chmod +x {} \;
   ```

2. **Missing Dependencies**
   ```bash
   # On macOS
   brew install bc coreutils
   
   # On Ubuntu/Debian
   sudo apt-get install bc timeout
   ```

3. **Test Timeouts**
   - Increase timeout values for slower systems
   - Check system resources (CPU, memory, disk space)
   - Verify network connectivity for dependency downloads

4. **Cache Permission Issues**
   ```bash
   chmod -R u+rw ~/.gradle/caches
   rm -rf ~/.gradle/daemon
   ```

### Debug Mode

Enable verbose logging for troubleshooting:

```bash
# Run with debug output
bash -x ./tests/run-all-tests.sh --unit-only
```

### Test Isolation

Each test runs in isolation with:
- Temporary directories for test artifacts
- Separate cache directories
- Clean git repositories
- Independent environment variables

## Performance Baselines

### Establishing Baselines

On first run, performance tests establish baselines:
```
build-metrics/performance-baseline.json
```

### Updating Baselines

To update performance baselines after optimization improvements:
```bash
rm build-metrics/performance-baseline.json
./tests/performance/test-build-performance.sh
```

## Test Development

### Adding New Tests

1. **Unit Tests**: Add to `tests/unit/` directory
2. **Integration Tests**: Add to `tests/integration/` directory
3. **Performance Tests**: Add to `tests/performance/` directory
4. **End-to-End Tests**: Add to `tests/e2e/` directory

### Test Script Template

```bash
#!/bin/bash
set -euo pipefail

# Test configuration
TESTS_RUN=0
TESTS_PASSED=0
TESTS_FAILED=0

# Logging functions
log_pass() {
    echo -e "\033[0;32m[PASS]\033[0m $1"
    TESTS_PASSED=$((TESTS_PASSED + 1))
}

log_fail() {
    echo -e "\033[0;31m[FAIL]\033[0m $1"
    TESTS_FAILED=$((TESTS_FAILED + 1))
}

# Test function
test_example() {
    TESTS_RUN=$((TESTS_RUN + 1))
    
    if [[ condition ]]; then
        log_pass "Test description"
    else
        log_fail "Test description"
    fi
}

# Main execution
run_all_tests() {
    test_example
    
    if [[ $TESTS_FAILED -eq 0 ]]; then
        echo "All tests passed!"
        return 0
    else
        echo "Some tests failed!"
        return 1
    fi
}

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    run_all_tests
fi
```

## Success Criteria

The test suite validates the following optimization requirements:

### Build Time Improvements
- ✅ 50-70% reduction in incremental builds
- ✅ 80-90% reduction for dependency-only changes
- ✅ 60-80% reduction for test-only changes
- ✅ Near-instant builds for documentation changes

### Cache Effectiveness
- ✅ >80% cache hit rate for dependencies
- ✅ >60% build cache hit rate for incremental changes
- ✅ <2GB total cache size
- ✅ Weekly automated cache cleanup

### System Reliability
- ✅ Robust error handling and recovery
- ✅ Cache corruption detection and repair
- ✅ Build failure retry with exponential backoff
- ✅ Fallback to clean builds when optimizations fail

### Performance Monitoring
- ✅ Comprehensive build metrics collection
- ✅ Performance trend analysis
- ✅ Automated optimization recommendations
- ✅ Regression detection and alerting

## Contributing

When contributing to the optimization system:

1. **Add Tests**: Ensure new features have corresponding tests
2. **Update Thresholds**: Adjust performance thresholds if improvements are made
3. **Document Changes**: Update test documentation for new test cases
4. **Validate**: Run the complete test suite before submitting changes

```bash
# Validate your changes
./tests/run-all-tests.sh
```

The comprehensive test suite ensures that the CI build optimization system delivers reliable, measurable performance improvements while maintaining build correctness and system stability.