package com.zioanacleto.feedtracker.domain.preferences

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class AppLanguageTest {

    @Test
    fun systemFollowsTheDeviceAndOverridesUseALanguageTag() {
        AppLanguage.SYSTEM.languageTag() shouldBe null
        AppLanguage.ENGLISH.languageTag() shouldBe "en"
        AppLanguage.ITALIAN.languageTag() shouldBe "it"
    }

    @Test
    fun languageTagRoundTripKeepsSystemForUnknownValues() {
        appLanguageFromTag(null) shouldBe AppLanguage.SYSTEM
        appLanguageFromTag("en") shouldBe AppLanguage.ENGLISH
        appLanguageFromTag("it") shouldBe AppLanguage.ITALIAN
        appLanguageFromTag("fr") shouldBe AppLanguage.SYSTEM
        appLanguageFromTag("en").languageTag() shouldBe "en"
        appLanguageFromTag("it").languageTag() shouldBe "it"
    }
}
