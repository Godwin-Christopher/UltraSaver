package com.nothing.ultrasaver.data

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val isSystemProtected: Boolean = false,
    val isWhitelisted: Boolean = false,
    val isSuspended: Boolean = false
)
