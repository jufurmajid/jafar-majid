package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EvaluatedLabResult
import com.example.data.TestStatus
import com.example.ui.theme.MedicalBlueContainer
import com.example.ui.theme.MedicalBluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    result: EvaluatedLabResult,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val knowledge = result.knowledge

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "تفاصيل ${result.arabicName}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Main Test Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = result.arabicName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${knowledge.englishName} (${result.abbreviation})",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MedicalBlueContainer
                        ) {
                            Text(
                                text = result.abbreviation,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MedicalBluePrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Measured Value & Status Box
                    val visuals = when (result.status) {
                        TestStatus.NORMAL -> StatusVisuals(
                            Color(0xFF2E7D32),
                            Color(0xFFE8F5E9),
                            Icons.Default.CheckCircle,
                            "طبيعي 🟢"
                        )
                        TestStatus.HIGH -> StatusVisuals(
                            Color(0xFFC62828),
                            Color(0xFFFFEBEE),
                            Icons.Default.ArrowUpward,
                            "مرتفع 🔴"
                        )
                        TestStatus.LOW -> StatusVisuals(
                            Color(0xFFE65100),
                            Color(0xFFFFF3E0),
                            Icons.Default.ArrowDownward,
                            "منخفض 🟠"
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = visuals.bg
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "نتيجة التحليل الحالية",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${result.rawValue} ${result.unit}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = visuals.color
                                )
                                Text(
                                    text = "المعدل الطبيعي: ${result.formattedRefRange}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = visuals.color
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = visuals.icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = visuals.text,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Mandatory Medical Disclaimer
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
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "هذا التطبيق لا يغني عن مراجعة الطبيب، والمعلومات المقدمة هي لأغراض تعليمية وتثقيفية فقط.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Section 1: ما هو هذا التحليل؟
            DetailSectionCard(
                title = "ما هو هذا التحليل؟",
                icon = Icons.Default.Info,
                content = knowledge.whatIsIt
            )

            // Section 2: وظيفة التحليل داخل الجسم
            DetailSectionCard(
                title = "وظيفة التحليل داخل الجسم",
                icon = Icons.Default.Psychology,
                content = knowledge.bodyFunction
            )

            // Section 3: لماذا يطلبه الطبيب؟
            DetailSectionCard(
                title = "لماذا يطلبه الطبيب؟",
                icon = Icons.Default.MedicalServices,
                content = knowledge.whyDoctorOrders
            )

            // Section 4: ماذا يعني ارتفاعه؟
            DetailSectionCard(
                title = "ماذا يعني ارتفاعه؟",
                icon = Icons.Default.ArrowUpward,
                iconColor = Color(0xFFC62828),
                content = knowledge.highCauses
            )

            // Section 5: ماذا يعني انخفاضه؟
            DetailSectionCard(
                title = "ماذا يعني انخفاضه؟",
                icon = Icons.Default.ArrowDownward,
                iconColor = Color(0xFFE65100),
                content = knowledge.lowCauses
            )

            // Section 6: الأعراض المحتملة
            DetailSectionCard(
                title = "الأعراض المحتملة المرتبطة",
                icon = Icons.Default.HelpOutline,
                content = knowledge.symptoms
            )

            // Section 7: نصائح عامة
            DetailSectionCard(
                title = "نصائح وإرشادات عامة",
                icon = Icons.Default.Lightbulb,
                iconColor = MedicalBluePrimary,
                content = knowledge.recommendations
            )

            // Section 8: الأطعمة والتغذية المفيدة
            DetailSectionCard(
                title = "الأطعمة والتغذية التي قد تساعد",
                icon = Icons.Default.LocalDining,
                iconColor = Color(0xFF2E7D32),
                content = knowledge.helpfulFoods
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DetailSectionCard(
    title: String,
    icon: ImageVector,
    content: String,
    iconColor: Color = MedicalBluePrimary
) {
    if (content.isBlank()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }
    }
}


