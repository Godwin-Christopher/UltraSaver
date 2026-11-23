package com.nothing.ultrasaver.core

import android.content.Context
import android.provider.Settings
import com.nothing.ultrasaver.data.PowerSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HardwarePowerController(private val context: Context) {

    private var savedSnapshot: PowerSnapshot? = null

    /**
     * Reads current hardware settings to create a restoration snapshot.
     */
    suspend fun captureCurrentSnapshot(): PowerSnapshot = withContext(Dispatchers.IO) {
        val cr = context.contentResolver

        val peakRefresh = try {
            Settings.System.getFloat(cr, "peak_refresh_rate", 120.0f)
        } catch (_: Exception) { 120.0f }

        val minRefresh = try {
            Settings.System.getFloat(cr, "min_refresh_rate", 60.0f)
        } catch (_: Exception) { 60.0f }

        val timeout = try {
            Settings.System.getInt(cr, Settings.System.SCREEN_OFF_TIMEOUT, 30000)
        } catch (_: Exception) { 30000 }

        val snapshot = PowerSnapshot(
            peakRefreshRate = peakRefresh,
            minRefreshRate = minRefresh,
            screenTimeoutMs = timeout
        )
        savedSnapshot = snapshot
        snapshot
    }

    /**
     * Enforces aggressive hardware battery constraints:
     * 1. 60Hz lock (Phone 1 drops from 120Hz)
     * 2. 15-second screen timeout
     * 3. Triggers Android System Battery Saver mode
     * 4. Forces immediate Doze mode
     */
    suspend fun applyUltraHardwareProfile() = withContext(Dispatchers.IO) {
        // Snapshot original settings if not already saved
        if (savedSnapshot == null) {
            captureCurrentSnapshot()
        }

        // 1. Lock refresh rate to 60Hz
        ShizukuManager.execShell("settings put system peak_refresh_rate 60.0")
        ShizukuManager.execShell("settings put system min_refresh_rate 60.0")

        // 2. Clamp screen timeout to 15 seconds
        ShizukuManager.execShell("settings put system screen_off_timeout 15000")

        // 3. Trigger Android native Power Saver
        ShizukuManager.execShell("cmd power set-mode 1")

        // 4. Force idle / Doze
        ShizukuManager.execShell("cmd deviceidle force-idle")

        // 5. Ensure smooth animation scaling remains intact
        ShizukuManager.execShell("settings put global window_animation_scale 1.0")
        ShizukuManager.execShell("settings put global transition_animation_scale 1.0")
        ShizukuManager.execShell("settings put global animator_duration_scale 1.0")
    }

    /**
     * Restores display and power settings back to user preferences.
     */
    suspend fun restoreOriginalHardwareProfile() = withContext(Dispatchers.IO) {
        val snapshot = savedSnapshot ?: PowerSnapshot()

        // 1. Restore refresh rates
        ShizukuManager.execShell("settings put system peak_refresh_rate ${snapshot.peakRefreshRate}")
        ShizukuManager.execShell("settings put system min_refresh_rate ${snapshot.minRefreshRate}")

        // 2. Restore screen timeout
        ShizukuManager.execShell("settings put system screen_off_timeout ${snapshot.screenTimeoutMs}")

        // 3. Disable native Power Saver mode
        ShizukuManager.execShell("cmd power set-mode 0")

        // 4. Restore animations
        ShizukuManager.execShell("settings put global window_animation_scale 1.0")
        ShizukuManager.execShell("settings put global transition_animation_scale 1.0")
        ShizukuManager.execShell("settings put global animator_duration_scale 1.0")

        savedSnapshot = null
    }
}
