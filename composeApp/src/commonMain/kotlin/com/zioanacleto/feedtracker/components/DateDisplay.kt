package com.zioanacleto.feedtracker.components

import com.zioanacleto.feedtracker.domain.preferences.DateDisplayFormat

internal fun DateDisplayFormat.placeholder(): String = when (this) {
    DateDisplayFormat.DAY_MONTH_YEAR -> "DD/MM/YYYY"
    DateDisplayFormat.MONTH_DAY_YEAR -> "MM/DD/YYYY"
    DateDisplayFormat.YEAR_MONTH_DAY -> "YYYY/MM/DD"
}

internal val DateDisplayFormat.dateTimePattern: String
    get() = when (this) {
        DateDisplayFormat.DAY_MONTH_YEAR -> "dd/MM/yyyy HH:mm"
        DateDisplayFormat.MONTH_DAY_YEAR -> "MM/dd/yyyy HH:mm"
        DateDisplayFormat.YEAR_MONTH_DAY -> "yyyy/MM/dd HH:mm"
    }

internal fun formatBirthDateForDisplay(canonical: String, format: DateDisplayFormat): String {
    val date = parseCanonicalBirthDate(canonical) ?: return canonical
    return formatBirthDate(date, format)
}

internal fun canonicalBirthDateFromDisplay(display: String, format: DateDisplayFormat): String? {
    val date = parseBirthDate(display, format) ?: return null
    return formatBirthDate(date, DateDisplayFormat.DAY_MONTH_YEAR)
}

internal fun parseCanonicalBirthDate(value: String): CivilDate? = parseBirthDate(value, DateDisplayFormat.DAY_MONTH_YEAR)

internal fun formatBirthDate(date: CivilDate, format: DateDisplayFormat): String {
    val day = date.day.toString().padStart(2, '0')
    val month = date.month.toString().padStart(2, '0')
    val year = date.year.toString().padStart(4, '0')
    return when (format) {
        DateDisplayFormat.DAY_MONTH_YEAR -> "$day/$month/$year"
        DateDisplayFormat.MONTH_DAY_YEAR -> "$month/$day/$year"
        DateDisplayFormat.YEAR_MONTH_DAY -> "$year/$month/$day"
    }
}

internal fun parseBirthDate(value: String, format: DateDisplayFormat): CivilDate? {
    val parts = value.split("/")
    if (parts.size != 3) return null
    val first = parts[0].toIntOrNull() ?: return null
    val second = parts[1].toIntOrNull() ?: return null
    val third = parts[2].toIntOrNull() ?: return null
    val (day, month, year) = when (format) {
        DateDisplayFormat.DAY_MONTH_YEAR -> Triple(first, second, third)
        DateDisplayFormat.MONTH_DAY_YEAR -> Triple(second, first, third)
        DateDisplayFormat.YEAR_MONTH_DAY -> Triple(third, second, first)
    }
    if (year !in 1900..2099 || month !in 1..12 || day !in 1..31) return null
    return CivilDate(year = year, month = month, day = day)
}

internal fun startOfTrackingDayMillis(nowMillis: Long, dayStartHour: Int, dayStartMinute: Int, startOfLocalDay: (Long) -> Long): Long {
    val hour = dayStartHour.coerceIn(0, 23)
    val minute = dayStartMinute.coerceIn(0, 59)
    val midnight = startOfLocalDay(nowMillis)
    val todayStart = midnight + hour * 3_600_000L + minute * 60_000L
    return if (nowMillis >= todayStart) {
        todayStart
    } else {
        startOfLocalDay(midnight - 1L) + hour * 3_600_000L + minute * 60_000L
    }
}
