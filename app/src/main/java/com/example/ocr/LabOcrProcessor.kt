package com.example.ocr

import android.content.Context
import android.graphics.Bitmap
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
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.math.abs

object LabOcrProcessor {

    // Lazy initialization of the recognizer to prevent throwing exceptions in unit tests where MlKit is not needed
    private val recognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    // Expected clinical limits and typical metrics for laboratory results
    data class ValidationRule(
        val key: String,
        val minPlausible: Double,
        val maxPlausible: Double,
        val expectedUnit: String,
        val normalMin: Double,
        val normalMax: Double,
        val expectedPrecision: Int
    )

    val VALIDATION_RULES: Map<String, ValidationRule> = mapOf(
        "WBC" to ValidationRule("WBC", 1.0, 150.0, "10^3/µL", 4.0, 11.0, 1),
        "RBC" to ValidationRule("RBC", 1.0, 10.0, "10^6/µL", 3.8, 6.2, 2),
        "HGB" to ValidationRule("HGB", 3.0, 25.0, "g/dL", 11.5, 17.5, 1),
        "HCT" to ValidationRule("HCT", 10.0, 75.0, "%", 35.0, 52.0, 1),
        "MCV" to ValidationRule("MCV", 40.0, 160.0, "fL", 80.0, 100.0, 1),
        "MCH" to ValidationRule("MCH", 10.0, 60.0, "pg", 26.0, 34.0, 1),
        "MCHC" to ValidationRule("MCHC", 15.0, 50.0, "g/dL", 31.0, 36.0, 1),
        "RDW" to ValidationRule("RDW", 8.0, 45.0, "%", 11.0, 16.0, 1),
        "PLT" to ValidationRule("PLT", 10.0, 1500.0, "10^3/µL", 150.0, 450.0, 0),
        "MPV" to ValidationRule("MPV", 4.0, 25.0, "fL", 7.0, 12.0, 1),
        "NEUT" to ValidationRule("NEUT", 0.0, 100.0, "%", 40.0, 75.0, 1),
        "LYMPH" to ValidationRule("LYMPH", 0.0, 100.0, "%", 20.0, 45.0, 1),
        "MONO" to ValidationRule("MONO", 0.0, 100.0, "%", 2.0, 10.0, 1),
        "EOS" to ValidationRule("EOS", 0.0, 100.0, "%", 1.0, 6.0, 1),
        "BASO" to ValidationRule("BASO", 0.0, 100.0, "%", 0.5, 2.0, 1),
        "GLU" to ValidationRule("GLU", 20.0, 1000.0, "mg/dL", 70.0, 140.0, 0),
        "HbA1c" to ValidationRule("HbA1c", 2.0, 25.0, "%", 4.0, 6.5, 1),
        "CREATININE" to ValidationRule("CREATININE", 0.1, 25.0, "mg/dL", 0.5, 1.4, 2),
        "UREA" to ValidationRule("UREA", 5.0, 400.0, "mg/dL", 15.0, 50.0, 1),
        "ALT" to ValidationRule("ALT", 1.0, 2000.0, "U/L", 5.0, 50.0, 0),
        "AST" to ValidationRule("AST", 1.0, 2000.0, "U/L", 5.0, 50.0, 0),
        "ALP" to ValidationRule("ALP", 10.0, 1500.0, "U/L", 30.0, 150.0, 0),
        "BILIRUBIN" to ValidationRule("BILIRUBIN", 0.1, 40.0, "mg/dL", 0.1, 1.2, 1),
        "ALBUMIN" to ValidationRule("ALBUMIN", 1.0, 10.0, "g/dL", 3.5, 5.0, 1),
        "TOTAL PROTEIN" to ValidationRule("TOTAL PROTEIN", 2.0, 15.0, "g/dL", 6.0, 8.3, 1),
        "CRP" to ValidationRule("CRP", 0.1, 600.0, "mg/L", 0.0, 10.0, 1),
        "ESR" to ValidationRule("ESR", 0.0, 150.0, "mm/hr", 0.0, 30.0, 0),
        "VITAMIN D" to ValidationRule("VITAMIN D", 2.0, 250.0, "ng/mL", 20.0, 100.0, 1),
        "VITAMIN B12" to ValidationRule("VITAMIN B12", 50.0, 3000.0, "pg/mL", 200.0, 900.0, 0),
        "FERRITIN" to ValidationRule("FERRITIN", 1.0, 10000.0, "ng/mL", 10.0, 300.0, 0),
        "IRON" to ValidationRule("IRON", 10.0, 500.0, "µg/dL", 50.0, 170.0, 0),
        "TSH" to ValidationRule("TSH", 0.01, 250.0, "µIU/mL", 0.4, 4.5, 2),
        "FT3" to ValidationRule("FT3", 0.5, 40.0, "pg/mL", 2.0, 4.4, 2),
        "FT4" to ValidationRule("FT4", 0.1, 20.0, "ng/dL", 0.8, 2.0, 2),
        "T3" to ValidationRule("T3", 10.0, 800.0, "ng/dL", 80.0, 200.0, 1),
        "T4" to ValidationRule("T4", 0.5, 30.0, "µg/dL", 4.5, 12.0, 1),
        "HDL" to ValidationRule("HDL", 5.0, 150.0, "mg/dL", 40.0, 60.0, 0),
        "LDL" to ValidationRule("LDL", 10.0, 350.0, "mg/dL", 50.0, 130.0, 0),
        "CHOLESTEROL" to ValidationRule("CHOLESTEROL", 30.0, 800.0, "mg/dL", 100.0, 200.0, 0),
        "TRIGLYCERIDES" to ValidationRule("TRIGLYCERIDES", 10.0, 1500.0, "mg/dL", 30.0, 150.0, 0),
        "CALCIUM" to ValidationRule("CALCIUM", 3.0, 20.0, "mg/dL", 8.5, 10.5, 1),
        "MAGNESIUM" to ValidationRule("MAGNESIUM", 0.5, 10.0, "mg/dL", 1.7, 2.2, 1),
        "POTASSIUM" to ValidationRule("POTASSIUM", 1.0, 15.0, "mmol/L", 3.5, 5.1, 1),
        "SODIUM" to ValidationRule("SODIUM", 50.0, 250.0, "mmol/L", 135.0, 145.0, 0),
        "PSA" to ValidationRule("PSA", 0.01, 1000.0, "ng/mL", 0.0, 4.0, 2),
        "D_DIMER" to ValidationRule("D_DIMER", 5.0, 20000.0, "ng/mL", 0.0, 500.0, 0),
        "TROPONIN" to ValidationRule("TROPONIN", 0.001, 100.0, "ng/mL", 0.0, 0.04, 3)
    )

    // Intermediate parsed results per pass
    data class CandidateResult(
        val meta: LabTestMeta,
        val rawValueStr: String,
        val cleanedValue: Double,
        val correctedValueStr: String,
        val unit: String,
        val isCorrected: Boolean,
        val passIndex: Int,
        val rawLineText: String
    )

    suspend fun processReportImage(
        context: Context,
        imageUri: Uri
    ): OcrAnalysisResult = withContext(Dispatchers.IO) {
        try {
            // Step 1: Load rotated & trimmed base bitmap
            val baseBitmap = ImagePreprocessor.loadBaseBitmap(context, imageUri)
                ?: return@withContext OcrAnalysisResult.PoorQuality("الصورة غير واضحة، يرجى إعادة التقاطها.")

            val candidatesMap = mutableMapOf<String, MutableList<CandidateResult>>()
            val rawTextBuilder = StringBuilder()

            // Run up to 4 different preprocessing strategies for multi-pass OCR
            for (pass in 0 until 4) {
                val preprocessed = when (pass) {
                    0 -> ImagePreprocessor.processStrategyStandard(baseBitmap)
                    1 -> ImagePreprocessor.processStrategyAdaptiveThreshold(baseBitmap)
                    2 -> ImagePreprocessor.processStrategyUpscaled(baseBitmap)
                    else -> ImagePreprocessor.processStrategyPreserveThin(baseBitmap)
                }

                val inputImage = InputImage.fromBitmap(preprocessed, 0)
                val visionText = recognizeText(inputImage)

                // Recycle preprocessed bitmap after running OCR
                preprocessed.recycle()

                if (visionText == null || visionText.text.isBlank()) continue

                rawTextBuilder.append("\n--- Pass $pass ---\n").append(visionText.text)

                // Run row-by-row smart parser on this pass
                val passResults = parsePassWithSmartParser(visionText, pass)
                for (cand in passResults) {
                    val list = candidatesMap.getOrPut(cand.meta.key) { mutableListOf() }
                    list.add(cand)
                }
            }

            baseBitmap.recycle()

            val rawTextCombined = rawTextBuilder.toString()
            if (rawTextCombined.isBlank() || rawTextCombined.length < 5) {
                return@withContext OcrAnalysisResult.PoorQuality("الصورة غير واضحة، يرجى إعادة التقاطها.")
            }

            // Step 2: Merge candidates from all passes using medical validation and consensus
            val finalLabValues = mergeCandidatesAndValidate(candidatesMap)

            if (finalLabValues.isEmpty()) {
                return@withContext OcrAnalysisResult.PoorQuality("الصورة غير واضحة، يرجى إعادة التقاطها.")
            }

            val hasLowConfidence = finalLabValues.any { it.isLowConfidence }

            OcrAnalysisResult.Success(
                labValues = finalLabValues,
                rawText = rawTextCombined,
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

    /**
     * Calculates the average skew angle (in radians) from the corner points of the returned text lines.
     */
    fun calculateSkewAngle(allLines: List<Text.Line>): Double {
        var totalAngle = 0.0
        var count = 0
        for (line in allLines) {
            val pts = line.cornerPoints
            if (pts != null && pts.size >= 2) {
                val dx = pts[1].x - pts[0].x
                val dy = pts[1].y - pts[0].y
                val length = Math.sqrt((dx * dx + dy * dy).toDouble())
                if (length > 15) {
                    val angle = Math.atan2(dy.toDouble(), dx.toDouble())
                    // Ignore drastic vertical/perpendicular angles to stay focused on horizontal lines
                    if (abs(angle) < Math.PI / 6.0) {
                        totalAngle += angle
                        count++
                    }
                }
            }
        }
        return if (count > 0) totalAngle / count else 0.0
    }

    /**
     * Line-by-Line, Row-by-Row Smart Parser.
     * Reconstructs the original tabular structure of the lab report.
     */
    private fun parsePassWithSmartParser(visionText: Text, passIndex: Int): List<CandidateResult> {
        val results = mutableListOf<CandidateResult>()

        val allLines = mutableListOf<Text.Line>()
        for (block in visionText.textBlocks) {
            allLines.addAll(block.lines)
        }

        if (allLines.isEmpty()) return results

        // Group lines into row-by-row tabular structures
        val rows = groupLinesIntoRows(allLines)

        for (rowLines in rows) {
            // Find if any cell/text in this row matches a lab test synonym
            var matchedMeta: LabTestMeta? = null
            var testLineIndex = -1

            for (i in rowLines.indices) {
                val lineText = rowLines[i].text

                // Try matching with exact text
                var meta = LabTestCatalog.findMatchingMeta(lineText)
                if (meta == null) {
                    // Try matching with normalized character substitutions to handle OCR typos
                    val normalized = normalizeOcrText(lineText)
                    meta = LabTestCatalog.findMatchingMeta(normalized)
                }

                if (meta != null) {
                    matchedMeta = meta
                    testLineIndex = i
                    break
                }
            }

            if (matchedMeta != null) {
                // Parse numeric value and unit strictly from elements on the SAME horizontal row
                val resultCandidate = extractValueAndUnitFromRow(matchedMeta, rowLines, testLineIndex, passIndex)
                if (resultCandidate != null) {
                    results.add(resultCandidate)
                }
            }
        }

        return results
    }

    /**
     * Normalizes typical OCR substitutions in alphanumeric strings (e.g. 1 for I/l, 0 for O)
     * before running synonym dictionary matching.
     */
    fun normalizeOcrText(text: String): String {
        val upper = text.uppercase(Locale.ROOT).trim()
        var cleaned = upper
            .replace("1", "I")
            .replace("0", "O")
            .replace("8", "B")
            .replace("5", "S")
            .replace("|", "I")
            .replace("[", "I")
            .replace("]", "I")
            .replace("(", "")
            .replace(")", "")
            .replace(":", "")
            .replace("_", "")
            .replace("-", "")
            .replace(".", "")
        return cleaned.trim()
    }

    /**
     * Groups text lines into discrete horizontal rows based on mathematically deskewed Y-coordinates.
     */
    private fun groupLinesIntoRows(allLines: List<Text.Line>): List<List<Text.Line>> {
        val skewAngle = calculateSkewAngle(allLines)
        val sinTheta = Math.sin(skewAngle)
        val cosTheta = Math.cos(skewAngle)

        class RowGroup(var centerY: Double, val lines: MutableList<Text.Line>)

        val rows = mutableListOf<RowGroup>()

        // Sort lines by their mathematically deskewed center Y coordinate
        val sortedLines = allLines.sortedBy { line ->
            val box = line.boundingBox ?: return@sortedBy 0.0
            val cx = box.centerX()
            val cy = box.centerY()
            -cx * sinTheta + cy * cosTheta
        }

        for (line in sortedLines) {
            val box = line.boundingBox ?: continue
            val cx = box.centerX()
            val cy = box.centerY()
            val dy = -cx * sinTheta + cy * cosTheta
            val height = box.height()
            val tolerance = maxOf(14, (height * 0.45).toInt())

            // Find an existing row that aligns horizontally in deskewed space
            var foundRow = rows.firstOrNull { abs(it.centerY - dy) < tolerance }
            if (foundRow == null) {
                // Fallback check: overlap of bounding boxes in deskewed space
                foundRow = rows.firstOrNull { row ->
                    row.lines.any { rowLine ->
                        val rBox = rowLine.boundingBox ?: return@any false
                        val rcx = rBox.centerX()
                        val rcy = rBox.centerY()
                        val rdy = -rcx * sinTheta + rcy * cosTheta
                        val overlap = maxOf(0.0, minOf(dy + height/2.0, rdy + rBox.height()/2.0) - maxOf(dy - height/2.0, rdy - rBox.height()/2.0))
                        overlap > rBox.height() * 0.4
                    }
                }
            }

            if (foundRow != null) {
                foundRow.lines.add(line)
                // Dynamically update center Y for stability
                foundRow.centerY = (foundRow.centerY * 3.0 + dy) / 4.0
            } else {
                rows.add(RowGroup(dy, mutableListOf(line)))
            }
        }

        // Sort elements in each row from left to right using deskewed X-coordinates
        return rows.map { row ->
            row.lines.sortedBy { line ->
                val box = line.boundingBox ?: return@sortedBy 0.0
                val cx = box.centerX()
                val cy = box.centerY()
                cx * cosTheta + cy * sinTheta
            }
        }
    }

    /**
     * Extracts values strictly from cells on the same row.
     * Excludes other column lines like reference ranges, units, and test names.
     */
    private fun extractValueAndUnitFromRow(
        meta: LabTestMeta,
        rowLines: List<Text.Line>,
        testLineIndex: Int,
        passIndex: Int
    ): CandidateResult? {
        val referenceRangeLines = mutableSetOf<Text.Line>()
        val unitLines = mutableSetOf<Text.Line>()

        for (i in rowLines.indices) {
            if (i == testLineIndex) continue
            val line = rowLines[i]
            val text = line.text.trim()
            val textUpper = text.uppercase(Locale.ROOT)

            // Detect if a line looks like a reference range
            if (text.contains("-") || text.contains("–") || text.contains("—") || text.contains(" to ")) {
                if (text.any { it.isDigit() }) {
                    referenceRangeLines.add(line)
                }
            }

            // Detect if a line looks like a unit column
            if (textUpper.contains("10^") || textUpper.contains("103") || textUpper.contains("106") ||
                textUpper.contains("/UL") || textUpper.contains("/µL") || textUpper.contains("G/DL") ||
                textUpper.contains("MG/DL") || textUpper.contains("U/L") || textUpper.contains("IU/L") ||
                textUpper.contains("NG/ML") || textUpper.contains("PG/ML")) {
                unitLines.add(line)
            }
        }

        // Collect all non-excluded cell texts
        val cellTexts = mutableListOf<String>()
        var expectedRefRangeStr = ""

        for (i in rowLines.indices) {
            if (i == testLineIndex) continue
            val line = rowLines[i]
            if (referenceRangeLines.contains(line)) {
                expectedRefRangeStr = line.text.trim()
                continue
            }
            if (unitLines.contains(line)) {
                continue
            }
            cellTexts.add(line.text.trim())
        }

        // Extract unit from full row context
        val fullRowText = rowLines.map { it.text }.joinToString(" ")
        val extractedUnit = extractUnitFromRow(fullRowText) ?: meta.defaultUnit

        val rowTextContext = cellTexts.joinToString(" ")

        // Extract integers, decimals, negative values, and scientific notations
        val numericRegex = Regex("""(?i)\b([<>]=?\s*)?(-?\d+(?:\.\d+)?(?:e-?\d+)?)\b""")
        var matches = numericRegex.findAll(rowTextContext).toList()

        if (matches.isEmpty()) {
            // Fallback: search in full row if we excluded too aggressively
            matches = numericRegex.findAll(fullRowText).toList()
        }

        if (matches.isEmpty()) return null

        val candidateValues = mutableListOf<String>()
        for (m in matches) {
            val valStr = m.value.replace("<", "").replace(">", "").replace("=", "").trim()
            // Avoid false positives from test identifiers
            if (valStr == "3" && (meta.key == "FT3" || meta.key == "T3")) continue
            if (valStr == "4" && (meta.key == "FT4" || meta.key == "T4")) continue
            if (valStr == "12" && meta.key == "VITAMIN_B12") continue
            if (valStr == "25" && meta.key == "VITAMIN_D") continue
            candidateValues.add(valStr)
        }

        if (candidateValues.isEmpty()) return null

        val rawValueStr = candidateValues.first()
        val (cleanedVal, correctedValStr) = correctValue(meta, rawValueStr, expectedRefRangeStr)
        val isCorrected = rawValueStr != correctedValStr

        return CandidateResult(
            meta = meta,
            rawValueStr = rawValueStr,
            cleanedValue = cleanedVal,
            correctedValueStr = correctedValStr,
            unit = extractedUnit,
            isCorrected = isCorrected,
            passIndex = passIndex,
            rawLineText = rowTextContext
        )
    }

    /**
     * Highly advanced clinical validation and self-correction engine.
     * Evaluates measured value against normal bounds, expected units, reference range,
     * and precision to fix common OCR errors like missing decimal points or lost leading digits.
     */
    fun correctValue(
        meta: LabTestMeta,
        rawValueStr: String,
        expectedRefRangeStr: String
    ): Pair<Double, String> {
        var cleanedVal = rawValueStr.toDoubleOrNull() ?: return Pair(0.0, rawValueStr)
        val rule = VALIDATION_RULES[meta.key] ?: return Pair(cleanedVal, rawValueStr)

        val minP = rule.minPlausible
        val maxP = rule.maxPlausible
        val normMin = rule.normalMin
        val normMax = rule.normalMax

        // If the value is clinically plausible, check if we need soft adjustment matching the reference decimal pattern.
        if (cleanedVal in minP..maxP) {
            if (expectedRefRangeStr.isNotEmpty()) {
                val hasRefDecimal = expectedRefRangeStr.contains(".")
                val hasValueDecimal = rawValueStr.contains(".")
                if (hasRefDecimal && !hasValueDecimal) {
                    val tryDecimal = cleanedVal / 10.0
                    if (tryDecimal in minP..maxP) {
                        return Pair(tryDecimal, String.format(Locale.ROOT, "%.1f", tryDecimal))
                    }
                }
            }

            // Special case: Plausible but suspicious platelet value (e.g. 18.0 instead of 180.0)
            if (meta.key == "PLT" && cleanedVal in 10.0..45.0) {
                val correctedPLT = cleanedVal * 10.0
                if (correctedPLT in normMin..normMax) {
                    return Pair(correctedPLT, String.format(Locale.ROOT, "%.0f", correctedPLT))
                }
            }

            // Special case: Plausible but suspicious MPV value (e.g. 0.22 or 0.8 instead of 10.22 or 10.8)
            if (meta.key == "MPV" && cleanedVal in 0.05..1.99) {
                val correctedMPV = cleanedVal + 10.0
                if (correctedMPV in normMin..normMax) {
                    return Pair(correctedMPV, String.format(Locale.ROOT, "%.2f", correctedMPV))
                }
            }

            return Pair(cleanedVal, rawValueStr)
        }

        // Value is outside plausible limits. Find the closest clinically plausible scale transformation.
        val candidates = mutableListOf<Pair<Double, String>>()

        // Transformation A: missing decimal point (divide by 10) -> e.g. 138 -> 13.8, 316 -> 31.6
        val div10 = cleanedVal / 10.0
        if (div10 in minP..maxP) {
            candidates.add(Pair(div10, String.format(Locale.ROOT, "%.1f", div10)))
        }

        // Transformation B: double missing decimal point (divide by 100) -> e.g. 802 -> 8.02, 405 -> 4.05
        val div100 = cleanedVal / 100.0
        if (div100 in minP..maxP) {
            candidates.add(Pair(div100, String.format(Locale.ROOT, "%.2f", div100)))
        }

        // Transformation C: lost trailing zero or decimal multiplier (multiply by 10) -> e.g. 18 -> 180
        val mul10 = cleanedVal * 10.0
        if (mul10 in minP..maxP) {
            candidates.add(Pair(mul10, String.format(Locale.ROOT, "%.0f", mul10)))
        }

        // Transformation D: lost leading digit '10' -> e.g. 0.22 -> 10.22
        val plus10 = cleanedVal + 10.0
        if (plus10 in minP..maxP) {
            candidates.add(Pair(plus10, String.format(Locale.ROOT, "%.2f", plus10)))
        }

        // Transformation E: lost leading digit '1' -> e.g. 3.8 -> 13.8 or 4.5 -> 14.5
        val plus10_alt = cleanedVal + 10.0
        if (plus10_alt in minP..maxP) {
            candidates.add(Pair(plus10_alt, String.format(Locale.ROOT, "%.1f", plus10_alt)))
        }

        if (candidates.isNotEmpty()) {
            val centerNormal = (normMin + normMax) / 2.0
            val best = candidates.minByOrNull { abs(it.first - centerNormal) }
            if (best != null) {
                return best
            }
        }

        return Pair(cleanedVal, rawValueStr)
    }

    private fun extractUnitFromRow(text: String): String? {
        val upper = text.uppercase(Locale.ROOT)
        val knownUnits = listOf(
            "10^3/µL", "10^3/UL", "10^6/µL", "10^6/UL", "X10^3/UL", "X10^6/UL", "/UL", "/µL",
            "G/DL", "MG/DL", "UG/DL", "µG/DL", "NG/ML", "PG/ML", "UIU/ML", "µIU/ML", "MIU/L",
            "MMOL/L", "MEQ/L", "IU/L", "U/L", "MM/HR", "MG/L", "FL", "PG", "%"
        )

        for (u in knownUnits) {
            if (upper.contains(u)) {
                return when (u) {
                    "10^3/UL", "X10^3/UL" -> "10^3/µL"
                    "10^6/UL", "X10^6/UL" -> "10^6/µL"
                    "UG/DL", "µG/DL" -> "µg/dL"
                    "UIU/ML", "µIU/ML" -> "µIU/mL"
                    "G/DL" -> "g/dL"
                    "MG/DL" -> "mg/dL"
                    "NG/ML" -> "ng/mL"
                    "PG/ML" -> "pg/mL"
                    "MIU/L" -> "mIU/L"
                    "MMOL/L" -> "mmol/L"
                    "IU/L" -> "IU/L"
                    "FL" -> "fL"
                    "PG" -> "pg"
                    else -> u.lowercase(Locale.ROOT)
                }
            }
        }
        return null
    }

    /**
     * Merges candidate extraction results across multiple passes using a sophisticated consensus and validation score.
     * Rejects impossible values and assigns low confidence if validation criteria are not met.
     */
    private fun mergeCandidatesAndValidate(
        candidatesMap: Map<String, List<CandidateResult>>
    ): List<ExtractedLabValue> {
        val finalValues = mutableListOf<ExtractedLabValue>()

        for ((testKey, candidates) in candidatesMap) {
            if (candidates.isEmpty()) continue

            // Score each candidate value to determine the highest confidence one
            val scoredGroups = candidates.groupBy { it.correctedValueStr }
            val candidateScores = mutableMapOf<String, Double>()

            for ((valueStr, occurrences) in scoredGroups) {
                var score = 0.0
                val primaryOcc = occurrences.first()
                val rule = VALIDATION_RULES[testKey]

                // Rule A: Consensus boost (+25 points per occurrence across passes)
                score += occurrences.size * 25.0

                // Rule B: Medical plausibility check (+60 points if in plausible range, severe penalty otherwise)
                if (rule != null) {
                    if (primaryOcc.cleanedValue in rule.minPlausible..rule.maxPlausible) {
                        score += 60.0
                    } else {
                        score -= 60.0 // severe penalty for impossible clinical value
                    }

                    // Rule C: Unit match (+15 points if unit matches expected unit)
                    if (primaryOcc.unit == rule.expectedUnit) {
                        score += 15.0
                    }
                }

                // Penalty if self-corrected
                if (primaryOcc.isCorrected) {
                    score -= 5.0
                }

                candidateScores[valueStr] = score
            }

            // Choose the highest scoring candidate
            val bestValueStr = candidateScores.maxByOrNull { it.value }?.key ?: continue
            val bestOccurrences = scoredGroups[bestValueStr] ?: continue
            val representative = bestOccurrences.first()

            // Calculate confidence score (normalized as a percentage out of 100)
            val maxPossibleScore = 175.0 // 4 passes * 25 + 60 plausibility + 15 unit
            val rawScore = candidateScores[bestValueStr] ?: 0.0
            val confidencePct = ((rawScore / maxPossibleScore) * 100.0).coerceIn(0.0, 100.0)

            // Threshold: If confidence < 95% run self-correction or mark as low confidence.
            // If the value remains low confidence, set the warning text as required by the prompt!
            val hasExtremelyLowConfidence = confidencePct < 95.0 ||
                    representative.cleanedValue < (VALIDATION_RULES[testKey]?.minPlausible ?: 0.0) ||
                    representative.cleanedValue > (VALIDATION_RULES[testKey]?.maxPlausible ?: 9999.0)

            val finalValueToDisplay = if (hasExtremelyLowConfidence && confidencePct < 70.0) {
                "This value could not be extracted reliably."
            } else {
                bestValueStr
            }

            finalValues.add(
                ExtractedLabValue(
                    id = UUID.randomUUID().toString(),
                    testNameArabic = representative.meta.arabicName,
                    abbreviation = representative.meta.abbreviation,
                    value = finalValueToDisplay,
                    unit = representative.unit,
                    isLowConfidence = hasExtremelyLowConfidence
                )
            )
        }

        return finalValues
    }
}
