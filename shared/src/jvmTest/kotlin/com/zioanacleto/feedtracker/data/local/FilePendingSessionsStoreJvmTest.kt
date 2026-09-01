package com.zioanacleto.feedtracker.data.local

import com.zioanacleto.feedtracker.data.datasources.TrackingSessionLocalDataSource
import com.zioanacleto.feedtracker.testutil.trackingSession
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.Test

class FilePendingSessionsStoreJvmTest {

    @Test
    fun reloadsSessionsFromDisk() = runTest {
        val file = Files.createTempFile("pending-sessions", ".json").toFile()
        val store = JsonPendingSessionsStore(FileTextStore(file.absolutePath))
        val session = trackingSession("disk-1")

        TrackingSessionLocalDataSource(store).saveNewTrackingSession(session)

        TrackingSessionLocalDataSource(
            JsonPendingSessionsStore(FileTextStore(file.absolutePath)),
        ).getTrackingSessions() shouldBe listOf(session)
    }

    @Test
    fun deletesFileWhenNoPendingSessionsRemain() = runTest {
        val file = Files.createTempFile("pending-sessions", ".json").toFile()
        val store = JsonPendingSessionsStore(FileTextStore(file.absolutePath))
        val session = trackingSession("disk-1")
        val dataSource = TrackingSessionLocalDataSource(store)

        dataSource.saveNewTrackingSession(session)
        file.exists() shouldBe true

        dataSource.deleteTrackingSession(session.id)

        file.exists() shouldBe false
        TrackingSessionLocalDataSource(
            JsonPendingSessionsStore(FileTextStore(file.absolutePath)),
        ).getTrackingSessions() shouldBe emptyList()
    }
}
