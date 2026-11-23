package com.nothing.ultrasaver.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.ultrasaver.data.AppInfo
import com.nothing.ultrasaver.service.BatteryStatus
import com.nothing.ultrasaver.ui.components.AppTile
import com.nothing.ultrasaver.ui.components.BatterySurvivalGauge
import com.nothing.ultrasaver.ui.components.DotMatrixClock
import com.nothing.ultrasaver.ui.theme.NothingDarkGrey
import com.nothing.ultrasaver.ui.theme.NothingGrey
import com.nothing.ultrasaver.ui.theme.NothingRed
import com.nothing.ultrasaver.ui.theme.NothingWhite
import com.nothing.ultrasaver.ui.theme.OledBlack
import com.nothing.ultrasaver.ui.theme.OledCard

@Composable
fun UltraLauncherScreen(
    batteryStatus: BatteryStatus,
    frozenCount: Int,
    whitelistedApps: List<AppInfo>,
    isFlashlightOn: Boolean,
    onToggleFlashlight: () -> Unit,
    onLaunchApp: (String) -> Unit,
    onExitUltraMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DotMatrixClock(modifier = Modifier.padding(top = 16.dp))

                BatterySurvivalGauge(
                    batteryStatus = batteryStatus,
                    frozenCount = frozenCount
                )

                // Quick Hardware Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Flashlight pill
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isFlashlightOn) NothingWhite else OledCard)
                            .border(1.dp, NothingDarkGrey, RoundedCornerShape(12.dp))
                            .clickable { onToggleFlashlight() }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isFlashlightOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Flashlight",
                                tint = if (isFlashlightOn) OledBlack else NothingWhite
                            )
                            Text(
                                text = if (isFlashlightOn) "TORCH ON" else "TORCH OFF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (isFlashlightOn) OledBlack else NothingWhite
                            )
                        }
                    }

                    // Network Settings pill
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(OledCard)
                            .border(1.dp, NothingDarkGrey, RoundedCornerShape(12.dp))
                            .clickable {
                                context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Network",
                                tint = NothingWhite
                            )
                            Text(
                                text = "NETWORK",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = NothingWhite
                            )
                        }
                    }
                }
            }

            // Essential Apps Grid (Whitelisted)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = "ALLOWED APPS (${whitelistedApps.size})",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    color = NothingGrey,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(whitelistedApps) { app ->
                        AppTile(
                            app = app,
                            onClick = { onLaunchApp(app.packageName) }
                        )
                    }
                }
            }

            // Exit Ultra Mode Button
            Button(
                onClick = { showExitDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = OledCard,
                    contentColor = NothingRed
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .border(1.dp, NothingRed, RoundedCornerShape(14.dp))
            ) {
                Text(
                    text = "EXIT ULTRA SAVER",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }
        }

        // Safety Exit Dialog
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                containerColor = OledCard,
                title = {
                    Text(
                        text = "EXIT ULTRA SAVER?",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = NothingWhite
                    )
                },
                text = {
                    Text(
                        text = "This will unsuspend all background apps, restore 120Hz refresh rate, and return to standard Nothing OS.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = NothingGrey
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitDialog = false
                            onExitUltraMode()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NothingRed,
                            contentColor = NothingWhite
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("EXIT", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showExitDialog = false },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("STAY", fontFamily = FontFamily.Monospace, color = NothingWhite)
                    }
                }
            )
        }
    }
}
