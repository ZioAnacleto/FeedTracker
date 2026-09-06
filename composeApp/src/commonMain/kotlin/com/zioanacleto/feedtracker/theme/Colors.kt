package com.zioanacleto.feedtracker.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val White = Color(0xFFFFFFFF)
private val OrangeDeep = Color(0xFF7A2E0E)
private val MustardDeep = Color(0xFF5C430E)
private val RustButton = Color(0xFF4A1A0C)
private val CardSurface = Color(0xFF3F160A)
private val VermilionDeep = Color(0xFF5E0B0B)
private val BrightOrange = Color(0xFFFB8C00)

val feedTrackerColorScheme = darkColorScheme(
    primary = RustButton,
    onPrimary = White,
    primaryContainer = OrangeDeep,
    onPrimaryContainer = White,
    secondary = MustardDeep,
    onSecondary = White,
    secondaryContainer = Color(0xFF4A3610),
    onSecondaryContainer = White,
    tertiary = Color(0xFF6B2A10),
    onTertiary = White,
    background = OrangeDeep,
    onBackground = White,
    surface = CardSurface,
    onSurface = White,
    surfaceVariant = Color(0xFF4A2210),
    onSurfaceVariant = White,
    surfaceContainerLowest = Color(0xFF2C1008),
    surfaceContainerLow = Color(0xFF36140A),
    surfaceContainer = CardSurface,
    surfaceContainerHigh = Color(0xFF4A1C0C),
    surfaceContainerHighest = Color(0xFF542210),
    outline = Color(0xFFFFE0B2),
    outlineVariant = Color(0xCCFFE0B2),
    inverseSurface = Color(0xFF2C1008),
    inverseOnSurface = White,
    inversePrimary = Color(0xFFFFCC80),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = White,
)

val FeedTrackerBackgroundBrush = Brush.verticalGradient(
    colors = listOf(VermilionDeep, BrightOrange),
)
