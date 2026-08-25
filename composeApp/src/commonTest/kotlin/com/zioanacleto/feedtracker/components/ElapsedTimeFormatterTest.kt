package com.zioanacleto.feedtracker.components

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ElapsedTimeFormatterTest {

    @Test
    fun formatsSecondsWithoutHours() {
        formatElapsedTime(10_000L) shouldBe listOf("0", "00", "10")
        formatElapsedTime(61_000L) shouldBe listOf("0", "01", "01")
        formatElapsedTime(0L) shouldBe listOf("0", "00", "00")
    }

    @Test
    fun formatsHoursWhenElapsedTimeIsAtLeastOneHour() {
        formatElapsedTime(3_600_000L) shouldBe listOf("01", "00", "00")
        formatElapsedTime(3_661_000L) shouldBe listOf("01", "01", "01")
    }
}
