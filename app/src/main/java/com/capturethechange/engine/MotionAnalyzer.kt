package com.capturethechange.engine

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.capturethechange.viewmodel.CameraViewModel

class MotionAnalyzer(
    private val viewModel: CameraViewModel,
    private val frameBuffer: FrameBuffer,
    private val deltaEngine: DeltaEngine,
    private val onLivePreviewReady: (Bitmap) -> Unit
) : ImageAnalysis.Analyzer {

    private var lastAnalysisTime = 0L
    private val fps = 30
    private val frameIntervalMs = 1000L / fps

    override fun analyze(image: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAnalysisTime < frameIntervalMs) {
            image.close()
            return
        }
        lastAnalysisTime = currentTime

        val width = image.width
        val height = image.height
        viewModel.analysisDimensions.value = Pair(width, height)

        // Extract Y plane
        val yData = YuvHelper.extractYPlane(image)
        
        // Update capacity based on timeframe (e.g. 500ms at 30fps = 15 frames)
        val timeFrameMs = viewModel.timeFrameMs.value
        val requiredFrames = (timeFrameMs / frameIntervalMs).toInt().coerceAtLeast(1)
        frameBuffer.updateCapacity(requiredFrames)

        frameBuffer.push(yData)

        val oldest = frameBuffer.oldest()
        val latest = frameBuffer.latest()

        if (oldest != null && latest != null && frameBuffer.isFull()) {
            val threshold = viewModel.noiseThreshold.value.toInt()
            val mode = viewModel.outputMode.value
            
            // Note: yData size may not exactly equal width * height due to rowStride.
            // A more robust implementation would respect rowStride from image.planes[0].
            // We assume rowStride == width for 720p on most devices.
            
            try {
                val colorBitmap = if (mode == com.capturethechange.viewmodel.OutputMode.COLOR_MOTION || mode == com.capturethechange.viewmodel.OutputMode.COLOR_STATIC) {
                    image.toBitmap()
                } else null

                var previewBitmap = deltaEngine.computeLivePreview(
                    oldest, latest, colorBitmap, width, height, threshold, mode
                )
                
                val rotation = image.imageInfo.rotationDegrees
                if (rotation != 0) {
                    val matrix = android.graphics.Matrix().apply { postRotate(rotation.toFloat()) }
                    previewBitmap = Bitmap.createBitmap(previewBitmap, 0, 0, previewBitmap.width, previewBitmap.height, matrix, false)
                }
                
                onLivePreviewReady(previewBitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        image.close()
    }
}
