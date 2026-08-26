package com.zioanacleto.feedtracker.data.datasources

import com.zioanacleto.feedtracker.testutil.trackingSession
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class TrackingSessionLocalDataSourceTest {

    @Test
    fun startsEmpty() = runTest {
        TrackingSessionLocalDataSource().getTrackingSessions().shouldBeEmpty()
    }

    @Test
    fun savesAndRetrievesSessions() = runTest {
        val dataSource = TrackingSessionLocalDataSource()
        val first = trackingSession("1", "Mario")
        val second = trackingSession("2", "Luigi")

        dataSource.saveNewTrackingSession(first)
        dataSource.saveNewTrackingSession(second)

        dataSource.getTrackingSessions() shouldBe listOf(first, second)
        dataSource.getTrackingSession("2") shouldBe second
    }

    @Test
    fun replacingSessionWithSameIdKeepsSingleEntry() = runTest {
        val dataSource = TrackingSessionLocalDataSource()
        val original = trackingSession("1", "Mario")
        val updated = trackingSession("1", "Maria")

        dataSource.saveNewTrackingSession(original)
        dataSource.saveNewTrackingSession(updated)

        dataSource.getTrackingSessions() shouldBe listOf(updated)
    }

    @Test
    fun getTrackingSessionThrowsWhenMissing() = runTest {
        shouldThrow<IllegalStateException> {
            TrackingSessionLocalDataSource().getTrackingSession("missing")
        }
    }
}
