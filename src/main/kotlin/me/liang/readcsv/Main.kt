// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package me.liang.readcsv

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeDialog
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import java.awt.FileDialog
import java.awt.Frame
import java.awt.event.KeyEvent
import java.text.SimpleDateFormat
import java.util.*

val dataFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

@Composable
@Preview
fun App(scope: WindowScope) {
    var path by remember { mutableStateOf("/Users/liang/Data/mysql/h=0") }
    var tableName by remember { mutableStateOf("inapps") }
    var createTableIfNotExist by remember { mutableStateOf(true) }
    var deleteUnzippedFile by remember { mutableStateOf(false) }
    val messageList = remember { mutableStateListOf<String>() }
    var openFileExplorer by remember { mutableStateOf(false) }
    var fileNamePrefix by remember { mutableStateOf("part-") }
    var fileNameSuffix by remember { mutableStateOf(".gz") }
    var folderMaxDepth by remember { mutableStateOf(4) }
    val scrollState = rememberScrollState()
    var readingStarted by remember { mutableStateOf(false) }
    fun sendMessage(msg: String) {
        messageList.add(dataFormat.format(Date()) + ": " + msg)
    }

    var showDialog by remember { mutableStateOf(false) }


    LaunchedEffect(readingStarted) {
        if (readingStarted) {
            showDialog = true
            readingStarted = false
            try {
                FileManager.readFiles(path, folderMaxDepth) { file ->
                    file.isFile && (fileNamePrefix.isEmpty() || file.name.startsWith(fileNamePrefix))
                            && (fileNameSuffix.isEmpty() || file.name.endsWith(fileNameSuffix))
                }.takeIf { it.any() }?.forEach { file ->
                    FileManager.unzipFile(file) { unzippedFile, isNew ->
                        SQLHelper.importFileToTable(unzippedFile.canonicalPath, tableName)
                        sendMessage("Imported ${file.canonicalPath} to $tableName")
                        if (isNew && deleteUnzippedFile) {
                            unzippedFile.delete()
                        }
                    }
                } ?: sendMessage("No file was found like $fileNamePrefix*$fileNameSuffix in folder of $path")
            } catch (e: Exception) {
                e.printStackTrace()
                messageList.add(e.localizedMessage)
            } finally {
                showDialog = false
            }
        }
    }
    if (openFileExplorer) {
        FileExplorer(
            onCloseRequest = { selectedFilePath ->
                openFileExplorer = false
                path = selectedFilePath ?: ""
            }
        )
    }
    if (showDialog) {
        Dialog(
            visible = showDialog,
            onPreviewKeyEvent = { false },
            onKeyEvent = { println(it.toString()); false },
            create = { ComposeDialog() },
            dispose = { showDialog = false },
            update = {}
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(500.dp)
                    .height(500.dp)
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
            ) {
                CircularProgressIndicator()
            }
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().background(color = Color(0xFFEFEFEF)).padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(
                        text = "A tool to import csv files to database!",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.h6
                    )
                }
                Spacer(modifier = Modifier.height(Dp(16f)))
                // DB Info
                Card(modifier = Modifier.padding(8.dp)) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        DBInfo()
                    }
                }
                Spacer(modifier = Modifier.height(Dp(16f)))
                Card {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // File Chooser
                        Row {
                            TextField(
                                value = path,
                                onValueChange = {
                                    path = it
                                },
                                label = { Text("input your path or click Browser to select a folder") },
                                modifier = Modifier.weight(1.0f),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Button(
                                modifier = Modifier.align(Alignment.Bottom),
                                onClick = {
                                    // Browser File
                                    FileManager.openFileChooser(path, scope.window)?.let {
                                        path = it
                                    }
                                }
                            ) {
                                Text("Browser")
                            }
                            Spacer(modifier = Modifier.width(4.dp))

                        }
                        Spacer(modifier = Modifier.height(Dp(4f)))
                        // File options
                        Row {
                            TextField(
                                value = folderMaxDepth.toString(),
                                onValueChange = {
                                    folderMaxDepth = it.toIntOrNull() ?: 4
                                },
                                label = { Text("Folder max depth") },
                                modifier = Modifier.weight(1.0f),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            TextField(
                                value = fileNamePrefix,
                                onValueChange = {
                                    fileNamePrefix = it
                                },
                                label = { Text("Filename prefix") },
                                modifier = Modifier.weight(1.0f),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            TextField(
                                value = fileNameSuffix,
                                onValueChange = {
                                    fileNameSuffix = it
                                },
                                label = { Text("Filename suffix") },
                                modifier = Modifier.weight(1.0f),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            ) {
                                Text("Delete unzipped files")
                                Checkbox(checked = deleteUnzippedFile, onCheckedChange = {
                                    deleteUnzippedFile = it
                                })
                            }
                        }
                        Spacer(modifier = Modifier.height(Dp(4f)))
                        // Table name
                        Row {
                            TextField(
                                value = tableName,
                                onValueChange = {
                                    tableName = it
                                },
                                label = { Text("Table name in DB") },
                                modifier = Modifier.weight(1.0f),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.align(Alignment.CenterVertically),
                            ) {
                                Text("Create if not exist")
                                Checkbox(checked = createTableIfNotExist, onCheckedChange = {
                                    createTableIfNotExist = it
                                })
                            }
                            Spacer(modifier = Modifier.width(4.dp))

                        }
                        // Read Button
                        Row {
                            Button(
                                modifier = Modifier.align(Alignment.Bottom),
                                onClick = {
                                    messageList.add(dataFormat.format(Date()) + ": reading started...")
                                    readingStarted = true
                                }
                            ) {
                                Text("Read")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Dp(16f)))
                // Message output
                messageList.forEach {
                    Row {
                        Text(text = it)
                    }
                }
//                repeat(messageList.size) { index ->
//                    Row {
//                        Text(text = messageList[messageList.size-index-1])
//                    }
//                }
            }
        }
    }
}


@Composable
private fun FileExplorer(
    parent: Frame? = null,
    onCloseRequest: (result: String?) -> Unit
) = AwtWindow(
    create = {
        object : FileDialog(parent, "Choose a folder", LOAD) {
            override fun setVisible(b: Boolean) {
                super.setVisible(b)
                if (b) onCloseRequest(directory)
            }
        }
    },
    dispose = FileDialog::dispose
)


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "CSV Import Manager",
        state = rememberWindowState(width = 1440.dp, height = 1080.dp)
    ) {
        App(this)
    }

}
