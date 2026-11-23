package com.nothing.ultrasaver.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.ultrasaver.data.AppInfo
import com.nothing.ultrasaver.ui.theme.NothingDarkGrey
import com.nothing.ultrasaver.ui.theme.NothingGreen
import com.nothing.ultrasaver.ui.theme.NothingGrey
import com.nothing.ultrasaver.ui.theme.NothingRed
import com.nothing.ultrasaver.ui.theme.NothingWhite
import com.nothing.ultrasaver.ui.theme.OledBlack
import com.nothing.ultrasaver.ui.theme.OledCard

@Composable
fun SetupWhitelistScreen(
    apps: List<AppInfo>,
    isShizukuConnected: Boolean,
    hasShizukuPermission: Boolean,
    onRequestShizukuPermission: () -> Unit,
    onToggleApp: (String) -> Unit,
    onEngageUltraMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val whitelistedCount = apps.count { it.isWhitelisted }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "ULTRA SAVER",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp,
                        color = NothingWhite
                    )
                    Text(
                        text = "NOTHING PHONE (1) EDITION",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp,
                        color = NothingRed
                    )
                }

                // Shizuku connection banner
                ShizukuStatusCard(
                    isConnected = isShizukuConnected,
                    hasPermission = hasShizukuPermission,
                    onRequestPermission = onRequestShizukuPermission
                )

                // Subtitle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECT ALLOWED APPS",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        color = NothingGrey
                    )
                    Text(
                        text = "$whitelistedCount ACTIVE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = NothingWhite
                    )
                }

                // App list
                if (apps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SCANNING INSTALLED APPS...",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = NothingGrey
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(apps, key = { it.packageName }) { app ->
                            AppWhitelistRow(
                                app = app,
                                onToggle = { onToggleApp(app.packageName) }
                            )
                        }
                    }
                }
            }

            // Bottom Engage Button
            Button(
                onClick = onEngageUltraMode,
                enabled = hasShizukuPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NothingWhite,
                    contentColor = OledBlack,
                    disabledContainerColor = OledCard,
                    disabledContentColor = NothingGrey
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = if (hasShizukuPermission) "ENGAGE ULTRA SAVER" else "GRANT SHIZUKU TO ENGAGE",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
private fun AppWhitelistRow(
    app: AppInfo,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(OledCard)
            .border(
                1.dp,
                if (app.isWhitelisted) NothingWhite else NothingDarkGrey,
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !app.isSystemProtected) { onToggle() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    color = NothingWhite
                )
                Text(
                    text = if (app.isSystemProtected) "SYSTEM PROTECTED (ALWAYS ALLOWED)" else app.packageName,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (app.isSystemProtected) NothingGreen else NothingGrey
                )
            }

            if (app.isSystemProtected) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = NothingGreen,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Checkbox(
                    checked = app.isWhitelisted,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = NothingWhite,
                        uncheckedColor = NothingDarkGrey,
                        checkmarkColor = OledBlack
                    )
                )
            }
        }
    }
}
