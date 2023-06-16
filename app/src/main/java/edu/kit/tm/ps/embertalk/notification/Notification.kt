package edu.kit.tm.ps.embertalk.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import androidx.core.app.NotificationCompat

object Notification {
    fun build(
        context: Context,
        channelName: String,
        contentText: String,
        importance: Int = NotificationManager.IMPORTANCE_DEFAULT
    ): Notification {

        val channel = NotificationChannel(
            context.javaClass.name,
            channelName,
            importance
        )
        (context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            channel
        )
        return NotificationCompat.Builder(context, context.javaClass.name)
            .setContentTitle(channelName)
            .setContentText(contentText).build()
    }
}