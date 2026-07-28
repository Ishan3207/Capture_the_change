package com.capturethechange.engine

import android.graphics.Bitmap
import com.capturethechange.viewmodel.OutputMode
import kotlin.math.abs

class DeltaEngine {
    
    // Computes a fast grayscale or color live preview based on the Y-channel difference
    fun computeLivePreview(
        oldest: ByteArray,
        latest: ByteArray,
        colorBitmap: Bitmap?,
        width: Int,
        height: Int,
        threshold: Int,
        mode: OutputMode
    ): Bitmap {
        val size = width * height
        val pixels = IntArray(size)

        var colorPixels: IntArray? = null
        if (colorBitmap != null && (mode == OutputMode.COLOR_MOTION || mode == OutputMode.COLOR_STATIC)) {
            colorPixels = IntArray(size)
            colorBitmap.getPixels(colorPixels, 0, width, 0, 0, width, height)
        }

        for (i in 0 until size) {
            val yLatest = latest[i].toInt() and 0xFF
            val yOldest = oldest[i].toInt() and 0xFF
            val delta = abs(yLatest - yOldest)

            val isMotion = delta > threshold
            val keepPixel = when (mode) {
                OutputMode.COLOR_MOTION, OutputMode.GRAYSCALE_MOTION -> isMotion
                OutputMode.COLOR_STATIC, OutputMode.GRAYSCALE_STATIC -> !isMotion
            }

            if (keepPixel) {
                if (colorPixels != null) {
                    pixels[i] = colorPixels[i]
                } else {
                    pixels[i] = (0xFF shl 24) or (yLatest shl 16) or (yLatest shl 8) or yLatest
                }
            } else {
                pixels[i] = 0xFF000000.toInt() // Pure Black
            }
        }

        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }

    // Computes the full final capture, combining the Y-channel mask with the full-color frame
    fun computeFinalCapture(
        oldest: ByteArray,
        latest: ByteArray,
        colorBitmap: Bitmap,
        width: Int,
        height: Int,
        threshold: Int,
        mode: OutputMode
    ): Bitmap {
        val size = width * height
        val colorPixels = IntArray(size)
        // Ensure colorBitmap matches our dimensions (it might differ if aspect ratios don't match exactly, 
        // but typically ImageAnalysis and ImageCapture can be configured to match)
        colorBitmap.getPixels(colorPixels, 0, width, 0, 0, width, height)
        
        val resultPixels = IntArray(size)

        for (i in 0 until size) {
            val yLatest = latest[i].toInt() and 0xFF
            val yOldest = oldest[i].toInt() and 0xFF
            val delta = abs(yLatest - yOldest)

            val isMotion = delta > threshold
            val keepPixel = when (mode) {
                OutputMode.COLOR_MOTION, OutputMode.GRAYSCALE_MOTION -> isMotion
                OutputMode.COLOR_STATIC, OutputMode.GRAYSCALE_STATIC -> !isMotion
            }

            if (keepPixel) {
                if (mode == OutputMode.GRAYSCALE_MOTION || mode == OutputMode.GRAYSCALE_STATIC) {
                    resultPixels[i] = (0xFF shl 24) or (yLatest shl 16) or (yLatest shl 8) or yLatest
                } else {
                    resultPixels[i] = colorPixels[i]
                }
            } else {
                resultPixels[i] = 0xFF000000.toInt() // Pure Black
            }
        }

        return Bitmap.createBitmap(resultPixels, width, height, Bitmap.Config.ARGB_8888)
    }
}
