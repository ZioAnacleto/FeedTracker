package com.zioanacleto.feedtracker.components

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class LocalDateTimeMillisTest {

    @Test
    fun formatsCivilDateAndClockTime() {
        formatCivilDate(CivilDate(2026, 9, 1)) shouldBe "01/09/2026"
        formatClockTime(8, 5) shouldBe "08:05"
        formatClockTime(0, 0) shouldBe "00:00"
        formatClockTime(23, 59) shouldBe "23:59"
    }

    @Test
    fun localDateTimeRoundTripsThroughEpochMillis() {
        val date = CivilDate(2026, 3, 15)
        val millis = localDateTimeToEpochMillis(date, hour = 9, minute = 41)
        localDateTimeFromEpochMillis(millis) shouldBe LocalDateTimeParts(date, hour = 9, minute = 41)
    }

    @Test
    fun utcCivilDateRoundTripsThroughEpochMillis() {
        val date = CivilDate(2026, 3, 15)
        civilDateFromUtcEpochMillis(utcEpochMillisFromCivilDate(date)) shouldBe date
    }

    @Test
    fun localDateTimeUsesSystemZoneIncludingDaylightSaving() {
        val zone = java.time.ZoneId.systemDefault()
        val date = CivilDate(2026, 7, 15)
        val millis = localDateTimeToEpochMillis(date, hour = 14, minute = 0)
        val zoned = java.time.Instant.ofEpochMilli(millis).atZone(zone)
        zoned.year shouldBe 2026
        zoned.monthValue shouldBe 7
        zoned.dayOfMonth shouldBe 15
        zoned.hour shouldBe 14
        zoned.minute shouldBe 0
    }
}
