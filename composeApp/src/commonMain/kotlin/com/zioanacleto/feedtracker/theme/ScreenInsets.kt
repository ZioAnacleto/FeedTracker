package com.zioanacleto.feedtracker.theme

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val ScreenHorizontalPadding = 16.dp

/** Forms and auth. Phone width is unchanged because the cap is wider than a phone. */
val FormContentMaxWidth = 520.dp

/** Settings lists. */
val SettingsContentMaxWidth = 640.dp

/** Home on tablet and desktop. */
val HomeContentMaxWidth = 1120.dp

/**
 * Caps width and centers the child when the parent is wider than [maxWidth].
 * On a phone the parent is already narrower, so the layout stays full width.
 */
fun Modifier.centeredMaxWidth(maxWidth: Dp): Modifier = this.then(
    Modifier.layout { measurable, constraints ->
        val cap = maxWidth.roundToPx()
        val childMaxWidth = if (constraints.hasBoundedWidth && constraints.maxWidth > cap) {
            cap
        } else {
            constraints.maxWidth
        }
        val placeable = measurable.measure(
            constraints.copy(
                minWidth = 0,
                maxWidth = childMaxWidth,
                minHeight = 0,
            ),
        )
        val layoutWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else placeable.width
        val layoutHeight = if (constraints.hasBoundedHeight) constraints.maxHeight else placeable.height
        val x = ((layoutWidth - placeable.width) / 2).coerceAtLeast(0)
        layout(layoutWidth, layoutHeight) {
            placeable.placeRelative(x, 0)
        }
    },
)

@Composable
fun Modifier.feedTrackerScreenWindowInsets(): Modifier = windowInsetsPadding(
    WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical),
)
