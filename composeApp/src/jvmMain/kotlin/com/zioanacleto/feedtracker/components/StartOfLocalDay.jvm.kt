package com.zioanacleto.feedtracker.components

import java.time.Instant
import java.time.ZoneId

actual fun startOfLocalDayMillis(nowMillis: Long): Long {
    val zone = ZoneId.systemDefault()
    return Instant.ofEpochMilli(nowMillis)
        .atZone(zone)
        .toLocalDate()
        .atStartOfDay(zone)
        .toInstant()
        .toEpochMilli()
}
