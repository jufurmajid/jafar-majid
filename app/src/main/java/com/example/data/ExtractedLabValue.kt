package com.example.data

import android.net.Uri

data class ExtractedLabValue(
    val id: String,
    val testNameArabic: String,
    val abbreviation: String,
    val value: String,
    val unit: String,
    val isLowConfidence: Boolean = false
)

sealed class OcrAnalysisResult {
    data class Success(
        val labValues: List<ExtractedLabValue>,
        val rawText: String,
        val hasLowConfidence: Boolean = false
    ) : OcrAnalysisResult()

    data class PoorQuality(
        val message: String = "الصورة غير واضحة، يرجى إعادة التقاطها."
    ) : OcrAnalysisResult()

    data class Failure(
        val message: String = "تعذر تحليل التقرير."
    ) : OcrAnalysisResult()
}
