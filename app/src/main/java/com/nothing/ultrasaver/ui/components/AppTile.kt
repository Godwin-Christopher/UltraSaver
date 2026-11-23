package com.nothing.ultrasaver.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.ultrasaver.data.AppInfo
import com.nothing.ultrasaver.ui.theme.NothingDarkGrey
import com.nothing.ultrasaver.ui.theme.NothingWhite
import com.nothing.ultrasaver.ui.theme.OledCard

private val iconBitmapCache = mutableMapOf<String, Bitmap>()

@Composable
fun AppTile(
    app: AppInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cachedBitmap = remember(app.packageName) {
        iconBitmapCache.getOrPut(app.packageName) {
            app.icon?.let { toMonochromeBitmap(it) } ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8)
        }
    }

    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(OledCard)
            .border(1.dp, NothingDarkGrey, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (cachedBitmap.width > 1) {
                Image(
                    bitmap = cachedBitmap.asImageBitmap(),
                    contentDescription = app.appName,
                    modifier = Modifier.size(38.dp)
                )
            }

            Text(
                text = app.appName.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                color = NothingWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun toMonochromeBitmap(drawable: Drawable): Bitmap {
    val width = drawable.intrinsicWidth.coerceIn(48, 128)
    val height = drawable.intrinsicHeight.coerceIn(48, 128)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint().apply {
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)
        colorFilter = ColorMatrixColorFilter(matrix)
    }

    drawable.setBounds(0, 0, width, height)
    drawable.draw(canvas)
    return bitmap
}
