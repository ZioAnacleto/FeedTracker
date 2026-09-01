package com.zioanacleto.feedtracker.components

import java.util.Calendar

actual fun startOfLocalDayMillis(nowMillis: Long): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = nowMillis
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}
