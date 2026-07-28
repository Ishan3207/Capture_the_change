package com.capturethechange.capture

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object GalleryManager {
    suspend fun saveBitmap(context: Context, bitmap: Bitmap): Uri? = withContext(Dispatchers.IO) {
        val filename = "CaptureTheChange_${System.currentTimeMillis()}.jpg"
        var uri: Uri? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/CaptureTheChange")
            }

            val resolver = context.contentResolver
            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                }
            }
        } else {
            // Pre-Android 10
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val appDir = File(imagesDir, "CaptureTheChange")
            if (!appDir.exists()) appDir.mkdirs()

            val imageFile = File(appDir, filename)
            FileOutputStream(imageFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos)
            }
            
            // Register in MediaStore
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }
            uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        }
        return@withContext uri
    }
}
