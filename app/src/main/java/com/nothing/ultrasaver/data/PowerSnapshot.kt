package com.nothing.ultrasaver.data

/**
 * Captures user hardware settings before entering Ultra Mode
 * so they can be seamlessly restored on exit.
 */
data class PowerSnapshot(
    val peakRefreshRate: Float = 120.0f,
    val minRefreshRate: Float = 60.0f,
    val screenTimeoutMs: Int = 30000,
    val originalLauncherPackage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
