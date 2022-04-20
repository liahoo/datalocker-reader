package me.liang.readcsv

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import java.util.*


@Composable
fun DBInfo() {
    var url by remember { mutableStateOf("jdbc:mysql://localhost:3306/testcsv") }
    var user by remember { mutableStateOf("root") }
    var password by remember { mutableStateOf("!Zxcv1234") }
    var connectingDb by remember { mutableStateOf(false) }
    val messageList = remember { mutableStateListOf<String>() }
    LaunchedEffect(connectingDb) {
        if (connectingDb) {
            connectingDb = false
            SQLHelper.connect(url, user, password) {
                messageList.add(dataFormat.format(Date()) + ": " + it)
            }
        }
    }
    Column {
        Row {
            TextField(
                value = url,
                onValueChange = {
                    url = it
                },
                label = { Text("DB URL") },
                modifier = Modifier.weight(1.0f),
            )
            Spacer(modifier = Modifier.width(Dp(16f)))
            TextField(
                value = user,
                onValueChange = {
                    user = it
                },
                label = { Text("username") },
                modifier = Modifier.weight(1.0f),
            )
            Spacer(modifier = Modifier.width(Dp(16f)))
            TextField(
                value = password,
                onValueChange = { changedValue ->
                    password = changedValue
                },
                label = { Text("password") },
                modifier = Modifier.weight(1.0f),
                visualTransformation = PasswordVisualTransformation()
            )
        }
        Spacer(modifier = Modifier.height(Dp(5f)))

        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = {
                messageList.add(dataFormat.format(Date()) +": DB Connecting...")
                connectingDb = true
//                LaunchedEffect(Unit) {
//                    SQLHelper.connect(url, user, password) {
//                        Thread.sleep(1000L)
//                        messageList.add(dataFormat.format(Date()) + ": " + it)
//                    }
//                }
            }
        ){
            Text("Connect")
        }
        Spacer(modifier = Modifier.height(Dp(5f)))
        repeat(messageList.size) { index ->
            Row {
                Text(text = messageList[messageList.size-index-1])
            }
        }

//        Surface {
//            LazyColumn {
//                items(messageList) { message ->
//                    Row {
//                        Text(text = message)
//                    }
//                }
//            }
//        }
    }
}

