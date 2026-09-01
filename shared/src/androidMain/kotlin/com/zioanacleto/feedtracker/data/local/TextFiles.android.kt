package com.zioanacleto.feedtracker.data.local

import java.io.File

internal actual fun readTextFile(path: String): String? {
    val file = File(path)
    return if (file.exists()) file.readText() else null
}

internal actual fun writeTextFile(path: String, content: String) {
    val file = File(path)
    file.parentFile?.mkdirs()
    val tmp = File(file.parentFile, "${file.name}.tmp")
    tmp.writeText(content)
    if (!tmp.renameTo(file)) {
        file.writeText(content)
        tmp.delete()
    }
}

internal actual fun deleteTextFile(path: String) {
    File(path).delete()
}
