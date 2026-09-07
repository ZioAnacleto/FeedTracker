package com.zioanacleto.feedtracker.components

fun userInitials(firstName: String, lastName: String, email: String = ""): String {
    val first = firstName.firstLetter()
    val last = lastName.firstLetter()
    return when {
        first != null && last != null -> "$first$last"
        first != null -> first.toString()
        last != null -> last.toString()
        else -> email.firstLetter()?.toString() ?: "?"
    }
}

private fun String.firstLetter(): Char? = trim().firstOrNull { it.isLetter() }?.uppercaseChar()
