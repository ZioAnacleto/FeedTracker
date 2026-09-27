package com.zioanacleto.feedtracker.features.settings.privacy

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.popoverPresentationController

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosSessionExportSharer : SessionExportSharer {
    override suspend fun shareTextFile(fileName: String, mimeType: String, content: String) {
        presentFile(fileName, content, share = true)
    }

    override suspend fun saveTextFile(fileName: String, mimeType: String, content: String): SessionExportSaveResult {
        presentFile(fileName, content, share = false)
        return SessionExportSaveResult.SAVED
    }

    private fun presentFile(fileName: String, content: String, share: Boolean) {
        val path = NSTemporaryDirectory().trimEnd('/') + "/" + fileName
        NSString.create(string = content).writeToFile(
            path,
            atomically = true,
            encoding = NSUTF8StringEncoding,
            error = null,
        )
        val fileUrl = NSURL.fileURLWithPath(path)
        val presenter = currentViewController()
        if (share) {
            val activityViewController = UIActivityViewController(
                activityItems = listOf(fileUrl),
                applicationActivities = null,
            )
            activityViewController.popoverPresentationController?.sourceView = presenter.view
            presenter.presentViewController(activityViewController, animated = true, completion = null)
        } else {
            val picker = UIDocumentPickerViewController(forExportingURLs = listOf(fileUrl), asCopy = true)
            presenter.presentViewController(picker, animated = true, completion = null)
        }
    }
}

private fun currentViewController(): UIViewController {
    val window = UIApplication.sharedApplication.windows
        .mapNotNull { it as? UIWindow }
        .firstOrNull { it.isKeyWindow() }
        ?: UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow
    var controller = requireNotNull(window?.rootViewController) { "No root view controller" }
    while (controller.presentedViewController != null) {
        controller = requireNotNull(controller.presentedViewController)
    }
    return controller
}
