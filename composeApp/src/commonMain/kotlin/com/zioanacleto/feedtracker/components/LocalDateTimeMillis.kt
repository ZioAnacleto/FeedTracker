package com.zioanacleto.feedtracker.components

import com.zioanacleto.feedtracker.domain.preferences.DateDisplayFormat

data class CivilDate(val year: Int, val month: Int, val day: Int)

data class LocalDateTimeParts(val date: CivilDate, val hour: Int, val minute: Int)

expect fun civilDateFromUtcEpochMillis(utcMidnightMillis: Long): CivilDate

expect fun utcEpochMillisFromCivilDate(date: CivilDate): Long

expect fun localDateTimeToEpochMillis(date: CivilDate, hour: Int, minute: Int): Long

expect fun localDateTimeFromEpochMillis(epochMillis: Long): LocalDateTimeParts

fun formatCivilDate(date: CivilDate, format: DateDisplayFormat = DateDisplayFormat.DAY_MONTH_YEAR): String = formatBirthDate(date, format)

fun formatClockTime(hour: Int, minute: Int): String {
    val hours = hour.toString().padStart(2, '0')
    val minutes = minute.toString().padStart(2, '0')
    return "$hours:$minutes"
}
