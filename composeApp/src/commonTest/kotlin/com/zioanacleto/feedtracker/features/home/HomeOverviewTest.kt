package com.zioanacleto.feedtracker.features.home

import com.zioanacleto.feedtracker.testutil.sampleSession
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class HomeOverviewTest {

    @Test
    fun emptySessionsProduceEmptyOverview() {
        buildHomeOverview(
            sessions = emptyList(),
            nowMillis = NOW,
            startOfTodayMillis = START_OF_TODAY,
        ) shouldBe HomeOverview(
            recentSessions = emptyList(),
            stats = HomeStats.Empty,
        )
    }

    @Test
    fun lastSessionIsTheNewestByStartTime() {
        val older = sampleSession(id = "older", sessionStartTime = START_OF_TODAY + 1_000L)
        val newer = sampleSession(id = "newer", sessionStartTime = START_OF_TODAY + 2_000L)
        val overview = buildHomeOverview(
            sessions = listOf(older, newer),
            nowMillis = NOW,
            startOfTodayMillis = START_OF_TODAY,
        )

        overview.lastSession shouldBe newer
        overview.recentSessions shouldBe listOf(newer, older)
    }

    @Test
    fun countsTodayAndLast7DaysDurations() {
        val today = sampleSession(
            id = "today",
            sessionStartTime = START_OF_TODAY + 60_000L,
            sessionEndTime = START_OF_TODAY + 180_000L,
        )
        val yesterday = sampleSession(
            id = "yesterday",
            sessionStartTime = START_OF_TODAY - 3_600_000L,
            sessionEndTime = START_OF_TODAY - 3_540_000L,
        )
        val olderThanWeek = sampleSession(
            id = "old",
            sessionStartTime = START_OF_TODAY - (8 * DAY_MS),
            sessionEndTime = START_OF_TODAY - (8 * DAY_MS) + 60_000L,
        )

        buildHomeOverview(
            sessions = listOf(today, yesterday, olderThanWeek),
            nowMillis = NOW,
            startOfTodayMillis = START_OF_TODAY,
        ).stats shouldBe HomeStats(
            sessionsToday = 1,
            durationTodayMs = 120_000L,
            sessionsLast7Days = 2,
            averageDurationLast7DaysMs = 90_000L,
        )
    }

    @Test
    fun recentSessionsKeepsNewestFirstAndLimitsCount() {
        val sessions = (1..7).map { index ->
            sampleSession(id = "s$index", sessionStartTime = START_OF_TODAY + index * 1_000L)
        }

        buildHomeOverview(
            sessions = sessions,
            nowMillis = NOW,
            startOfTodayMillis = START_OF_TODAY,
        ).recentSessions shouldBe sessions.reversed().take(RECENT_SESSIONS_LIMIT)
    }

    @Test
    fun ignoresFutureSessions() {
        val future = sampleSession(
            id = "future",
            sessionStartTime = NOW + 1_000L,
            sessionEndTime = NOW + 2_000L,
        )

        buildHomeStats(
            sessions = listOf(future),
            nowMillis = NOW,
            startOfTodayMillis = START_OF_TODAY,
        ) shouldBe HomeStats.Empty
    }

    @Test
    fun timeAgoBucketsElapsedTime() {
        timeAgoSince(NOW, NOW) shouldBe TimeAgo.JustNow
        timeAgoSince(NOW - 59_000L, NOW) shouldBe TimeAgo.JustNow
        timeAgoSince(NOW - 60_000L, NOW) shouldBe TimeAgo.Minutes(1)
        timeAgoSince(NOW - 3_600_000L, NOW) shouldBe TimeAgo.Hours(1, 0)
        timeAgoSince(NOW - 3_660_000L, NOW) shouldBe TimeAgo.Hours(1, 1)
        timeAgoSince(NOW - DAY_MS, NOW) shouldBe TimeAgo.Days(1)
    }

    companion object {
        private const val DAY_MS = 24 * 60 * 60 * 1000L
        private const val START_OF_TODAY = 1_700_000_000_000L
        private const val NOW = START_OF_TODAY + 12 * 60 * 60 * 1000L
    }
}
