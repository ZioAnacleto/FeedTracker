package com.zioanacleto.feedtracker.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun FeedTrackerTheme(content: @Composable () -> Unit) {
    val colors = if(isSystemInDarkTheme()) darkColorScheme else lightColorScheme

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}