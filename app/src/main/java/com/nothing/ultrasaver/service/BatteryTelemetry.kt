package com.nothing.ultrasaver.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class BatteryStatus(
    val percentage: Int = 100,
    val isCharging: Boolean = false,
    val estimatedHoursNormal: Float = 20.0f,
    val estimatedHoursUltra: Float = 60.0f
)

class BatteryTelemetry(private val context: Context) {

    private val _status = MutableStateFlow(BatteryStatus())
    val status: StateFlow<BatteryStatus> = _status

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                updateFromIntent(intent)
            }
        }
    }

    fun startListening() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initialIntent = context.registerReceiver(receiver, filter)
        initialIntent?.let { updateFromIntent(it) }
    }

    fun stopListening() {
        try {
            context.unregisterReceiver(receiver)
        } catch (_: Exception) {}
    }

    private fun updateFromIntent(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

        val percentage = if (level >= 0 && scale > 0) {
            (level * 100) / scale
        } else {
            50
        }

        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        // Phone (1) has a 4500mAh battery.
        // In deep ultra mode (OLED true black, 60Hz, 0 background apps, forced Doze),
        // idle drain is ~0.35% per hour (~2.8 hours per 1%).
        val ultraHours = percentage * 2.8f
        val normalHours = percentage * 0.7f

        _status.value = BatteryStatus(
            percentage = percentage,
            isCharging = isCharging,
            estimatedHoursNormal = normalHours,
            estimatedHoursUltra = ultraHours
        )
    }
}
