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
            e.printStackTrace()
        }
    }

    fun setAFLock(camera: Camera, locked: Boolean) {
        try {
            val camera2Control = Camera2CameraControl.from(camera.cameraControl)
            val builder = CaptureRequestOptions.Builder()
                .setCaptureRequestOption(CaptureRequest.CONTROL_AF_MODE, if (locked) CaptureRequest.CONTROL_AF_MODE_OFF else CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            
            if (locked) {
                builder.setCaptureRequestOption(CaptureRequest.LENS_FOCUS_DISTANCE, 0.0f)
            }
            
            camera2Control.setCaptureRequestOptions(builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setStabilizationMode(camera: Camera, mode: com.capturethechange.viewmodel.StabilizationMode) {
        try {
            val camera2Control = Camera2CameraControl.from(camera.cameraControl)
            val builder = CaptureRequestOptions.Builder()
            
            when (mode) {
                com.capturethechange.viewmodel.StabilizationMode.OFF -> {
                    builder.setCaptureRequestOption(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, android.hardware.camera2.CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_OFF)
                    builder.setCaptureRequestOption(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, android.hardware.camera2.CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_OFF)
                }
                com.capturethechange.viewmodel.StabilizationMode.OPTICAL -> {
                    builder.setCaptureRequestOption(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, android.hardware.camera2.CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON)
                    builder.setCaptureRequestOption(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, android.hardware.camera2.CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_OFF)
                }
                com.capturethechange.viewmodel.StabilizationMode.ELECTRONIC -> {
                    builder.setCaptureRequestOption(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, android.hardware.camera2.CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_OFF)
                    builder.setCaptureRequestOption(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, android.hardware.camera2.CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_ON)
                }
                com.capturethechange.viewmodel.StabilizationMode.BOTH -> {
                    builder.setCaptureRequestOption(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, android.hardware.camera2.CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_ON)
                    builder.setCaptureRequestOption(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE, android.hardware.camera2.CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_ON)
                }
            }
            
            camera2Control.setCaptureRequestOptions(builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @androidx.annotation.OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)
    fun checkCapabilities(camera: Camera, viewModel: com.capturethechange.viewmodel.CameraViewModel) {
        try {
            val camera2Info = androidx.camera.camera2.interop.Camera2CameraInfo.from(camera.cameraInfo)
            
            // OIS support
            val oisModes = camera2Info.getCameraCharacteristic(android.hardware.camera2.CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
            val hasOis = oisModes?.any { it != android.hardware.camera2.CameraMetadata.LENS_OPTICAL_STABILIZATION_MODE_OFF } == true
            viewModel.isOisSupported.value = hasOis

            // EIS support
            val eisModes = camera2Info.getCameraCharacteristic(android.hardware.camera2.CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES)
            val hasEis = eisModes?.any { it != android.hardware.camera2.CameraMetadata.CONTROL_VIDEO_STABILIZATION_MODE_OFF } == true
            viewModel.isEisSupported.value = hasEis
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
