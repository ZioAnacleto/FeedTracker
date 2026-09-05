package com.zioanacleto.feedtracker.components

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt

internal const val MINUTE_MS = 60L * 1000L
internal const val MINUTES_PER_REVOLUTION = 60
internal const val MIN_DURATION_MS = MINUTE_MS
internal const val MAX_DURATION_MINUTES = 12 * 60
internal const val MAX_DURATION_MS = MAX_DURATION_MINUTES * MINUTE_MS
internal const val DURATION_MS_PER_REVOLUTION = MINUTES_PER_REVOLUTION * MINUTE_MS

internal fun durationMsFromTurns(turns: Float): Long {
    val minutes = (turns * MINUTES_PER_REVOLUTION).roundToInt()
        .coerceIn(1, MAX_DURATION_MINUTES)
    return minutes * MINUTE_MS
}

internal fun turnsFromDurationMs(durationMs: Long): Float =
    durationMs.coerceIn(MIN_DURATION_MS, MAX_DURATION_MS).toFloat() / DURATION_MS_PER_REVOLUTION

internal fun shortestAngleDelta(fromDegrees: Float, toDegrees: Float): Float {
    var delta = toDegrees - fromDegrees
    if (delta > 180f) delta -= 360f
    if (delta < -180f) delta += 360f
    return delta
}

internal fun pointerAngleDegrees(x: Float, y: Float, centerX: Float, centerY: Float): Float {
    val radians = atan2(y - centerY, x - centerX)
    val degreesFromThreeOClock = radians * 180f / PI.toFloat()
    return (degreesFromThreeOClock + 90f + 360f) % 360f
}

internal fun formatDurationHm(durationMs: Long): String {
    val totalMinutes = (durationMs.coerceAtLeast(0L) / MINUTE_MS)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val hoursAndMinutes = listOf(hours, minutes).joinToString(":") { it.toString().padStart(2, '0') }
    return "$hoursAndMinutes:00"
}

internal fun civilDateTimeToMinutes(date: CivilDate, hour: Int, minute: Int): Long {
    val utcMidnightMinutes = utcEpochMillisFromCivilDate(date) / MINUTE_MS
    return utcMidnightMinutes + hour * 60L + minute
}

internal fun isPastSessionInRange(date: CivilDate, hour: Int, minute: Int, durationMs: Long, nowMillis: Long): Boolean {
    val durationMinutes = durationMs / MINUTE_MS
    if (durationMinutes < 1L) return false
    val startMinutes = civilDateTimeToMinutes(date, hour, minute)
    val nowParts = localDateTimeFromEpochMillis(nowMillis)
    val nowMinutes = civilDateTimeToMinutes(nowParts.date, nowParts.hour, nowParts.minute)
    return startMinutes <= nowMinutes && startMinutes + durationMinutes <= nowMinutes
}

internal fun startPartsKeepingSessionInPast(
    date: CivilDate,
    hour: Int,
    minute: Int,
    durationMs: Long,
    nowMillis: Long,
): LocalDateTimeParts {
    if (isPastSessionInRange(date, hour, minute, durationMs, nowMillis)) {
        return LocalDateTimeParts(date, hour, minute)
    }
    val clampedDuration = durationMs.coerceAtLeast(MIN_DURATION_MS)
    return localDateTimeFromEpochMillis((nowMillis - clampedDuration).coerceAtLeast(0L))
}

internal fun startPartsAfterDurationChange(
    date: CivilDate,
    hour: Int,
    minute: Int,
    previousDurationMs: Long,
    newDurationMs: Long,
    nowMillis: Long,
): LocalDateTimeParts {
    if (newDurationMs <= previousDurationMs) {
        return LocalDateTimeParts(date, hour, minute)
    }
    return startPartsKeepingSessionInPast(date, hour, minute, newDurationMs, nowMillis)
}

internal fun isPastTrackingSaveEnabled(name: String, surname: String, birthDate: String, isPastSession: Boolean): Boolean =
    name.isNotEmpty() && surname.isNotEmpty() && birthDate.length == 10 && isPastSession

internal fun isSelectablePastUtcDate(utcTimeMillis: Long, nowMillis: Long): Boolean = utcTimeMillis <= nowMillis
