package com.zioanacleto.feedtracker.features.settings.privacy

import io.kotest.matchers.shouldBe
import java.io.File
import kotlin.test.Test

class JvmSessionExportSharerTest {

    @Test
    fun addsTheExportExtensionWhenTheChosenNameOmitsIt() {
        val chosen = File("/tmp/sessions")

        fileWithExportExtension(chosen, "csv").path shouldBe File("/tmp/sessions.csv").path
    }

    @Test
    fun keepsANameThatAlreadyHasTheExportExtension() {
        val chosen = File("/tmp/sessions.CSV")

        fileWithExportExtension(chosen, "csv") shouldBe chosen
    }
}
