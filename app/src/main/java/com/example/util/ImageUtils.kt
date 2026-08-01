package com.example.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ImageUtils {
    fun createImageUri(context: Context): Uri {
        val imagesDir = File(context.cacheDir, "images").apply {
            if (!exists()) mkdirs()
        }
        val file = File(imagesDir, "captured_report_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
