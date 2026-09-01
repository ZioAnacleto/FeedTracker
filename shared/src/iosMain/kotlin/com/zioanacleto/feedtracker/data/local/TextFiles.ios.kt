@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.zioanacleto.feedtracker.data.local

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

internal actual fun readTextFile(path: String): String? = NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)

internal actual fun writeTextFile(path: String, content: String) {
    NSString.create(string = content).writeToFile(
        path,
        atomically = true,
        encoding = NSUTF8StringEncoding,
        error = null,
    )
}

internal actual fun deleteTextFile(path: String) {
    NSFileManager.defaultManager.removeItemAtPath(path, null)
}
