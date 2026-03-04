package com.escapecall.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import com.escapecall.R
import com.escapecall.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Homescreen widget — a single tap triggers the escape call.
 *
 * Flow:  Widget tap → custom broadcast → onReceive() → goAsync() → API call → Twilio → incoming call
 *
 * Uses goAsync() to extend the BroadcastReceiver window and make the network call
 * directly, avoiding foreground service restrictions on Android 12+.
 */
class EscapeCallWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_TRIGGER) {
            Log.d(TAG, "Widget tapped — triggering escape call")
            Toast.makeText(context, "Calling…", Toast.LENGTH_SHORT).show()

            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = ApiClient.triggerCall()
                    Log.d(TAG, "API result: $result")
                } catch (e: Exception) {
                    Log.e(TAG, "API call failed", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        private const val TAG = "EscapeCallWidget"
        private const val ACTION_TRIGGER = "com.escapecall.ACTION_TRIGGER_CALL"

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            val intent = Intent(context, EscapeCallWidget::class.java).apply {
                action = ACTION_TRIGGER
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val views = RemoteViews(context.packageName, R.layout.widget_escape_call).apply {
                setOnClickPendingIntent(R.id.widget_button, pendingIntent)
            }

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
