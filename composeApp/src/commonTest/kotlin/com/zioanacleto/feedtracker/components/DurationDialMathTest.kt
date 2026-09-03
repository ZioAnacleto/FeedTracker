package com.zioanacleto.feedtracker.components

import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class DurationDialMathTest {

    @Test
    fun formatsHoursAndMinutesWithSecondsFixedAtZero() {
        formatDurationHm(0L) shouldBe "00:00:00"
        formatDurationHm(MINUTE_MS) shouldBe "00:01:00"
        formatDurationHm(61_000L) shouldBe "00:01:00"
        formatDurationHm(3_661_000L) shouldBe "01:01:00"
    }

    @Test
    fun convertsTurnsAndDurationSnappingToMinutes() {
        durationMsFromTurns(0.5f) shouldBe 30L * MINUTE_MS
        turnsFromDurationMs(30L * MINUTE_MS) shouldBe (0.5f plusOrMinus 0.0001f)
        durationMsFromTurns(0f) shouldBe MIN_DURATION_MS
        durationMsFromTurns(20f) shouldBe MAX_DURATION_MS
        durationMsFromTurns(1f / 120f) shouldBe MIN_DURATION_MS
    }

    @Test
    fun shortestAngleDeltaWrapsAroundTwelveOClock() {
        shortestAngleDelta(350f, 10f) shouldBe (20f plusOrMinus 0.01f)
        shortestAngleDelta(10f, 350f) shouldBe (-20f plusOrMinus 0.01f)
        shortestAngleDelta(0f, 90f) shouldBe (90f plusOrMinus 0.01f)
    }

    @Test
    fun pointerAngleIsZeroAtTwelveOClock() {
        pointerAngleDegrees(x = 50f, y = 0f, centerX = 50f, centerY = 50f) shouldBe (0f plusOrMinus 0.1f)
        pointerAngleDegrees(x = 100f, y = 50f, centerX = 50f, centerY = 50f) shouldBe (90f plusOrMinus 0.1f)
    }

    @Test
    fun pastSessionMustEndAtOrBeforeCurrentMinute() {
        val date = CivilDate(2026, 6, 15)
        val noon = localDateTimeToEpochMillis(date, hour = 12, minute = 0)

        isPastSessionInRange(date, hour = 11, minute = 0, durationMs = MINUTE_MS, nowMillis = noon) shouldBe true
        isPastSessionInRange(date, hour = 11, minute = 45, durationMs = 15 * MINUTE_MS, nowMillis = noon) shouldBe true
        isPastSessionInRange(date, hour = 12, minute = 0, durationMs = MINUTE_MS, nowMillis = noon) shouldBe false
        isPastSessionInRange(date, hour = 13, minute = 0, durationMs = MINUTE_MS, nowMillis = noon) shouldBe false
        isPastSessionInRange(date, hour = 11, minute = 0, durationMs = 0L, nowMillis = noon) shouldBe false
    }

    @Test
    fun keepsStartWhenLongerDurationStillEndsInThePast() {
        val date = CivilDate(2026, 6, 15)
        val noon = localDateTimeToEpochMillis(date, hour = 12, minute = 0)

        startPartsKeepingSessionInPast(
            date = date,
            hour = 11,
            minute = 0,
            durationMs = 30 * MINUTE_MS,
            nowMillis = noon,
        ) shouldBe LocalDateTimeParts(date, hour = 11, minute = 0)
    }

    @Test
    fun shiftsStartBackWhenDurationWouldEndInTheFuture() {
        val date = CivilDate(2026, 6, 15)
        val noon = localDateTimeToEpochMillis(date, hour = 12, minute = 0)

        startPartsKeepingSessionInPast(
            date = date,
            hour = 11,
            minute = 50,
            durationMs = 20 * MINUTE_MS,
            nowMillis = noon,
        ) shouldBe LocalDateTimeParts(date, hour = 11, minute = 40)
    }

    @Test
    fun shiftsStartToPreviousDayWhenDurationCrossesMidnight() {
        val date = CivilDate(2026, 6, 15)
        val justAfterMidnight = localDateTimeToEpochMillis(date, hour = 0, minute = 10)

        startPartsKeepingSessionInPast(
            date = date,
            hour = 0,
            minute = 5,
            durationMs = 30 * MINUTE_MS,
            nowMillis = justAfterMidnight,
        ) shouldBe LocalDateTimeParts(CivilDate(2026, 6, 14), hour = 23, minute = 40)
    }
}
