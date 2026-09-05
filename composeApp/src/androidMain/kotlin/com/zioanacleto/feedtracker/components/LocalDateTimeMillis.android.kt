package com.zioanacleto.feedtracker.components

import java.util.Calendar
import java.util.TimeZone

actual fun civilDateFromUtcEpochMillis(utcMidnightMillis: Long): CivilDate {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendar.timeInMillis = utcMidnightMillis
    return CivilDate(
        year = calendar.get(Calendar.YEAR),
        month = calendar.get(Calendar.MONTH) + 1,
        day = calendar.get(Calendar.DAY_OF_MONTH),
    )
}

actual fun utcEpochMillisFromCivilDate(date: CivilDate): Long {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendar.clear()
    calendar.set(date.year, date.month - 1, date.day, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

actual fun localDateTimeToEpochMillis(date: CivilDate, hour: Int, minute: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.set(date.year, date.month - 1, date.day, hour, minute, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

actual fun localDateTimeFromEpochMillis(epochMillis: Long): LocalDateTimeParts {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = epochMillis
    return LocalDateTimeParts(
        date = CivilDate(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH) + 1,
            day = calendar.get(Calendar.DAY_OF_MONTH),
        ),
        hour = calendar.get(Calendar.HOUR_OF_DAY),
        minute = calendar.get(Calendar.MINUTE),
    )
}
