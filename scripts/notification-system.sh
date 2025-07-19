#!/bin/bash

# Notification System Script
# Handles error reporting and notifications for build failures and recoveries

set -euo pipefail

# Configuration
NOTIFICATION_ENABLED=${NOTIFICATION_ENABLED:-true}
SLACK_WEBHOOK_URL=${SLACK_WEBHOOK_URL:-""}
TEAMS_WEBHOOK_URL=${TEAMS_WEBHOOK_URL:-""}
EMAIL_ENABLED=${EMAIL_ENABLED:-false}
EMAIL_TO=${EMAIL_TO:-""}
EMAIL_FROM=${EMAIL_FROM:-"ci-system@example.com"}
GITHUB_ISSUE_ENABLED=${GITHUB_ISSUE_ENABLED:-false}
NOTIFICATION_LOG_FILE="notifications.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1" | tee -a "$NOTIFICATION_LOG_FILE"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1" | tee -a "$NOTIFICATION_LOG_FILE"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a "$NOTIFICATION_LOG_FILE"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1" | tee -a "$NOTIFICATION_LOG_FILE"
}

# Initialize notification system
initialize_notifications() {
    log_info "Initializing notification system..."
    
    # Create notification log
    echo "=== Notification System Started at $(date) ===" > "$NOTIFICATION_LOG_FILE"
    
    # Validate configuration
    local config_valid=true
    
    if [[ "$NOTIFICATION_ENABLED" != "true" ]]; then
        log_info "Notifications disabled by configuration"
        return 0
    fi
    
    # Check webhook configurations
    if [[ -n "$SLACK_WEBHOOK_URL" ]]; then
        log_info "Slack notifications enabled"
    fi
    
    if [[ -n "$TEAMS_WEBHOOK_URL" ]]; then
        log_info "Microsoft Teams notifications enabled"
    fi
    
    if [[ "$EMAIL_ENABLED" == "true" ]]; then
        if [[ -z "$EMAIL_TO" ]]; then
            log_warn "Email enabled but EMAIL_TO not configured"
            config_valid=false
        else
            log_info "Email notifications enabled for: $EMAIL_TO"
        fi
    fi
    
    if [[ "$GITHUB_ISSUE_ENABLED" == "true" ]]; then
        if [[ -z "${GITHUB_TOKEN:-}" ]]; then
            log_warn "GitHub issue creation enabled but GITHUB_TOKEN not set"
            config_valid=false
        else
            log_info "GitHub issue creation enabled"
        fi
    fi
    
    if [[ "$config_valid" == true ]]; then
        log_success "Notification system initialized successfully"
        return 0
    else
        log_warn "Notification system initialized with configuration warnings"
        return 1
    fi
}

# Send Slack notification
send_slack_notification() {
    local title="$1"
    local message="$2"
    local severity="${3:-info}"
    local details="${4:-}"
    
    if [[ -z "$SLACK_WEBHOOK_URL" ]]; then
        log_warn "Slack webhook URL not configured, skipping Slack notification"
        return 1
    fi
    
    log_info "Sending Slack notification: $title"
    
    # Determine color based on severity
    local color="good"
    case "$severity" in
        "error"|"critical")
            color="danger"
            ;;
        "warning")
            color="warning"
            ;;
        "success")
            color="good"
            ;;
    esac
    
    # Build Slack payload
    local payload=$(cat << EOF
{
  "attachments": [
    {
      "color": "$color",
      "title": "$title",
      "text": "$message",
      "fields": [
        {
          "title": "Repository",
          "value": "${GITHUB_REPOSITORY:-unknown}",
          "short": true
        },
        {
          "title": "Branch",
          "value": "${GITHUB_REF_NAME:-unknown}",
          "short": true
        },
        {
          "title": "Commit",
          "value": "${GITHUB_SHA:-unknown}",
          "short": true
        },
        {
          "title": "Run ID",
          "value": "${GITHUB_RUN_ID:-unknown}",
          "short": true
        }
      ],
      "footer": "CI Build System",
      "ts": $(date +%s)
    }
  ]
}
EOF
    )
    
    # Add details if provided
    if [[ -n "$details" ]]; then
        payload=$(echo "$payload" | jq --arg details "$details" '.attachments[0].fields += [{"title": "Details", "value": $details, "short": false}]')
    fi
    
    # Send notification
    if curl -s -X POST -H "Content-Type: application/json" -d "$payload" "$SLACK_WEBHOOK_URL" >/dev/null; then
        log_success "Slack notification sent successfully"
        return 0
    else
        log_error "Failed to send Slack notification"
        return 1
    fi
}

# Send Microsoft Teams notification
send_teams_notification() {
    local title="$1"
    local message="$2"
    local severity="${3:-info}"
    local details="${4:-}"
    
    if [[ -z "$TEAMS_WEBHOOK_URL" ]]; then
        log_warn "Teams webhook URL not configured, skipping Teams notification"
        return 1
    fi
    
    log_info "Sending Teams notification: $title"
    
    # Determine theme color based on severity
    local theme_color="0078D4"
    case "$severity" in
        "error"|"critical")
            theme_color="FF0000"
            ;;
        "warning")
            theme_color="FFA500"
            ;;
        "success")
            theme_color="00FF00"
            ;;
    esac
    
    # Build Teams payload
    local payload=$(cat << EOF
{
  "@type": "MessageCard",
  "@context": "http://schema.org/extensions",
  "themeColor": "$theme_color",
  "summary": "$title",
  "sections": [
    {
      "activityTitle": "$title",
      "activitySubtitle": "$message",
      "facts": [
        {
          "name": "Repository",
          "value": "${GITHUB_REPOSITORY:-unknown}"
        },
        {
          "name": "Branch",
          "value": "${GITHUB_REF_NAME:-unknown}"
        },
        {
          "name": "Commit",
          "value": "${GITHUB_SHA:-unknown}"
        },
        {
          "name": "Run ID",
          "value": "${GITHUB_RUN_ID:-unknown}"
        }
      ]
    }
  ]
}
EOF
    )
    
    # Add details if provided
    if [[ -n "$details" ]]; then
        payload=$(echo "$payload" | jq --arg details "$details" '.sections[0].facts += [{"name": "Details", "value": $details}]')
    fi
    
    # Send notification
    if curl -s -X POST -H "Content-Type: application/json" -d "$payload" "$TEAMS_WEBHOOK_URL" >/dev/null; then
        log_success "Teams notification sent successfully"
        return 0
    else
        log_error "Failed to send Teams notification"
        return 1
    fi
}

# Send email notification
send_email_notification() {
    local title="$1"
    local message="$2"
    local severity="${3:-info}"
    local details="${4:-}"
    
    if [[ "$EMAIL_ENABLED" != "true" ]] || [[ -z "$EMAIL_TO" ]]; then
        log_warn "Email notifications not configured, skipping email"
        return 1
    fi
    
    log_info "Sending email notification: $title"
    
    # Build email content
    local email_subject="[CI Build] $title"
    local email_body=$(cat << EOF
Build Notification: $title

Message: $message

Build Information:
- Repository: ${GITHUB_REPOSITORY:-unknown}
- Branch: ${GITHUB_REF_NAME:-unknown}
- Commit: ${GITHUB_SHA:-unknown}
- Run ID: ${GITHUB_RUN_ID:-unknown}
- Timestamp: $(date)

EOF
    )
    
    if [[ -n "$details" ]]; then
        email_body+="Details:
$details

"
    fi
    
    email_body+="This is an automated message from the CI build system."
    
    # Send email using mail command if available
    if command -v mail >/dev/null 2>&1; then
        echo "$email_body" | mail -s "$email_subject" -r "$EMAIL_FROM" "$EMAIL_TO"
        if [[ $? -eq 0 ]]; then
            log_success "Email notification sent successfully"
            return 0
        else
            log_error "Failed to send email notification"
            return 1
        fi
    else
        log_warn "Mail command not available, cannot send email notification"
        return 1
    fi
}

# Create GitHub issue for critical errors
create_github_issue() {
    local title="$1"
    local message="$2"
    local error_report="${3:-}"
    
    if [[ "$GITHUB_ISSUE_ENABLED" != "true" ]] || [[ -z "${GITHUB_TOKEN:-}" ]]; then
        log_warn "GitHub issue creation not configured, skipping"
        return 1
    fi
    
    log_info "Creating GitHub issue: $title"
    
    # Build issue body
    local issue_body=$(cat << EOF
## Build Error Report

**Error:** $title

**Message:** $message

**Build Information:**
- Branch: ${GITHUB_REF_NAME:-unknown}
- Commit: ${GITHUB_SHA:-unknown}
- Run ID: ${GITHUB_RUN_ID:-unknown}
- Timestamp: $(date)

EOF
    )
    
    if [[ -n "$error_report" ]] && [[ -f "$error_report" ]]; then
        issue_body+="
**Error Report:**
\`\`\`json
$(cat "$error_report")
\`\`\`
"
    fi
    
    issue_body+="
**Labels:** ci-error, build-failure

This issue was automatically created by the CI build system."
    
    # Create GitHub issue
    local issue_payload=$(jq -n \
        --arg title "$title" \
        --arg body "$issue_body" \
        --argjson labels '["ci-error", "build-failure"]' \
        '{
            title: $title,
            body: $body,
            labels: $labels
        }')
    
    local api_url="https://api.github.com/repos/${GITHUB_REPOSITORY}/issues"
    
    if curl -s -X POST \
        -H "Authorization: token $GITHUB_TOKEN" \
        -H "Accept: application/vnd.github.v3+json" \
        -d "$issue_payload" \
        "$api_url" >/dev/null; then
        log_success "GitHub issue created successfully"
        return 0
    else
        log_error "Failed to create GitHub issue"
        return 1
    fi
}

# Send comprehensive notification
send_notification() {
    local title="$1"
    local message="$2"
    local severity="${3:-info}"
    local details="${4:-}"
    local error_report="${5:-}"
    
    if [[ "$NOTIFICATION_ENABLED" != "true" ]]; then
        log_info "Notifications disabled, skipping: $title"
        return 0
    fi
    
    log_info "Sending comprehensive notification: $title (severity: $severity)"
    
    local notification_success=false
    
    # Send Slack notification
    if send_slack_notification "$title" "$message" "$severity" "$details"; then
        notification_success=true
    fi
    
    # Send Teams notification
    if send_teams_notification "$title" "$message" "$severity" "$details"; then
        notification_success=true
    fi
    
    # Send email notification
    if send_email_notification "$title" "$message" "$severity" "$details"; then
        notification_success=true
    fi
    
    # Create GitHub issue for critical errors
    if [[ "$severity" == "critical" ]] || [[ "$severity" == "error" ]]; then
        if create_github_issue "$title" "$message" "$error_report"; then
            notification_success=true
        fi
    fi
    
    # Log to GitHub Actions if available
    if [[ -n "${GITHUB_ACTIONS:-}" ]]; then
        case "$severity" in
            "error"|"critical")
                echo "::error title=$title::$message"
                ;;
            "warning")
                echo "::warning title=$title::$message"
                ;;
            *)
                echo "::notice title=$title::$message"
                ;;
        esac
        notification_success=true
    fi
    
    if [[ "$notification_success" == true ]]; then
        log_success "Notification sent successfully"
        return 0
    else
        log_error "All notification methods failed"
        return 1
    fi
}

# Predefined notification templates
notify_build_failure() {
    local build_type="$1"
    local error_details="$2"
    local error_report="${3:-}"
    
    send_notification \
        "Build Failed: $build_type" \
        "The $build_type build has failed after all retry attempts." \
        "error" \
        "$error_details" \
        "$error_report"
}

notify_cache_corruption() {
    local corruption_details="$1"
    local recovery_status="$2"
    
    local severity="warning"
    local message="Cache corruption detected and recovery attempted."
    
    if [[ "$recovery_status" == "failed" ]]; then
        severity="error"
        message="Cache corruption detected and recovery failed."
    fi
    
    send_notification \
        "Cache Corruption Detected" \
        "$message" \
        "$severity" \
        "$corruption_details"
}

notify_build_recovery() {
    local recovery_method="$1"
    local build_type="$2"
    
    send_notification \
        "Build Recovered: $build_type" \
        "Build was successfully recovered using $recovery_method." \
        "success" \
        "Recovery method: $recovery_method"
}

notify_system_health() {
    local health_status="$1"
    local health_details="$2"
    
    local severity="info"
    if [[ "$health_status" == "critical" ]]; then
        severity="error"
    elif [[ "$health_status" == "warning" ]]; then
        severity="warning"
    fi
    
    send_notification \
        "System Health: $health_status" \
        "Build system health check completed with status: $health_status" \
        "$severity" \
        "$health_details"
}

# Test notification system
test_notifications() {
    log_info "Testing notification system..."
    
    local test_success=true
    
    # Test each notification method
    if [[ -n "$SLACK_WEBHOOK_URL" ]]; then
        if ! send_slack_notification "Test Notification" "This is a test message from the CI notification system" "info" "Test details"; then
            test_success=false
        fi
    fi
    
    if [[ -n "$TEAMS_WEBHOOK_URL" ]]; then
        if ! send_teams_notification "Test Notification" "This is a test message from the CI notification system" "info" "Test details"; then
            test_success=false
        fi
    fi
    
    if [[ "$EMAIL_ENABLED" == "true" ]]; then
        if ! send_email_notification "Test Notification" "This is a test message from the CI notification system" "info" "Test details"; then
            test_success=false
        fi
    fi
    
    if [[ "$test_success" == true ]]; then
        log_success "Notification system test completed successfully"
        return 0
    else
        log_error "Notification system test failed"
        return 1
    fi
}

# Main execution function
main() {
    local command="${1:-help}"
    shift || true
    
    case "$command" in
        "init")
            initialize_notifications
            ;;
        "test")
            test_notifications
            ;;
        "send")
            send_notification "$@"
            ;;
        "build-failure")
            notify_build_failure "$@"
            ;;
        "cache-corruption")
            notify_cache_corruption "$@"
            ;;
        "build-recovery")
            notify_build_recovery "$@"
            ;;
        "system-health")
            notify_system_health "$@"
            ;;
        "help")
            echo "Notification System Usage:"
            echo "  init                     - Initialize notification system"
            echo "  test                     - Test all configured notification methods"
            echo "  send <title> <message> [severity] [details] [report] - Send notification"
            echo "  build-failure <type> <details> [report] - Send build failure notification"
            echo "  cache-corruption <details> <status> - Send cache corruption notification"
            echo "  build-recovery <method> <type> - Send build recovery notification"
            echo "  system-health <status> <details> - Send system health notification"
            echo ""
            echo "Environment Variables:"
            echo "  NOTIFICATION_ENABLED     - Enable/disable notifications (default: true)"
            echo "  SLACK_WEBHOOK_URL        - Slack webhook URL for notifications"
            echo "  TEAMS_WEBHOOK_URL        - Microsoft Teams webhook URL"
            echo "  EMAIL_ENABLED            - Enable email notifications (default: false)"
            echo "  EMAIL_TO                 - Email recipient address"
            echo "  EMAIL_FROM               - Email sender address"
            echo "  GITHUB_ISSUE_ENABLED     - Enable GitHub issue creation (default: false)"
            echo "  GITHUB_TOKEN             - GitHub token for issue creation"
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