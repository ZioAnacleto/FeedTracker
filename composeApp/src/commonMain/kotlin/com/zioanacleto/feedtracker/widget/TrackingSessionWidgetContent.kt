package com.zioanacleto.feedtracker.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zioanacleto.feedtracker.components.formatElapsedTimerText
import com.zioanacleto.feedtracker.getCurrentTimeMillis
import com.zioanacleto.feedtracker.theme.FeedTrackerBackgroundBrush
import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.widget_idle_label
import feedtracker.composeapp.generated.resources.widget_idle_timer
import feedtracker.composeapp.generated.resources.widget_tracking_label
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun TrackingSessionWidgetContent(startTimeMillis: Long?, onClick: () -> Unit, modifier: Modifier = Modifier, displayName: String = "") {
    var nowMillis by remember { mutableLongStateOf(getCurrentTimeMillis()) }
    LaunchedEffect(startTimeMillis) {
        if (startTimeMillis == null) return@LaunchedEffect
        while (true) {
            nowMillis = getCurrentTimeMillis()
            delay(200)
        }
    }

    val isActive = startTimeMillis != null
    val timerText = if (startTimeMillis == null) {
        stringResource(Res.string.widget_idle_timer)
    } else {
        formatElapsedTimerText((nowMillis - startTimeMillis).coerceAtLeast(0L))
    }
    val label = if (isActive) {
        stringResource(Res.string.widget_tracking_label)
    } else {
        stringResource(Res.string.widget_idle_label)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FeedTrackerBackgroundBrush, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (isActive && displayName.isNotEmpty()) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = timerText,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
