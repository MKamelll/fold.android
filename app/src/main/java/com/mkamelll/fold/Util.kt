package com.mkamelll.fold

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

fun Uri.fileName(context: Context): String? {
    return context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        cursor.getString(nameIndex)
    }
}