package com.example.coursetable.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coursetable.data.model.Course
import com.example.coursetable.ui.theme.CourseColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddCourseScreen(
    existingCourse: Course? = null,
    onSave: (Course) -> Unit,
    onBack: () -> Unit
) {
    val isEditing = existingCourse != null

    var name by remember { mutableStateOf(existingCourse?.name ?: "") }
    var teacher by remember { mutableStateOf(existingCourse?.teacher ?: "") }
    var classroom by remember { mutableStateOf(existingCourse?.classroom ?: "") }
    var dayOfWeek by remember { mutableIntStateOf(existingCourse?.dayOfWeek ?: 1) }
    var startTime by remember { mutableStateOf(existingCourse?.startTime ?: "08:00") }
    var endTime by remember { mutableStateOf(existingCourse?.endTime ?: "09:35") }
    var startWeek by remember { mutableStateOf((existingCourse?.startWeek ?: 1).toString()) }
    var endWeek by remember { mutableStateOf((existingCourse?.endWeek ?: 20).toString()) }
    var weekType by remember { mutableStateOf(existingCourse?.weekType ?: "ALL") }
    var selectedColor by remember { mutableIntStateOf(existingCourse?.color ?: CourseColors[0].hashCode()) }
    var notes by remember { mutableStateOf(existingCourse?.notes ?: "") }

    val dayLabels = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    val weekTypeLabels = listOf("每周", "单周", "双周")
    val weekTypeValues = listOf("ALL", "ODD", "EVEN")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "编辑课程" else "添加课程",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Course name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("课程名称") },
                placeholder = { Text("例如：高等数学") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Teacher
            OutlinedTextField(
                value = teacher,
                onValueChange = { teacher = it },
                label = { Text("授课教师") },
                placeholder = { Text("例如：张老师") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Classroom
            OutlinedTextField(
                value = classroom,
                onValueChange = { classroom = it },
                label = { Text("上课教室") },
                placeholder = { Text("例如：教学楼A201") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Day of week
            Text(
                text = "上课日期",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dayLabels.forEachIndexed { index, label ->
                    FilterChip(
                        selected = dayOfWeek == index + 1,
                        onClick = { dayOfWeek = index + 1 },
                        label = { Text(label, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Time selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("开始时间") },
                    placeholder = { Text("08:00") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Text("\u2014", color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("结束时间") },
                    placeholder = { Text("09:35") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Week range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = startWeek,
                    onValueChange = { startWeek = it.filter { c -> c.isDigit() } },
                    label = { Text("起始周") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "\u2014",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 24.dp)
                )
                OutlinedTextField(
                    value = endWeek,
                    onValueChange = { endWeek = it.filter { c -> c.isDigit() } },
                    label = { Text("结束周") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            // Week type
            Text(
                text = "周类型",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                weekTypeLabels.forEachIndexed { index, label ->
                    FilterChip(
                        selected = weekType == weekTypeValues[index],
                        onClick = { weekType = weekTypeValues[index] },
                        label = { Text(label, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Color picker
            Text(
                text = "课程颜色",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CourseColors.forEachIndexed { index, color ->
                    val isSelected = Color(selectedColor) == color
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (isSelected) {
                                    Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                } else Modifier
                            )
                            .clickable { selectedColor = color.hashCode() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "选中",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("备注") },
                placeholder = { Text("可选备注信息") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            Button(
                onClick = {
                    val course = Course(
                        id = existingCourse?.id ?: 0,
                        scheduleId = existingCourse?.scheduleId ?: 0,
                        name = name.ifBlank { return@Button },
                        teacher = teacher,
                        classroom = classroom,
                        dayOfWeek = dayOfWeek,
                        startTime = startTime.ifBlank { "08:00" },
                        endTime = endTime.ifBlank { "09:35" },
                        startWeek = startWeek.toIntOrNull() ?: 1,
                        endWeek = endWeek.toIntOrNull() ?: 20,
                        weekType = weekType,
                        color = selectedColor,
                        notes = notes.ifBlank { null }
                    )
                    onSave(course)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = name.isNotBlank()
            ) {
                Text(
                    text = "保存课程",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
