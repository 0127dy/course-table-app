package com.example.coursetable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.coursetable.ui.screens.AddCourseScreen
import com.example.coursetable.ui.screens.HomeScreen
import com.example.coursetable.ui.screens.OCRImportScreen
import com.example.coursetable.ui.screens.SettingsScreen
import com.example.coursetable.ui.theme.CourseTableTheme
import com.example.coursetable.viewmodel.CourseViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CourseTableTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CourseTableApp()
                }
            }
        }
    }
}

@Composable
fun CourseTableApp() {
    val navController = rememberNavController()
    val viewModel: CourseViewModel = viewModel()

    val courses by viewModel.courses.collectAsState()
    val schedules by viewModel.allSchedules.collectAsState()
    val activeSchedule by viewModel.activeSchedule.collectAsState()
    val snackbarMessage = viewModel.snackbarMessage

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                courses = courses,
                schedules = schedules,
                activeSchedule = activeSchedule,
                currentWeek = viewModel.currentWeek,
                displayedWeek = viewModel.displayedWeek,
                snackbarMessage = snackbarMessage,
                onNavigateToAddCourse = {
                    viewModel.selectEditingCourse(null)
                    navController.navigate("add_course")
                },
                onNavigateToOCRImport = {
                    navController.navigate("ocr_import")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onSwitchSchedule = { scheduleId ->
                    viewModel.switchSchedule(scheduleId)
                },
                onSetDisplayedWeek = { week ->
                    viewModel.navigateToWeek(week)
                },
                onGoToCurrentWeek = {
                    viewModel.goToCurrentWeek()
                },
                onEditCourse = { course ->
                    viewModel.selectEditingCourse(course)
                    navController.navigate("add_course")
                },
                onDeleteCourse = { course ->
                    viewModel.deleteCourse(course)
                },
                onClearSnackbar = {
                    viewModel.clearSnackbar()
                }
            )
        }

        composable("add_course") {
            val editingCourse = viewModel.editingCourse
            AddCourseScreen(
                existingCourse = editingCourse,
                onSave = { course ->
                    if (editingCourse != null) {
                        viewModel.updateCourse(course)
                    } else {
                        viewModel.addCourse(course)
                    }
                    navController.popBackStack()
                },
                onBack = {
                    viewModel.selectEditingCourse(null)
                    navController.popBackStack()
                }
            )
        }

        composable("ocr_import") {
            OCRImportScreen(
                onCoursesParsed = { parsedCourses ->
                    viewModel.saveParsedCourses(parsedCourses)
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                schedules = schedules,
                activeScheduleId = activeSchedule?.id,
                onCreateSchedule = { name ->
                    viewModel.createSchedule(name)
                },
                onRenameSchedule = { id, name ->
                    viewModel.renameSchedule(id, name)
                },
                onDeleteSchedule = { id ->
                    viewModel.deleteSchedule(id)
                },
                onSelectSchedule = { id ->
                    viewModel.switchSchedule(id)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
