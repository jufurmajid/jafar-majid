package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.example.data.EvaluatedLabResult
import com.example.data.Gender
import com.example.data.TestStatus
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generatePdfReport(
        context: Context,
        patientAge: Int,
        patientGender: Gender,
        evaluatedResults: List<EvaluatedLabResult>,
        timestamp: Long = System.currentTimeMillis()
    ): File? {
        try {
            val pdfDocument = PdfDocument()
            val pageWidth = 595 // A4 width in points
            val pageHeight = 842 // A4 height in points

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val titlePaint = TextPaint().apply {
                color = Color.parseColor("#1565C0") // Medical Blue
                textSize = 18f
                isFakeBoldText = true
            }

            val headerPaint = TextPaint().apply {
                color = Color.parseColor("#1E293B")
                textSize = 12f
                isFakeBoldText = true
            }

            val bodyPaint = TextPaint().apply {
                color = Color.parseColor("#334155")
                textSize = 10f
            }

            val subTextPaint = TextPaint().apply {
                color = Color.parseColor("#64748B")
                textSize = 9f
            }

            val linePaint = Paint().apply {
                color = Color.parseColor("#E2E8F0")
                strokeWidth = 1f
            }

            var currentY = 40f

            // 1. Draw Document Header
            val dateFormat = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar"))
            val dateStr = dateFormat.format(Date(timestamp))

            canvas.drawText("مترجم التحاليل الطبية - تقرير التقييم المخبري", 40f, currentY, titlePaint)
            currentY += 20f
            canvas.drawText("تقرير تحليلي تعليمي توضيحي للنتائج المخبرية", 40f, currentY, subTextPaint)
            currentY += 15f
            canvas.drawLine(40f, currentY, pageWidth - 40f, currentY, linePaint)
            currentY += 20f

            // 2. Patient Info Box
            val bgRect = RectF(40f, currentY, pageWidth - 40f, currentY + 50f)
            val boxPaint = Paint().apply {
                color = Color.parseColor("#F1F5F9")
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(bgRect, 8f, 8f, boxPaint)

            val genderStr = if (patientGender == Gender.MALE) "ذكر" else "أنثى"
            canvas.drawText("العمر: $patientAge سنة  |  الجنس: $genderStr", 55f, currentY + 22f, headerPaint)
            canvas.drawText("تاريخ الصدور: $dateStr", 55f, currentY + 40f, bodyPaint)

            currentY += 65f

            // 3. Disclaimer Header Box
            val discRect = RectF(40f, currentY, pageWidth - 40f, currentY + 40f)
            val discBgPaint = Paint().apply {
                color = Color.parseColor("#FEF3C7") // Warm Amber
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(discRect, 8f, 8f, discBgPaint)

            val discTextPaint = TextPaint().apply {
                color = Color.parseColor("#92400E")
                textSize = 9f
                isFakeBoldText = true
            }
            canvas.drawText("⚠️ تنبيه هام: هذا التقرير مخصص لأغراض تثقيفية وتعليمية فقط، ولا يغني إطلاقاً عن مراجعة الطبيب المختص.", 50f, currentY + 24f, discTextPaint)

            currentY += 55f

            // 4. Results List
            canvas.drawText("تفاصيل التحاليل المقاسة (${evaluatedResults.size}):", 40f, currentY, headerPaint)
            currentY += 15f

            for (result in evaluatedResults) {
                // Check if page height boundary reached
                if (currentY + 110f > pageHeight - 50f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = 40f
                }

                val cardHeight = 90f
                val cardRect = RectF(40f, currentY, pageWidth - 40f, currentY + cardHeight)

                val (statusBgHex, statusTextHex, statusText) = when (result.status) {
                    TestStatus.NORMAL -> Triple("#E8F5E9", "#2E7D32", "طبيعي 🟢")
                    TestStatus.HIGH -> Triple("#FFEBEE", "#C62828", "مرتفع 🔴")
                    TestStatus.LOW -> Triple("#FFF3E0", "#E65100", "منخفض 🟠")
                }

                val cardBgPaint = Paint().apply {
                    color = Color.parseColor("#FFFFFF")
                    style = Paint.Style.FILL
                }
                val cardBorderPaint = Paint().apply {
                    color = Color.parseColor("#CBD5E1")
                    style = Paint.Style.STROKE
                    strokeWidth = 1f
                }

                canvas.drawRoundRect(cardRect, 8f, 8f, cardBgPaint)
                canvas.drawRoundRect(cardRect, 8f, 8f, cardBorderPaint)

                // Test Name & Abbreviation
                canvas.drawText("${result.arabicName} (${result.abbreviation})", 55f, currentY + 22f, headerPaint)

                // Result value & Range
                val valPaint = TextPaint().apply {
                    color = Color.parseColor(statusTextHex)
                    textSize = 12f
                    isFakeBoldText = true
                }
                canvas.drawText("النتيجة: ${result.rawValue} ${result.unit}", 55f, currentY + 42f, valPaint)
                canvas.drawText("المعدل الطبيعي: ${result.formattedRefRange}", 250f, currentY + 42f, bodyPaint)

                // Status Badge
                val badgeRect = RectF(pageWidth - 140f, currentY + 12f, pageWidth - 55f, currentY + 32f)
                val badgeBgPaint = Paint().apply {
                    color = Color.parseColor(statusBgHex)
                    style = Paint.Style.FILL
                }
                val badgeTextPaint = TextPaint().apply {
                    color = Color.parseColor(statusTextHex)
                    textSize = 9f
                    isFakeBoldText = true
                }
                canvas.drawRoundRect(badgeRect, 6f, 6f, badgeBgPaint)
                canvas.drawText(statusText, pageWidth - 130f, currentY + 26f, badgeTextPaint)

                // Explanation line
                val shortSummary = result.knowledge.whatIsIt.take(85) + if (result.knowledge.whatIsIt.length > 85) "..." else ""
                canvas.drawText("نبذة: $shortSummary", 55f, currentY + 68f, subTextPaint)

                currentY += cardHeight + 12f
            }

            // Footer
            canvas.drawText("تمت المعالجة وحفظ التقرير أوفلاين بالكامل على الهاتف.", 40f, pageHeight - 30f, subTextPaint)
            canvas.drawText("صفحة $pageNumber", pageWidth - 80f, pageHeight - 30f, subTextPaint)

            pdfDocument.finishPage(page)

            // Save PDF to cache directory
            val pdfDir = File(context.cacheDir, "pdfs")
            if (!pdfDir.exists()) pdfDir.mkdirs()

            val pdfFile = File(pdfDir, "Medical_Report_${timestamp}.pdf")
            FileOutputStream(pdfFile).use { out ->
                pdfDocument.writeTo(out)
            }
            pdfDocument.close()

            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun sharePdf(context: Context, pdfFile: File) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "تقرير التحاليل الطبية")
                putExtra(Intent.EXTRA_TEXT, "مرفق تقرير تقييم التحاليل الطبية المخبرية.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "مشاركة تقرير PDF")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
