package de.mg.noty.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.ZonedDateTime


class NotificationScheduler {

    private val deliveryHour = 17
    private val deliveryMinute = 0

    fun schedule(context: Context) {

        createOrUpdateNotificationChannel(context)
        scheduleNotification(context)
    }

    fun channelId(context: Context): String {
        return "${context.packageName}-notifications"
    }

    private fun createOrUpdateNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId(context), "Noty", NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "noty's notifications"
            channel.setShowBadge(true)

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun scheduleNotification(context: Context) {
        val intent = Intent(context, NotificationOpenReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(pendingIntent)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP, nextExecutionEpochMillis(),
            24 * 60 * 60 * 1000, pendingIntent
        )
    }

    private fun nextExecutionEpochMillis(): Long {

        val now = ZonedDateTime.now()
        val todayAtScheduleTime = now.withHour(deliveryHour).withMinute(deliveryMinute).withSecond(0)
        val next = if (todayAtScheduleTime.isBefore(now)) todayAtScheduleTime.plusDays(1) else todayAtScheduleTime

        return next.toInstant().toEpochMilli()
    }

}