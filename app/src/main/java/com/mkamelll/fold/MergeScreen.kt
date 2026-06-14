package com.mkamelll.fold

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.util.UUID

data class FileItem(val id: UUID = UUID.randomUUID(), val uri: Uri)

@Composable
fun MergeScreen(modifier: Modifier = Modifier) {
    val files = remember { mutableStateListOf<FileItem>() }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        files.addAll(uris.map { FileItem(uri = it) })
    }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val reordableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val mutable = files.toMutableList()
        mutable.apply { add(to.index, removeAt(from.index)) }
        files.clear()
        files.addAll(mutable)

        haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }
    var isMerging by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        uri?.let { outputUri ->
            scope.launch(Dispatchers.IO) {
                isMerging = true
                mergeFiles(context, files.map { file -> file.uri }, outputUri)
                withContext(Dispatchers.Main) {
                    isMerging = false
                    showCompletedNotification(context, outputUri, "Merge Completed!")
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(visible = !isMerging) {
                FloatingActionButton(
                    onClick = {
                        launcher.launch(arrayOf("application/pdf"))
                    },
                ) {
                    Icon(Icons.Filled.Add, "floating action button")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerpadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerpadding)
                .padding(horizontal = 8.dp),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(files, key = { it.id }) {
                ReorderableItem(
                    reordableLazyListState,
                    key = it.id
                ) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                    Surface(
                        shadowElevation = elevation
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .background(Color.DarkGray)
                                .border(1.dp, Color.Transparent, RoundedCornerShape(12.dp)),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                it.uri.fileName(context) ?: "unknown",
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                modifier = Modifier
                                    .draggableHandle(
                                        onDragStarted = {
                                            haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                        },
                                        onDragStopped = {
                                            haptic.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                        }
                                    )
                                    .padding(horizontal = 8.dp),
                                onClick = {}
                            ) {
                                Icon(Icons.Rounded.DragHandle, contentDescription = "Reorder")
                            }
                        }
                    }
                }
            }
            if (files.isNotEmpty()) {
                item {
                    Button(
                        enabled = !isMerging,
                        onClick = {
                            saveLauncher.launch("merged.pdf")
                        }) {

                        if (isMerging) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Merge")
                        }
                    }
                }
            }
        }
    }
}

fun mergeFiles(context: Context, uris: List<Uri>, outputUri: Uri) {
    val merger = PDFMergerUtility()
    uris.forEach { uri ->
        context.contentResolver.openInputStream(uri)?.let {
            merger.addSource(it)
        }
    }

    context.contentResolver.openOutputStream(outputUri)?.let {
        merger.destinationStream = it
        merger.mergeDocuments(null)
    }
}