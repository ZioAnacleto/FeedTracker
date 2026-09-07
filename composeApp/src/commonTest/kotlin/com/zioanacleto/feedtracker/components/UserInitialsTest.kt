package com.zioanacleto.feedtracker.components

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class UserInitialsTest {

    @Test
    fun usesFirstAndLastNameLetters() {
        userInitials("Mario", "Rossi") shouldBe "MR"
    }

    @Test
    fun usesSingleAvailableName() {
        userInitials("Mario", "") shouldBe "M"
        userInitials("", "Rossi") shouldBe "R"
    }

    @Test
    fun fallsBackToEmailWhenNamesAreBlank() {
        userInitials("", "", "mario@example.com") shouldBe "M"
    }

    @Test
    fun fallsBackToQuestionMarkWhenNothingIsAvailable() {
        userInitials("  ", "", "") shouldBe "?"
    }
}
