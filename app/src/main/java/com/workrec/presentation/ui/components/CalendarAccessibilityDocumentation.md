# Calendar Responsive Design & Accessibility Documentation

## Overview

This document describes the responsive design and accessibility features implemented for the workout calendar view. The implementation ensures that the calendar adapts to different screen sizes while maintaining accessibility standards.

## Responsive Design Features

### Screen Size Categories

The calendar uses three screen size categories based on Material Design guidelines:

- **COMPACT** (< 600dp width): Phones in portrait mode
- **MEDIUM** (600-840dp width): Tablets, foldables, phones in landscape
- **EXPANDED** (> 840dp width): Large tablets, desktop screens

### Responsive Layout Configurations

Each screen size has its own layout configuration:

#### Compact Screens (Phones)
- Horizontal padding: 8dp
- Vertical padding: 8dp
- Cell spacing: 4dp
- Header padding: 16dp
- Minimum cell size: 48dp (accessibility minimum)
- Layout: Compact vertical stack

#### Medium Screens (Tablets)
- Horizontal padding: 16dp
- Vertical padding: 12dp
- Cell spacing: 6dp
- Header padding: 24dp
- Minimum cell size: 56dp
- Layout: Enhanced spacing

#### Expanded Screens (Large Tablets/Desktop)
- Horizontal padding: 24dp
- Vertical padding: 16dp
- Cell spacing: 8dp
- Header padding: 32dp
- Minimum cell size: 64dp
- Layout: Side-by-side calendar and workout list

### Layout Adaptations

#### Phone Layout (Compact)
```
┌─────────────────────┐
│ Navigation Header   │
├─────────────────────┤
│ Calendar Grid       │
├─────────────────────┤
│ Selected Date       │
│ Workout List        │
└─────────────────────┘
```

#### Tablet/Desktop Layout (Medium/Expanded)
```
┌─────────────────────────────────────┐
│ Navigation Header                   │
├─────────────────┬───────────────────┤
│ Calendar Grid   │ Selected Date     │
│                 │ Workout List      │
│                 │                   │
└─────────────────┴───────────────────┘
```

## Accessibility Features

### Touch Target Sizes

All interactive elements meet or exceed the Material Design minimum touch target size of 48dp:

- **Compact screens**: 48dp minimum (accessibility baseline)
- **Medium screens**: 56dp minimum (enhanced usability)
- **Expanded screens**: 64dp minimum (optimal for large screens)

### Content Descriptions

Each calendar day cell includes comprehensive accessibility information:

```kotlin
// Example content description
"1月15日, ワークアウト2件, 今日"
// Translation: "January 15th, 2 workouts, today"
```

Content descriptions include:
- Date information (month and day)
- Workout count (if any workouts exist)
- Today indicator (if the date is today)
- Month context (if date is from previous/next month)

### State Descriptions

Selected and workout states are communicated to accessibility services:

```kotlin
// Example state description
"選択中, ワークアウトあり"
// Translation: "Selected, has workout"
```

### Semantic Roles

- Calendar day cells: `Role.Button` for proper interaction
- Navigation buttons: `Role.Button` with descriptive labels
- Calendar grid: Proper semantic structure for screen readers

### Keyboard Navigation

The calendar supports keyboard navigation:
- Tab navigation between interactive elements
- Arrow key navigation within the calendar grid
- Enter/Space activation of calendar cells

## Implementation Details

### ResponsiveUtils Class

The `ResponsiveUtils` object provides utilities for responsive design:

```kotlin
// Get current screen size
val screenSize = ResponsiveUtils.getScreenSize()

// Get responsive layout configuration
val layoutConfig = ResponsiveUtils.getCalendarLayoutConfig()

// Check if tablet or larger
val isTablet = ResponsiveUtils.isTabletOrLarger()
```

### CalendarLayoutConfig Data Class

Configuration object that defines layout parameters for each screen size:

```kotlin
data class CalendarLayoutConfig(
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val cellSpacing: Dp,
    val headerPadding: Dp,
    val minCellSize: Dp,
    val showCompactLayout: Boolean
)
```

### Component Updates

#### MonthlyCalendarGrid
- Uses responsive padding and spacing
- Adapts cell sizes based on screen size
- Maintains minimum height for proper layout

#### CalendarDayCell
- Accepts `minSize` parameter for responsive sizing
- Uses `sizeIn` modifier to ensure minimum touch targets
- Maintains aspect ratio while respecting minimum size

#### CalendarNavigationHeader
- Responsive padding for different screen sizes
- Minimum touch target sizes for navigation buttons
- Proper spacing for different layouts

#### WorkoutListScreen
- Conditional layout based on screen size
- Side-by-side layout for tablets and larger screens
- Vertical stack layout for phones

## Testing

### Unit Tests

- `ResponsiveUtilsTest`: Tests responsive design calculations
- `CalendarAccessibilityTest`: Tests accessibility features and layout configurations

### Test Coverage

- Screen size detection and configuration
- Touch target size validation
- Accessibility content description generation
- Layout configuration validation
- Padding and spacing calculations

## Performance Considerations

### Optimizations

- Layout configurations are computed once per screen size change
- Responsive values are cached during composition
- Minimal recomposition when screen size changes
- Efficient grid layout with proper sizing constraints

### Memory Usage

- Configuration objects are lightweight data classes
- No unnecessary object creation during layout
- Proper disposal of resources when screen rotates

## Future Enhancements

### Potential Improvements

1. **Dynamic Text Scaling**: Support for user-defined text size preferences
2. **Orientation Handling**: Enhanced landscape mode layouts
3. **Foldable Support**: Specific layouts for foldable devices
4. **High Contrast Mode**: Enhanced visual accessibility
5. **Voice Navigation**: Voice commands for calendar navigation

### Accessibility Enhancements

1. **Screen Reader Optimization**: Enhanced announcements for calendar navigation
2. **High Contrast Themes**: Better visibility for users with visual impairments
3. **Reduced Motion**: Respect user preferences for reduced animations
4. **Focus Management**: Improved focus handling during navigation

## Conclusion

The responsive design implementation ensures that the workout calendar provides an optimal user experience across all device sizes while maintaining strict accessibility standards. The modular design allows for easy maintenance and future enhancements.