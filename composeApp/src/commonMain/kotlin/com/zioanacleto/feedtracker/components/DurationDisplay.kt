package com.zioanacleto.feedtracker.components

import com.zioanacleto.feedtracker.domain.preferences.DurationDisplayFormat

internal fun durationDisplayParts(durationMs: Long, format: DurationDisplayFormat): Pair<Int, Int> = when (format) {
    DurationDisplayFormat.MINUTES_SECONDS -> {
        val totalSeconds = durationMs.coerceAtLeast(0L) / 1000
        (totalSeconds / 60).toInt() to (totalSeconds % 60).toInt()
    }
    DurationDisplayFormat.HOURS_MINUTES -> {
        val totalMinutes = durationMs.coerceAtLeast(0L) / MINUTE_MS
        (totalMinutes / 60).toInt() to (totalMinutes % 60).toInt()
    }
}
