// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package me.liang.readcsv

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.WindowScope
import java.awt.Container
import java.awt.FileDialog
import java.awt.Frame
import java.text.SimpleDateFormat
import java.util.*

val dataFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
@Composable
@Preview
fun App(scope: WindowScope) {
    var path by remember { mutableStateOf("") }
    var tableName by remember { mutableStateOf("") }
    var createTableIfNotExist by remember { mutableStateOf(true) }
    val messageList = remember { mutableStateListOf<String>() }
    var openFileExplorer by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    if(openFileExplorer){
        FileExplorer(
            onCloseRequest = { selectedFilePath ->
                openFileExplorer=false
                path = selectedFilePath ?: ""
            }
        )
    }
    MaterialTheme {
        Surface(modifier = Modifier.height(1000.dp).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().background(color = Color.LightGray).padding(16.dp).verticalScroll(scrollState)) {
                Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(text = "A tool to import csv files to database!", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.h6)
                }
                Spacer(modifier = Modifier.height(Dp(16f)))
                DBInfo()
                Spacer(modifier = Modifier.height(Dp(16f)))
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
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
//                                openFileExplorer = true
                                FileManager.openFileChooser(path, scope.window)?.let {
                                    path = it
                                }
                            }
                        ) {
                            Text("Browser")
                        }
                        Spacer(modifier = Modifier.width(4.dp))

                    }
                    Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        TextField(
                            value = tableName,
                            onValueChange = {
                                tableName = it
                            },
                            label = { Text("Table name in DB") },
                            modifier = Modifier.weight(1.0f),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            modifier = Modifier.align(Alignment.CenterVertically)
//                        ) {
//                            Text("Create if not exist")
//                            Checkbox(checked = createTableIfNotExist, onCheckedChange = {
//                                createTableIfNotExist = it
//                            })
//                        }
//                        Spacer(modifier = Modifier.width(4.dp))

                    }
                    Spacer(modifier = Modifier.height(Dp(5f)))
                    Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Button(
                            modifier = Modifier.align(Alignment.Bottom),
                            onClick = {
                                FileManager.readCSVFiles(path, tableName, createTableIfNotExist) { message ->
                                    messageList.add(dataFormat.format(Date()) +": " +   message)
                                }
                            }
                        ) {
                            Text("Read")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Dp(5f)))
                repeat(messageList.size) { index ->
                    Row {
                        Text(text = messageList[messageList.size-index-1])
                    }
                }

//                Surface {
//                    LazyColumn {
//                        items(messageList) { message ->
//                            Row() {
//                                Text(text = message)
//                            }
//                        }
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
                if(b) onCloseRequest(directory)
            }
        }
    },
    dispose = FileDialog::dispose
)

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "CSV Import Manager",
    ) {
        App(this)
    }

}
