package com.nothing.ultrasaver.core

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuManager {

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission

    private val receivedListener = Shizuku.OnBinderReceivedListener {
        _isConnected.value = true
        checkPermission()
    }

    private val deadListener = Shizuku.OnBinderDeadListener {
        _isConnected.value = false
        _hasPermission.value = false
    }

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        _hasPermission.value = (grantResult == PackageManager.PERMISSION_GRANTED)
    }

    fun init() {
        try {
            Shizuku.addBinderReceivedListenerSticky(receivedListener)
            Shizuku.addBinderDeadListener(deadListener)
            Shizuku.addRequestPermissionResultListener(permissionListener)
        } catch (_: Exception) {}
        checkPermission()
    }

    fun destroy() {
        try {
            Shizuku.removeBinderReceivedListener(receivedListener)
            Shizuku.removeBinderDeadListener(deadListener)
            Shizuku.removeRequestPermissionResultListener(permissionListener)
        } catch (_: Exception) {}
    }

    fun checkPermission(): Boolean {
        return try {
            if (!Shizuku.pingBinder()) {
                _isConnected.value = false
                _hasPermission.value = false
                false
            } else {
                _isConnected.value = true
                val granted = if (Shizuku.isPreV11()) {
                    false
                } else {
                    Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
                }
                _hasPermission.value = granted
                granted
            }
        } catch (e: Exception) {
            _isConnected.value = false
            _hasPermission.value = false
            false
        }
    }

    fun requestPermission(requestCode: Int = 1001) {
        try {
            if (Shizuku.pingBinder()) {
                if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                    Shizuku.requestPermission(requestCode)
                } else {
                    _hasPermission.value = true
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Executes an ADB shell command via Shizuku.
     * e.g., execShell("pm suspend com.instagram.android")
     */
    suspend fun execShell(command: String): ShellResult = withContext(Dispatchers.IO) {
        if (!Shizuku.pingBinder() || !_hasPermission.value) {
            return@withContext ShellResult(-1, "", "Shizuku not ready or permission denied")
        }

        try {
            val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            newProcessMethod.isAccessible = true
            val process = newProcessMethod.invoke(
                null,
                arrayOf("sh", "-c", command),
                null,
                null
            ) as java.lang.Process

            val stdout = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
            val stderr = BufferedReader(InputStreamReader(process.errorStream)).use { it.readText() }
            val exitCode = process.waitFor()
            ShellResult(exitCode, stdout.trim(), stderr.trim())
        } catch (e: Exception) {
            ShellResult(-1, "", e.localizedMessage ?: "Unknown shell error")
        }
    }
}

data class ShellResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
) {
    val isSuccess: Boolean get() = exitCode == 0
}
