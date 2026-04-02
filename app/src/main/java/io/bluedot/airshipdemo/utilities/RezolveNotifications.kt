package io.bluedot.airshipdemo.utilities

import android.app.Notification
import android.app.Notification.BigTextStyle
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import io.bluedot.airshipdemo.MainActivity
import io.bluedot.airshipdemo.R.mipmap
import java.util.Random

const val channelId = "BluedotChannel1234"
const val channelName = "BluedotChannelService"

fun createNotification(
    title: String,
    content: String,
    onGoing: Boolean,
    context: Context
): Notification {
    val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    if (notificationManager.getNotificationChannel(channelId) == null) {
        val notificationChannel = NotificationChannel(channelId, channelName, IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    val actionIntent = Intent(context, MainActivity::class.java)
    val flag: Int = if (VERSION.SDK_INT >= VERSION_CODES.S) {
        PendingIntent.FLAG_IMMUTABLE
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }
    val pendingIntent = PendingIntent.getActivity(context, Random().nextInt(), actionIntent, flag)

    val notification = Notification.Builder(context, channelId)
        .setContentTitle(title)
        .setContentText(content)
        .setStyle(BigTextStyle().bigText(content))
        .setOngoing(onGoing)
        .setCategory(Notification.CATEGORY_SERVICE)
        .setContentIntent(pendingIntent)
        .setSmallIcon(mipmap.ic_launcher)
    return notification.build()
}

fun fireNotification(notification: Notification, context: Context) {
    val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(Random().nextInt(), notification)
}