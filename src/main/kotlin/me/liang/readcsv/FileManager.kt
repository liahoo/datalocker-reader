package me.liang.readcsv

import java.awt.Component
import java.io.File
import javax.swing.JFileChooser


object FileManager {
    fun readCSVFiles(path: String, tableName: String, createTableIfNotExist: Boolean, cb: ((String) -> Unit)?) {
        readFiles(path,4) { it.isFile && it.name.startsWith("part-") }.forEach { file ->
            readFileToTable(file, tableName)
            cb?.invoke("Imported ${file.canonicalPath} to $tableName")
        }
    }

    fun readFiles(path: String, maxDepth: Int, filter: ((File) -> Boolean)?): Sequence<File> {
        val dir = File(path)
        if(!dir.exists()) {
            throw NoSuchFileException(dir)
        }
        return dir.walk().maxDepth(maxDepth).filter { file ->
            file.isFile && (filter?.invoke(file) ?: true)
        }
    }

    /**
     * Find a new file name like file-1 if the file already exist
     */
    fun ensureTargetFileNotExist(file: File): File {
        val originFileNameWithoutExt = file.nameWithoutExtension
        val ext = file.extension.takeIf { it.isNotEmpty() }?.let { ".$it" } ?: ""
        val folder = file.parent
        var nextNum = 0
        var nextFile = file
        while(nextFile.exists()) {
            nextNum++
            nextFile = File(folder, "$originFileNameWithoutExt-$nextNum$ext")
        }
        return nextFile
    }
    fun unzipFile(file: File, cb: ((File, Boolean) -> Unit)? = null): File {
        var targetFile: File = file
        var isNewFile = false
        if(file.extension=="gz") {
            targetFile = File(file.parent, file.nameWithoutExtension)
            targetFile = ensureTargetFileNotExist(targetFile)
            GZipHelper.unzip(file, targetFile, false)
            isNewFile = true
        }
        cb?.invoke(targetFile, isNewFile)
        return targetFile
    }
    fun readFileToTable(file: File, tableName: String) {
        SQLHelper.importFileToTable(file.canonicalPath, tableName)
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