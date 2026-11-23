package com.nothing.ultrasaver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.nothing.ultrasaver.ui.MainViewModel
import com.nothing.ultrasaver.ui.screens.SetupWhitelistScreen
import com.nothing.ultrasaver.ui.screens.UltraLauncherScreen
import com.nothing.ultrasaver.ui.theme.OledBlack
import com.nothing.ultrasaver.ui.theme.UltraSaverTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            UltraSaverTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = OledBlack
                ) {
                    val isUltraActive by viewModel.isUltraActive.collectAsState()
                    val apps by viewModel.appsForUi.collectAsState()
                    val whitelistedApps by viewModel.whitelistedAppInfos.collectAsState()
                    val isShizukuConnected by viewModel.isShizukuConnected.collectAsState()
                    val hasShizukuPermission by viewModel.hasShizukuPermission.collectAsState()
                    val isFlashlightOn by viewModel.isFlashlightOn.collectAsState()
                    val batteryStatus by viewModel.batteryStatus.collectAsState()
                    val suspendedPackages by viewModel.suspendedPackages.collectAsState()

                    // Intercept back button when Ultra Mode is active
                    BackHandler(enabled = isUltraActive) {
                        // Do not exit on back gesture; user must use "EXIT ULTRA SAVER" button
                    }

                    if (isUltraActive) {
                        UltraLauncherScreen(
                            batteryStatus = batteryStatus,
                            frozenCount = suspendedPackages.size,
                            whitelistedApps = whitelistedApps,
                            isFlashlightOn = isFlashlightOn,
                            onToggleFlashlight = { viewModel.toggleFlashlight() },
                            onLaunchApp = { pkg -> viewModel.launchApp(pkg) },
                            onExitUltraMode = { viewModel.exitUltraSaver() }
                        )
                    } else {
                        SetupWhitelistScreen(
                            apps = apps,
                            isShizukuConnected = isShizukuConnected,
                            hasShizukuPermission = hasShizukuPermission,
                            onRequestShizukuPermission = { viewModel.requestShizukuPermission() },
                            onToggleApp = { pkg -> viewModel.toggleAppWhitelist(pkg) },
                            onEngageUltraMode = { viewModel.engageUltraSaver() }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkShizuku()
        viewModel.loadAppsOnce()
    }
}
