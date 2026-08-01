package com.example.util

import android.content.Context
import android.graphics.*
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.InputStream
import kotlin.math.max

object ImagePreprocessor {

    /**
     * Loads, rotates, and trims the raw image.
     */
    fun loadBaseBitmap(context: Context, imageUri: Uri): Bitmap? {
        return try {
            val rawBitmap = loadBitmapFromUri(context, imageUri) ?: return null
            val rotatedBitmap = rotateImageIfRequired(context, imageUri, rawBitmap)
            trimOuterBorders(rotatedBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Preprocessing Strategy A: Standard High-Contrast & Sharpened Grayscale.
     */
    fun processStrategyStandard(baseBitmap: Bitmap): Bitmap {
        val width = baseBitmap.width
        val height = baseBitmap.height
        val enhanced = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(enhanced)
        val paint = Paint()

        // Boost contrast by 1.45x and brightness by +12
        val contrast = 1.45f
        val brightness = 12f
        val cm = ColorMatrix(
            floatArrayOf(
                contrast * 0.299f, contrast * 0.587f, contrast * 0.114f, 0f, brightness,
                contrast * 0.299f, contrast * 0.587f, contrast * 0.114f, 0f, brightness,
                contrast * 0.299f, contrast * 0.587f, contrast * 0.114f, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(baseBitmap, 0f, 0f, paint)

        // Apply a sharpening convolution pass
        return sharpenBitmap(enhanced)
    }

    /**
     * Preprocessing Strategy B: Localized / Adaptive Thresholding (Binarization).
     * Eliminates gradients, shadows, and converts background to pure white and text to pure black.
     */
    fun processStrategyAdaptiveThreshold(baseBitmap: Bitmap): Bitmap {
        val width = baseBitmap.width
        val height = baseBitmap.height
        val binarized = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        baseBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Calculate global or adaptive luminance thresholding
        // Using a 16x16 grid for adaptive local thresholding to handle shadows/gradients perfectly.
        val gridSizeX = 16
        val gridSizeY = 16
        val blockW = max(1, width / gridSizeX)
        val blockH = max(1, height / gridSizeY)

        val thresholds = IntArray(gridSizeX * gridSizeY)

        for (gy in 0 until gridSizeY) {
            for (gx in 0 until gridSizeX) {
                val startX = gx * blockW
                val startY = gy * blockH
                val endX = minOf(width, startX + blockW)
                val endY = minOf(height, startY + blockH)

                var sumLuminance = 0L
                var count = 0
                for (y in startY until endY) {
                    for (x in startX until endX) {
                        val pixel = pixels[y * width + x]
                        val r = (pixel shr 16) and 0xff
                        val g = (pixel shr 8) and 0xff
                        val b = pixel and 0xff
                        val luminance = (0.299f * r + 0.587f * g + 0.114f * b).toInt()
                        sumLuminance += luminance
                        count++
                    }
                }
                thresholds[gy * gridSizeX + gx] = if (count > 0) (sumLuminance / count).toInt() else 128
            }
        }

        // Apply local thresholding with a 15% bias to preserve thin strokes while removing gradients.
        for (y in 0 until height) {
            val gy = (y / blockH).coerceAtMost(gridSizeY - 1)
            for (x in 0 until width) {
                val gx = (x / blockW).coerceAtMost(gridSizeX - 1)
                val localThreshold = thresholds[gy * gridSizeX + gx] - 15

                val idx = y * width + x
                val pixel = pixels[idx]
                val r = (pixel shr 16) and 0xff
                val g = (pixel shr 8) and 0xff
                val b = pixel and 0xff
                val luminance = (0.299f * r + 0.587f * g + 0.114f * b).toInt()

                pixels[idx] = if (luminance > localThreshold) Color.WHITE else Color.BLACK
            }
        }

        binarized.setPixels(pixels, 0, width, 0, 0, width, height)
        return binarized
    }

    /**
     * Preprocessing Strategy C: Upscaled & Sharpened for Small Character Enhancement.
     */
    fun processStrategyUpscaled(baseBitmap: Bitmap): Bitmap {
        val width = baseBitmap.width
        val height = baseBitmap.height

        // Upscale image 1.5x with bilinear filtering
        val scale = 1.5f
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        val scaled = Bitmap.createScaledBitmap(baseBitmap, newWidth, newHeight, true)

        // Boost contrast heavily on the scaled image to make small text stand out
        val enhanced = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(enhanced)
        val paint = Paint()

        val contrast = 1.6f
        val brightness = 5f
        val cm = ColorMatrix(
            floatArrayOf(
                contrast * 0.299f, contrast * 0.587f, contrast * 0.114f, 0f, brightness,
                contrast * 0.299f, contrast * 0.587f, contrast * 0.114f, 0f, brightness,
                contrast * 0.299f, contrast * 0.587f, contrast * 0.114f, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(scaled, 0f, 0f, paint)

        scaled.recycle()
        return sharpenBitmap(enhanced)
    }

    /**
     * Preprocessing Strategy D: Thin Text Preservation & Edge Enhancement.
     * Soft contrast adjustment with low noise filter to ensure ultra-thin text doesn't fragment.
     */
    fun processStrategyPreserveThin(baseBitmap: Bitmap): Bitmap {
        val width = baseBitmap.width
        val height = baseBitmap.height
        val enhanced = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(enhanced)
        val paint = Paint()

        // Softer contrast (1.2x) and slightly higher brightness to retain delicate thin lines
        val contrast = 1.2f
        val brightness = 20f
        val cm = ColorMatrix(
            floatArrayOf(
                contrast * 0.299f, contrast * 0.587f, contrast * 0.114f, 0f, brightness,
                contrast * 0.299f, contrast * 0.587f, contrast * 0.114f, 0f, brightness,
                contrast * 0.299f, contrast * 0.587f, contrast * 0.114f, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(baseBitmap, 0f, 0f, paint)

        // Perform a softer edge boost (unsharp mask simulation)
        return softSharpenBitmap(enhanced)
    }

    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }

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
        val width = bitmap.width
        val height = bitmap.height
        if (width <= 200 || height <= 200) return bitmap

        // Trim 2% margin to eliminate scanner/shadow outer borders cleanly
        val cropX = (width * 0.02).toInt()
        val cropY = (height * 0.02).toInt()
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

    /**
     * Sharpening filter using convolution kernel:
     * [ 0  -1   0 ]
     * [-1   5  -1 ]
     * [ 0  -1   0 ]
     */
    private fun sharpenBitmap(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        val config = src.config ?: Bitmap.Config.ARGB_8888
        val dest = Bitmap.createBitmap(width, height, config)

        val pixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        src.getPixels(pixels, 0, width, 0, 0, width, height)

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = y * width + x

                // Center
                val c = pixels[idx]
                val cr = (c shr 16) and 0xff
                val cg = (c shr 8) and 0xff
                val cb = c and 0xff

                // Neighbors
                val n1 = pixels[idx - width] // top
                val n2 = pixels[idx - 1]     // left
                val n3 = pixels[idx + 1]     // right
                val n4 = pixels[idx + width] // bottom

                val r = 5 * cr - (((n1 shr 16) and 0xff) + ((n2 shr 16) and 0xff) + ((n3 shr 16) and 0xff) + ((n4 shr 16) and 0xff))
                val g = 5 * cg - (((n1 shr 8) and 0xff) + ((n2 shr 8) and 0xff) + ((n3 shr 8) and 0xff) + ((n4 shr 8) and 0xff))
                val b = 5 * cb - ((n1 and 0xff) + (n2 and 0xff) + (n3 and 0xff) + (n4 and 0xff))

                val fr = r.coerceIn(0, 255)
                val fg = g.coerceIn(0, 255)
                val fb = b.coerceIn(0, 255)

                outPixels[idx] = (0xff000000.toInt()) or (fr shl 16) or (fg shl 8) or fb
            }
        }

        dest.setPixels(outPixels, 0, width, 0, 0, width, height)
        src.recycle()
        return dest
    }

    /**
     * Softer sharpening convolution kernel:
     * [ 0   -0.5  0  ]
     * [-0.5   3  -0.5]
     * [ 0   -0.5  0  ]
     */
    private fun softSharpenBitmap(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        val config = src.config ?: Bitmap.Config.ARGB_8888
        val dest = Bitmap.createBitmap(width, height, config)

        val pixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        src.getPixels(pixels, 0, width, 0, 0, width, height)

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = y * width + x

                val c = pixels[idx]
                val cr = (c shr 16) and 0xff
                val cg = (c shr 8) and 0xff
                val cb = c and 0xff

                val n1 = pixels[idx - width]
                val n2 = pixels[idx - 1]
                val n3 = pixels[idx + 1]
                val n4 = pixels[idx + width]

                val r = (3f * cr - 0.5f * (((n1 shr 16) and 0xff) + ((n2 shr 16) and 0xff) + ((n3 shr 16) and 0xff) + ((n4 shr 16) and 0xff))).toInt()
                val g = (3f * cg - 0.5f * (((n1 shr 8) and 0xff) + ((n2 shr 8) and 0xff) + ((n3 shr 8) and 0xff) + ((n4 shr 8) and 0xff))).toInt()
                val b = (3f * cb - 0.5f * ((n1 and 0xff) + (n2 and 0xff) + (n3 and 0xff) + (n4 and 0xff))).toInt()

                val fr = r.coerceIn(0, 255)
                val fg = g.coerceIn(0, 255)
                val fb = b.coerceIn(0, 255)

                outPixels[idx] = (0xff000000.toInt()) or (fr shl 16) or (fg shl 8) or fb
            }
        }

        dest.setPixels(outPixels, 0, width, 0, 0, width, height)
        src.recycle()
        return dest
    }

    /**
     * Helper to keep backward compatibility or general simple process.
     */
    fun processImageForOcr(context: Context, imageUri: Uri): Bitmap? {
        val base = loadBaseBitmap(context, imageUri) ?: return null
        return processStrategyStandard(base)
    }
}
