package com.nothing.ultrasaver.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.BatteryManager
import android.widget.RemoteViews
import com.nothing.ultrasaver.MainActivity
import com.nothing.ultrasaver.R
import com.nothing.ultrasaver.core.AppFreezeController
import com.nothing.ultrasaver.core.HardwarePowerController
import com.nothing.ultrasaver.data.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class UltraSaverWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_TOGGLE_ULTRA = "com.nothing.ultrasaver.ACTION_TOGGLE_ULTRA_WIDGET"
        const val ACTION_UPDATE_WIDGET = "com.nothing.ultrasaver.ACTION_UPDATE_WIDGET"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, UltraSaverWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            if (allWidgetIds.isNotEmpty()) {
                val intent = Intent(context, UltraSaverWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        CoroutineScope(Dispatchers.IO).launch {
            val prefs = PreferencesManager(context)
            val isUltra = prefs.isUltraActive.first()

            // Read battery
            val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatusIntent = context.registerReceiver(null, batteryFilter)
            val level = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 80
            val scale = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
            val batteryPct = if (level >= 0 && scale > 0) (level * 100) / scale else 80
            val survivalHours = (batteryPct * 2.8f).toInt()

            for (widgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_ultra_saver)

                views.setTextViewText(R.id.widget_battery_pct, "$batteryPct%")

                if (isUltra) {
                    views.setTextViewText(R.id.widget_battery_sub, "ULTRA ACTIVE • ${survivalHours}H EST")
                    views.setTextViewText(R.id.widget_btn_text, "EXIT")
                    views.setTextColor(R.id.widget_btn_text, Color.parseColor("#D71921"))
                    views.setInt(R.id.widget_btn_toggle, "setBackgroundResource", R.drawable.bg_widget_btn_active)
                } else {
                    views.setTextViewText(R.id.widget_battery_sub, "${survivalHours}H SURVIVAL EST")
                    views.setTextViewText(R.id.widget_btn_text, "ENGAGE")
                    views.setTextColor(R.id.widget_btn_text, Color.parseColor("#000000"))
                    views.setInt(R.id.widget_btn_toggle, "setBackgroundResource", R.drawable.bg_widget_btn_engage)
                }

                // Click left side -> Open App
                val openAppIntent = Intent(context, MainActivity::class.java)
                val openAppPendingIntent = PendingIntent.getActivity(
                    context, 0, openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_info_container, openAppPendingIntent)

                // Click button -> Toggle Ultra Saver directly
                val toggleIntent = Intent(context, UltraSaverWidgetProvider::class.java).apply {
                    action = ACTION_TOGGLE_ULTRA
                }
                val togglePendingIntent = PendingIntent.getBroadcast(
                    context, 1, toggleIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_toggle, togglePendingIntent)

                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_TOGGLE_ULTRA) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prefs = PreferencesManager(context)
                    val isUltra = prefs.isUltraActive.first()
                    val hardware = HardwarePowerController(context)
                    val freezer = AppFreezeController(context)

                    if (!isUltra) {
                        // Engage Ultra
                        hardware.applyUltraHardwareProfile()
                        val whitelist = prefs.whitelistedApps.first()
                        val suspended = freezer.freezeNonWhitelisted(whitelist)
                        prefs.setUltraActive(true)
                    } else {
                        // Exit Ultra
                        freezer.unfreezeAll(emptySet()) // will unsuspend via system
                        hardware.restoreOriginalHardwareProfile()
                        prefs.setUltraActive(false)
                    }
                } finally {
                    updateAllWidgets(context)
                    pendingResult.finish()
                }
            }
        } else if (intent.action == Intent.ACTION_BATTERY_CHANGED || intent.action == ACTION_UPDATE_WIDGET) {
            updateAllWidgets(context)
        }
    }
}
