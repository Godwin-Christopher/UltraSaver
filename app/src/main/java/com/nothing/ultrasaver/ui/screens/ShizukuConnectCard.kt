package com.nothing.ultrasaver.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.ultrasaver.ui.theme.NothingDarkGrey
import com.nothing.ultrasaver.ui.theme.NothingGreen
import com.nothing.ultrasaver.ui.theme.NothingGrey
import com.nothing.ultrasaver.ui.theme.NothingRed
import com.nothing.ultrasaver.ui.theme.NothingWhite
import com.nothing.ultrasaver.ui.theme.OledBlack
import com.nothing.ultrasaver.ui.theme.OledCard

@Composable
fun ShizukuStatusCard(
    isConnected: Boolean,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(OledCard)
            .border(
                1.dp,
                if (hasPermission) NothingDarkGrey else NothingRed,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (hasPermission) NothingGreen else NothingRed
                )
                Text(
                    text = when {
                        !isConnected -> "SHIZUKU NOT RUNNING"
                        !hasPermission -> "SHIZUKU PERMISSION REQUIRED"
                        else -> "SHIZUKU READY (DEEP FREEZE ACTIVE)"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = if (hasPermission) NothingGreen else NothingRed
                )
            }

            Text(
                text = when {
                    !isConnected -> "Start Shizuku on your Nothing Phone via Wireless Debugging to allow background app freezing."
                    !hasPermission -> "Grant permission to Ultra Saver so it can execute 'pm suspend' and lock 60Hz."
                    else -> "Privileged shell access granted. Full background app suspension and hardware lock enabled."
                },
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = NothingGrey
            )

            if (!hasPermission) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NothingWhite,
                            contentColor = OledBlack
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "GRANT ACCESS",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
                            if (launchIntent != null) {
                                context.startActivity(launchIntent)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "OPEN SHIZUKU",
                            fontFamily = FontFamily.Monospace,
                            color = NothingWhite,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
