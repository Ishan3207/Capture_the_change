package com.capturethechange.capture

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.capturethechange.engine.DeltaEngine
import com.capturethechange.engine.FrameBuffer
import com.capturethechange.engine.YuvHelper
import com.capturethechange.viewmodel.CameraViewModel
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CaptureManager(
    private val viewModel: CameraViewModel,
    private val deltaEngine: DeltaEngine
) {

    suspend fun processAndSaveCapture(
        context: Context,
        frameBuffer: FrameBuffer,
        latestImageProxy: ImageProxy
    ): Uri? = withContext(Dispatchers.Default) {
        val oldest = frameBuffer.oldest()
        val latest = frameBuffer.latest()

        if (oldest == null || latest == null) {
            latestImageProxy.close()
            return@withContext null
        }

        // Full color conversion for the latest frame
        val colorBitmap = YuvHelper.imageProxyToBitmap(latestImageProxy)
        val rotationDegrees = latestImageProxy.imageInfo.rotationDegrees
        latestImageProxy.close()
        
        if (colorBitmap == null) return@withContext null

        val dims = viewModel.analysisDimensions.value
        if (dims == null) return@withContext null
        val targetWidth = dims.first
        val targetHeight = dims.second
        
        val threshold = viewModel.noiseThreshold.value.toInt()
        val mode = viewModel.outputMode.value
        
        val scaledColorBitmap = if (colorBitmap.width != targetWidth || colorBitmap.height != targetHeight) {
            Bitmap.createScaledBitmap(colorBitmap, targetWidth, targetHeight, true)
        } else {
            colorBitmap
        }

        var finalBitmap = deltaEngine.computeFinalCapture(
            oldest, latest, scaledColorBitmap, targetWidth, targetHeight, threshold, mode
        )
        
        if (rotationDegrees != 0) {
            val matrix = android.graphics.Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            finalBitmap = Bitmap.createBitmap(finalBitmap, 0, 0, finalBitmap.width, finalBitmap.height, matrix, false)
        }

        GalleryManager.saveBitmap(context, finalBitmap)
    }
}
