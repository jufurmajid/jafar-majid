package com.example

import com.example.data.LabTestCatalog
import com.example.ocr.LabOcrProcessor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LabOcrProcessorTest {

    @Test
    fun testNormalizeOcrText() {
        val input = "HEM0GL0B1N:"
        val result = LabOcrProcessor.normalizeOcrText(input)
        assertEquals("HEMOGLOBIN", result)
    }

    @Test
    fun testSynonymExpansionCatalog() {
        // Test that some target procedural names and synonyms resolve perfectly
        val matchHgb = LabTestCatalog.findMatchingMeta("hgb")
        assertNotNull(matchHgb)
        assertEquals("HGB", matchHgb?.key)

        val matchHemoglobin = LabTestCatalog.findMatchingMeta("HEMOGLOBIN")
        assertNotNull(matchHemoglobin)
        assertEquals("HGB", matchHemoglobin?.key)

        val matchArabicHgb = LabTestCatalog.findMatchingMeta("نسبة الهيموجلوبين")
        assertNotNull(matchArabicHgb)
        assertEquals("HGB", matchArabicHgb?.key)

        val matchPlt = LabTestCatalog.findMatchingMeta("plt count")
        assertNotNull(matchPlt)
        assertEquals("PLT", matchPlt?.key)

        val matchRbc = LabTestCatalog.findMatchingMeta("RED BLOOD CELLS")
        assertNotNull(matchRbc)
        assertEquals("RBC", matchRbc?.key)

        // Assert that lookalike collapsing resolves P1ATELET_COUNT perfectly!
        val matchLookalike = LabTestCatalog.findMatchingMeta("P1ATELET_COUNT")
        assertNotNull(matchLookalike)
        assertEquals("PLT", matchLookalike?.key)
    }

    @Test
    fun testClinicalSelfCorrection_MPV_LosingLeadingDigit() {
        // True value was 10.22, but OCR extracts 0.22
        val meta = LabTestCatalog.findMatchingMeta("MPV")!!
        val (cleaned, correctedStr) = LabOcrProcessor.correctValue(meta, "0.22", "")
        assertEquals(10.22, cleaned, 0.01)
        assertEquals("10.22", correctedStr)
    }

    @Test
    fun testClinicalSelfCorrection_Platelets_LosingTrailingZero() {
        // True value was 180, but OCR extracts 18
        val meta = LabTestCatalog.findMatchingMeta("PLT")!!
        val (cleaned, correctedStr) = LabOcrProcessor.correctValue(meta, "18", "")
        assertEquals(180.0, cleaned, 0.01)
        assertEquals("180", correctedStr)
    }

    @Test
    fun testClinicalSelfCorrection_RDW_MissingDecimalPoint() {
        // True value was 31.6, but OCR extracts 316
        val meta = LabTestCatalog.findMatchingMeta("RDW")!!
        val (cleaned, correctedStr) = LabOcrProcessor.correctValue(meta, "316", "")
        assertEquals(31.6, cleaned, 0.01)
        assertEquals("31.6", correctedStr)
    }

    @Test
    fun testClinicalSelfCorrection_RBC_MissingDecimalPoint() {
        // True value was 4.05, but OCR extracts 405
        val meta = LabTestCatalog.findMatchingMeta("RBC")!!
        val (cleaned, correctedStr) = LabOcrProcessor.correctValue(meta, "405", "")
        assertEquals(4.05, cleaned, 0.01)
        assertEquals("4.05", correctedStr)
    }

    @Test
    fun testClinicalSelfCorrection_HGB_MissingDecimalPoint() {
        // True value was 13.8, but OCR extracts 138
        val meta = LabTestCatalog.findMatchingMeta("HGB")!!
        val (cleaned, correctedStr) = LabOcrProcessor.correctValue(meta, "138", "")
        assertEquals(13.8, cleaned, 0.01)
        assertEquals("13.8", correctedStr)
    }

    @Test
    fun testClinicalSelfCorrection_Calcium_MissingDecimalPoint() {
        // True value was 8.02, but OCR extracts 802
        val meta = LabTestCatalog.findMatchingMeta("CALCIUM")!!
        val (cleaned, correctedStr) = LabOcrProcessor.correctValue(meta, "802", "")
        assertEquals(8.02, cleaned, 0.01)
        assertEquals("8.02", correctedStr)
    }
}
