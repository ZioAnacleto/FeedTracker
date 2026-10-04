package com.zioanacleto.feedtracker.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val White = Color(0xFFFFFFFF)
private val OrangeDeep = Color(0xFF7A2E0E)
private val MustardDeep = Color(0xFF5C430E)
private val RustButton = Color(0xFF4A1A0C)
private val CardSurface = Color(0xFF3F160A)
private val VermilionDeep = Color(0xFF5E0B0B)
private val BrightOrange = Color(0xFFFB8C00)
private val Ink = Color(0xFF3B140C)
private val LightVermilion = Color(0xFFF8D7CC)
private val LightOrange = Color(0xFFFFD19A)

val feedTrackerDarkColorScheme = darkColorScheme(
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

val feedTrackerLightColorScheme = lightColorScheme(
    primary = RustButton,
    onPrimary = White,
    primaryContainer = Color(0xFFFFDBCB),
    onPrimaryContainer = Ink,
    secondary = Color(0xFF6B4E12),
    onSecondary = White,
    secondaryContainer = Color(0xFFFFE8B8),
    onSecondaryContainer = Color(0xFF3A2804),
    tertiary = Color(0xFF8A3A18),
    onTertiary = White,
    background = Color(0xFFFFF3EA),
    onBackground = Ink,
    surface = Color(0xFFFFF8F4),
    onSurface = Ink,
    surfaceVariant = Color(0xFFF3E0D4),
    onSurfaceVariant = Color(0xFF5C3A28),
    surfaceContainerLowest = White,
    surfaceContainerLow = Color(0xFFFFF6EF),
    surfaceContainer = Color(0xFFFFF1E6),
    surfaceContainerHigh = Color(0xFFF8E4D6),
    surfaceContainerHighest = Color(0xFFF0D6C6),
    outline = Color(0xFF8C4A32),
    outlineVariant = Color(0xFFD7B5A4),
    inverseSurface = Ink,
    inverseOnSurface = Color(0xFFFFF3EA),
    inversePrimary = Color(0xFFFFB58A),
    error = Color(0xFFBA1A1A),
    onError = White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

val feedTrackerDarkBackgroundBrush = Brush.verticalGradient(
    colors = listOf(VermilionDeep, BrightOrange),
)

val feedTrackerLightBackgroundBrush = Brush.verticalGradient(
    colors = listOf(LightVermilion, LightOrange),
)
