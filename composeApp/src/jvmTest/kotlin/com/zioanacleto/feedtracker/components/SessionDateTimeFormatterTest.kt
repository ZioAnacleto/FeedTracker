package com.zioanacleto.feedtracker.components

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import kotlin.test.Test

class SessionDateTimeFormatterTest {

    @Test
    fun formatsEpochMillisAsDayMonthYearAndTime() {
        val formatted = formatSessionDateTime(1_704_067_200_000L)
        formatted shouldMatch Regex("""\d{2}/\d{2}/\d{4} \d{2}:\d{2}""")
        formatted.length shouldBe 16
    }
}
