package com.zioanacleto.feedtracker.features.settings.privacy

import android.content.Context
import com.zioanacleto.feedtracker.domain.core.DispatcherProvider
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test

class AndroidSessionExportSharerTest {

    @Test
    fun shareTextFileWritesTheExportOnTheIoDispatcher() = runTest {
        val dispatchers = IoRecordingDispatcherProvider()
        val sharer = AndroidSessionExportSharer(mockk<Context>(relaxed = true), dispatchers)

        val failure = runCatching {
            sharer.shareTextFile("feedtracker-sessions.csv", "text/csv", "initials,notes")
        }.exceptionOrNull()

        dispatchers.ioCalled shouldBe true
        failure?.message shouldBe "io"
    }

    @Test
    fun writeCachedExportReplacesPreviousExports() {
        val cacheDir = File(System.getProperty("java.io.tmpdir"), "feedtracker-export-test-${System.nanoTime()}")
        val previous = File(cacheDir, "exports/feedtracker-sessions.json").apply {
            parentFile?.mkdirs()
            writeText("old")
        }

        val written = writeCachedExport(cacheDir, "feedtracker-sessions.csv", "initials,notes")

        previous.exists() shouldBe false
        written.readText() shouldBe "initials,notes"
        cacheDir.deleteRecursively()
    }

    @Test
    fun deleteCachedExportsRemovesTheExportDirectory() {
        val cacheDir = File(System.getProperty("java.io.tmpdir"), "feedtracker-export-test-${System.nanoTime()}")
        File(cacheDir, "exports/feedtracker-sessions.csv").apply {
            parentFile?.mkdirs()
            writeText("notes")
        }

        deleteCachedExports(cacheDir)

        File(cacheDir, "exports").exists() shouldBe false
        cacheDir.deleteRecursively()
    }
}

private class IoRecordingDispatcherProvider : DispatcherProvider {
    var ioCalled: Boolean = false

    override fun io(): CoroutineDispatcher = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            ioCalled = true
            throw IllegalStateException("io")
        }
    }

    override fun main(): CoroutineDispatcher = error("unused")

    override fun default(): CoroutineDispatcher = error("unused")

    override fun unconfined(): CoroutineDispatcher = error("unused")
}
