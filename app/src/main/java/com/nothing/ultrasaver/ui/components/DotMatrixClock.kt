package com.nothing.ultrasaver.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nothing.ultrasaver.ui.theme.NothingGrey
import com.nothing.ultrasaver.ui.theme.NothingWhite
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DotMatrixClock(modifier: Modifier = Modifier) {
    var currentTime by remember { mutableStateOf(Date()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            val cal = Calendar.getInstance()
            val secondsRemaining = 60 - cal.get(Calendar.SECOND)
            delay((secondsRemaining * 1000L).coerceAtLeast(1000L))
        }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEE, dd MMM", Locale.getDefault()) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = timeFormat.format(currentTime),
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp,
            color = NothingWhite
        )
        Text(
            text = dateFormat.format(currentTime).uppercase(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 3.sp,
            color = NothingGrey
        )
    }
}
