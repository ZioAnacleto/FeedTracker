package com.zioanacleto.feedtracker.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Composable
fun AnimatedTimer(time: Long, modifier: Modifier = Modifier) {
    val (hours, minutes, seconds) = formatElapsedTime(time)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (hours != "0") {
            AnimatedTextView(text = hours, name = "Hours")
            Text(
                text = ":",
                fontSize = 44.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        AnimatedTextView(text = minutes, name = "Minutes")
        Text(
            text = ":",
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        AnimatedTextView(text = seconds, name = "Seconds")
    }
}

@Composable
private fun AnimatedTextView(text: String, name: String, modifier: Modifier = Modifier) {
    AnimatedContent(
        targetState = text,
        transitionSpec = {
            (slideInVertically { height -> height } + fadeIn())
                .togetherWith(slideOutVertically { height -> -height } + fadeOut())
        },
        label = "AnimatedTextView_$name",
    ) {
        Text(
            text = it,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = modifier,
        )
    }
}

fun formatElapsedTimerText(millis: Long): String {
    val (hours, minutes, seconds) = formatElapsedTime(millis)
    return if (hours != "0") {
        "$hours:$minutes:$seconds"
    } else {
        "$minutes:$seconds"
    }
}

internal fun formatElapsedTime(millis: Long): List<String> {
    fun Long.formatTime() = toString().padStart(2, '0')

    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        listOf(
            hours.formatTime(),
            minutes.formatTime(),
            seconds.formatTime(),
        )
    } else {
        listOf(
            "0",
            minutes.formatTime(),
            seconds.formatTime(),
        )
    }
}

@Preview
@Composable
fun AnimatedTimerPreview() {
    AnimatedTimer(10000)
}
