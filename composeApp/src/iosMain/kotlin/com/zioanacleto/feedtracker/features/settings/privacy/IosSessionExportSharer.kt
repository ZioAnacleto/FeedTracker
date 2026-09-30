package com.zioanacleto.feedtracker.features.settings.privacy

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.useContents
import kotlinx.cinterop.value
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosSessionExportSharer : SessionExportSharer {
    private val pickerSession = DocumentExportPickerSession()
    private var pickerDelegate: NSObject? = null

    override suspend fun shareTextFile(fileName: String, mimeType: String, content: String) {
        val fileUrl = writeTemporaryExportFile(fileName, content)
        val presenter = currentViewController()
        val activityViewController = UIActivityViewController(
            activityItems = listOf(fileUrl),
            applicationActivities = null,
        )
        anchorPopover(activityViewController, presenter)
        presenter.presentViewController(activityViewController, animated = true, completion = null)
    }

    override suspend fun saveTextFile(fileName: String, mimeType: String, content: String): SessionExportSaveResult {
        val fileUrl = writeTemporaryExportFile(fileName, content)
        val presenter = currentViewController()
        val delegate = IosExportDocumentPickerDelegate(pickerSession)
        val picker = UIDocumentPickerViewController(forExportingURLs = listOf(fileUrl), asCopy = true)
        picker.delegate = delegate
        pickerDelegate = delegate
        return try {
            awaitDocumentExport(
                session = pickerSession,
                onCancel = {
                    pickerDelegate = null
                    picker.dismissViewControllerAnimated(true, completion = null)
                },
            ) {
                presenter.presentViewController(picker, animated = true, completion = null)
            }
        } finally {
            pickerDelegate = null
        }
    }

    override suspend fun clearCachedExports() {
        deleteCachedExportFiles()
    }

    private fun writeTemporaryExportFile(fileName: String, content: String): NSURL {
        deleteCachedExportFiles()
        val path = NSTemporaryDirectory().trimEnd('/') + "/" + fileName
        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            val written = NSString.create(string = content).writeToFile(
                path = path,
                atomically = true,
                encoding = NSUTF8StringEncoding,
                error = error.ptr,
            )
            val failure = exportWriteFailureMessage(written, error.value?.localizedDescription)
            if (failure != null) {
                throw IllegalStateException(failure)
            }
        }
        return NSURL.fileURLWithPath(path)
    }

    private fun deleteCachedExportFiles() {
        val directory = NSTemporaryDirectory().trimEnd('/')
        SessionExportFormat.entries.forEach { format ->
            NSFileManager.defaultManager.removeItemAtPath("$directory/${format.fileName}", null)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class IosExportDocumentPickerDelegate(private val session: DocumentExportPickerSession) :
    NSObject(),
    UIDocumentPickerDelegateProtocol {
    override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
        session.onDocumentsPicked()
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        session.onCancelled()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun anchorPopover(activityViewController: UIActivityViewController, presenter: UIViewController) {
    val popover = activityViewController.popoverPresentationController ?: return
    val view = presenter.view
    popover.sourceView = view
    popover.sourceRect = view.bounds.useContents {
        CGRectMake(size.width / 2.0, size.height / 2.0, 1.0, 1.0)
    }
}

private fun currentViewController(): UIViewController {
    val window = keyWindow()
    var controller = requireNotNull(window?.rootViewController) { "No root view controller" }
    while (controller.presentedViewController != null) {
        controller = requireNotNull(controller.presentedViewController)
    }
    return controller
}

private fun keyWindow(): UIWindow? {
    val scenes = UIApplication.sharedApplication.connectedScenes.mapNotNull { it as? UIWindowScene }
    val activeScene = scenes.firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
    return activeScene?.keyWindow
        ?: scenes.firstNotNullOfOrNull { it.keyWindow }
}
