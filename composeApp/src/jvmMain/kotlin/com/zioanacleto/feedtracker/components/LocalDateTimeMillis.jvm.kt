package com.zioanacleto.feedtracker.components

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

actual fun civilDateFromUtcEpochMillis(utcMidnightMillis: Long): CivilDate {
    val localDate = Instant.ofEpochMilli(utcMidnightMillis).atZone(ZoneOffset.UTC).toLocalDate()
    return CivilDate(localDate.year, localDate.monthValue, localDate.dayOfMonth)
}

actual fun utcEpochMillisFromCivilDate(date: CivilDate): Long = LocalDate.of(date.year, date.month, date.day)
    .atStartOfDay(ZoneOffset.UTC)
    .toInstant()
    .toEpochMilli()

actual fun localDateTimeToEpochMillis(date: CivilDate, hour: Int, minute: Int): Long =
    LocalDateTime.of(date.year, date.month, date.day, hour, minute)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

actual fun localDateTimeFromEpochMillis(epochMillis: Long): LocalDateTimeParts {
    val dateTime = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
    return LocalDateTimeParts(
        date = CivilDate(dateTime.year, dateTime.monthValue, dateTime.dayOfMonth),
        hour = dateTime.hour,
        minute = dateTime.minute,
    )
}
