package com.zioanacleto.feedtracker.components

import com.zioanacleto.feedtracker.domain.preferences.DateDisplayFormat

expect fun formatSessionDateTime(epochMillis: Long, datePattern: String): String

fun formatSessionDateTime(epochMillis: Long, dateFormat: DateDisplayFormat = DateDisplayFormat.DAY_MONTH_YEAR): String =
    formatSessionDateTime(epochMillis, dateFormat.dateTimePattern)
