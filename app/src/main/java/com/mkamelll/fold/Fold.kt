package com.mkamelll.fold

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class Fold : Application() {
    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(this)
        val channel =
            NotificationChannel("fold_channel", "Fold", NotificationManager.IMPORTANCE_DEFAULT)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

    }
}

fun showCompletedNotification(context: Context, outputUri: Uri, message: String) {
    val openIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(outputUri, "application/pdf")
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
    }

    val chooserIntent = Intent.createChooser(openIntent, "Open With").apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    val pendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            chooserIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    val notification = NotificationCompat.Builder(context, "fold_channel")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(message)
        .setContentText("Tap to open the file")
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
        != PackageManager.PERMISSION_GRANTED
    ) return
    NotificationManagerCompat.from(context).notify(1, notification)
}