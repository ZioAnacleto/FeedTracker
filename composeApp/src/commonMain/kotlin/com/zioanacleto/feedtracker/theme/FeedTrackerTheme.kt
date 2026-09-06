package com.zioanacleto.feedtracker.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun FeedTrackerTheme(content: @Composable () -> Unit) {
    val colors = feedTrackerColorScheme

    MaterialTheme(
        colorScheme = colors,
        shapes = feedTrackerShapes,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FeedTrackerBackgroundBrush),
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

@Composable
fun feedTrackerCardColors() = CardDefaults.cardColors(
    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
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
        focusedBorderColor = colors.outline,
        unfocusedBorderColor = colors.outline.copy(alpha = 0.7f),
        disabledBorderColor = colors.outline.copy(alpha = 0.7f),
        focusedLabelColor = colors.onSurface,
        unfocusedLabelColor = colors.onSurface.copy(alpha = 0.85f),
        disabledLabelColor = colors.onSurface.copy(alpha = 0.85f),
        cursorColor = colors.onSurface,
        focusedPlaceholderColor = colors.onSurface.copy(alpha = 0.6f),
        unfocusedPlaceholderColor = colors.onSurface.copy(alpha = 0.6f),
        disabledPlaceholderColor = colors.onSurface.copy(alpha = 0.6f),
        focusedTrailingIconColor = colors.onSurface,
        unfocusedTrailingIconColor = colors.onSurface,
        disabledTrailingIconColor = colors.onSurface,
    )
}
