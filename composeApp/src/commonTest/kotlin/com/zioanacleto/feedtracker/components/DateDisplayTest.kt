package com.zioanacleto.feedtracker.components

import com.zioanacleto.feedtracker.domain.preferences.DateDisplayFormat
import com.zioanacleto.feedtracker.domain.preferences.DurationDisplayFormat
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class DateDisplayTest {

    @Test
    fun convertsCanonicalBirthDatesToPreferredDisplayFormats() {
        formatBirthDateForDisplay("01/09/1990", DateDisplayFormat.DAY_MONTH_YEAR) shouldBe "01/09/1990"
        formatBirthDateForDisplay("01/09/1990", DateDisplayFormat.MONTH_DAY_YEAR) shouldBe "09/01/1990"
        formatBirthDateForDisplay("01/09/1990", DateDisplayFormat.YEAR_MONTH_DAY) shouldBe "1990/09/01"
    }

    @Test
    fun convertsDisplayedBirthDatesBackToCanonical() {
        canonicalBirthDateFromDisplay("09/01/1990", DateDisplayFormat.MONTH_DAY_YEAR) shouldBe "01/09/1990"
        canonicalBirthDateFromDisplay("1990/09/01", DateDisplayFormat.YEAR_MONTH_DAY) shouldBe "01/09/1990"
    }

    @Test
    fun startOfTrackingDayMovesToPreviousDayBeforeConfiguredHour() {
        val midnight = 1_000_000L
        val sixHours = 6 * 3_600_000L
        startOfTrackingDayMillis(
            nowMillis = midnight + 5 * 3_600_000L,
            dayStartHour = 6,
            dayStartMinute = 0,
            startOfLocalDay = { now -> if (now >= midnight) midnight else midnight - 86_400_000L },
        ) shouldBe midnight - 86_400_000L + sixHours

        startOfTrackingDayMillis(
            nowMillis = midnight + 7 * 3_600_000L,
            dayStartHour = 6,
            dayStartMinute = 0,
            startOfLocalDay = { midnight },
        ) shouldBe midnight + sixHours
    }

    @Test
    fun durationDisplayPartsFollowSelectedFormat() {
        durationDisplayParts(90_000L, DurationDisplayFormat.MINUTES_SECONDS) shouldBe (1 to 30)
        durationDisplayParts(90 * 60_000L, DurationDisplayFormat.HOURS_MINUTES) shouldBe (1 to 30)
    }
}
