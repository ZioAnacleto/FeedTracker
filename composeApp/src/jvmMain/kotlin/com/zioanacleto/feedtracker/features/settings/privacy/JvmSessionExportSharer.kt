package com.zioanacleto.feedtracker.features.settings.privacy

import feedtracker.composeapp.generated.resources.Res
import feedtracker.composeapp.generated.resources.export_sessions_replace_file
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import java.awt.Component
import java.awt.KeyboardFocusManager
import java.awt.Window
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter

class JvmSessionExportSharer : SessionExportSharer {
    override val showsSeparateSaveActions: Boolean = false

    override suspend fun shareTextFile(fileName: String, mimeType: String, content: String) {
        saveTextFile(fileName, mimeType, content)
    }

    override suspend fun saveTextFile(fileName: String, mimeType: String, content: String): SessionExportSaveResult {
        val extension = fileName.substringAfterLast('.', "")
        val replaceMessage = getString(Res.string.export_sessions_replace_file)
        val selectedFile = withContext(Dispatchers.Main) {
            chooseExportFile(fileName, extension, replaceMessage)
        } ?: return SessionExportSaveResult.CANCELLED
        withContext(Dispatchers.IO) {
            selectedFile.writeText(content)
        }
        return SessionExportSaveResult.SAVED
    }
}

internal fun fileWithExportExtension(file: File, extension: String): File {
    if (extension.isEmpty() || file.name.endsWith(".$extension", ignoreCase = true)) return file
    return File(file.parentFile, "${file.name}.$extension")
}

internal fun saveDialogParent(): Component? {
    val focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager()
    return focusManager.activeWindow ?: Window.getWindows().lastOrNull { it.isShowing }
}

private fun chooseExportFile(suggestedName: String, extension: String, replaceMessage: String): File? {
    val parent = saveDialogParent()
    val chooser = JFileChooser().apply {
        selectedFile = File(suggestedName)
        if (extension.isNotEmpty()) {
            fileFilter = FileNameExtensionFilter(extension, extension)
        }
    }
    while (true) {
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return null
        val selected = chooser.selectedFile ?: return null
        val file = fileWithExportExtension(selected, extension)
        if (!file.exists() || confirmReplace(parent, replaceMessage)) return file
    }
}

private fun confirmReplace(parent: Component?, message: String): Boolean {
    val choice = JOptionPane.showConfirmDialog(parent, message, null, JOptionPane.YES_NO_OPTION)
    return choice == JOptionPane.YES_OPTION
}
