package com.zioanacleto.feedtracker.data.local

internal expect fun readTextFile(path: String): String?

internal expect fun writeTextFile(path: String, content: String)

internal expect fun deleteTextFile(path: String)
