package com.zioanacleto.feedtracker.components

import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSTimeIntervalSince1970
import platform.Foundation.dateWithTimeIntervalSince1970

actual fun startOfLocalDayMillis(nowMillis: Long): Long {
    val calendar = NSCalendar.currentCalendar
    val date = NSDate.dateWithTimeIntervalSince1970(nowMillis / 1000.0)
    val startOfDay = calendar.startOfDayForDate(date)
    val secondsSince1970 = startOfDay.timeIntervalSinceReferenceDate + NSTimeIntervalSince1970
    return (secondsSince1970 * 1000.0).toLong()
}
