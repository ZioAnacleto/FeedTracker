package com.zioanacleto.feedtracker.components

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class BirthDateInputTest {

    @Test
    fun insertsSlashesWhileTypingDayAndMonth() {
        formatBirthDateDigits("1") shouldBe "1"
        formatBirthDateDigits("12") shouldBe "12/"
        formatBirthDateDigits("123") shouldBe "12/3"
        formatBirthDateDigits("1234") shouldBe "12/34/"
        formatBirthDateDigits("12345678") shouldBe "12/34/5678"
    }

    @Test
    fun formatsAddedDigitsAndCompletesAtTenCharacters() {
        birthDateChange(oldText = "12/3", newText = "12/34") shouldBe BirthDateChange(
            text = "12/34/",
            placeCursorAtEnd = true,
            complete = false,
        )
        birthDateChange(oldText = "12/34/567", newText = "12/34/5678") shouldBe BirthDateChange(
            text = "12/34/5678",
            placeCursorAtEnd = true,
            complete = true,
        )
    }

    @Test
    fun allowsDeletionWithoutReformatting() {
        birthDateChange(oldText = "12/34/", newText = "12/34") shouldBe BirthDateChange(
            text = "12/34",
            placeCursorAtEnd = false,
            complete = false,
        )
    }

    @Test
    fun rejectsInputLongerThanTenCharacters() {
        birthDateChange(oldText = "12/34/5678", newText = "12/34/56789").shouldBeNull()
    }
}
