package com.example.ui.screens

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.BackHandler
import com.example.util.AdManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import com.example.util.PdfReportGenerator
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.EvaluatedLabResult
import com.example.data.Gender
import com.example.data.MedicalDatabase
import com.example.data.OcrAnalysisResult
import com.example.data.ReportRepository
import com.example.data.TestStatus
import com.example.ui.theme.MedicalBlueContainer
import com.example.ui.theme.MedicalBlueOnPrimary
import com.example.ui.theme.MedicalBluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    imageUri: Uri,
    age: Int,
    gender: Gender,
    ocrResult: OcrAnalysisResult,
    onTestClick: (EvaluatedLabResult) -> Unit,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onViewImageClick: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { ReportRepository(context) }
    val activity = remember(context) { context as? Activity }

    val handleNavigationWithAd = { navigateAction: () -> Unit ->
        if (activity != null && AdManager.shouldShowAd()) {
            AdManager.showAdIfReady(activity) {
                navigateAction()
            }
        } else {
            navigateAction()
        }
    }

    BackHandler {
        handleNavigationWithAd(onBackClick)
    }

    val evaluatedResults = remember(ocrResult, age, gender) {
        if (ocrResult is OcrAnalysisResult.Success) {
            ocrResult.labValues.map { lab ->
                MedicalDatabase.evaluateResult(
                    testKey = lab.abbreviation,
                    rawValue = lab.value,
                    age = age,
                    gender = gender,
                    isLowConfidence = lab.isLowConfidence
                )
            }
        } else {
            emptyList()
        }
    }

    // Auto-save report to Room database history
    LaunchedEffect(evaluatedResults) {
        if (evaluatedResults.isNotEmpty()) {
            try {
                repository.saveReport(
                    patientAge = age,
                    patientGender = gender,
                    imageUriString = imageUri.toString(),
                    evaluatedResults = evaluatedResults
                )
            } catch (e: Exception) {
                // Ignore silent save errors
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "نتائج التحاليل الطبية",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { handleNavigationWithAd(onBackClick) },
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    if (ocrResult is OcrAnalysisResult.Success && evaluatedResults.isNotEmpty()) {
                        val context = LocalContext.current
                        IconButton(
                            onClick = {
                                val file = PdfReportGenerator.generatePdfReport(
                                    context = context,
                                    patientAge = age,
                                    patientGender = gender,
                                    evaluatedResults = evaluatedResults
                                )
                                if (file != null) {
                                    PdfReportGenerator.sharePdf(context, file)
                                }
                            },
                            modifier = Modifier.testTag("export_pdf_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "تصدير تقرير PDF",
                                tint = MedicalBluePrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->

        when (ocrResult) {
            is OcrAnalysisResult.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))

                        // Patient & Report Header Info Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(2.dp, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MedicalBluePrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "العمر: $age سنة",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (gender == Gender.MALE) Icons.Default.Male else Icons.Default.Female,
                                            contentDescription = null,
                                            tint = MedicalBluePrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "الجنس: ${if (gender == Gender.MALE) "ذكر" else "أنثى"}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MedicalBlueContainer,
                                        modifier = Modifier.clickable { onViewImageClick(imageUri) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Visibility,
                                                contentDescription = "عرض",
                                                tint = MedicalBluePrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "التقرير الأصلي",
                                                color = MedicalBluePrimary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Mandatory Medical Disclaimer
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "تنبيه",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "⚠️ هذا التطبيق لا يغني عن مراجعة الطبيب، والمعلومات المقدمة هي لأغراض تعليمية وتثقيفية فقط.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    // Count Banner
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MedicalBluePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تم استخراج ومعالجة ${evaluatedResults.size} مادة مخبرية (اضغط للتفاصيل)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // Low Confidence Banner if applicable
                    if (ocrResult.hasLowConfidence) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "تنبيه",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "ملاحظة: ينصح بمراجعة القيم المشار إليها بعلامة التنبيه مع التقرير الأصلي لضمان الدقة.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // List of evaluated lab test cards
                    items(evaluatedResults, key = { it.id }) { item ->
                        EvaluatedLabTestCard(
                            item = item,
                            onClick = { onTestClick(item) }
                        )
                    }

                    // Bottom navigation buttons
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { handleNavigationWithAd(onHomeClick) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .testTag("home_button"),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "الرئيسية",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "الرئيسية",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = { handleNavigationWithAd(onBackClick) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .shadow(4.dp, RoundedCornerShape(18.dp))
                                    .testTag("rescan_button"),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MedicalBluePrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "تحليل صورة أخرى",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "تقرير آخر",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }

            is OcrAnalysisResult.PoorQuality -> {
                ErrorResultView(
                    message = ocrResult.message,
                    imageUri = imageUri,
                    onBackClick = { handleNavigationWithAd(onBackClick) },
                    onHomeClick = { handleNavigationWithAd(onHomeClick) },
                    onViewImageClick = onViewImageClick,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is OcrAnalysisResult.Failure -> {
                ErrorResultView(
                    message = ocrResult.message,
                    imageUri = imageUri,
                    onBackClick = { handleNavigationWithAd(onBackClick) },
                    onHomeClick = { handleNavigationWithAd(onHomeClick) },
                    onViewImageClick = onViewImageClick,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
fun EvaluatedLabTestCard(
    item: EvaluatedLabResult,
    onClick: () -> Unit
) {
    val cleanedValue = remember(item) {
        val valueToFormat = item.parsedValue
        if (valueToFormat != null) {
            val df = java.text.DecimalFormat("#.######", java.text.DecimalFormatSymbols(java.util.Locale.ROOT))
            df.format(valueToFormat)
        } else {
            val regex = Regex("""\d+(?:\.\d+)?""")
            val match = regex.find(item.rawValue)
            if (match != null) {
                val parsed = match.value.toDoubleOrNull()
                if (parsed != null) {
                    val df = java.text.DecimalFormat("#.######", java.text.DecimalFormatSymbols(java.util.Locale.ROOT))
                    df.format(parsed)
                } else {
                    match.value
                }
            } else {
                item.rawValue
            }
        }
    }

    val visuals = when (item.status) {
        TestStatus.NORMAL -> StatusVisuals(
            Color(0xFF2E7D32),
            Color(0xFFE8F5E9),
            Icons.Default.CheckCircle,
            "طبيعي"
        )
        TestStatus.HIGH -> StatusVisuals(
            Color(0xFFC62828),
            Color(0xFFFFEBEE),
            Icons.Default.ArrowUpward,
            "مرتفع"
        )
        TestStatus.LOW -> StatusVisuals(
            Color(0xFFE65100),
            Color(0xFFFFF3E0),
            Icons.Default.ArrowDownward,
            "منخفض"
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("lab_card_${item.abbreviation}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Arabic laboratory name
            Text(
                text = item.arabicName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 2. Abbreviation
            Text(
                text = item.abbreviation,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MedicalBluePrimary,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )

            // 3. Status
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = visuals.bg,
                modifier = Modifier.height(32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = visuals.icon,
                        contentDescription = null,
                        tint = visuals.color,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = visuals.text,
                        color = visuals.color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 4. Numeric value only
            Text(
                text = cleanedValue,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = visuals.color,
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 5. Interpretation button
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MedicalBlueContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clickable { onClick() }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "عرض التفسير الطبي",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalBluePrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "التفاصيل",
                        tint = MedicalBluePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorResultView(
    message: String,
    imageUri: Uri,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onViewImageClick: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "تعذر قراءة التحاليل",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBackClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MedicalBluePrimary)
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("إعادة المحاولة بصورة أوضح", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onHomeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(imageVector = Icons.Default.Home, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("العودة للرئيسية", fontWeight = FontWeight.Bold)
        }
    }
}


