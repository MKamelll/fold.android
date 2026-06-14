package com.mkamelll.fold

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.DragHandle
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
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
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    launcher.launch(arrayOf("application/pdf"))
                },
            ) {
                Icon(Icons.Filled.Add, "floating action button")
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
        }
    }
}