package com.zioanacleto.feedtracker.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MessageSnackbar(message: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.inverseSurface,
        shadowElevation = 6.dp,
        tonalElevation = 6.dp,
    ) {
        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            color = MaterialTheme.colorScheme.inverseOnSurface,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
