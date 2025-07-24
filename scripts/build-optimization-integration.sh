#!/bin/bash

# Build Optimization Integration Script
# Orchestrates all optimization documentation and monitoring components
#
# Prerequisites:
#   - jq (JSON processor) - for parsing build metrics
#   - bc (calculator) - for precise numerical calculations
#   - Standard Unix tools: awk, sed, grep, find
#
# Installation:
#   macOS: brew install jq bc
#   Ubuntu/Debian: sudo apt-get install jq bc
#   CentOS/RHEL: sudo yum install jq bc
#
# Note: The script will run with reduced functionality if optional tools are missing

set -euo pipefail

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
DOCS_DIR="$PROJECT_ROOT/docs"
METRICS_DIR="$PROJECT_ROOT/build-metrics"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Logging
LOG_FILE="$PROJECT_ROOT/build-optimization-integration.log"
log() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" | tee -a "$LOG_FILE"
}

# Display banner
display_banner() {
    echo -e "${BLUE}"
    echo "╔══════════════════════════════════════════════════════════════╗"
    echo "║                 CI Build Optimization Suite                  ║"
    echo "║              Documentation & Monitoring Integration          ║"
    echo "╚══════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

# Check prerequisites
check_prerequisites() {
    log "Checking prerequisites..."
    
    local missing_tools=()
    
    # Check for required tools
    if ! command -v bc &> /dev/null; then
        missing_tools+=("bc")
    fi
    
    # Check for optional tools
    local optional_tools=("jq" "curl")
    for tool in "${optional_tools[@]}"; do
        if ! command -v "$tool" &> /dev/null; then
            echo -e "${YELLOW}⚠️  Optional tool '$tool' not found - some features may be limited${NC}"
        fi
    done
    
    # Document missing required tools as prerequisites
    if [[ ${#missing_tools[@]} -gt 0 ]]; then
        echo -e "${YELLOW}⚠️ Missing required tools detected: ${missing_tools[*]}${NC}"
        echo -e "${BLUE}📋 Please install the following prerequisites:${NC}"
        echo ""
        
        for tool in "${missing_tools[@]}"; do
            case "$tool" in
                "jq")
                    echo -e "  ${BLUE}•${NC} jq (JSON processor)"
                    echo -e "    macOS: ${GREEN}brew install jq${NC}"
                    echo -e "    Ubuntu/Debian: ${GREEN}sudo apt-get install jq${NC}"
                    echo -e "    CentOS/RHEL: ${GREEN}sudo yum install jq${NC}"
                    ;;
                "bc")
                    echo -e "  ${BLUE}•${NC} bc (calculator)"
                    echo -e "    macOS: ${GREEN}brew install bc${NC}"
                    echo -e "    Ubuntu/Debian: ${GREEN}sudo apt-get install bc${NC}"
                    echo -e "    CentOS/RHEL: ${GREEN}sudo yum install bc${NC}"
                    ;;
                *)
                    echo -e "  ${BLUE}•${NC} $tool"
                    echo -e "    Please install using your system's package manager"
                    ;;
            esac
            echo ""
        done
        
        echo -e "${YELLOW}💡 Note: The script will continue with reduced functionality.${NC}"
        echo -e "${YELLOW}   Some calculations may be less precise without these tools.${NC}"
        echo ""
        
        # Allow user to continue or exit
        if [[ "${CI:-false}" == "true" ]]; then
            echo -e "${YELLOW}⚠️ Running in CI environment - continuing with available tools${NC}"
        else
            read -p "Continue anyway? (y/N): " -n 1 -r
            echo
            if [[ ! $REPLY =~ ^[Yy]$ ]]; then
                echo -e "${RED}❌ Exiting. Please install required tools and try again.${NC}"
                exit 1
            fi
        fi
    fi
    
    log "Prerequisites check completed"
}

# Initialize project structure
initialize_structure() {
    log "Initializing project structure..."
    
    # Create necessary directories
    mkdir -p "$DOCS_DIR"
    mkdir -p "$METRICS_DIR"
    mkdir -p "$PROJECT_ROOT/scripts"
    
    # Ensure scripts are executable
    find "$PROJECT_ROOT/scripts" -name "*.sh" -exec chmod +x {} \;
    
    log "Project structure initialized"
}

# Generate comprehensive documentation
generate_documentation() {
    log "Generating comprehensive documentation..."
    
    echo -e "${BLUE}📚 Generating optimization documentation...${NC}"
    
    # The documentation is already created in the optimization guide
    if [[ -f "$DOCS_DIR/build-optimization-guide.md" ]]; then
        echo -e "${GREEN}✅ Build optimization guide available${NC}"
    else
        echo -e "${RED}❌ Build optimization guide missing${NC}"
        return 1
    fi
    
    log "Documentation generation completed"
}

# Run optimization analysis
run_optimization_analysis() {
    log "Running optimization analysis..."
    
    echo -e "${BLUE}🔍 Running automated optimization analysis...${NC}"
    
    if [[ -x "$SCRIPT_DIR/optimization-recommendations.sh" ]]; then
        "$SCRIPT_DIR/optimization-recommendations.sh"
        echo -e "${GREEN}✅ Optimization analysis completed${NC}"
    else
        echo -e "${RED}❌ Optimization recommendations script not found or not executable${NC}"
        return 1
    fi
    
    log "Optimization analysis completed"
}

# Generate performance dashboard
generate_dashboard() {
    log "Generating performance dashboard..."
    
    echo -e "${BLUE}📊 Generating performance dashboard...${NC}"
    
    if [[ -x "$SCRIPT_DIR/build-dashboard.sh" ]]; then
        "$SCRIPT_DIR/build-dashboard.sh"
        echo -e "${GREEN}✅ Performance dashboard generated${NC}"
    else
        echo -e "${RED}❌ Build dashboard script not found or not executable${NC}"
        return 1
    fi
    
    log "Performance dashboard generation completed"
}

# Create integration summary
create_integration_summary() {
    log "Creating integration summary..."
    
    local summary_file="$DOCS_DIR/build-optimization-summary.md"
    
    cat > "$summary_file" << 'EOF'
# Build Optimization Integration Summary

## Overview

This document provides a comprehensive overview of the CI build optimization implementation, including documentation, monitoring, and automated recommendations.

## Components Implemented

### 1. Documentation Suite
- **Build Optimization Guide** (`docs/build-optimization-guide.md`)
  - Comprehensive optimization strategies
  - Configuration options and best practices
  - Troubleshooting guide for common issues
  - Performance tuning checklist

### 2. Monitoring Dashboard
- **Performance Dashboard** (`build-metrics/dashboard.html`)
  - Real-time build performance metrics
  - Visual representation of optimization effectiveness
  - Interactive charts and graphs
  - Performance trend analysis

### 3. Automated Analysis
- **Optimization Recommendations** (`docs/optimization-recommendations.md`)
  - Automated performance analysis
  - Actionable optimization suggestions
  - Priority-based implementation plan
  - Configuration templates

## Key Features

### Documentation Features
- ✅ Multi-layer caching strategies
- ✅ Parallel execution optimization
- ✅ Selective build execution
- ✅ Resource utilization guidelines
- ✅ Troubleshooting procedures
- ✅ Performance monitoring setup

### Dashboard Features
- ✅ Real-time metrics display
- ✅ Performance trend visualization
- ✅ Optimization status indicators
- ✅ Automated report generation
- ✅ Interactive HTML interface
- ✅ Mobile-responsive design

### Analysis Features
- ✅ Automated cache performance analysis
- ✅ Build time trend detection
- ✅ Resource utilization assessment
- ✅ Priority-based recommendations
- ✅ Configuration template generation
- ✅ Implementation roadmap

## Usage Instructions

### Viewing Documentation
```bash
# Open the comprehensive optimization guide
open docs/build-optimization-guide.md
```

### Generating Dashboard
```bash
# Generate performance dashboard
./scripts/build-dashboard.sh

# View dashboard in browser
open build-metrics/dashboard.html
```

### Running Analysis
```bash
# Generate optimization recommendations
./scripts/optimization-recommendations.sh

# View recommendations
open docs/optimization-recommendations.md
```

### Integration Script
```bash
# Run complete optimization suite
./scripts/build-optimization-integration.sh
```

## Performance Targets

### Build Time Improvements
- **Incremental builds**: 50-70% reduction
- **Dependency-only changes**: 80-90% reduction
- **Test-only changes**: 60-80% reduction
- **Documentation changes**: Near-instant (skip)

### Cache Effectiveness
- **Cache hit rate**: >80% for dependencies
- **Build cache hit rate**: >60% for incremental changes
- **Cache size**: <2GB total
- **Cleanup frequency**: Weekly automated

### Resource Optimization
- **CPU utilization**: 80-90% during parallel execution
- **Memory usage**: <4GB peak
- **Network usage**: 90% reduction in downloads
- **Storage efficiency**: Optimal cache-to-benefit ratio

## Maintenance Procedures

### Daily Monitoring
- Check dashboard for performance trends
- Review build failure rates
- Monitor cache effectiveness

### Weekly Analysis
- Run optimization recommendations script
- Review and implement suggested improvements
- Update performance baselines

### Monthly Review
- Comprehensive performance analysis
- Documentation updates
- Strategy refinement

## Integration with CI/CD

### GitHub Actions Integration
The optimization suite integrates seamlessly with existing GitHub Actions workflows:

1. **Automated Metrics Collection**: Build metrics are collected automatically
2. **Performance Monitoring**: Dashboard updates with each build
3. **Optimization Alerts**: Automated notifications for performance degradation
4. **Recommendation Engine**: Regular analysis and improvement suggestions

### Local Development
Developers can use the optimization tools locally:

1. **Performance Analysis**: Run optimization scripts before pushing changes
2. **Cache Management**: Monitor and optimize local build cache
3. **Configuration Tuning**: Use templates for optimal local settings

## Success Metrics

### Quantitative Metrics
- Build time reduction percentage
- Cache hit rate improvement
- Resource utilization efficiency
- Developer productivity increase

### Qualitative Metrics
- Developer satisfaction with build times
- Reduced build-related issues
- Improved development workflow
- Enhanced CI/CD reliability

## Next Steps

### Short-term (1-2 weeks)
- Monitor initial performance improvements
- Fine-tune optimization parameters
- Address any integration issues

### Medium-term (1-2 months)
- Implement advanced caching strategies
- Set up automated performance regression detection
- Expand monitoring capabilities

### Long-term (3+ months)
- Continuous optimization refinement
- Integration with additional tools
- Performance benchmarking and comparison

EOF

    echo -e "${GREEN}✅ Integration summary created: $summary_file${NC}"
    log "Integration summary created"
}

# Display results summary
display_results() {
    echo -e "\n${PURPLE}📋 Build Optimization Integration Results${NC}"
    echo -e "${BLUE}════════════════════════════════════════${NC}"
    
    # Check documentation
    if [[ -f "$DOCS_DIR/build-optimization-guide.md" ]]; then
        echo -e "${GREEN}✅ Documentation Suite${NC}"
        echo -e "   📖 Build Optimization Guide: $DOCS_DIR/build-optimization-guide.md"
    else
        echo -e "${RED}❌ Documentation Suite${NC}"
    fi
    
    # Check dashboard
    if [[ -f "$METRICS_DIR/dashboard.html" ]]; then
        echo -e "${GREEN}✅ Performance Dashboard${NC}"
        echo -e "   📊 Dashboard: $METRICS_DIR/dashboard.html"
        echo -e "   📋 Report: $METRICS_DIR/performance-report.md"
    else
        echo -e "${RED}❌ Performance Dashboard${NC}"
    fi
    
    # Check recommendations
    if [[ -f "$DOCS_DIR/optimization-recommendations.md" ]]; then
        echo -e "${GREEN}✅ Optimization Analysis${NC}"
        echo -e "   🔍 Recommendations: $DOCS_DIR/optimization-recommendations.md"
    else
        echo -e "${RED}❌ Optimization Analysis${NC}"
    fi
    
    # Check integration summary
    if [[ -f "$DOCS_DIR/build-optimization-summary.md" ]]; then
        echo -e "${GREEN}✅ Integration Summary${NC}"
        echo -e "   📄 Summary: $DOCS_DIR/build-optimization-summary.md"
    else
        echo -e "${RED}❌ Integration Summary${NC}"
    fi
    
    echo -e "\n${YELLOW}🚀 Quick Start Commands:${NC}"
    echo -e "   View Documentation: ${BLUE}open docs/build-optimization-guide.md${NC}"
    echo -e "   View Dashboard: ${BLUE}open build-metrics/dashboard.html${NC}"
    echo -e "   View Recommendations: ${BLUE}open docs/optimization-recommendations.md${NC}"
    echo -e "   View Summary: ${BLUE}open docs/build-optimization-summary.md${NC}"
}

# Main execution function
main() {
    display_banner
    
    log "Starting build optimization integration"
    
    check_prerequisites
    initialize_structure
    generate_documentation
    run_optimization_analysis
    generate_dashboard
    create_integration_summary
    
    display_results
    
    echo -e "\n${GREEN}🎉 Build Optimization Integration Completed Successfully!${NC}"
    log "Build optimization integration completed successfully"
}

# Handle script arguments
case "${1:-}" in
    --help|-h)
        echo "Build Optimization Integration Script"
        echo ""
        echo "Usage: $0 [options]"
        echo ""
        echo "Options:"
        echo "  --help, -h     Show this help message"
        echo "  --docs-only    Generate documentation only"
        echo "  --dashboard    Generate dashboard only"
        echo "  --analysis     Run analysis only"
        echo ""
        exit 0
        ;;
    --docs-only)
        generate_documentation
        ;;
    --dashboard)
        generate_dashboard
        ;;
    --analysis)
        run_optimization_analysis
        ;;
    *)
        main "$@"
        ;;
esac