package com.zioanacleto.feedtracker.domain.preferences

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class AppearanceModeTest {

    @Test
    fun systemFollowsThePlatform() {
        AppearanceMode.SYSTEM.isDarkTheme(systemDark = true) shouldBe true
        AppearanceMode.SYSTEM.isDarkTheme(systemDark = false) shouldBe false
    }

    @Test
    fun explicitModesIgnoreThePlatform() {
        AppearanceMode.DARK.isDarkTheme(systemDark = false) shouldBe true
        AppearanceMode.LIGHT.isDarkTheme(systemDark = true) shouldBe false
    }
}
