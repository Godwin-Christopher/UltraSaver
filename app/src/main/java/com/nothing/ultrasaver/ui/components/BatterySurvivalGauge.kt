package com.nothing.ultrasaver.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.ultrasaver.service.BatteryStatus
import com.nothing.ultrasaver.ui.theme.NothingDarkGrey
import com.nothing.ultrasaver.ui.theme.NothingGreen
import com.nothing.ultrasaver.ui.theme.NothingGrey
import com.nothing.ultrasaver.ui.theme.NothingRed
import com.nothing.ultrasaver.ui.theme.NothingWhite
import com.nothing.ultrasaver.ui.theme.OledCard

@Composable
fun BatterySurvivalGauge(
    batteryStatus: BatteryStatus,
    frozenCount: Int,
    modifier: Modifier = Modifier
) {
    val barColor = when {
        batteryStatus.isCharging -> NothingGreen
        batteryStatus.percentage <= 15 -> NothingRed
        else -> NothingWhite
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(OledCard)
            .border(1.dp, NothingDarkGrey, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${batteryStatus.percentage}%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = barColor
                    )
                    if (batteryStatus.isCharging) {
                        Text(
                            text = " (CHARGING)",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = NothingGreen,
                            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                        )
                    }
                }

                Text(
                    text = "${batteryStatus.estimatedHoursUltra.toInt()}H REMAINING",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = NothingWhite
                )
            }

            // Minimalist Battery Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF222222))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = (batteryStatus.percentage / 100f).coerceIn(0f, 1f))
                        .background(barColor)
                )
            }

            // Status message
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ULTRA PROFILE ACTIVE",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = NothingGrey
                )
                Text(
                    text = "$frozenCount APPS FROZEN",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = if (frozenCount > 0) NothingRed else NothingGrey
                )
            }
        }
    }
}
