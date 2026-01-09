package com.copypastefail.offlinereminder.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.copypastefail.offlinereminder.MainActivity
import com.copypastefail.offlinereminder.R

object NotificationHelper {

    private const val CHANNEL_ID = "snippet_channel"
    private const val CHANNEL_NAME = "Snippets"
    private const val MAX_NOTIFICATION_TEXT_LENGTH = 400
    private const val TRUNCATION_SUFFIX = "..."

    fun showNotification(
        context: Context,
        listId: Int,
        listName: String,
        snippetId: Int,
        snippetText: String
    ) {
        createChannel(context)

        val notificationText = buildNotificationText(snippetText)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(MainActivity.EXTRA_LIST_ID, listId)
            putExtra(MainActivity.EXTRA_SNIPPET_ID, snippetId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            listId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(notificationText.text)
            .setBigContentTitle(listName)
        if (notificationText.wasTruncated) {
            bigTextStyle.setSummaryText(context.getString(R.string.notification_truncated_summary))
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_reminder)
            .setContentTitle(listName)
            .setContentText(notificationText.text)
            .setStyle(bigTextStyle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
        }
        NotificationManagerCompat.from(context).notify(listId, notification)
    }

    private fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    private fun buildNotificationText(snippetText: String): NotificationText {
        val trimmed = snippetText.trim()
        if (trimmed.length <= MAX_NOTIFICATION_TEXT_LENGTH) {
            return NotificationText(trimmed, false)
        }
        val truncated = trimmed.take(MAX_NOTIFICATION_TEXT_LENGTH).trimEnd()
        return NotificationText("$truncated$TRUNCATION_SUFFIX", true)
    }

    private data class NotificationText(
        val text: String,
        val wasTruncated: Boolean
    )
}
