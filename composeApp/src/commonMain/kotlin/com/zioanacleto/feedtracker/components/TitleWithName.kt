package com.zioanacleto.feedtracker.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
fun TitleWithName(
    modifier: Modifier = Modifier,
    name: String
) {
    var showName by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(700)
        showName = true
    }

    Row(modifier = modifier) {
        Text(
            style = MaterialTheme.typography.titleLarge,
            text = "Hello, "
        )
        AnimatedVisibility(
            showName,
            enter = fadeIn()
        ) {
            Text(
                style = MaterialTheme.typography.titleLarge,
                text = name
            )
        }
    }
}