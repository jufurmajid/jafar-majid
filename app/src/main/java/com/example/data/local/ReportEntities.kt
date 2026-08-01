package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val patientAge: Int,
    val patientGender: String,
    val imageUriString: String,
    val totalTestsCount: Int,
    val normalCount: Int,
    val abnormalCount: Int
)

@Entity(tableName = "report_test_items")
data class ReportTestItemEntity(
    @PrimaryKey val id: String,
    val reportId: String,
    val testKey: String,
    val arabicName: String,
    val abbreviation: String,
    val rawValue: String,
    val unit: String,
    val status: String,
    val referenceRange: String,
    val isLowConfidence: Boolean
)
