package edu.kit.tm.ps.embertalk.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context.NOTIFICATION_SERVICE
import androidx.core.app.NotificationCompat

object PersistentNotification {
    fun build(
        service: Service,
        channelName: String,
        contentText: String
    ): Notification? {

        val channel = NotificationChannel(
            service.javaClass.name,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )
        (service.getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
            channel
        )
        return NotificationCompat.Builder(service, service.javaClass.name)
            .setContentTitle(channelName)
            .setContentText(contentText).build()
    }
}