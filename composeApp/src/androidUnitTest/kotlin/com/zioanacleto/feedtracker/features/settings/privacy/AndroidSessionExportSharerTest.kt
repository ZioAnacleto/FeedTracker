package com.zioanacleto.feedtracker.features.settings.privacy

import android.content.Context
import com.zioanacleto.feedtracker.domain.core.DispatcherProvider
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test

class AndroidSessionExportSharerTest {

    @Test
    fun shareTextFileWritesTheExportOnTheIoDispatcher() = runTest {
        val cacheDir = File(System.getProperty("java.io.tmpdir"), "feedtracker-export-test-${System.nanoTime()}")
        val context = mockk<Context>(relaxed = true)
        every { context.cacheDir } returns cacheDir
        val dispatchers = IoRecordingDispatcherProvider()
        val sharer = AndroidSessionExportSharer(context, dispatchers)

        runCatching {
            sharer.shareTextFile("feedtracker-sessions.csv", "text/csv", "initials,notes")
        }

        dispatchers.ioCalled shouldBe true
        File(cacheDir, "exports/feedtracker-sessions.csv").readText() shouldBe "initials,notes"
        cacheDir.deleteRecursively()
    }
}

private class IoRecordingDispatcherProvider : DispatcherProvider {
    var ioCalled: Boolean = false

    override fun io(): CoroutineDispatcher {
        ioCalled = true
        return Dispatchers.Unconfined
    }

    override fun main(): CoroutineDispatcher = Dispatchers.Unconfined

    override fun default(): CoroutineDispatcher = Dispatchers.Unconfined

    override fun unconfined(): CoroutineDispatcher = Dispatchers.Unconfined
}
