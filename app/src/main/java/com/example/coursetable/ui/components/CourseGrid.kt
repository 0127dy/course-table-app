package com.example.coursetable.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coursetable.data.model.Course
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

// Time slots from 8:00 to 21:00, each slot is 1 hour with 30min intervals
private val TIME_SLOTS = listOf(
    "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
    "11:00", "11:30", "12:00", "12:30", "13:00", "13:30",
    "14:00", "14:30", "15:00", "15:30", "16:00", "16:30",
    "17:00", "17:30", "18:00", "18:30", "19:00", "19:30",
    "20:00", "20:30", "21:00"
)

private val HOUR_LABELS = listOf(
    "8:00", "9:00", "10:00", "11:00", "12:00",
    "13:00", "14:00", "15:00", "16:00", "17:00",
    "18:00", "19:00", "20:00", "21:00"
)

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private const val SLOT_HEIGHT_DP = 48f
private const val SLOT_WIDTH_DP = 100f
private const val TIME_COL_WIDTH_DP = 52f
private const val HEADER_HEIGHT_DP = 40f

/**
 * Weekly course grid showing Mon-Sun with 8:00-21:00 time slots.
 * Properly positions courses based on start/end times.
 */
@Composable
fun CourseGrid(
    courses: List<Course>,
    displayedWeek: Int,
    currentWeek: Int,
    onCourseClick: (Course) -> Unit,
    modifier: Modifier = Modifier
) {
    val dayHeaders = listOf("一", "二", "三", "四", "五", "六", "日")
    val dayFullHeaders = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    val isCurrentDay = remember {
        val today = java.time.LocalDate.now().dayOfWeek.value // 1=Mon, 7=Sun
        today
    }

    val scrollState = rememberScrollState()
    val listState = rememberLazyListState()

    // Auto-scroll to current time on first load
    LaunchedEffect(Unit) {
        val currentTime = LocalTime.now()
        val currentHour = currentTime.hour
        val scrollToIndex = (currentHour - 8).coerceIn(0, HOUR_LABELS.size - 2)
        if (scrollToIndex > 0) {
            listState.scrollToItem(scrollToIndex * 2)
        }
    }

    // Calculate position for a course based on time
    fun getTimeMinutes(time: String): Int {
        return try {
            val parts = time.split(":")
            parts[0].toInt() * 60 + parts[1].toInt()
        } catch (e: Exception) {
            480 // default 8:00
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Header row (day labels)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            // Empty top-left corner
            Box(
                modifier = Modifier
                    .width(TIME_COL_WIDTH_DP.dp)
                    .height(HEADER_HEIGHT_DP.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("时间", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Day headers
            for (day in 0 until 7) {
                val isToday = day + 1 == isCurrentDay
                Box(
                    modifier = Modifier
                        .width(SLOT_WIDTH_DP.dp)
                        .height(HEADER_HEIGHT_DP.dp)
                        .background(
                            if (isToday) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = dayFullHeaders[day],
                            fontSize = 12.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Grid body
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState)
        ) {
            // Time slot rows
            itemsIndexed(TIME_SLOTS) { index, time ->
                if (index < TIME_SLOTS.size - 1) {
                    val isHour = time.endsWith(":00")
                    val hourIndex = index / 2

                    Row(
                        modifier = Modifier
                            .height(SLOT_HEIGHT_DP.dp)
                            .width(((TIME_COL_WIDTH_DP + 7 * SLOT_WIDTH_DP).dp))
                    ) {
                        // Time label column
                        Box(
                            modifier = Modifier
                                .width(TIME_COL_WIDTH_DP.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.surface)
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isHour) {
                                Text(
                                    text = time.substring(0, 5),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Day columns for this time slot
                        for (day in 0 until 7) {
                            val coursesAtSlot = courses.filter { course ->
                                course.dayOfWeek == day + 1 &&
                                        course.isInWeek(displayedWeek) &&
                                        isCourseInTimeSlot(course, time)
                            }

                            val isCurrentTimeSlot = isCurrentTimeSlot(day + 1, time)

                            Box(
                                modifier = Modifier
                                    .width(SLOT_WIDTH_DP.dp)
                                    .fillMaxHeight()
                                    .background(
                                        when {
                                            isCurrentTimeSlot -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                            index % 2 == 0 -> MaterialTheme.colorScheme.surface
                                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        }
                                    )
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            ) {
                                if (coursesAtSlot.isNotEmpty()) {
                                    val course = coursesAtSlot.first()
                                    CourseCard(
                                        course = course,
                                        isOngoing = isCurrentTimeSlot,
                                        cardWidth = (SLOT_WIDTH_DP - 2).dp,
                                        cardHeight = calculateCourseHeight(course).dp,
                                        onClick = { onCourseClick(course) },
                                        modifier = Modifier.padding(horizontal = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Check if a course is active during this time slot.
 */
private fun isCourseInTimeSlot(course: Course, slotTime: String): Boolean {
    return try {
        val slot = LocalTime.parse(slotTime, timeFormatter)
        val start = LocalTime.parse(course.startTime, timeFormatter)
        val end = LocalTime.parse(course.endTime, timeFormatter)
        // Course covers this slot if slot time is within its range
        !slot.isBefore(start) && slot.isBefore(end)
    } catch (e: Exception) {
        false
    }
}

/**
 * Check if the given time slot is the current time.
 */
private fun isCurrentTimeSlot(dayOfWeek: Int, slotTime: String): Boolean {
    val now = java.time.LocalDateTime.now()
    if (now.dayOfWeek.value != dayOfWeek) return false
    return try {
        val time = LocalTime.parse(slotTime, timeFormatter)
        val currentTime = now.toLocalTime()
        // Highlight half-hour slots
        val diff = java.time.Duration.between(time, currentTime).toMinutes()
        diff in 0..29
    } catch (e: Exception) {
        false
    }
}

/**
 * Calculate course card height based on duration.
 * Each half-hour = SLOT_HEIGHT_DP dp.
 */
private fun calculateCourseHeight(course: Course): Float {
    return try {
        val start = LocalTime.parse(course.startTime, timeFormatter)
        val end = LocalTime.parse(course.endTime, timeFormatter)
        val durationMinutes = java.time.Duration.between(start, end).toMinutes()
        val slots = durationMinutes / 30.0
        (slots * SLOT_HEIGHT_DP).toFloat().coerceAtLeast(SLOT_HEIGHT_DP)
    } catch (e: Exception) {
        SLOT_HEIGHT_DP
    }
}
