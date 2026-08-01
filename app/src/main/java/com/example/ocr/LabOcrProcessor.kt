package com.example.ocr

import android.content.Context
import android.net.Uri
import com.example.data.ExtractedLabValue
import com.example.data.LabTestCatalog
import com.example.data.LabTestMeta
import com.example.data.OcrAnalysisResult
import com.example.util.ImagePreprocessor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.math.abs

object LabOcrProcessor {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun processReportImage(
        context: Context,
        imageUri: Uri
    ): OcrAnalysisResult = withContext(Dispatchers.IO) {
        try {
            // Step 1: Preprocess image (Rotation, cropping, contrast & noise enhancement)
            val processedBitmap = ImagePreprocessor.processImageForOcr(context, imageUri)
                ?: return@withContext OcrAnalysisResult.PoorQuality("الصورة غير واضحة، يرجى إعادة التقاطها.")

            val inputImage = InputImage.fromBitmap(processedBitmap, 0)

            // Step 2: Run ML Kit Text Recognition
            val visionText = recognizeText(inputImage)
                ?: return@withContext OcrAnalysisResult.Failure("تعذر تحليل التقرير.")

            val rawText = visionText.text
            if (rawText.isBlank() || rawText.length < 5) {
                return@withContext OcrAnalysisResult.PoorQuality("الصورة غير واضحة، يرجى إعادة التقاطها.")
            }

            // Step 3: Parse extracted text blocks and lines specifically for lab test results
            val extractedValues = parseLabValuesFromVisionText(visionText)

            if (extractedValues.isEmpty()) {
                // If text was recognized but no laboratory test values could be reliably identified
                return@withContext OcrAnalysisResult.PoorQuality("الصورة غير واضحة، يرجى إعادة التقاطها.")
            }

            val hasLowConfidence = extractedValues.any { it.isLowConfidence }

            OcrAnalysisResult.Success(
                labValues = extractedValues,
                rawText = rawText,
                hasLowConfidence = hasLowConfidence
            )
        } catch (e: Exception) {
            e.printStackTrace()
            OcrAnalysisResult.Failure("تعذر تحليل التقرير.")
        }
    }

    private suspend fun recognizeText(image: InputImage): Text? =
        suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { text ->
                    if (continuation.isActive) continuation.resume(text)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(null)
                }
        }

    private fun parseLabValuesFromVisionText(visionText: Text): List<ExtractedLabValue> {
        val resultsMap = mutableMapOf<String, ExtractedLabValue>()

        // Ignored non-lab header/footer phrases
        val ignoredKeywords = listOf(
            "HOSPITAL", "CLINIC", "PATIENT", "DOCTOR", "DR.", "DATE", "AGE", "GENDER",
            "MALE", "FEMALE", "ID:", "MRN", "BARCODE", "PHONE", "ADDRESS", "PAGE",
            "LABORATORY REPORT", "TEST NAME", "REFERENCE RANGE", "UNIT", "RESULT", "SIGNATURE"
        )

        val allLines = mutableListOf<Text.Line>()
        for (block in visionText.textBlocks) {
            for (line in block.lines) {
                allLines.add(line)
            }
        }

        // Sort lines top-to-bottom
        allLines.sortBy { it.boundingBox?.top ?: 0 }

        for (i in allLines.indices) {
            val line = allLines[i]
            val lineText = line.text.trim()
            val upperLine = lineText.uppercase()

            // Skip lines that are clearly demographic headers or hospital metadata
            if (ignoredKeywords.any { upperLine.startsWith(it) || upperLine == it }) {
                continue
            }

            // Check if this line matches any lab test in our catalog
            val matchedMeta = LabTestCatalog.findMatchingMeta(lineText)
            if (matchedMeta != null) {
                if (resultsMap.containsKey(matchedMeta.key)) {
                    continue // Already extracted this test
                }

                // Attempt to extract value & unit from current line, or combine with adjacent line
                val extracted = extractValueAndUnitForTest(matchedMeta, line, allLines, i)
                if (extracted != null) {
                    resultsMap[matchedMeta.key] = extracted
                }
            }
        }

        return resultsMap.values.toList()
    }

    private fun extractValueAndUnitForTest(
        meta: LabTestMeta,
        matchedLine: Text.Line,
        allLines: List<Text.Line>,
        lineIndex: Int
    ): ExtractedLabValue? {
        val fullLineText = matchedLine.text

        // Search text context (the line itself plus same horizontal y-level lines if fragmented)
        val textContext = buildString {
            append(fullLineText)

            val matchedBox = matchedLine.boundingBox
            if (matchedBox != null) {
                // Check neighboring lines on the same Y-level (tabular columns)
                for (other in allLines) {
                    if (other === matchedLine) continue
                    val otherBox = other.boundingBox ?: continue
                    if (abs(otherBox.centerY() - matchedBox.centerY()) < 25) {
                        append(" ").append(other.text)
                    }
                }
            }
        }

        // Extract numeric value
        val numericRegex = Regex("""(?i)\b([<>]=?\s*)?(\d{1,5}(?:\.\d{1,3})?)\b""")
        val matches = numericRegex.findAll(textContext).toList()

        if (matches.isEmpty()) {
            return null
        }

        // Filter out matches that belong to test names or percentages in aliases
        val filteredValues = matches.map { it.value.trim() }.filter { valStr ->
            // Skip numbers that match numbers in test aliases like T3, T4, B12
            val cleanVal = valStr.replace("<", "").replace(">", "").replace("=", "").trim()
            cleanVal != "3" && cleanVal != "4" && cleanVal != "12" && cleanVal != "25"
        }

        if (filteredValues.isEmpty()) {
            return null
        }

        // The first valid number in the row is typically the measured result value
        val primaryValue = filteredValues.first()

        // Extract unit
        val extractedUnit = extractUnitFromText(textContext) ?: meta.defaultUnit

        // Check confidence
        val isLowConfidence = isValueLowConfidence(primaryValue, textContext)

        return ExtractedLabValue(
            id = UUID.randomUUID().toString(),
            testNameArabic = meta.arabicName,
            abbreviation = meta.abbreviation,
            value = primaryValue,
            unit = extractedUnit,
            isLowConfidence = isLowConfidence
        )
    }

    private fun extractUnitFromText(text: String): String? {
        val upper = text.uppercase()
        val knownUnits = listOf(
            "10^3/µL", "10^3/UL", "10^6/µL", "10^6/UL", "X10^3/UL", "X10^6/UL", "/UL", "/µL",
            "G/DL", "MG/DL", "UG/DL", "µG/DL", "NG/ML", "PG/ML", "UIU/ML", "µIU/ML", "MIU/L",
            "MMOL/L", "MEQ/L", "IU/L", "U/L", "MM/HR", "MG/L", "FL", "PG", "%"
        )

        for (u in knownUnits) {
            if (upper.contains(u)) {
                // Return clean formatted unit
                return when (u) {
                    "10^3/UL" -> "10^3/µL"
                    "10^6/UL" -> "10^6/µL"
                    "X10^3/UL" -> "10^3/µL"
                    "X10^6/UL" -> "10^6/µL"
                    "UG/DL" -> "µg/dL"
                    "UIU/ML" -> "µIU/mL"
                    else -> u.lowercase().replace("g/dl", "g/dL").replace("mg/dl", "mg/dL")
                        .replace("ng/ml", "ng/mL").replace("pg/ml", "pg/mL")
                }
            }
        }
        return null
    }

    private fun isValueLowConfidence(valueStr: String, textContext: String): Boolean {
        // If value has unexpected characters or seems suspicious, mark low confidence
        val num = valueStr.replace("<", "").replace(">", "").replace("=", "").trim().toDoubleOrNull()
            ?: return true
        if (num < 0.0001 || num > 99999) return true
        return false
    }
}
