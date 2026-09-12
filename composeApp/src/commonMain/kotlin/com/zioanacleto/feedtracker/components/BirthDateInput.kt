package com.zioanacleto.feedtracker.components

import com.zioanacleto.feedtracker.domain.preferences.DateDisplayFormat

internal data class BirthDateChange(val text: String, val placeCursorAtEnd: Boolean, val complete: Boolean)

internal fun birthDateChange(
    oldText: String,
    newText: String,
    format: DateDisplayFormat = DateDisplayFormat.DAY_MONTH_YEAR,
): BirthDateChange? {
    if (newText.length > 10) return null
    if (newText.length > oldText.length) {
        val formatted = formatBirthDateDigits(newText.filter { it.isDigit() }, format)
        return BirthDateChange(
            text = formatted,
            placeCursorAtEnd = true,
            complete = formatted.length == 10,
        )
    }
    return BirthDateChange(text = newText, placeCursorAtEnd = false, complete = false)
}

internal fun formatBirthDateDigits(digits: String, format: DateDisplayFormat = DateDisplayFormat.DAY_MONTH_YEAR): String {
    val slashAfter = when (format) {
        DateDisplayFormat.YEAR_MONTH_DAY -> setOf(3, 5)
        else -> setOf(1, 3)
    }
    val lastAutoSlashIndex = slashAfter.max()
    return buildString {
        for (i in digits.indices) {
            append(digits[i])
            if (i in slashAfter) {
                val isLastTypedDigit = i == digits.lastIndex
                if ((isLastTypedDigit && i < lastAutoSlashIndex + 1) || i < digits.lastIndex) {
                    append("/")
                }
            }
        }
    }
}
