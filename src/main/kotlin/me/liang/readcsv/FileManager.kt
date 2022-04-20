package me.liang.readcsv

import java.awt.Component
import java.io.File
import javax.swing.JFileChooser


object FileManager {
    fun readCSVFiles(path: String, tableName: String, createTableIfNotExist: Boolean, cb: ((String) -> Unit)?) {
        val dir = File(path)
        if(!dir.exists()) {
            cb?.invoke("No such a directory in $path")
            return
        }
        dir.walk().filter { it.isFile && it.name.startsWith("part-") }.forEach { file ->
            readFileToTable(file, tableName)
        }
    }

    fun readFileToTable(file: File, tableName: String) {
        var targetFile: File = file
        if(file.extension=="gz") {
            targetFile = File(file.parent, file.nameWithoutExtension)
            GZipHelper.unzip(file, targetFile, true)
        }
        SQLHelper.importCsvFileToTable(targetFile.canonicalPath, tableName)
    }

    fun openFileChooser(path: String, parent: Component): String? {
        with(JFileChooser()) {
            currentDirectory = File(path).takeIf { it.exists() && it.isDirectory } ?: File(".")
            fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
            val returnVal = showOpenDialog(parent)
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                 return selectedFile.canonicalPath
            }
        }
        return null
    }
}