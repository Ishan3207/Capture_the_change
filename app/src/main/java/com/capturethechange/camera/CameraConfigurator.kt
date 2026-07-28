package com.capturethechange.camera

import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.CaptureRequestOptions
import android.hardware.camera2.CaptureRequest

@androidx.annotation.OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)
object CameraConfigurator {
    
    fun setAELock(camera: Camera, locked: Boolean) {
        try {
            val camera2Control = Camera2CameraControl.from(camera.cameraControl)
            val options = CaptureRequestOptions.Builder()
                .setCaptureRequestOption(CaptureRequest.CONTROL_AE_LOCK, locked)
                .build()
            camera2Control.setCaptureRequestOptions(options)
        } catch (e: Exception) {
            // Not all devices support Camera2Interop or AE Lock
            e.printStackTrace()
        }
    }
    
    fun setAWBLock(camera: Camera, locked: Boolean) {
        try {
            val camera2Control = Camera2CameraControl.from(camera.cameraControl)
            val options = CaptureRequestOptions.Builder()
                .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_LOCK, locked)
                .build()
            camera2Control.setCaptureRequestOptions(options)
        } catch (e: Exception) {
            // Not all devices support Camera2Interop or AWB Lock
            e.printStackTrace()
        }
    }
}
