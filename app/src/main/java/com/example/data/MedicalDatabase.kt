package com.example.data

import androidx.compose.ui.graphics.Color
import java.util.UUID

enum class TestStatus(
    val titleArabic: String,
    val colorHex: String,
    val isNormal: Boolean
) {
    NORMAL(
        titleArabic = "طبيعي",
        colorHex = "#2E7D32", // Green
        isNormal = true
    ),
    HIGH(
        titleArabic = "مرتفع",
        colorHex = "#C62828", // Red
        isNormal = false
    ),
    LOW(
        titleArabic = "منخفض",
        colorHex = "#E65100", // Orange
        isNormal = false
    )
}

data class RefRange(
    val min: Double?,
    val max: Double?,
    val unit: String,
    val formatted: String
)

data class LabTestKnowledge(
    val key: String,
    val abbreviation: String,
    val arabicName: String,
    val englishName: String,
    val defaultUnit: String,
    val whatIsIt: String,
    val bodyFunction: String,
    val whyDoctorOrders: String,
    val highCauses: String,
    val lowCauses: String,
    val symptoms: String,
    val recommendations: String,
    val helpfulFoods: String,
    val educationalNotes: String,
    val refRangeProvider: (age: Int, gender: Gender) -> RefRange
)

data class EvaluatedLabResult(
    val id: String = UUID.randomUUID().toString(),
    val testKey: String,
    val arabicName: String,
    val abbreviation: String,
    val rawValue: String,
    val parsedValue: Double?,
    val unit: String,
    val formattedRefRange: String,
    val status: TestStatus,
    val isLowConfidence: Boolean = false,
    val knowledge: LabTestKnowledge
)

object MedicalDatabase {

    val KNOWLEDGE_MAP: Map<String, LabTestKnowledge> = mapOf(
        "WBC" to LabTestKnowledge(
            key = "WBC",
            abbreviation = "WBC",
            arabicName = "خلايا الدم البيضاء",
            englishName = "White Blood Cells",
            defaultUnit = "10^3/µL",
            whatIsIt = "تحليل يقيس عدد خلايا الدم البيضاء، وهي جزء أساسي من جهاز المناعة في الجسم.",
            bodyFunction = "تدافع خلايا الدم البيضاء عن الجسم ضد الميكروبات والجراثيم والفيروسات والالتهابات.",
            whyDoctorOrders = "يُطلب للكشف عن وجود التهابات بكتيرية أو فيروسية، أو تقييم استجابة جهاز المناعة والمتابعة الدورية.",
            highCauses = "العدوى البكتيرية، الالتهابات الحادة، الإجهاد البدني والنفسي الشديد، التدخين، أو تناول بعض الأدوية مثل الكورتيزون.",
            lowCauses = "العدوى الفيروسية الشديدة (مثل الأنفلونزا)، اضطرابات نخاع العظم، نقص بعض الفيتامينات، أو التأثير الجانبي لبعض الأدوية.",
            symptoms = "ارتفاع الحرارة، الحمى، التعب والوهن، أو التعرق ليلاً عند وجود التهاب نشط.",
            recommendations = "الراحة الكافية، تناول كميات وافرة من الماء والسوائل، ومراجعة الطبيب لتحديد السبب الدقيق واستكمال الفحوصات.",
            helpfulFoods = "الأطعمة الغنية بفيتايمين C مثل البرتقال والليمون، الأغذية الغنية بالمغذيات مثل البروكلي والمكسرات والعسل.",
            educationalNotes = "تختلف النسبة الطبيعية للأطفال حديثي الولادة والأطفال مقارنة بالبالغين.",
            refRangeProvider = { age, _ ->
                if (age < 12) RefRange(5.0, 14.5, "10^3/µL", "5.0 - 14.5 ×10³ / µL")
                else RefRange(4.0, 11.0, "10^3/µL", "4.0 - 11.0 ×10³ / µL")
            }
        ),
        "RBC" to LabTestKnowledge(
            key = "RBC",
            abbreviation = "RBC",
            arabicName = "خلايا الدم الحمراء",
            englishName = "Red Blood Cells",
            defaultUnit = "10^6/µL",
            whatIsIt = "فحص يقيس عدد كريات الدم الحمراء المسؤولة عن نقل الأكسجين من الرئتين إلى جميع خلايا الجسم.",
            bodyFunction = "تحتوي خلايا الدم الحمراء على بروتين الهيموجلوبين الذي يرتبط بالأكسجين وينقله لأعضاء الجسم.",
            whyDoctorOrders = "لتشخيص فقر الدم (الأنيميا)، التعب المزمن، أو حالات زيادة كرات الدم الحمراء.",
            highCauses = "الجفاف ونقص السوائل، التدخين، العيش في المناطق المرتفعة عن سطح البحر، أو أمراض الرئة المزمنة.",
            lowCauses = "فقر الدم، سوء التغذية، نقص الحديد أو الفيتامينات، النزيف الفعلي أو الدورة الشهرية الغزيرة.",
            symptoms = "شحوب الوجه، التعب الشديد عند المجهود، الشديد، الصداع، أو الإرهاق المستمر.",
            recommendations = "شرب ماء كافٍ، تجنب التدخين، ومتابعة الفحوصات الطبية لمراقبة أسباب تغير العدد.",
            helpfulFoods = "الأغذية الغنية بالحديد مثل اللحوم الحمراء والسبانخ والعدس والكبذة والبيض.",
            educationalNotes = "تركيز كريات الدم الحمراء أعلى بشكل طبيعي لدى الذكور مقارنة بالإناث.",
            refRangeProvider = { age, gender ->
                if (gender == Gender.MALE) RefRange(4.3, 5.9, "10^6/µL", "4.3 - 5.9 ×10⁶ / µL")
                else RefRange(3.8, 5.2, "10^6/µL", "3.8 - 5.2 ×10⁶ / µL")
            }
        ),
        "HGB" to LabTestKnowledge(
            key = "HGB",
            abbreviation = "HGB",
            arabicName = "الهيموجلوبين",
            englishName = "Hemoglobin",
            defaultUnit = "g/dL",
            whatIsIt = "البروتين الحديدي الموجود داخل خلايا الدم الحمراء والذي يمنح الدم لونه الأحمر القاني.",
            bodyFunction = "يحمل الأكسجين من الرئتين إلى أنسجة وأعضاء الجسم، ويعيد ثاني أكسيد الكربون إلى الرئتين للتخلص منه.",
            whyDoctorOrders = "الكشف عن فقر الدم، تقييم حالات النزيف، متابعة الحوامل، والاطمئنان العام على الصحة.",
            highCauses = "الجفاف الشديد، التدخين المزمن، العيش في الجبال المرتفعة، أو مشاكل في الرئتين.",
            lowCauses = "فقر الدم بسبب نقص الحديد، سوء التغذية، النزيف المزمن، الدورة الشهرية لدى النساء، أو بعض الأمراض المزمنة.",
            symptoms = "الدوخة، شحوب الجلد والشفاه، تسارع ضربات القلب عند الصعود، والشعور بالإرهاق.",
            recommendations = "حرص على نظام غذائي متوازن، التقليل من الشاي والقهوة فوراً بعد الوجبات للحد من تثبيط امتصاص الحديد.",
            helpfulFoods = "اللحوم الحمراء، الكبدة، الدواجن، الأسماك، السبانخ، العدس، والفواكه الغنية بفيتامين C للمساعدة على الامتصاص.",
            educationalNotes = "تأكد من تناول فيتامين ج (فيتامين C) مع الأغذية النباتية الغنية بالحديد لتعزيز الامتصاص.",
            refRangeProvider = { age, gender ->
                if (gender == Gender.MALE) RefRange(13.5, 17.5, "g/dL", "13.5 - 17.5 g/dL")
                else RefRange(12.0, 15.5, "g/dL", "12.0 - 15.5 g/dL")
            }
        ),
        "HCT" to LabTestKnowledge(
            key = "HCT",
            abbreviation = "HCT",
            arabicName = "حجم الكريات المكدسة",
            englishName = "Hematocrit",
            defaultUnit = "%",
            whatIsIt = "نسبة حجم خلايا الدم الحمراء مقارنة بالحجم الكلي للدم.",
            bodyFunction = "يعكس مدى لزوجة الدم وقدرته على توصيل الأكسجين بكفاءة دون التسبب بلزوجة زائدة.",
            whyDoctorOrders = "متابعة مستوى الجفاف، تشخيص الأنيميا، وتقييم حجم الدم ونسبة السوائل.",
            highCauses = "الجفاف وعدم شرب الماء الكافي، التدخين، الارتفاعات، أو فرط حمر الدم.",
            lowCauses = "فقر الدم، النزيف، زيادة السوائل في الجسم، أو سوء التغذية.",
            symptoms = "الصداع والدوخة عند الانخفاض، أو الشعور بثقل وثقل بالرأس عند الارتفاع الشديد.",
            recommendations = "شرب كميات كافية من المياه طوال اليوم والتأكد من التغذية الصحية.",
            helpfulFoods = "الأغذية الغنية بالحديد وفيتامينات ب المساعدة على تصنيع كرات الدم.",
            educationalNotes = "يرتبط بشكل مباشر بقيمة الهيموجلوبين (عادة تكون نسبة HCT تعادل 3 أضعاف قيمة HGB تقريباً).",
            refRangeProvider = { age, gender ->
                if (gender == Gender.MALE) RefRange(41.0, 50.0, "%", "41.0 - 50.0 %")
                else RefRange(36.0, 46.0, "%", "36.0 - 46.0 %")
            }
        ),
        "MCV" to LabTestKnowledge(
            key = "MCV",
            abbreviation = "MCV",
            arabicName = "متوسط حجم الكرية",
            englishName = "Mean Corpuscular Volume",
            defaultUnit = "fL",
            whatIsIt = "مؤشر يقيس متوسط حجم خلية الدم الحمراء الواحدة.",
            bodyFunction = "يساعد على تحديد نوع فقر الدم (هل هو بكرات صغيرا الحجم أم كبار الحجم).",
            whyDoctorOrders = "لتصنيف ونوع فقر الدم بدقة لمعرفة السبب العلاجي المناسب.",
            highCauses = "نقص فيتامين ب12 أو الفوليك أسيد، أمراض الكبد، أو بعض العلاجات الدوائية.",
            lowCauses = "فقر الدم الناتج عن نقص الحديد، أو الثلاسيميا (فقر دم البحر الأبيض المتوسط).",
            symptoms = "تعب، خدران بالطرفين أو تنميل القدمين (في حالات نقص ب12)، أو شحوب.",
            recommendations = "تناول الفيتامينات أو المكملات الغذائية المناسبة بناءً على استشارة الطبيب.",
            helpfulFoods = "للارتفاع: الأغذية الخضراء والألبان. للانخفاض: اللحوم الحمراء والأغذية الغنية بالحديد.",
            educationalNotes = "يساعد الطبيب في التفريق السريع بين نقص الحديد ونقص فيتامين ب12.",
            refRangeProvider = { _, _ ->
                RefRange(80.0, 100.0, "fL", "80.0 - 100.0 fL")
            }
        ),
        "MCH" to LabTestKnowledge(
            key = "MCH",
            abbreviation = "MCH",
            arabicName = "متوسط هيموجلوبين الكرية",
            englishName = "Mean Corpuscular Hemoglobin",
            defaultUnit = "pg",
            whatIsIt = "معدل كمية الهيموجلوبين الموجودة داخل خلية الدم الحمراء الواحدة.",
            bodyFunction = "يعبر عن مدى تشبع الكرية ببروتين الهيموجلوبين الحامل للأكسجين.",
            whyDoctorOrders = "جزء من صورة الدم الكاملة (CBC) لتصنيف شكل ولون كرات الدم الحمراء.",
            highCauses = "نقص فيتامين B12 أو حمض الفوليك.",
            lowCauses = "نقص كمية الحديد المتوفرة للدم.",
            symptoms = "إرهاق عام وشحوب ولون باهت للجلد.",
            recommendations = "الاهتمام بالتغذية الصحية والتأكد من أسباب نقص صبغة الدم.",
            helpfulFoods = "المأكولات البحرية، الكبدة، المكسرات، والخضروات الورقية.",
            educationalNotes = "يرتبط تغيره غالباً بمؤشر MCV.",
            refRangeProvider = { _, _ ->
                RefRange(27.0, 33.0, "pg", "27.0 - 33.0 pg")
            }
        ),
        "MCHC" to LabTestKnowledge(
            key = "MCHC",
            abbreviation = "MCHC",
            arabicName = "تركيز هيموجلوبين الكريات",
            englishName = "Mean Corpuscular Hgb Conc",
            defaultUnit = "g/dL",
            whatIsIt = "تركيز الهيموجلوبين بالنسبة لحجم كريات الدم الحمراء المكدسة.",
            bodyFunction = "يقيس مدى كثافة تشبع كرات الدم الحمراء بالهيموجلوبين.",
            whyDoctorOrders = "تقييم حالات صغر أو كبر كرات الدم وحالات تكور الكريات الحمراء.",
            highCauses = "حالات تكور الكريات الحمراء الوراثي، الجفاف الشديد.",
            lowCauses = "فقر الدم الشديد بنقص الحديد أو الثلاسيميا.",
            symptoms = "تعب، دوخة، ضعف باللياقة والجهد.",
            recommendations = "مراجعة الطبيب وتناول النمط الغذائي المتوازن.",
            helpfulFoods = "البروتينات والأغذية الغنية بالحديد والفيتامينات.",
            educationalNotes = "يحسب رياضياً من قيم الهيموجلوبين والهيماتوكريت.",
            refRangeProvider = { _, _ ->
                RefRange(32.0, 36.0, "g/dL", "32.0 - 36.0 g/dL")
            }
        ),
        "RDW" to LabTestKnowledge(
            key = "RDW",
            abbreviation = "RDW",
            arabicName = "مدى توزيع الكريات الحمراء",
            englishName = "Red Cell Distribution Width",
            defaultUnit = "%",
            whatIsIt = "مقاييس يوضح مدى التباين والتفاوت في حجم كريات الدم الحمراء.",
            bodyFunction = "يدل على مدى تجانس حجم الكريات الحمراء في مجرى الدم.",
            whyDoctorOrders = "التمييز بين نقص الحديد المبكر والأسباب الأخرى لنقص كرات الدم.",
            highCauses = "اختلاف أحجام الكريات نتيجة نقص الحديد، نقص فيتامين B12، أو حديثاً بعد نقل الدم.",
            lowCauses = "يدل على أن جميع الكريات متساوية في الحجم تقريباً وهو أمر طبيعي ومستحب.",
            symptoms = "تعب عام وأعراض فقر الدم الشائعة.",
            recommendations = "فحص مستويات الحديد وفيتامين ب12 للوقوف على التوازن المطلوب.",
            helpfulFoods = "الفواكه الطازجة، الخضروات، والمصادر الحيوانية للحديد.",
            educationalNotes = "ارتفاع RDW مع انخفاض MCV يشير غالباً لنقص الحديد.",
            refRangeProvider = { _, _ ->
                RefRange(11.5, 14.5, "%", "11.5 - 14.5 %")
            }
        ),
        "PLT" to LabTestKnowledge(
            key = "PLT",
            abbreviation = "PLT",
            arabicName = "الصفائح الدموية",
            englishName = "Platelets",
            defaultUnit = "10^3/µL",
            whatIsIt = "أجزاء خلوية صغيرة في الدم تساهم في عمل التخثر وتخثر الجروح.",
            bodyFunction = "تساعد على تجلط الدم وإيقاف النزيف وتخثر الجروح والخدوش.",
            whyDoctorOrders = "تقييم تجلط الدم، متابعة حالات كدمات الجلد، والتحضير للعمليات الجراحية.",
            highCauses = "الالتهابات المزمنة، استئصال الطحال، النزيف الحاد الملتئم، أو الجهد البدني.",
            lowCauses = "العدوى الفيروسية (مثل حمى الضنك)، بعض الأدوية، تضخم الطحال، أو أسباب مناعية.",
            symptoms = "سهولة ظهور الكدمات الزرقاء على الجلد، نزيف اللثة أو الأنف المتكرر.",
            recommendations = "تجنب تناول الأسبيرين أو مسكنات تجلط الدم دون إشراف طبي عند انخفاض العدد.",
            helpfulFoods = "الأغذية الغنية بفيتايمين K مثل البروكلي والخس والسبانخ للتدعيم.",
            educationalNotes = "الانخفاض الشديد يتطلب مراجعة فورية لتفادي خطورة النزيف.",
            refRangeProvider = { _, _ ->
                RefRange(150.0, 450.0, "10^3/µL", "150.0 - 450.0 ×10³ / µL")
            }
        ),
        "GLU" to LabTestKnowledge(
            key = "GLU",
            abbreviation = "GLU",
            arabicName = "سكر الدم الصائم",
            englishName = "Fasting Blood Glucose",
            defaultUnit = "mg/dL",
            whatIsIt = "تحليل يقيس تركيز الجلوكوز في الدم بعد صيام لا يقل عن 8 ساعات.",
            bodyFunction = "الجلوكوز هو المصدر الأساسي للطاقة لجميع خلايا الجسم والمخ.",
            whyDoctorOrders = "التشخيص والمتابعة الدورية لمرض السكري أو تقييم هبوط السكر.",
            highCauses = "مرض السكري، مقاومة الأنسولين، الضغط النفسي الشديد، أو تناول علاجات كورتيزونية.",
            lowCauses = "الصيام الطويل الجرعات الزائدة من علاج السكر، الجهد العضلي المكثف دون غذاء.",
            symptoms = "الارتفاع: العطش الشديد، كثرة التبول، الضبابية بالرؤية. الانخفاض: التعرق، التعرق والتعرق، التعرق البارد، التسارع بالقلب، والدوخة.",
            recommendations = "تقليل السكريات والمشروبات الغازية، ممارسة الرياضة، ومتابعة القراءات.",
            helpfulFoods = "الأغذية الغنية بالألياف، الخضروات الورقية، الشوفان، والحبوب الكاملة.",
            educationalNotes = "من 70 إلى 99 طبيعي، ومن 100 إلى 125 مرحلة ما قبل السكري.",
            refRangeProvider = { _, _ ->
                RefRange(70.0, 99.0, "mg/dL", "70.0 - 99.0 mg/dL")
            }
        ),
        "HbA1c" to LabTestKnowledge(
            key = "HbA1c",
            abbreviation = "HbA1c",
            arabicName = "السكر التراكمي",
            englishName = "Glycated Hemoglobin",
            defaultUnit = "%",
            whatIsIt = "تحليل يوضح متوسط نسبة السكر في الدم خلال الأشهر الثلاثة الماضية.",
            bodyFunction = "يقيس كمية الجلوكوز الملتصق بالهيموجلوبين طوال فترة حياة خلية الدم الحمراء.",
            whyDoctorOrders = "تقييم السيطرة على مرض السكري وتأكيد تشخيص الحالة.",
            highCauses = "عدم انضباط مستويات سكر الدم خلال الشهور الثلاثة السابقة.",
            lowCauses = "قد يظهر لدى المصابين بنوع من أنواع الأنيميا التحليلة التي تقلل عمر الخلايا الحمراء.",
            symptoms = "يعكس مدى انضباط السكر وليس له أعراض فورية منفصلة.",
            recommendations = "الالتزام بالنظام الغذائي المناسب وضبط الوزن وممارسة النشاط الرياضي المنتظم.",
            helpfulFoods = "الأطعمة ذات المؤشر الجليسمي المنخفض كالخضروات والبقوليات.",
            educationalNotes = "أقل من 5.7% طبيعي، 5.7% - 6.4% مرحلة ما قبل السكري، 6.5% فأكثر سكري.",
            refRangeProvider = { _, _ ->
                RefRange(4.0, 5.6, "%", "< 5.7 %")
            }
        ),
        "CREATININE" to LabTestKnowledge(
            key = "CREATININE",
            abbreviation = "CREA",
            arabicName = "الكرياتينين",
            englishName = "Creatinine",
            defaultUnit = "mg/dL",
            whatIsIt = "مادة ناتجة عن تحلل الفوسفوكرياتين في العضلات وتصفى وتخرج بالكامل عبر الكليتين.",
            bodyFunction = "مؤشر حيوي هائل لتقييم كفاءة وظائف الكلى وقدرتها على تنقية الدم.",
            whyDoctorOrders = "الاطمئنان على صحة الكليتين، متابعة أدوية الضغط والسكري، وقبل الصبغات الطبية.",
            highCauses = "قصور كلي، الجفاف الشديد، انسداد مجرى البول، أو تناول أدوية مجهدة للكلى.",
            lowCauses = "ضمور العضلات، سوء التغذية الشديد، أو الحمل الطبيعي.",
            symptoms = "تغيم أو تورم القدمين، تغير لون البول أو قلته، الشعور بالغثيان.",
            recommendations = "شرب ماء وافر يومياً وتجنب المسكنات بدون استشارة خاصة أدوية NSAIDs.",
            helpfulFoods = "تقليل استهلاك اللحوم الحمراء بكثرة والتركيز على الخضروات الطازجة.",
            educationalNotes = "تختلف نسبته حسب الكتلة العضلية (أعلى لدى الذكور).",
            refRangeProvider = { _, gender ->
                if (gender == Gender.MALE) RefRange(0.7, 1.3, "mg/dL", "0.7 - 1.3 mg/dL")
                else RefRange(0.6, 1.1, "mg/dL", "0.6 - 1.1 mg/dL")
            }
        ),
        "UREA" to LabTestKnowledge(
            key = "UREA",
            abbreviation = "UREA",
            arabicName = "اليوريا / نيتروجين البولينا",
            englishName = "Blood Urea Nitrogen",
            defaultUnit = "mg/dL",
            whatIsIt = "ناتج تكسير البروتينات في الكبد والذي تخرجه الكليتان خارج الجسم مع البول.",
            bodyFunction = "مؤشر ثانوي لتقييم وظائف الكلى ومستوى توازن البروتينات والسوائل.",
            whyDoctorOrders = "تقييم كفاءة عمل الكليتين والجهاز البولي ومستوى توازن السوائل.",
            highCauses = "الجفاف ونقص الماء، نظام غذائي فرط البروتين، نزيف الجهاز الهضمي، أو ضعف الكلى.",
            lowCauses = "سوء التغذية، نقص بروتين الغذاء، الحمل، أو أمراض الكبد الشديدة.",
            symptoms = "الشعور بالعطش الشديد، الإرهاق، وتغيرات في التبول.",
            recommendations = "شرب كميات كافية من المياه، واعتدال تناول البروتينات.",
            helpfulFoods = "الخضروات الورقية، الخيار، والتفاح.",
            educationalNotes = "يتأثر بشكل سريع بمستوى شرب الماء والأغذية اليومية.",
            refRangeProvider = { _, _ ->
                RefRange(15.0, 45.0, "mg/dL", "15.0 - 45.0 mg/dL")
            }
        ),
        "ALT" to LabTestKnowledge(
            key = "ALT",
            abbreviation = "ALT",
            arabicName = "إنزيم الكبد ALT",
            englishName = "Alanine Aminotransferase",
            defaultUnit = "U/L",
            whatIsIt = "إنزيم يوجد بشكل رئيسي داخل خلايا الكبد.",
            bodyFunction = "يساعد الكبد على تحويل البروتينات إلى طاقة لعمل خلايا الجسم.",
            whyDoctorOrders = "الفحص الأساسي للكشف عن صحة وسلامة خلايا الكبد وتأثير الأدوية.",
            highCauses = "دهون الكبد، الالتهاب الكبدي الفيروسي، تناول بعض المكملات والأدوية، أو زيادة الوزن.",
            lowCauses = "يعتبر الانخفاض أمراً طبيعياً ولا يدعو للقلق في أغلب الأحيان.",
            symptoms = "تعب عام، ألم في أعلى الأيمن من البطن، أو اصفرار بالعينين عند الارتفاع الشديد.",
            recommendations = "تقليل المقالي والأطعمة الدسمة، تجنب الأدوية دون وصفة، والعمل على إنقاص الوزن.",
            helpfulFoods = "الخضروات الصليبية كالبروكلي، التفاح، والأسماك المشوية.",
            educationalNotes = "يعتبر الإنزيم الأكثر تخصصاً للكبد مقارنة بإنزيم AST.",
            refRangeProvider = { _, gender ->
                if (gender == Gender.MALE) RefRange(10.0, 40.0, "U/L", "10 - 40 U/L")
                else RefRange(7.0, 35.0, "U/L", "7 - 35 U/L")
            }
        ),
        "AST" to LabTestKnowledge(
            key = "AST",
            abbreviation = "AST",
            arabicName = "إنزيم الكبد AST",
            englishName = "Aspartate Aminotransferase",
            defaultUnit = "U/L",
            whatIsIt = "إنزيم يتواجد في الكبد والعضلات والقلب.",
            bodyFunction = "يشارك في عمليات الأيض واستقلاب الأحماض الأمينية.",
            whyDoctorOrders = "تقييم أذى الكبد أو أذى العضلات الإيكولوجي الهيكلي.",
            highCauses = "إجهاد الكبد، المجهود العضلي الشديد جداً، أو بعض العلاجات الأدوية.",
            lowCauses = "نسبة طبيعية لا تدعو للقلق.",
            symptoms = "إجهاد بالجسد أو آلام عضلية.",
            recommendations = "شرب الماء، الابتعاد عن الدهون والمشروبات الضارة، ومراجعة الطبيب.",
            helpfulFoods = "الشاي الأخضر، الخضروات، وزيت الزيتون.",
            educationalNotes = "يقارن دائماً مع إنزيم ALT للحصول على صورة دقيقة.",
            refRangeProvider = { _, _ ->
                RefRange(10.0, 40.0, "U/L", "10 - 40 U/L")
            }
        ),
        "BILIRUBIN" to LabTestKnowledge(
            key = "BILIRUBIN",
            abbreviation = "BILI",
            arabicName = "البيليروبين الكلي",
            englishName = "Total Bilirubin",
            defaultUnit = "mg/dL",
            whatIsIt = "مادة صفراء تتكون عند تكسر وتجدد كرات الدم الحمراء القديمة.",
            bodyFunction = "يعالجها الكبد ويفرزها في العصارة الصفرواية للمساعدة في هضم الدهون.",
            whyDoctorOrders = "تقييم حالات اليرقان (الصفار)، سلامة الكبد والمرارة ومجرى الصفراء.",
            highCauses = "انسداد القنوات الصفرواية، حصوات المرارة، أمراض الكبد، أو تكسر الدم السريع.",
            lowCauses = "لا يعتبر انخفاضه ذو أهمية مرضية.",
            symptoms = "اصفرار الجلد وبياض العينين (اليرقان)، تغير لون البول إلى الداكن.",
            recommendations = "شرب السوائل بكثرة والتقليل من الدهون المسببة لجهد المرارة.",
            helpfulFoods = "الأطعمة الخفيفة وسهلة الهضم مثل الخضروات المسلوقة بالفواكه.",
            educationalNotes = "يوجد منه نوع مباشر (مقترن) وغير مباشر (حر).",
            refRangeProvider = { _, _ ->
                RefRange(0.2, 1.2, "mg/dL", "0.2 - 1.2 mg/dL")
            }
        ),
        "VITAMIN D" to LabTestKnowledge(
            key = "VITAMIN D",
            abbreviation = "VIT D",
            arabicName = "فيتامين د",
            englishName = "Vitamin D (25-OH)",
            defaultUnit = "ng/mL",
            whatIsIt = "فيتامين وذائب بالدهون يصنعه الجسم عند تعرض الجلد لأشعة الشمس.",
            bodyFunction = "أساسي لامتصاص الكالسيوم والفوسفور وبناء العظام وتقوية المناعة.",
            whyDoctorOrders = "تقييم صحة العظام، آلام المفاصل، الإرهاق الدائم، والمناعة.",
            highCauses = "تناول جرعات عالية جداً ومفرطة من مكملات فيتامين د بدون متابعة.",
            lowCauses = "قلة التعرض للشمس، سوء الامتصاص، أو عدم تناول أغذية غنية به.",
            symptoms = "آلام العظام والعضلات، تساقط الشعر، خمول وتعب دائم، ضعف المناعة.",
            recommendations = "التعرض للشمس 15 دقيقة صباحاً، وتناول المكمل الغذائي تحت إشراف طبي.",
            helpfulFoods = "الأسماك الدهنية (السلمون والسردين)، صفار البيض، والألبان المدعمة.",
            educationalNotes = "يعتبر النقص شائعاً جداً في معظم الدول العربية نظراً لقلة التعرض المباشر للشمس.",
            refRangeProvider = { _, _ ->
                RefRange(30.0, 100.0, "ng/mL", "30.0 - 100.0 ng/mL")
            }
        ),
        "TSH" to LabTestKnowledge(
            key = "TSH",
            abbreviation = "TSH",
            arabicName = "هرمون محفز الدرقية",
            englishName = "Thyroid Stimulating Hormone",
            defaultUnit = "µIU/mL",
            whatIsIt = "هرمون تفرزه الغدة النخامية للتحكم في نشاط الغدة الدرقية وإفرازاتها.",
            bodyFunction = "ينظم سرعة عمليات الأيض وتوليد الطاقة وحرق السعرات بالحرارة.",
            whyDoctorOrders = "الفحص الأهم للكشف عن خمول أو نشاط الغدة الدرقية وتغيرات الوزن.",
            highCauses = "خمول الغدة الدرقية (حيث تفرز النخامية كمية أكبر لتحفيزها).",
            lowCauses = "فرط نشاط الغدة الدرقية (حيث تقلل النخامية الإفراز).",
            symptoms = "الارتفاع (خمول): زيادة الوزن، الشعور بالبرد، جفاف الجلد. الانخفاض (نشاط): النحافة، التعرق، خفقان القلب.",
            recommendations = "المتابعة مع طبيب الغدد لتنظيم الجرعة العلاجية الهرمونية إن لزم.",
            helpfulFoods = "الأطعمة البحرية المحتوية على اليود الطبيعي مع اعتدال الخضراوات الصليبية.",
            educationalNotes = "الارتباط بين TSH وهرمونات الدرقية علاقة عكسية.",
            refRangeProvider = { _, _ ->
                RefRange(0.4, 4.0, "µIU/mL", "0.40 - 4.00 µIU/mL")
            }
        ),
        "CHOLESTEROL" to LabTestKnowledge(
            key = "CHOL",
            abbreviation = "CHOL",
            arabicName = "الكوليسترول الكلي",
            englishName = "Total Cholesterol",
            defaultUnit = "mg/dL",
            whatIsIt = "مادة دهنية شمعية أساسية لبناء أجهزة وخلايا الجسم وهرموناته.",
            bodyFunction = "يدخل في تركيب أغشية الخلايا وفيتامين د والعصارة الصفراوية.",
            whyDoctorOrders = "تقييم صحة القلب والشرايين والوقاية من التصلبات الدهنية.",
            highCauses = "تناول الدهون المشبعة والأطعمة المقلية، الوراثة، قلة الحركة، السمنة.",
            lowCauses = "سوء التغذية الشديد، أو فرط نشاط الغدة الدرقية.",
            symptoms = "لا يسبب أعراضاً ظاهرة بشكل مباشر ولذا يسمى بـ 'المؤشر الخفي'.",
            recommendations = "ممارسة الرياضة يومياً لمدة 30 دقيقة، واستبدال الدهون الضارة بزيوت صحية.",
            helpfulFoods = "الشوفان، زيت الزيتون، المكسرات النيّة، الأسماك، والأفوكادو.",
            educationalNotes = "يفضل أن يكون إجمالي الكوليسترول أقل من 200 mg/dL.",
            refRangeProvider = { _, _ ->
                RefRange(120.0, 200.0, "mg/dL", "< 200 mg/dL")
            }
        ),
        "HDL" to LabTestKnowledge(
            key = "HDL",
            abbreviation = "HDL",
            arabicName = "الكوليسترول النافع",
            englishName = "HDL Cholesterol",
            defaultUnit = "mg/dL",
            whatIsIt = "البروتين الدهني عالي الكثافة المعروف بـ 'الكوليسترول النافع أو الجيد'.",
            bodyFunction = "ينقل الدهون الفائضة من الشرايين والدم ويعيدها للكبد للتخلص منها.",
            whyDoctorOrders = "تقييم مستوى الحماية الطبيعية للشرايين والقلب.",
            highCauses = "يعتبر ارتفاعه علامة صحية ممتازة وحماية للشرايين والقلب.",
            lowCauses = "خمول الحركة، التدخين، الوجبات السريعة، والسمنة.",
            symptoms = "لا توجد أعراض مباشرة.",
            recommendations = "ممارسة تمارين الكارديو، الإقلاع عن التدخين، وتناول الأسماك.",
            helpfulFoods = "زيت الزيتون البكر، الأسماك الزيتية، بذور الكتان، واللوز.",
            educationalNotes = "ارتفاع HDL ينقص من مخاطر أمراض القلب الشريانية.",
            refRangeProvider = { _, gender ->
                if (gender == Gender.MALE) RefRange(40.0, 90.0, "mg/dL", "> 40 mg/dL")
                else RefRange(50.0, 90.0, "mg/dL", "> 50 mg/dL")
            }
        ),
        "LDL" to LabTestKnowledge(
            key = "LDL",
            abbreviation = "LDL",
            arabicName = "الكوليسترول الضار",
            englishName = "LDL Cholesterol",
            defaultUnit = "mg/dL",
            whatIsIt = "البروتين الدهني منخفض الكثافة أو 'الكوليسترول الضار'.",
            bodyFunction = "ينقل الكوليسترول من الكبد إلى أنسجة الجسم وقد يترسب بجدران الشرايين.",
            whyDoctorOrders = "الفحص الأهم لتحديد مخاطر تصلب الشرايين وتراكم الدهون.",
            highCauses = "الوجبات الغنية بالدهون المشبعة والمتحولة، قلة النشاط البدني، والوراثة.",
            lowCauses = "نادرة الغالب أو نتيجة التغذية النباتية الخالصة وأدوية خفض الدهون.",
            symptoms = "لا توجد أعراض فورية ولكن يؤثر مستقبلاً على مرونة الشرايين.",
            recommendations = "تجنب المقالي والزيوت المهدرجة والوجبات السريعة نهائياً.",
            helpfulFoods = "تفاح، شوفان، بقوليات، وثوم.",
            educationalNotes = "المستوى المستهدف الشائع هو أقل من 100 mg/dL.",
            refRangeProvider = { _, _ ->
                RefRange(50.0, 100.0, "mg/dL", "< 100 mg/dL")
            }
        ),
        "TRIGLYCERIDES" to LabTestKnowledge(
            key = "TRIGLYCERIDES",
            abbreviation = "TRIG",
            arabicName = "الدهون الثلاثية",
            englishName = "Triglycerides",
            defaultUnit = "mg/dL",
            whatIsIt = "نوع من الدهون المخزنة بالدم والجسم الناتجة عن السعرات الحرارية الفائضة.",
            bodyFunction = "تخزن السعرات الزائدة لتوفير الطاقة للجسم بين الوجبات.",
            whyDoctorOrders = "تقييم مخاطر متلازمة الأيض والتهابات البنكرياس والصحة القلبية.",
            highCauses = "الإفراط في تناول النشويات، السكريات، المشروبات المشروبة، والسمنة.",
            lowCauses = "سوء التغذية أو الأنماط الغذائية منخفضة الدهون جداً.",
            symptoms = "عادة بدون أعراض ما لم تكن مرتفعة جداً للغاية فوق 500 mg/dL.",
            recommendations = "تقليل السكريات والمخبوزات البيضاء والحلويات والرياضة.",
            helpfulFoods = "أوميجا 3، الخضروات الورقية، والأسماك المشوية.",
            educationalNotes = "تتأثر بسرعة كبيرة بنوع وجبة العشاء السابقة للفحص.",
            refRangeProvider = { _, _ ->
                RefRange(30.0, 150.0, "mg/dL", "< 150 mg/dL")
            }
        ),
        "CRP" to LabTestKnowledge(
            key = "CRP",
            abbreviation = "CRP",
            arabicName = "بروتين سي التفاعلي",
            englishName = "C-Reactive Protein",
            defaultUnit = "mg/L",
            whatIsIt = "بروتين ينتجه الكبد استجابة لوجود التهاب في مكان ما في الجسم.",
            bodyFunction = "يرتبط بالخلايا المتضررة ليحفز جهاز المناعة للتخلص منها وترميم الأنسجة.",
            whyDoctorOrders = "مؤشر حساس للكشف عن وجود التهاب حاد أو عدوى بكتيرية ومتابعة العلاج.",
            highCauses = "العدوى البكتيرية الحادة، الروماتيزم والتهاب المفاصل، أو الجراحات حديثاً.",
            lowCauses = "المستوى الطبيعي المنخفض جداً يعكس خلو الجسم من الالتهابات النشطة.",
            symptoms = "أعراض المسبب مثل الحرارة، آلام المفاصل، أو التورم.",
            recommendations = "الراحة وشرب السوائل واستكمال العلاج المضاد للالتهاب المحدد طبيباً.",
            helpfulFoods = "الزنجبيل، الكركم، الأسماك الزيتية، وزيت الزيتون مضادات التهاب طبيعية.",
            educationalNotes = "يزداد بسرعة مع الالتهاب وينخفض بسرعة عند التعافي.",
            refRangeProvider = { _, _ ->
                RefRange(0.0, 5.0, "mg/L", "< 5.0 mg/L")
            }
        ),
        "FERRITIN" to LabTestKnowledge(
            key = "FERRITIN",
            abbreviation = "FERRITIN",
            arabicName = "مخزون الحديد",
            englishName = "Serum Ferritin",
            defaultUnit = "ng/mL",
            whatIsIt = "بروتين مخصص لتخزين الحديد داخل خلايا الكبد والأنسجة بشكل آمن.",
            bodyFunction = "يطلق الحديد في الدم عند حاجة الجسم لتصنيع هيموجلوبين جديد.",
            whyDoctorOrders = "المعيار الأدق لتشخيص كمية ونقص مخزون الحديد في الجسم قبل ظهور الأنيميا.",
            highCauses = "التهابات بالدم (مستجيب التهابي)، كثرة نقل الدم، أو أمراض الكبد.",
            lowCauses = "استنزاف مخزون الحديد بالجسم، سوء التغذية، أو النزيف المتكرر.",
            symptoms = "تساقط الشعر الشديد، تكسر الأظافر، التعب المزمن، والرغبة في أكل الثلج.",
            recommendations = "تناول كورسات علاجية لمكملات الحديد بناءً على توصية الطبيب المباشر.",
            helpfulFoods = "اللحوم، الكبدة، التمر، السبانخ، العسل الأسود مع فيتامين C.",
            educationalNotes = "قد ينخفض مخزون الحديد بالرغم من كون نسبة الهيموجلوبين طبيعية ظاهرياً.",
            refRangeProvider = { _, gender ->
                if (gender == Gender.MALE) RefRange(20.0, 250.0, "ng/mL", "20.0 - 250.0 ng/mL")
                else RefRange(10.0, 120.0, "ng/mL", "10.0 - 120.0 ng/mL")
            }
        ),
        "CALCIUM" to LabTestKnowledge(
            key = "CALCIUM",
            abbreviation = "CA",
            arabicName = "الكالسيوم الكلي",
            englishName = "Calcium",
            defaultUnit = "mg/dL",
            whatIsIt = "المعدن الأكثر وفرة في الجسم والمسؤول عن بناء العظام والأسنان.",
            bodyFunction = "ضروري لانقباض العضلات (بما فيها القلب)، نقل الإشارات العصبية والتجلط.",
            whyDoctorOrders = "تقييم صحة العظام والأسنان، تشنجات العضلات، ووظائف جارات الدرقية.",
            highCauses = "فرط نشاط الغدة جارة الدرقية، زيادة فيتامين د، أو بعض الأدوية.",
            lowCauses = "نقص فيتامين د، نقص الكالسيوم بالتغذية، أو أمراض الكلى.",
            symptoms = "الارتفاع: العطش، الغثيان، الإمساك. الانخفاض: تشنج وتنميل العضلات والأصابع.",
            recommendations = "المحافظة على التوازن المائي والغذائي والتعرض المعتدل للشمس.",
            helpfulFoods = "الحليب ومشتقاته، الأجبان، السمسم، اللوز، والخضروات الداكنة.",
            educationalNotes = "يرتبط تركيز الكالسيوم بالدم ببروتين الألبومين.",
            refRangeProvider = { _, _ ->
                RefRange(8.5, 10.5, "mg/dL", "8.5 - 10.5 mg/dL")
            }
        ),
        "POTASSIUM" to LabTestKnowledge(
            key = "POTASSIUM",
            abbreviation = "K",
            arabicName = "البوتاسيوم",
            englishName = "Potassium",
            defaultUnit = "mmol/L",
            whatIsIt = "عنصر كهرلي حاد الأهمية لتنظيم توازن السوائل والإشارات الكهربائية.",
            bodyFunction = "ينظم انتظام ضربات القلب وانقباض العضلات والضغط الشرياني.",
            whyDoctorOrders = "متابعة تنظيم القلب، أدوية الضغط والمدرات البولية، ومشاكل الكلى.",
            highCauses = "قصور الكلى، مكملات مفرطة، أو بعض مدرات الضغط الحافظة للبوتاسيوم.",
            lowCauses = "القيء الإسهال، استخدام مدرات البول بدون تعويض، نقص الأغذية.",
            symptoms = "الارتفاع أو الانخفاض الشديد: خفقان واضطراب بنبض القلب وضعف عضلات.",
            recommendations = "عدم أخذ مكملات البوتاسيوم بغير وصفة دقيقة نظراً لحساسيته الكبيرة.",
            helpfulFoods = "الموز، البرتقال، البطاطا الحلوة، الطماطم، والماء العذب.",
            educationalNotes = "أي تباين كبير يستلزم متابعة سريعة للحفاظ على سلامة نبض القلب.",
            refRangeProvider = { _, _ ->
                RefRange(3.5, 5.1, "mmol/L", "3.5 - 5.1 mmol/L")
            }
        )
    )

    fun getKnowledge(testKey: String): LabTestKnowledge {
        return KNOWLEDGE_MAP[testKey] ?: LabTestKnowledge(
            key = testKey,
            abbreviation = testKey,
            arabicName = LabTestCatalog.ALL_TESTS.find { it.key == testKey }?.arabicName ?: testKey,
            englishName = testKey,
            defaultUnit = LabTestCatalog.ALL_TESTS.find { it.key == testKey }?.defaultUnit ?: "",
            whatIsIt = "تحليل مخبري طبي تقييمي لمتابعة الحالة الصحية وتحديد المؤشرات الحيوية.",
            bodyFunction = "يساعد في تقييم أداء الأعضاء والوظائف الفسيولوجية داخل الجسم.",
            whyDoctorOrders = "يُطلب لتشخيص الحالات الصحية أو الاطمئنان الدوري المباشر.",
            highCauses = "قد يعود الارتفاع إلى عوامل متعددة مثل التغذية، الإجهاد، الالتهاب أو طبيعة استجابة الجسم.",
            lowCauses = "قد يعود الانخفاض إلى نقص التغذية، الإجهاد، أو تغيرات في معدلات الاستقلاب.",
            symptoms = "الشعور بالتعب أو الإرهاق قد يرافق أي تغيرات في المؤشرات الحيوية العامة.",
            recommendations = "ينصح بمراجعة الطبيب المختص وقراءة النتائج ضمن السياق السريري الكامل.",
            helpfulFoods = "تناول نظام غذائي متوازن وغني بالخضروات والفواكه والماء الكافي.",
            educationalNotes = "النتائج المختبرية تعتمد دائماً على المدى المرجعي للمختبر والمستشفى.",
            refRangeProvider = { _, _ -> RefRange(null, null, "", "حسب تعليمات المختبر") }
        )
    }

    fun evaluateResult(
        testKey: String,
        rawValue: String,
        age: Int,
        gender: Gender,
        isLowConfidence: Boolean = false
    ): EvaluatedLabResult {
        val knowledge = getKnowledge(testKey)
        val refRange = knowledge.refRangeProvider(age, gender)

        // Clean numeric string
        val cleanValStr = rawValue.replace("<", "").replace(">", "").replace("=", "").trim()
        val numValue = cleanValStr.toDoubleOrNull()

        val status = if (numValue == null || refRange.min == null || refRange.max == null) {
            TestStatus.NORMAL
        } else {
            when {
                numValue < refRange.min -> TestStatus.LOW
                numValue > refRange.max -> TestStatus.HIGH
                else -> TestStatus.NORMAL
            }
        }

        return EvaluatedLabResult(
            testKey = testKey,
            arabicName = knowledge.arabicName,
            abbreviation = knowledge.abbreviation,
            rawValue = rawValue,
            parsedValue = numValue,
            unit = if (knowledge.defaultUnit.isNotBlank()) knowledge.defaultUnit else "وحدة",
            formattedRefRange = refRange.formatted,
            status = status,
            isLowConfidence = isLowConfidence,
            knowledge = knowledge
        )
    }

    fun searchTests(query: String): List<LabTestKnowledge> {
        val trimmed = query.trim().uppercase()
        if (trimmed.isEmpty()) return KNOWLEDGE_MAP.values.toList()

        return KNOWLEDGE_MAP.values.filter { test ->
            test.arabicName.contains(query, ignoreCase = true) ||
                    test.abbreviation.contains(trimmed, ignoreCase = true) ||
                    test.englishName.contains(trimmed, ignoreCase = true) ||
                    test.key.contains(trimmed, ignoreCase = true)
        }
    }
}
