package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

object ImagePreprocessor {

    fun processImageForOcr(context: Context, imageUri: Uri): Bitmap? {
        return try {
            // 1. Load scaled Bitmap safely from Uri
            val rawBitmap = loadBitmapFromUri(context, imageUri) ?: return null

            // 2. Correct EXIF orientation
            val rotatedBitmap = rotateImageIfRequired(context, imageUri, rawBitmap)

            // 3. Auto-crop or trim outer dark borders if present
            val croppedBitmap = trimOuterBorders(rotatedBitmap)

            // 4. Enhance contrast & sharpen for maximum OCR accuracy
            enhanceForOcr(croppedBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }

        // Limit maximum dimension to ~2000px for optimal speed and memory
        val maxDim = max(options.outWidth, options.outHeight)
        var sampleSize = 1
        if (maxDim > 2048) {
            sampleSize = (maxDim / 2048.0).toInt().coerceAtLeast(1)
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        }
    }

    private fun rotateImageIfRequired(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) return bitmap

            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )

            val rotationDegrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }

            if (rotationDegrees == 0) return bitmap

            val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
            val rotated = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
            )
            if (rotated != bitmap) {
                bitmap.recycle()
            }
            return rotated
        } catch (e: Exception) {
            return bitmap
        } finally {
            inputStream?.close()
        }
    }

    private fun trimOuterBorders(bitmap: Bitmap): Bitmap {
        // If image has wide black or empty borders from scanner/camera frame, trim slightly
        val width = bitmap.width
        val height = bitmap.height
        if (width <= 200 || height <= 200) return bitmap

        // Trim 2% margin from edges if needed
        val cropX = (width * 0.01).toInt()
        val cropY = (height * 0.01).toInt()
        val cropWidth = width - (cropX * 2)
        val cropHeight = height - (cropY * 2)

        return try {
            val cropped = Bitmap.createBitmap(bitmap, cropX, cropY, cropWidth, cropHeight)
            if (cropped != bitmap) {
                bitmap.recycle()
            }
            cropped
        } catch (e: Exception) {
            bitmap
        }
    }

    private fun enhanceForOcr(bitmap: Bitmap): Bitmap {
        // Create enhanced grayscale & high contrast bitmap
        val width = bitmap.width
        val height = bitmap.height

        val enhanced = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(enhanced)
        val paint = Paint()

        // High contrast matrix: increases contrast by 1.3x and boosts brightness slightly
        val contrast = 1.35f
        val brightness = 10f

        val cm = ColorMatrix(
            floatArrayOf(
                contrast * 0.299f, contrast * 0.587f, contrast * 0.114f, 0f, brightness,
                contrast * 0.299f, contrast * 0.587f, contrast * 0.114f, 0f, brightness,
                contrast * 0.299f, contrast * 0.587f, contrast * 0.114f, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            )
        )

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        bitmap.recycle()
        return enhanced
    }
}
private infix fun Any.avenue(nothing: Nothing?) {}
