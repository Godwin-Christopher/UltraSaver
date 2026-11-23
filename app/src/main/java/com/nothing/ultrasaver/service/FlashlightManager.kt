package com.nothing.ultrasaver.service

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FlashlightManager(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn: StateFlow<Boolean> = _isFlashlightOn

    private var cameraId: String? = null

    init {
        try {
            val ids = cameraManager?.cameraIdList ?: emptyArray()
            for (id in ids) {
                val chars = cameraManager?.getCameraCharacteristics(id)
                val hasFlash = chars?.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = chars?.get(CameraCharacteristics.LENS_FACING)
                if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id
                    break
                }
            }
        } catch (_: Exception) {}
    }

    fun toggle() {
        val targetId = cameraId ?: return
        try {
            val newState = !_isFlashlightOn.value
            cameraManager?.setTorchMode(targetId, newState)
            _isFlashlightOn.value = newState
        } catch (_: Exception) {}
    }

    fun turnOff() {
        val targetId = cameraId ?: return
        try {
            if (_isFlashlightOn.value) {
                cameraManager?.setTorchMode(targetId, false)
                _isFlashlightOn.value = false
            }
        } catch (_: Exception) {}
    }
}
