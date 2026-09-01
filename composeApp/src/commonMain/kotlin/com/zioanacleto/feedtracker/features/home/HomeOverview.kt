package com.zioanacleto.feedtracker.features.home

import com.zioanacleto.feedtracker.domain.TrackingSessionModel
import kotlin.math.max

data class HomeStats(
    val sessionsToday: Int,
    val durationTodayMs: Long,
    val sessionsLast7Days: Int,
    val averageDurationLast7DaysMs: Long?,
) {
    companion object {
        val Empty = HomeStats(
            sessionsToday = 0,
            durationTodayMs = 0L,
            sessionsLast7Days = 0,
            averageDurationLast7DaysMs = null,
        )
    }
}

data class HomeOverview(val recentSessions: List<TrackingSessionModel>, val stats: HomeStats) {
    val lastSession: TrackingSessionModel? get() = recentSessions.firstOrNull()
}

sealed interface TimeAgo {
    data object JustNow : TimeAgo
    data class Minutes(val minutes: Int) : TimeAgo
    data class Hours(val hours: Int, val minutes: Int) : TimeAgo
    data class Days(val days: Int) : TimeAgo
}

internal fun buildHomeOverview(sessions: List<TrackingSessionModel>, nowMillis: Long, startOfTodayMillis: Long): HomeOverview {
    val newestFirst = compareByDescending<TrackingSessionModel> { it.sessionStartTime }
        .thenByDescending { it.sessionEndTime }
        .thenBy { it.id }
    return HomeOverview(
        recentSessions = sessions.sortedWith(newestFirst).take(RECENT_SESSIONS_LIMIT),
        stats = buildHomeStats(sessions, nowMillis, startOfTodayMillis),
    )
}

internal fun buildHomeStats(sessions: List<TrackingSessionModel>, nowMillis: Long, startOfTodayMillis: Long): HomeStats {
    if (sessions.isEmpty()) return HomeStats.Empty

    val startOfLast7Days = startOfTodayMillis - SIX_DAYS_MS
    var sessionsToday = 0
    var durationTodayMs = 0L
    var sessionsLast7Days = 0
    var durationLast7DaysMs = 0L

    sessions.forEach { session ->
        val started = session.sessionStartTime
        if (started > nowMillis) return@forEach
        val duration = sessionDurationMs(session)
        if (started >= startOfTodayMillis) {
            sessionsToday += 1
            durationTodayMs += duration
        }
        if (started >= startOfLast7Days) {
            sessionsLast7Days += 1
            durationLast7DaysMs += duration
        }
    }

    val average = if (sessionsLast7Days == 0) {
        null
    } else {
        durationLast7DaysMs / sessionsLast7Days
    }
    return HomeStats(
        sessionsToday = sessionsToday,
        durationTodayMs = durationTodayMs,
        sessionsLast7Days = sessionsLast7Days,
        averageDurationLast7DaysMs = average,
    )
}

internal fun timeAgoSince(fromMillis: Long, nowMillis: Long): TimeAgo {
    val elapsedSeconds = ((nowMillis - fromMillis).coerceAtLeast(0L)) / 1000
    return when {
        elapsedSeconds < 60 -> TimeAgo.JustNow
        elapsedSeconds < 3_600 -> TimeAgo.Minutes((elapsedSeconds / 60).toInt())
        elapsedSeconds < 86_400 -> {
            val hours = (elapsedSeconds / 3_600).toInt()
            val minutes = ((elapsedSeconds % 3_600) / 60).toInt()
            TimeAgo.Hours(hours, minutes)
        }
        else -> TimeAgo.Days((elapsedSeconds / 86_400).toInt())
    }
}

internal fun sessionEndedAt(session: TrackingSessionModel): Long = max(session.sessionStartTime, session.sessionEndTime)

internal fun sessionDurationMs(session: TrackingSessionModel): Long = (session.sessionEndTime - session.sessionStartTime).coerceAtLeast(0L)

internal const val RECENT_SESSIONS_LIMIT = 5
private const val SIX_DAYS_MS = 6 * 24 * 60 * 60 * 1000L
