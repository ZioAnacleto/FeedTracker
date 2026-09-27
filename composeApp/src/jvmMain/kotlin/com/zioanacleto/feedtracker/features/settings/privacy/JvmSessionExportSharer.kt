package com.zioanacleto.feedtracker.features.settings.privacy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

class JvmSessionExportSharer : SessionExportSharer {
    override val showsSeparateSaveActions: Boolean = false

    override suspend fun shareTextFile(fileName: String, mimeType: String, content: String) {
        saveTextFile(fileName, mimeType, content)
    }

    override suspend fun saveTextFile(fileName: String, mimeType: String, content: String) {
        val selectedFile = withContext(Dispatchers.Main) {
            val extension = fileName.substringAfterLast('.')
            val chooser = JFileChooser().apply {
                selectedFile = File(fileName)
                fileFilter = FileNameExtensionFilter(extension, extension)
            }
            val result = chooser.showSaveDialog(null)
            chooser.selectedFile.takeIf { result == JFileChooser.APPROVE_OPTION }
        } ?: return
        withContext(Dispatchers.IO) {
            selectedFile.writeText(content)
        }
    }
}
