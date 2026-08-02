package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.util.AdManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.ExtractedLabValue
import com.example.data.Gender
import com.example.data.MedicalDatabase
import com.example.data.OcrAnalysisResult
import com.example.data.ReportRepository
import com.example.ui.screens.DetailsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ImageViewerScreen
import com.example.ui.screens.LoadingScreen
import com.example.ui.screens.PatientInfoScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.MedicalTranslatorTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AdManager.initialize(applicationContext)
        setContent {
            MedicalTranslatorApp()
        }
    }
}

@Composable
fun MedicalTranslatorApp() {
    MedicalTranslatorTheme {
        val context = LocalContext.current
        val navController = rememberNavController()
        val scope = rememberCoroutineScope()
        val repository = remember { ReportRepository(context) }
        var currentOcrResult by remember { mutableStateOf<OcrAnalysisResult?>(null) }

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.fillMaxSize()
        ) {
            // Home Screen
            composable("home") {
                HomeScreen(
                    onImageSelected = { uri ->
                        val encodedUri = Uri.encode(uri.toString())
                        navController.navigate("patient_info/$encodedUri")
                    },
                    onSearchClick = {
                        navController.navigate("search")
                    },
                    onHistoryClick = {
                        navController.navigate("history")
                    }
                )
            }

            // Patient Info Screen
            composable(
                route = "patient_info/{imageUri}",
                arguments = listOf(
                    navArgument("imageUri") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val encodedUriStr = backStackEntry.arguments?.getString("imageUri") ?: ""
                val decodedUriStr = Uri.decode(encodedUriStr)
                val imageUri = Uri.parse(decodedUriStr)

                PatientInfoScreen(
                    imageUri = imageUri,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onAnalyzeClick = { age, gender ->
                        val encodedImageUri = Uri.encode(imageUri.toString())
                        navController.navigate("loading/$encodedImageUri/$age/${gender.name}")
                    },
                    onViewImageClick = { uri ->
                        val encodedUri = Uri.encode(uri.toString())
                        navController.navigate("image_viewer/$encodedUri")
                    }
                )
            }

            // Loading Screen (Performs Offline OCR)
            composable(
                route = "loading/{imageUri}/{age}/{gender}",
                arguments = listOf(
                    navArgument("imageUri") { type = NavType.StringType },
                    navArgument("age") { type = NavType.IntType },
                    navArgument("gender") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val encodedUriStr = backStackEntry.arguments?.getString("imageUri") ?: ""
                val decodedUriStr = Uri.decode(encodedUriStr)
                val imageUri = Uri.parse(decodedUriStr)
                val age = backStackEntry.arguments?.getInt("age") ?: 0
                val genderStr = backStackEntry.arguments?.getString("gender") ?: Gender.MALE.name
                val gender = try {
                    Gender.valueOf(genderStr)
                } catch (e: Exception) {
                    Gender.MALE
                }

                LoadingScreen(
                    imageUri = imageUri,
                    age = age,
                    gender = gender,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onHomeClick = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    onViewImageClick = { uri ->
                        val encodedUri = Uri.encode(uri.toString())
                        navController.navigate("image_viewer/$encodedUri")
                    },
                    onAnalysisFinished = { result ->
                        if (result is OcrAnalysisResult.Success) {
                            AdManager.incrementAnalysisCount()
                        }
                        currentOcrResult = result
                        val encodedImageUri = Uri.encode(imageUri.toString())
                        navController.navigate("result/$encodedImageUri/$age/${gender.name}") {
                            popUpTo("loading/$encodedImageUri/$age/${gender.name}") { inclusive = true }
                        }
                    }
                )
            }

            // Result Screen
            composable(
                route = "result/{imageUri}/{age}/{gender}",
                arguments = listOf(
                    navArgument("imageUri") { type = NavType.StringType },
                    navArgument("age") { type = NavType.IntType },
                    navArgument("gender") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val encodedUriStr = backStackEntry.arguments?.getString("imageUri") ?: ""
                val decodedUriStr = Uri.decode(encodedUriStr)
                val imageUri = Uri.parse(decodedUriStr)
                val age = backStackEntry.arguments?.getInt("age") ?: 0
                val genderStr = backStackEntry.arguments?.getString("gender") ?: Gender.MALE.name
                val gender = try {
                    Gender.valueOf(genderStr)
                } catch (e: Exception) {
                    Gender.MALE
                }

                val result = currentOcrResult ?: OcrAnalysisResult.Failure("تعذر تحليل التقرير.")

                ResultScreen(
                    imageUri = imageUri,
                    age = age,
                    gender = gender,
                    ocrResult = result,
                    onTestClick = { evaluated ->
                        val encodedVal = Uri.encode(evaluated.rawValue)
                        val encodedUnit = Uri.encode(evaluated.unit)
                        navController.navigate("details/${evaluated.testKey}/$encodedVal/$encodedUnit/$age/${gender.name}")
                    },
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onHomeClick = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    onViewImageClick = { uri ->
                        val encodedUri = Uri.encode(uri.toString())
                        navController.navigate("image_viewer/$encodedUri")
                    }
                )
            }

            // Details Screen
            composable(
                route = "details/{testKey}/{value}/{unit}/{age}/{gender}",
                arguments = listOf(
                    navArgument("testKey") { type = NavType.StringType },
                    navArgument("value") { type = NavType.StringType },
                    navArgument("unit") { type = NavType.StringType },
                    navArgument("age") { type = NavType.IntType },
                    navArgument("gender") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val testKey = backStackEntry.arguments?.getString("testKey") ?: ""
                val rawVal = Uri.decode(backStackEntry.arguments?.getString("value") ?: "0")
                val age = backStackEntry.arguments?.getInt("age") ?: 30
                val genderStr = backStackEntry.arguments?.getString("gender") ?: Gender.MALE.name
                val gender = try {
                    Gender.valueOf(genderStr)
                } catch (e: Exception) {
                    Gender.MALE
                }

                val evaluatedResult = MedicalDatabase.evaluateResult(
                    testKey = testKey,
                    rawValue = rawVal,
                    age = age,
                    gender = gender
                )

                DetailsScreen(
                    result = evaluatedResult,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            // Search Screen
            composable("search") {
                SearchScreen(
                    onTestSelect = { knowledge ->
                        navController.navigate("details/${knowledge.key}/0/${Uri.encode(knowledge.defaultUnit)}/30/MALE")
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            // History Screen
            composable("history") {
                HistoryScreen(
                    repository = repository,
                    onReportSelect = { reportWithTests ->
                        val labValues = reportWithTests.tests.map { item ->
                            ExtractedLabValue(
                                id = item.id,
                                testNameArabic = item.arabicName,
                                abbreviation = item.abbreviation,
                                value = item.rawValue,
                                unit = item.unit,
                                isLowConfidence = item.isLowConfidence
                            )
                        }
                        currentOcrResult = OcrAnalysisResult.Success(labValues = labValues, rawText = "")
                        val imageUri = try {
                            Uri.parse(reportWithTests.report.imageUriString)
                        } catch (e: Exception) {
                            Uri.EMPTY
                        }
                        val encodedImageUri = Uri.encode(imageUri.toString())
                        val age = reportWithTests.report.patientAge
                        val gender = reportWithTests.report.patientGender
                        navController.navigate("result/$encodedImageUri/$age/$gender")
                    },
                    onDeleteReport = { reportId ->
                        scope.launch {
                            repository.deleteReport(reportId)
                        }
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            // Image Viewer Screen
            composable(
                route = "image_viewer/{imageUri}",
                arguments = listOf(
                    navArgument("imageUri") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val encodedUriStr = backStackEntry.arguments?.getString("imageUri") ?: ""
                val decodedUriStr = Uri.decode(encodedUriStr)
                val imageUri = Uri.parse(decodedUriStr)

                ImageViewerScreen(
                    imageUri = imageUri,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
