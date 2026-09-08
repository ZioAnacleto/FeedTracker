package com.zioanacleto.feedtracker.components

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSTimeIntervalSince1970
import platform.Foundation.NSTimeZone
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.systemTimeZone
import platform.Foundation.timeZoneForSecondsFromGMT

private val dateTimeUnits =
    NSCalendarUnitYear or
        NSCalendarUnitMonth or
        NSCalendarUnitDay or
        NSCalendarUnitHour or
        NSCalendarUnitMinute

actual fun civilDateFromUtcEpochMillis(utcMidnightMillis: Long): CivilDate {
    val calendar = NSCalendar.currentCalendar
    calendar.timeZone = NSTimeZone.timeZoneForSecondsFromGMT(0)
    val date = NSDate.dateWithTimeIntervalSince1970(utcMidnightMillis / 1000.0)
    val components = calendar.components(dateTimeUnits, fromDate = date)
    return CivilDate(
        year = components.year.toInt(),
        month = components.month.toInt(),
        day = components.day.toInt(),
    )
}

actual fun utcEpochMillisFromCivilDate(date: CivilDate): Long {
    val calendar = NSCalendar.currentCalendar
    calendar.timeZone = NSTimeZone.timeZoneForSecondsFromGMT(0)
    val components = NSDateComponents()
    components.year = date.year.toLong()
    components.month = date.month.toLong()
    components.day = date.day.toLong()
    components.hour = 0
    components.minute = 0
    components.second = 0
    val nsDate = calendar.dateFromComponents(components) ?: return 0L
    return nsDate.toEpochMillis()
}

actual fun localDateTimeToEpochMillis(date: CivilDate, hour: Int, minute: Int): Long {
    val calendar = NSCalendar.currentCalendar
    calendar.timeZone = NSTimeZone.systemTimeZone
    val components = NSDateComponents()
    components.timeZone = NSTimeZone.systemTimeZone
    components.year = date.year.toLong()
    components.month = date.month.toLong()
    components.day = date.day.toLong()
    components.hour = hour.toLong()
    components.minute = minute.toLong()
    components.second = 0
    val nsDate = calendar.dateFromComponents(components) ?: return 0L
    return nsDate.toEpochMillis()
}

actual fun localDateTimeFromEpochMillis(epochMillis: Long): LocalDateTimeParts {
    val calendar = NSCalendar.currentCalendar
    calendar.timeZone = NSTimeZone.systemTimeZone
    val date = NSDate.dateWithTimeIntervalSince1970(epochMillis / 1000.0)
    val components = calendar.components(dateTimeUnits, fromDate = date)
    return LocalDateTimeParts(
        date = CivilDate(
            year = components.year.toInt(),
            month = components.month.toInt(),
            day = components.day.toInt(),
        ),
        hour = components.hour.toInt(),
        minute = components.minute.toInt(),
    )
}

private fun NSDate.toEpochMillis(): Long {
    val secondsSince1970 = timeIntervalSinceReferenceDate + NSTimeIntervalSince1970
    return (secondsSince1970 * 1000.0).toLong()
}
