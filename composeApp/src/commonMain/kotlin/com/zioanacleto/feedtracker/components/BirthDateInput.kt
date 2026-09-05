package com.zioanacleto.feedtracker.components

internal data class BirthDateChange(val text: String, val placeCursorAtEnd: Boolean, val complete: Boolean)

internal fun birthDateChange(oldText: String, newText: String): BirthDateChange? {
    if (newText.length > 10) return null
    if (newText.length > oldText.length) {
        val formatted = formatBirthDateDigits(newText.filter { it.isDigit() })
        return BirthDateChange(
            text = formatted,
            placeCursorAtEnd = true,
            complete = newText.length == 10,
        )
    }
    return BirthDateChange(text = newText, placeCursorAtEnd = false, complete = false)
}

internal fun formatBirthDateDigits(digits: String): String = buildString {
    for (i in digits.indices) {
        append(digits[i])
        if ((i == 1 || i == 3) && i == digits.lastIndex && i < 4) {
            append("/")
        } else if ((i == 1 || i == 3) && i < digits.lastIndex) {
            append("/")
        }
    }
}
