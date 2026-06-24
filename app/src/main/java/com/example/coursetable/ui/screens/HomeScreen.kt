package com.example.coursetable.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coursetable.data.model.Course
import com.example.coursetable.data.model.Schedule
import com.example.coursetable.ui.components.CourseDetailSheet
import com.example.coursetable.ui.components.CourseGrid
import com.example.coursetable.ui.components.WeekSelector

/**
 * Main home screen with the course grid, week selector, and schedule management.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    courses: List<Course>,
    schedules: List<Schedule>,
    activeSchedule: Schedule?,
    currentWeek: Int,
    displayedWeek: Int,
    snackbarMessage: String?,
    onNavigateToAddCourse: () -> Unit,
    onNavigateToOCRImport: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSwitchSchedule: (Long) -> Unit,
    onSetDisplayedWeek: (Int) -> Unit,
    onGoToCurrentWeek: () -> Unit,
    onEditCourse: (Course) -> Unit,
    onDeleteCourse: (Course) -> Unit,
    onClearSnackbar: () -> Unit
) {
    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var showScheduleMenu by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearSnackbar()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showScheduleMenu = true }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = activeSchedule?.name ?: "课程表",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            activeSchedule?.semester?.let {
                                Text(
                                    text = it,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = showScheduleMenu,
                        onDismissRequest = { showScheduleMenu = false }
                    ) {
                        schedules.forEach { schedule ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = schedule.name,
                                            fontWeight = if (schedule.id == activeSchedule?.id)
                                                FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (schedule.id == activeSchedule?.id) {
                                            Text(
                                                text = "\u2713",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onSwitchSchedule(schedule.id)
                                    showScheduleMenu = false
                                }
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onGoToCurrentWeek) {
                        Icon(
                            imageVector = Icons.Filled.Today,
                            contentDescription = "回到本周",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Box {
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "更多选项"
                            )
                        }
                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("OCR导入") },
                                onClick = {
                                    showOptionsMenu = false
                                    onNavigateToOCRImport()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.DocumentScanner,
                                        contentDescription = null
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("设置") },
                                onClick = {
                                    showOptionsMenu = false
                                    onNavigateToSettings()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Schedule,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddCourse,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "添加课程",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            WeekSelector(
                currentWeek = currentWeek,
                displayedWeek = displayedWeek,
                onPreviousWeek = { onSetDisplayedWeek(displayedWeek - 1) },
                onNextWeek = { onSetDisplayedWeek(displayedWeek + 1) },
                onGoToCurrentWeek = onGoToCurrentWeek
            )

            if (courses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "暂无课程",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "点击右下角 + 按钮添加课程\n或使用 OCR 导入课程表",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                val filteredCourses = courses.filter { it.isInWeek(displayedWeek) }

                if (filteredCourses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "\u7B2C${displayedWeek}\u5468\u65E0\u8BFE\u7A0B",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    CourseGrid(
                        courses = filteredCourses,
                        displayedWeek = displayedWeek,
                        currentWeek = currentWeek,
                        onCourseClick = { course -> selectedCourse = course },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    selectedCourse?.let { course ->
        CourseDetailSheet(
            course = course,
            onDismiss = { selectedCourse = null },
            onEdit = {
                selectedCourse = null
                onEditCourse(course)
            },
            onDelete = {
                selectedCourse = null
                onDeleteCourse(course)
            }
        )
    }
}
