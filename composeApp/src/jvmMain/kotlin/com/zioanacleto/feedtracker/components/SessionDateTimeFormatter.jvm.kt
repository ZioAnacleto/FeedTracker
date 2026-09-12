package com.zioanacleto.feedtracker.components

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

actual fun formatSessionDateTime(epochMillis: Long, datePattern: String): String {
    val formatter = SimpleDateFormat(datePattern, Locale.getDefault())
    formatter.timeZone = TimeZone.getDefault()
    return formatter.format(Date(epochMillis))
}
