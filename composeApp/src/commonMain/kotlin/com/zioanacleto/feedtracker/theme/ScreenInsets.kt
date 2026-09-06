package com.zioanacleto.feedtracker.theme

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val ScreenHorizontalPadding = 16.dp

@Composable
fun Modifier.feedTrackerScreenWindowInsets(): Modifier = windowInsetsPadding(
    WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical),
)
