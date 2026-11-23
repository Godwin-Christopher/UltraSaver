package com.nothing.ultrasaver.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nothing.ultrasaver.core.AppFreezeController
import com.nothing.ultrasaver.core.HardwarePowerController
import com.nothing.ultrasaver.core.ShizukuManager
import com.nothing.ultrasaver.data.AppInfo
import com.nothing.ultrasaver.data.PreferencesManager
import com.nothing.ultrasaver.service.BatteryStatus
import com.nothing.ultrasaver.service.BatteryTelemetry
import com.nothing.ultrasaver.service.FlashlightManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val prefs = PreferencesManager(context)
    private val freezeController = AppFreezeController(context)
    private val hardwareController = HardwarePowerController(context)
    private val flashlightManager = FlashlightManager(context)
    private val batteryTelemetry = BatteryTelemetry(context)

    val isShizukuConnected: StateFlow<Boolean> = ShizukuManager.isConnected
    val hasShizukuPermission: StateFlow<Boolean> = ShizukuManager.hasPermission
    val isFlashlightOn: StateFlow<Boolean> = flashlightManager.isFlashlightOn
    val batteryStatus: StateFlow<BatteryStatus> = batteryTelemetry.status

    private val _isUltraActive = MutableStateFlow(false)
    val isUltraActive: StateFlow<Boolean> = _isUltraActive.asStateFlow()

    private val _rawInstalledApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _whitelistedPackageNames = MutableStateFlow<Set<String>>(emptySet())
    val whitelistedPackageNames: StateFlow<Set<String>> = _whitelistedPackageNames.asStateFlow()

    private val _suspendedPackages = MutableStateFlow<Set<String>>(emptySet())
    val suspendedPackages: StateFlow<Set<String>> = _suspendedPackages.asStateFlow()

    // Smooth UI state: Instant in-memory map without re-scanning PackageManager on every click
    val appsForUi: StateFlow<List<AppInfo>> = combine(_rawInstalledApps, _whitelistedPackageNames) { rawApps, whitelist ->
        rawApps.map { app ->
            val isWhitelisted = app.isSystemProtected || whitelist.contains(app.packageName)
            if (app.isWhitelisted != isWhitelisted) {
                app.copy(isWhitelisted = isWhitelisted)
            } else {
                app
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val whitelistedAppInfos: StateFlow<List<AppInfo>> = combine(appsForUi, _whitelistedPackageNames) { apps, _ ->
        apps.filter { it.isWhitelisted }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        ShizukuManager.init()
        batteryTelemetry.startListening()

        viewModelScope.launch {
            prefs.isUltraActive.collect { active ->
                _isUltraActive.value = active
            }
        }

        viewModelScope.launch {
            prefs.whitelistedApps.collect { savedWhitelist ->
                _whitelistedPackageNames.value = savedWhitelist
            }
        }

        loadAppsOnce()
    }

    fun requestShizukuPermission() {
        ShizukuManager.requestPermission()
    }

    fun checkShizuku() {
        ShizukuManager.checkPermission()
    }

    fun loadAppsOnce() {
        if (_rawInstalledApps.value.isNotEmpty()) return
        viewModelScope.launch {
            val apps = freezeController.getInstalledUserApps(_whitelistedPackageNames.value)
            _rawInstalledApps.value = apps
        }
    }

    // Instant O(1) in-memory toggle, NO slow PackageManager queries!
    fun toggleAppWhitelist(packageName: String) {
        val current = _whitelistedPackageNames.value.toMutableSet()
        if (current.contains(packageName)) {
            current.remove(packageName)
        } else {
            current.add(packageName)
        }
        _whitelistedPackageNames.value = current

        viewModelScope.launch {
            prefs.updateWhitelist(current)
        }
    }

    fun engageUltraSaver() {
        // 1. Immediately switch UI to Ultra Launcher (0ms delay)
        _isUltraActive.value = true

        viewModelScope.launch {
            prefs.setUltraActive(true)
            com.nothing.ultrasaver.widget.UltraSaverWidgetProvider.updateAllWidgets(context)

            // 2. Hardware profile (60Hz, 15s timeout, power-mode 1, doze)
            hardwareController.applyUltraHardwareProfile()

            // 3. Fast bulk freeze non-whitelisted apps in background
            val suspended = freezeController.freezeNonWhitelisted(_whitelistedPackageNames.value)
            _suspendedPackages.value = suspended
        }
    }

    fun exitUltraSaver() {
        // 1. Immediately switch UI back (0ms delay)
        _isUltraActive.value = false

        viewModelScope.launch {
            prefs.setUltraActive(false)
            com.nothing.ultrasaver.widget.UltraSaverWidgetProvider.updateAllWidgets(context)

            // 2. Turn off flashlight if on
            flashlightManager.turnOff()

            // 3. Fast bulk unsuspend all apps
            freezeController.unfreezeAll(_suspendedPackages.value)
            _suspendedPackages.value = emptySet()

            // 4. Restore original hardware profile (120Hz, normal timeout)
            hardwareController.restoreOriginalHardwareProfile()
        }
    }

    fun toggleFlashlight() {
        flashlightManager.toggle()
    }

    fun launchApp(packageName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        }
    }

    override fun onCleared() {
        super.onCleared()
        batteryTelemetry.stopListening()
        ShizukuManager.destroy()
    }
}
