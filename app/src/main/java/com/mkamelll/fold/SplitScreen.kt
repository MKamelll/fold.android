package com.mkamelll.fold

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SplitScreen(modifier: Modifier = Modifier) {
    var file by remember { mutableStateOf<Uri?>(null) }
    val pages = remember { mutableListOf<Bitmap>() }
    var isSplitting by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        file = uri
        uri?.let {
            scope.launch(Dispatchers.IO) {
                pages.addAll(it.renderAllPages(context))
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(visible = (file == null && !isSplitting)) {
                FloatingActionButton(
                    onClick = {
                        launcher.launch(arrayOf("application/pdf"))
                    }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "floating add file button")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerpadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerpadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                enabled = (file != null && !isSplitting),
                value = input,
                onValueChange = { input = it },
                placeholder = { Text("pages(i.e 1,1-5,4)") }
            )
        }
    }
}