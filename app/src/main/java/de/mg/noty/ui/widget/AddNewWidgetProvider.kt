package de.mg.noty.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import de.mg.noty.R
import de.mg.noty.ui.MainActivity

class AddNewWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            var intent = Intent(context, MainActivity::class.java)
            intent = intent.putExtra(EXTRA_NAME, EXTRA_VALUE)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            val views: RemoteViews = RemoteViews(
                context.packageName,
                R.layout.widget_add
            ).apply {
                setOnClickPendingIntent(R.id.addNewBtn, pendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    companion object {
        val EXTRA_NAME = "start"
        val EXTRA_VALUE = "newNote"
    }
}
