package com.zioanacleto.feedtracker.components

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSTimeZone
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localTimeZone

actual fun formatSessionDateTime(epochMillis: Long, datePattern: String): String {
    val date = NSDate.dateWithTimeIntervalSince1970(epochMillis / 1000.0)
    val formatter = NSDateFormatter()
    formatter.dateFormat = datePattern
    formatter.timeZone = NSTimeZone.localTimeZone
    return formatter.stringFromDate(date)
}
