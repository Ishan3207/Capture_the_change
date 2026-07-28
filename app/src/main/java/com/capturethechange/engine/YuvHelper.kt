package com.capturethechange.engine

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

object YuvHelper {
    
    fun extractYPlane(imageProxy: ImageProxy): ByteArray {
        val yPlane = imageProxy.planes[0]
        val buffer = yPlane.buffer
        val rowStride = yPlane.rowStride
        val width = imageProxy.width
        val height = imageProxy.height

        val bytes = ByteArray(width * height)
        
        if (rowStride == width) {
            buffer.get(bytes)
        } else {
            for (y in 0 until height) {
                buffer.position(y * rowStride)
                buffer.get(bytes, y * width, width)
            }
        }
        return bytes
    }

    fun imageProxyToBitmap(image: ImageProxy): Bitmap? {
        if (image.format == android.graphics.ImageFormat.JPEG) {
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            return android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
        
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        
        val imageBytes = out.toByteArray()
        return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}
