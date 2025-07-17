package com.workrec.presentation.ui.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.plus
import kotlinx.datetime.minus

/**
 * Wrapper component that adds keyboard navigation support to calendar components
 * 
 * @param selectedDate Currently selected date
 * @param onDateSelected Callback when a date is selected via keyboard
 * @param onPreviousMonth Callback to navigate to previous month
 * @param onNextMonth Callback to navigate to next month
 * @param onTodayClick Callback to navigate to today
 * @param modifier Modifier for styling
 * @param content The calendar content to wrap
 */
@Composable
fun CalendarKeyboardNavigation(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    
    Box(
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionLeft -> {
                            selectedDate?.let { date ->
                                onDateSelected(date.minus(DatePeriod(days = 1)))
                            }
                            true
                        }
                        Key.DirectionRight -> {
                            selectedDate?.let { date ->
                                onDateSelected(date.plus(DatePeriod(days = 1)))
                            }
                            true
                        }
                        Key.DirectionUp -> {
                            selectedDate?.let { date ->
                                onDateSelected(date.minus(DatePeriod(days = 7)))
                            }
                            true
                        }
                        Key.DirectionDown -> {
                            selectedDate?.let { date ->
                                onDateSelected(date.plus(DatePeriod(days = 7)))
                            }
                            true
                        }
                        Key.PageUp -> {
                            onPreviousMonth()
                            true
                        }
                        Key.PageDown -> {
                            onNextMonth()
                            true
                        }
                        Key.Home -> {
                            onTodayClick()
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .semantics {
                contentDescription = "カレンダーキーボードナビゲーション。矢印キーで日付移動、PageUp/PageDownで月移動、Homeキーで今日に移動"
            }
    ) {
        content()
    }
    
    // Request focus when component is first composed
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}