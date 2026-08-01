package com.example.data

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.ReportEntity
import com.example.data.local.ReportTestItemEntity
import com.example.data.local.ReportWithTests
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ReportRepository(context: Context) {

    private val reportDao = AppDatabase.getDatabase(context).reportDao()

    val allReports: Flow<List<ReportWithTests>> = reportDao.getAllReportsWithTests()

    suspend fun getReportById(reportId: String): ReportWithTests? {
        return reportDao.getReportWithTestsById(reportId)
    }

    suspend fun saveReport(
        patientAge: Int,
        patientGender: Gender,
        imageUriString: String,
        evaluatedResults: List<EvaluatedLabResult>
    ): String {
        val reportId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()

        val normalCount = evaluatedResults.count { it.status == TestStatus.NORMAL }
        val abnormalCount = evaluatedResults.size - normalCount

        val reportEntity = ReportEntity(
            id = reportId,
            timestamp = timestamp,
            patientAge = patientAge,
            patientGender = patientGender.name,
            imageUriString = imageUriString,
            totalTestsCount = evaluatedResults.size,
            normalCount = normalCount,
            abnormalCount = abnormalCount
        )

        val testItemEntities = evaluatedResults.map { res ->
            ReportTestItemEntity(
                id = UUID.randomUUID().toString(),
                reportId = reportId,
                testKey = res.testKey,
                arabicName = res.arabicName,
                abbreviation = res.abbreviation,
                rawValue = res.rawValue,
                unit = res.unit,
                status = res.status.name,
                referenceRange = res.formattedRefRange,
                isLowConfidence = res.isLowConfidence
            )
        }

        reportDao.saveReportWithTests(reportEntity, testItemEntities)
        return reportId
    }

    suspend fun deleteReport(reportId: String) {
        reportDao.deleteReportAndTests(reportId)
    }
}
