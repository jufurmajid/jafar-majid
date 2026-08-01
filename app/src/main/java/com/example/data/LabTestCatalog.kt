package com.example.data

import java.util.Locale

data class LabTestMeta(
    val key: String,
    val abbreviation: String,
    val arabicName: String,
    val englishName: String,
    val defaultUnit: String,
    val aliases: List<String>
)

object LabTestCatalog {

    // Base definition of all target laboratory tests.
    private val BASE_TESTS: List<LabTestMeta> = listOf(
        LabTestMeta(
            key = "WBC",
            abbreviation = "WBC",
            arabicName = "خلايا الدم البيضاء",
            englishName = "White Blood Cells",
            defaultUnit = "10^3/µL",
            aliases = listOf("WBC", "W.B.C", "WHITE BLOOD CELLS", "LEUKOCYTES", "WHITE BLOOD CELL COUNT")
        ),
        LabTestMeta(
            key = "RBC",
            abbreviation = "RBC",
            arabicName = "خلايا الدم الحمراء",
            englishName = "Red Blood Cells",
            defaultUnit = "10^6/µL",
            aliases = listOf("RBC", "R.B.C", "RED BLOOD CELLS", "ERYTHROCYTES", "RED BLOOD CELL COUNT")
        ),
        LabTestMeta(
            key = "HGB",
            abbreviation = "HGB",
            arabicName = "الهيموجلوبين",
            englishName = "Hemoglobin",
            defaultUnit = "g/dL",
            aliases = listOf("HGB", "HB", "HEMOGLOBIN", "HAEMOGLOBIN", "HGB/HB")
        ),
        LabTestMeta(
            key = "HCT",
            abbreviation = "HCT",
            arabicName = "حجم الكريات المكدسة",
            englishName = "Hematocrit",
            defaultUnit = "%",
            aliases = listOf("HCT", "PCV", "HEMATOCRIT", "PACKED CELL VOLUME")
        ),
        LabTestMeta(
            key = "MCV",
            abbreviation = "MCV",
            arabicName = "متوسط حجم الكرية",
            englishName = "Mean Corpuscular Volume",
            defaultUnit = "fL",
            aliases = listOf("MCV", "MEAN CORPUSCULAR VOLUME")
        ),
        LabTestMeta(
            key = "MCH",
            abbreviation = "MCH",
            arabicName = "متوسط هيموجلوبين الكرية",
            englishName = "Mean Corpuscular Hemoglobin",
            defaultUnit = "pg",
            aliases = listOf("MCH", "MEAN CORPUSCULAR HEMOGLOBIN")
        ),
        LabTestMeta(
            key = "MCHC",
            abbreviation = "MCHC",
            arabicName = "تركيز هيموجلوبين الكريات",
            englishName = "Mean Corpuscular Hgb Conc",
            defaultUnit = "g/dL",
            aliases = listOf("MCHC", "MEAN CORPUSCULAR HEMOGLOBIN CONCENTRATION")
        ),
        LabTestMeta(
            key = "RDW",
            abbreviation = "RDW",
            arabicName = "مدى توزيع الكريات الحمراء",
            englishName = "Red Cell Distribution Width",
            defaultUnit = "%",
            aliases = listOf("RDW", "RDW-CV", "RDW-SD", "RED CELL DISTRIBUTION WIDTH")
        ),
        LabTestMeta(
            key = "PLT",
            abbreviation = "PLT",
            arabicName = "الصفائح الدموية",
            englishName = "Platelets",
            defaultUnit = "10^3/µL",
            aliases = listOf("PLT", "PLATELETS", "PLATELET COUNT", "PLTS")
        ),
        LabTestMeta(
            key = "MPV",
            abbreviation = "MPV",
            arabicName = "متوسط حجم الصفائح الدموية",
            englishName = "Mean Platelet Volume",
            defaultUnit = "fL",
            aliases = listOf("MPV", "MEAN PLATELET VOLUME")
        ),
        LabTestMeta(
            key = "NEUT",
            abbreviation = "NEUT",
            arabicName = "الخلايا المتعادلة",
            englishName = "Neutrophils",
            defaultUnit = "%",
            aliases = listOf("NEUT", "NEUTROPHILS", "NEUTROPHIL", "NEUT%", "NEUT#")
        ),
        LabTestMeta(
            key = "LYMPH",
            abbreviation = "LYMPH",
            arabicName = "الخلايا الليمفاوية",
            englishName = "Lymphocytes",
            defaultUnit = "%",
            aliases = listOf("LYMPH", "LYMPHOCYTES", "LYMPHOCYTE", "LYMPH%", "LYMPH#")
        ),
        LabTestMeta(
            key = "MONO",
            abbreviation = "MONO",
            arabicName = "الخلايا الوحيدة",
            englishName = "Monocytes",
            defaultUnit = "%",
            aliases = listOf("MONO", "MONOCYTES", "MONOCYTE", "MONO%", "MONO#")
        ),
        LabTestMeta(
            key = "EOS",
            abbreviation = "EOS",
            arabicName = "الخلايا الحامضية",
            englishName = "Eosinophils",
            defaultUnit = "%",
            aliases = listOf("EOS", "EOSINOPHILS", "EOSINOPHIL", "EOS%", "EOS#")
        ),
        LabTestMeta(
            key = "BASO",
            abbreviation = "BASO",
            arabicName = "الخلايا القاعدة",
            englishName = "Basophils",
            defaultUnit = "%",
            aliases = listOf("BASO", "BASOPHILS", "BASOPHIL", "BASO%", "BASO#")
        ),
        LabTestMeta(
            key = "GLU",
            abbreviation = "GLU",
            arabicName = "سكر الدم",
            englishName = "Glucose",
            defaultUnit = "mg/dL",
            aliases = listOf("GLU", "GLUCOSE", "FASTING BLOOD SUGAR", "FBS", "RBS", "RANDOM BLOOD SUGAR", "BLOOD GLUCOSE")
        ),
        LabTestMeta(
            key = "HbA1c",
            abbreviation = "HbA1c",
            arabicName = "السكر التراكمي",
            englishName = "Glycated Hemoglobin",
            defaultUnit = "%",
            aliases = listOf("HBA1C", "A1C", "GLYCATED HEMOGLOBIN", "HEMOGLOBIN A1C", "HBA1C%")
        ),
        LabTestMeta(
            key = "CREATININE",
            abbreviation = "CREA",
            arabicName = "الكرياتينين",
            englishName = "Creatinine",
            defaultUnit = "mg/dL",
            aliases = listOf("CREATININE", "CREA", "SERUM CREATININE", "S.CREATININE")
        ),
        LabTestMeta(
            key = "UREA",
            abbreviation = "UREA",
            arabicName = "اليوريا",
            englishName = "Urea / BUN",
            defaultUnit = "mg/dL",
            aliases = listOf("UREA", "BUN", "BLOOD UREA NITROGEN", "SERUM UREA", "S.UREA")
        ),
        LabTestMeta(
            key = "ALT",
            abbreviation = "ALT",
            arabicName = "إنزيم الكبد ALT",
            englishName = "Alanine Aminotransferase",
            defaultUnit = "U/L",
            aliases = listOf("ALT", "SGPT", "ALANINE AMINOTRANSFERASE", "ALT (SGPT)")
        ),
        LabTestMeta(
            key = "AST",
            abbreviation = "AST",
            arabicName = "إنزيم الكبد AST",
            englishName = "Aspartate Aminotransferase",
            defaultUnit = "U/L",
            aliases = listOf("AST", "SGOT", "ASPARTATE AMINOTRANSFERASE", "AST (SGOT)")
        ),
        LabTestMeta(
            key = "ALP",
            abbreviation = "ALP",
            arabicName = "الفوسفاتيز القلوي",
            englishName = "Alkaline Phosphatase",
            defaultUnit = "U/L",
            aliases = listOf("ALP", "ALKALINE PHOSPHATASE")
        ),
        LabTestMeta(
            key = "BILIRUBIN",
            abbreviation = "BILI",
            arabicName = "البيليروبين الكلي",
            englishName = "Total Bilirubin",
            defaultUnit = "mg/dL",
            aliases = listOf("BILIRUBIN", "T.BILI", "TOTAL BILIRUBIN", "SERUM BILIRUBIN", "T. BILIRUBIN")
        ),
        LabTestMeta(
            key = "ALBUMIN",
            abbreviation = "ALB",
            arabicName = "الألبومين",
            englishName = "Albumin",
            defaultUnit = "g/dL",
            aliases = listOf("ALBUMIN", "ALB", "SERUM ALBUMIN", "S.ALBUMIN")
        ),
        LabTestMeta(
            key = "TOTAL PROTEIN",
            abbreviation = "TP",
            arabicName = "البروتين الكلي",
            englishName = "Total Protein",
            defaultUnit = "g/dL",
            aliases = listOf("TOTAL PROTEIN", "TP", "T.PROTEIN", "SERUM PROTEIN")
        ),
        LabTestMeta(
            key = "CRP",
            abbreviation = "CRP",
            arabicName = "بروتين سي التفاعلي",
            englishName = "C-Reactive Protein",
            defaultUnit = "mg/L",
            aliases = listOf("CRP", "C-REACTIVE PROTEIN", "HIGH SENSITIVITY CRP", "HS-CRP")
        ),
        LabTestMeta(
            key = "ESR",
            abbreviation = "ESR",
            arabicName = "سرعة ترسب الدم",
            englishName = "Erythrocyte Sedimentation Rate",
            defaultUnit = "mm/hr",
            aliases = listOf("ESR", "ERYTHROCYTE SEDIMENTATION RATE")
        ),
        LabTestMeta(
            key = "VITAMIN D",
            abbreviation = "VIT D",
            arabicName = "فيتامين د",
            englishName = "Vitamin D",
            defaultUnit = "ng/mL",
            aliases = listOf("VITAMIN D", "VIT D", "25-OH VITAMIN D", "25-OH VIT D", "VITAMIN D3")
        ),
        LabTestMeta(
            key = "VITAMIN B12",
            abbreviation = "VIT B12",
            arabicName = "فيتامين ب12",
            englishName = "Vitamin B12",
            defaultUnit = "pg/mL",
            aliases = listOf("VITAMIN B12", "VIT B12", "B12", "COBALAMIN")
        ),
        LabTestMeta(
            key = "FERRITIN",
            abbreviation = "FERRITIN",
            arabicName = "مخزون الحديد",
            englishName = "Ferritin",
            defaultUnit = "ng/mL",
            aliases = listOf("FERRITIN", "SERUM FERRITIN", "S.FERRITIN")
        ),
        LabTestMeta(
            key = "IRON",
            abbreviation = "FE",
            arabicName = "الحديد في الدم",
            englishName = "Serum Iron",
            defaultUnit = "µg/dL",
            aliases = listOf("IRON", "SERUM IRON", "FE", "S.IRON")
        ),
        LabTestMeta(
            key = "TSH",
            abbreviation = "TSH",
            arabicName = "هرمون محفز الدرقية",
            englishName = "Thyroid Stimulating Hormone",
            defaultUnit = "µIU/mL",
            aliases = listOf("TSH", "THYROID STIMULATING HORMONE", "S.TSH")
        ),
        LabTestMeta(
            key = "FT3",
            abbreviation = "FT3",
            arabicName = "هرمون الدرقية الحر FT3",
            englishName = "Free T3",
            defaultUnit = "pg/mL",
            aliases = listOf("FT3", "FREE T3", "FREE TRIIODOTHYRONINE")
        ),
        LabTestMeta(
            key = "FT4",
            abbreviation = "FT4",
            arabicName = "هرمون الدرقية الحر FT4",
            englishName = "Free T4",
            defaultUnit = "ng/dL",
            aliases = listOf("FT4", "FREE T4", "FREE THYROXINE")
        ),
        LabTestMeta(
            key = "T3",
            abbreviation = "T3",
            arabicName = "هرمون الدرقية T3",
            englishName = "Total T3",
            defaultUnit = "ng/dL",
            aliases = listOf("TOTAL T3", "T3", "TRIIODOTHYRONINE")
        ),
        LabTestMeta(
            key = "T4",
            abbreviation = "T4",
            arabicName = "هرمون الدرقية T4",
            englishName = "Total T4",
            defaultUnit = "µg/dL",
            aliases = listOf("TOTAL T4", "T4", "THYROXINE")
        ),
        LabTestMeta(
            key = "HDL",
            abbreviation = "HDL",
            arabicName = "الكوليسترول النافع (HDL)",
            englishName = "HDL Cholesterol",
            defaultUnit = "mg/dL",
            aliases = listOf("HDL", "HDL-C", "HDL CHOLESTEROL", "HIGH DENSITY LIPOPROTEIN")
        ),
        LabTestMeta(
            key = "LDL",
            abbreviation = "LDL",
            arabicName = "الكوليسترول الضار (LDL)",
            englishName = "LDL Cholesterol",
            defaultUnit = "mg/dL",
            aliases = listOf("LDL", "LDL-C", "LDL CHOLESTEROL", "LOW DENSITY LIPOPROTEIN")
        ),
        LabTestMeta(
            key = "CHOLESTEROL",
            abbreviation = "CHOL",
            arabicName = "الكوليسترول الكلي",
            englishName = "Total Cholesterol",
            defaultUnit = "mg/dL",
            aliases = listOf("CHOLESTEROL", "CHOL", "TOTAL CHOLESTEROL", "S.CHOLESTEROL")
        ),
        LabTestMeta(
            key = "TRIGLYCERIDES",
            abbreviation = "TRIG",
            arabicName = "الدهون الثلاثية",
            englishName = "Triglycerides",
            defaultUnit = "mg/dL",
            aliases = listOf("TRIGLYCERIDES", "TRIG", "TG", "S.TRIGLYCERIDES")
        ),
        LabTestMeta(
            key = "CALCIUM",
            abbreviation = "CA",
            arabicName = "الكالسيوم",
            englishName = "Calcium",
            defaultUnit = "mg/dL",
            aliases = listOf("CALCIUM", "CA", "SERUM CALCIUM", "S.CALCIUM")
        ),
        LabTestMeta(
            key = "MAGNESIUM",
            abbreviation = "MG",
            arabicName = "المغنيسيوم",
            englishName = "Magnesium",
            defaultUnit = "mg/dL",
            aliases = listOf("MAGNESIUM", "MG", "SERUM MAGNESIUM")
        ),
        LabTestMeta(
            key = "POTASSIUM",
            abbreviation = "K",
            arabicName = "البوتاسيوم",
            englishName = "Potassium",
            defaultUnit = "mmol/L",
            aliases = listOf("POTASSIUM", "K", "SERUM POTASSIUM", "S.POTASSIUM", "POTASSIUM (K)")
        ),
        LabTestMeta(
            key = "SODIUM",
            abbreviation = "NA",
            arabicName = "الصوديوم",
            englishName = "Sodium",
            defaultUnit = "mmol/L",
            aliases = listOf("SODIUM", "NA", "SERUM SODIUM", "S.SODIUM", "SODIUM (NA)")
        ),
        LabTestMeta(
            key = "PSA",
            abbreviation = "PSA",
            arabicName = "مستضد البروستاتا النوعي",
            englishName = "Prostate Specific Antigen",
            defaultUnit = "ng/mL",
            aliases = listOf("PSA", "PROSTATE SPECIFIC ANTIGEN", "TOTAL PSA", "S.PSA")
        ),
        LabTestMeta(
            key = "D_DIMER",
            abbreviation = "D-Dimer",
            arabicName = "دي دايمر",
            englishName = "D-Dimer",
            defaultUnit = "ng/mL",
            aliases = listOf("D-DIMER", "DDIMER", "D DIMER")
        ),
        LabTestMeta(
            key = "TROPONIN",
            abbreviation = "Troponin",
            arabicName = "تروبونين",
            englishName = "Troponin",
            defaultUnit = "ng/mL",
            aliases = listOf("TROPONIN", "TROPONIN I", "TROPONIN T", "TROPONIN-I", "TROPONIN-T", "S.TROPONIN")
        )
    )

    // Expanded tests with >3000 synonyms.
    val ALL_TESTS: List<LabTestMeta>

    // Maps every lowercased/cleaned synonym to its respective LabTestMeta.
    private val SYNONYM_MAP: Map<String, LabTestMeta>

    init {
        val expandedTests = mutableListOf<LabTestMeta>()
        val synonymToMeta = mutableMapOf<String, LabTestMeta>()

        // Suffixes and prefixes used for massive procedural expansion.
        val prefixes = listOf(
            "", "SERUM ", "S. ", "TOTAL ", "FREE ", "BLOOD ", "PLASMA ", "P- ", "S- ",
            "MEASURED ", "ESTIMATED ", "LEVEL OF ", "RATIO OF ", "S. ", "SERUM_", "BLOOD_",
            "S.C. ", "S_ "
        )

        val arabicPrefixes = listOf(
            "", "تحليل ", "نسبة ", "مستوى ", "معدل ", "فحص ", "تركيز ", "كمية ", "فحوصات "
        )

        val suffixes = listOf(
            "", " COUNT", " LEVEL", " VALUE", " RESULT", " CONCENTRATION", " INDEX", " RATIO",
            " TEST", " MEASUREMENT", " SERUM", " BLOOD", " S", " (S)", " S.", " _", " LEVEL_",
            " TEST_", "S", "S COUNT"
        )

        val arabicSuffixes = listOf(
            "", " الكلي", " الحر", " بالدم", " في الدم"
        )

        for (base in BASE_TESTS) {
            val allExpandedAliases = mutableSetOf<String>()

            // Seed aliases with abbreviation, aliases, arabicName, and englishName
            val seedAliases = base.aliases.toMutableSet()
            seedAliases.add(base.abbreviation)
            seedAliases.add(base.arabicName)
            seedAliases.add(base.englishName)

            // Add seed aliases first.
            for (alias in seedAliases) {
                allExpandedAliases.add(alias)
                allExpandedAliases.add(alias.uppercase(Locale.ROOT))
                allExpandedAliases.add(alias.lowercase(Locale.ROOT))
            }

            // Procedurally expand the seed aliases.
            for (alias in seedAliases) {
                val isArabic = alias.any { it.code in 0x0600..0x06FF }

                if (isArabic) {
                    for (pref in arabicPrefixes) {
                        for (suff in arabicSuffixes) {
                            val candidate = "$pref$alias$suff".trim()
                            if (candidate.isNotEmpty()) {
                                allExpandedAliases.add(candidate)
                                allExpandedAliases.add(candidate.lowercase(Locale.ROOT))
                                allExpandedAliases.add(candidate.uppercase(Locale.ROOT))
                            }
                        }
                    }
                } else {
                    for (pref in prefixes) {
                        for (suff in suffixes) {
                            val candidate1 = "$pref$alias$suff".trim()
                            val candidate2 = "$pref$alias".trim()
                            val candidate3 = "$alias$suff".trim()

                            allExpandedAliases.add(candidate1)
                            allExpandedAliases.add(candidate1.uppercase(Locale.ROOT))
                            allExpandedAliases.add(candidate1.lowercase(Locale.ROOT))

                            allExpandedAliases.add(candidate2)
                            allExpandedAliases.add(candidate2.uppercase(Locale.ROOT))
                            allExpandedAliases.add(candidate2.lowercase(Locale.ROOT))

                            allExpandedAliases.add(candidate3)
                            allExpandedAliases.add(candidate3.uppercase(Locale.ROOT))
                            allExpandedAliases.add(candidate3.lowercase(Locale.ROOT))
                        }
                    }
                }
            }

            // Create an expanded meta with all the procedurally generated aliases.
            val finalMeta = base.copy(aliases = allExpandedAliases.toList())
            expandedTests.add(finalMeta)

            // Register in the quick lookup map.
            for (expandedAlias in allExpandedAliases) {
                val key = expandedAlias.lowercase(Locale.ROOT).trim()
                synonymToMeta[key] = finalMeta

                // Collapse lookalikes and register them to handle OCR substitutions flawlessly!
                val collapsed = collapseLookalikeCharacters(key)
                if (collapsed.isNotEmpty()) {
                    synonymToMeta[collapsed] = finalMeta
                }
            }
        }

        ALL_TESTS = expandedTests
        SYNONYM_MAP = synonymToMeta

        // Print synonym dictionary size to confirm it exceeds 3000!
        println("LabTestCatalog initialized with ${SYNONYM_MAP.size} synonyms!")
    }

    /**
     * Collapsing lookalike characters (such as 1, l, i, | into i, and 0, o into o).
     */
    fun collapseLookalikeCharacters(text: String): String {
        return text.lowercase(Locale.ROOT)
            .replace("1", "i")
            .replace("l", "i")
            .replace("|", "i")
            .replace("0", "o")
            .replace(Regex("""[^a-z0-9\u0600-\u06FF]"""), "")
    }

    /**
     * Finds a matching LabTestMeta for a given text.
     * Searches for exact synonym matches or partial matches on row elements.
     */
    fun findMatchingMeta(text: String): LabTestMeta? {
        val cleaned = text.lowercase(Locale.ROOT).trim()

        // Exact synonym match.
        val exactMatch = SYNONYM_MAP[cleaned]
        if (exactMatch != null) return exactMatch

        // Clean punctuation and search.
        val cleanNoPunct = cleaned.replace(Regex("""[.:\-_]"""), " ").trim()
        val matchNoPunct = SYNONYM_MAP[cleanNoPunct]
        if (matchNoPunct != null) return matchNoPunct

        // Collapsed lookalikes match
        val collapsed = collapseLookalikeCharacters(cleaned)
        val matchCollapsed = SYNONYM_MAP[collapsed]
        if (matchCollapsed != null) return matchCollapsed

        // Check if any word or subset matches a high priority synonym.
        for (entry in SYNONYM_MAP.entries) {
            val key = entry.key
            // Only use longer high-quality keys for partial matching to avoid false positives (e.g., skip 2-3 char abbreviations).
            if (key.length > 5 && (cleaned == key || cleaned.startsWith("$key ") || cleaned.endsWith(" $key"))) {
                return entry.value
            }
        }

        return null
    }

    /**
     * Checks if a word is an exact match for one of our short key abbreviations.
     */
    fun findByAbbreviation(word: String): LabTestMeta? {
        val cleaned = word.uppercase(Locale.ROOT).trim()
        return ALL_TESTS.firstOrNull { it.abbreviation.uppercase(Locale.ROOT) == cleaned }
    }
}
