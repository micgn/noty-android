package de.mg.noty.notifications

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.mg.noty.R
import de.mg.noty.db.NotyRepository


class NotificationOpenReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = createNotification(context)
        if (notification != null)
            notificationManager.notify(1, notification)
    }

    private fun createNotification(context: Context): Notification? {

        val dueNotes = getDueNotes(context)
        if (dueNotes.isNullOrEmpty()) return null

        val builder = Notification.Builder(context, NotificationScheduler().channelId(context))
        builder.setContentTitle("due noty note(s)")

        var style = Notification.InboxStyle()
        dueNotes.forEach { dueNote -> style = style.addLine(dueNote) }
        builder.style = style

        builder.setSmallIcon(R.drawable.baseline_calendar_today_black_18dp)

        addButton(context, builder, NotificationActionHandler.Action.DELETE.name, "delete")
        addButton(context, builder, NotificationActionHandler.Action.NEXT_DAY.name, "+1")
        addButton(context, builder, NotificationActionHandler.Action.NEXT_WEEK.name, "+7")

        return builder.build()
    }

    private fun addButton(
        context: Context,
        builder: Notification.Builder,
        action: String,
        text: String
    ) {

        val intent = Intent(context, NotificationActionHandler::class.java)
        intent.action = action
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.addAction(Notification.Action.Builder(null, text, pendingIntent).build())
    }

    private fun getDueNotes(context: Context): List<String>? {
        val repo = NotyRepository(context.applicationContext as Application)
        return repo.findAllDueNotes().map { it.text }
    }

}