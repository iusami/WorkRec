package com.workrec.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.workrec.presentation.ui.theme.WorkRecTheme
import com.workrec.presentation.ui.utils.ResponsiveUtils
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Calendar navigation header component with month/year display and navigation controls
 * 
 * @param currentMonth The currently displayed month
 * @param onPreviousMonth Callback when previous month button is clicked
 * @param onNextMonth Callback when next month button is clicked
 * @param onTodayClick Callback when "Today" button is clicked
 * @param modifier Modifier for styling
 */
@Composable
fun CalendarNavigationHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val layoutConfig = ResponsiveUtils.getCalendarLayoutConfig()
    val minTouchTarget = ResponsiveUtils.getMinTouchTargetSize()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = layoutConfig.headerPadding, vertical = layoutConfig.verticalPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous month button
        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier
                .size(minTouchTarget)
                .semantics {
                    contentDescription = "前の月に移動"
                    role = Role.Button
                }
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = null, // Handled by parent semantics
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Month/Year display and Today button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.semantics {
                contentDescription = "カレンダーナビゲーション, 現在の表示: ${formatMonthYear(currentMonth)}"
            }
        ) {
            Text(
                text = formatMonthYear(currentMonth),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics {
                    contentDescription = "現在の表示月: ${formatMonthYear(currentMonth)}"
                }
            )
            
            TextButton(
                onClick = onTodayClick,
                modifier = Modifier.semantics {
                    contentDescription = "今日の日付に移動"
                    role = Role.Button
                }
            ) {
                Text(
                    text = "今日",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Next month button
        IconButton(
            onClick = onNextMonth,
            modifier = Modifier
                .size(minTouchTarget)
                .semantics {
                    contentDescription = "次の月に移動"
                    role = Role.Button
                }
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null, // Handled by parent semantics
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Format YearMonth to Japanese display format (e.g., "2024年1月")
 */
private fun formatMonthYear(yearMonth: YearMonth): String {
    return "${yearMonth.year}年${yearMonth.monthValue}月"
}

@Preview(showBackground = true)
@Composable
private fun CalendarNavigationHeaderPreview() {
    WorkRecTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current month
            CalendarNavigationHeader(
                currentMonth = YearMonth.of(2024, 1),
                onPreviousMonth = { },
                onNextMonth = { },
                onTodayClick = { }
            )
            
            // Different month for variety
            CalendarNavigationHeader(
                currentMonth = YearMonth.of(2024, 12),
                onPreviousMonth = { },
                onNextMonth = { },
                onTodayClick = { }
            )
        }
    }
}