package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {

    @Transaction
    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReportsWithTests(): Flow<List<ReportWithTests>>

    @Transaction
    @Query("SELECT * FROM reports WHERE id = :reportId")
    suspend fun getReportWithTestsById(reportId: String): ReportWithTests?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestItems(items: List<ReportTestItemEntity>)

    @Transaction
    suspend fun saveReportWithTests(report: ReportEntity, tests: List<ReportTestItemEntity>) {
        insertReport(report)
        insertTestItems(tests)
    }

    @Query("DELETE FROM reports WHERE id = :reportId")
    suspend fun deleteReportById(reportId: String)

    @Query("DELETE FROM report_test_items WHERE reportId = :reportId")
    suspend fun deleteTestItemsForReport(reportId: String)

    @Transaction
    suspend fun deleteReportAndTests(reportId: String) {
        deleteTestItemsForReport(reportId)
        deleteReportById(reportId)
    }
}
