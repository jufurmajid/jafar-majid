package com.example.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class ReportWithTests(
    @Embedded val report: ReportEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "reportId"
    )
    val tests: List<ReportTestItemEntity>
)
