package com.mkamelll.fold

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.graphics.createBitmap

fun Uri.fileName(context: Context): String? {
    return context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        cursor.getString(nameIndex)
    }
}

fun Uri.renderAllPages(context: Context): List<Bitmap> {
    return try {
        val fd = context.contentResolver.openFileDescriptor(this, "r") ?: return emptyList()
        val renderer = PdfRenderer(fd)
        (0 until renderer.pageCount).map { index ->
            val page = renderer.openPage(index)
            val bitmap = createBitmap(page.width, page.height)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            bitmap
        }.also { renderer.close() }
    } catch (e: Exception) {
        emptyList()
    }
}