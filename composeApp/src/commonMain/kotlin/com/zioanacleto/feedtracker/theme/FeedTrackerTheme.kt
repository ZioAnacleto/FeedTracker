package com.zioanacleto.feedtracker.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.zioanacleto.feedtracker.domain.preferences.AppearanceMode
import com.zioanacleto.feedtracker.domain.preferences.isDarkTheme
import com.zioanacleto.feedtracker.domain.repositories.AppearancePreferencesRepository
import org.koin.compose.koinInject

private val LocalFeedTrackerDarkTheme = staticCompositionLocalOf { true }
private val LocalFeedTrackerBackgroundBrush = staticCompositionLocalOf { feedTrackerDarkBackgroundBrush }

@Composable
fun feedTrackerBackgroundBrush(): Brush = LocalFeedTrackerBackgroundBrush.current

@Composable
fun FeedTrackerTheme(content: @Composable () -> Unit) {
    val appearancePreferencesRepository = koinInject<AppearancePreferencesRepository>()
    val mode by appearancePreferencesRepository.mode.collectAsState()
    FeedTrackerTheme(
        darkTheme = mode.isDarkTheme(isSystemInDarkTheme()),
        followSystem = mode == AppearanceMode.SYSTEM,
        content = content,
    )
}

@Composable
fun FeedTrackerTheme(darkTheme: Boolean, followSystem: Boolean = false, content: @Composable () -> Unit) {
    val colors = if (darkTheme) feedTrackerDarkColorScheme else feedTrackerLightColorScheme
    val backgroundBrush = if (darkTheme) feedTrackerDarkBackgroundBrush else feedTrackerLightBackgroundBrush

    CompositionLocalProvider(
        LocalFeedTrackerDarkTheme provides darkTheme,
        LocalFeedTrackerBackgroundBrush provides backgroundBrush,
    ) {
        MaterialTheme(
            colorScheme = colors,
            shapes = feedTrackerShapes,
        ) {
            ApplyFeedTrackerSystemBars(darkTheme = darkTheme, followSystem = followSystem)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush),
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent,
                    contentColor = colors.onBackground,
                    content = content,
                )
            }
        }
    }
}

@Composable
fun feedTrackerCardColors() = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.surface.copy(
        alpha = if (LocalFeedTrackerDarkTheme.current) 0.72f else 0.94f,
    ),
    contentColor = MaterialTheme.colorScheme.onSurface,
)

@Composable
fun feedTrackerTextButtonColors() = ButtonDefaults.textButtonColors(
    contentColor = MaterialTheme.colorScheme.onBackground,
)

@Composable
fun feedTrackerTextFieldColors(): TextFieldColors {
    val colors = MaterialTheme.colorScheme
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = colors.onSurface,
        unfocusedTextColor = colors.onSurface,
        disabledTextColor = colors.onSurface,
        errorTextColor = colors.onSurface,
        focusedBorderColor = colors.outline,
        unfocusedBorderColor = colors.outline.copy(alpha = 0.7f),
        disabledBorderColor = colors.outline.copy(alpha = 0.7f),
        errorBorderColor = colors.error,
        focusedLabelColor = colors.onSurface,
        unfocusedLabelColor = colors.onSurface.copy(alpha = 0.85f),
        disabledLabelColor = colors.onSurface.copy(alpha = 0.85f),
        errorLabelColor = colors.error,
        cursorColor = colors.onSurface,
        errorCursorColor = colors.error,
        focusedPlaceholderColor = colors.onSurface.copy(alpha = 0.6f),
        unfocusedPlaceholderColor = colors.onSurface.copy(alpha = 0.6f),
        disabledPlaceholderColor = colors.onSurface.copy(alpha = 0.6f),
        errorPlaceholderColor = colors.onSurface.copy(alpha = 0.6f),
        focusedTrailingIconColor = colors.onSurface,
        unfocusedTrailingIconColor = colors.onSurface,
        disabledTrailingIconColor = colors.onSurface,
        errorTrailingIconColor = colors.error,
        focusedSupportingTextColor = colors.onSurface.copy(alpha = 0.75f),
        unfocusedSupportingTextColor = colors.onSurface.copy(alpha = 0.75f),
        errorSupportingTextColor = colors.error,
    )
}
