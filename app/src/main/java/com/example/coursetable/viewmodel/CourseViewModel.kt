package com.example.coursetable.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursetable.data.db.AppDatabase
import com.example.coursetable.data.model.Course
import com.example.coursetable.data.model.Schedule
import com.example.coursetable.ocr.OCRProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CourseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val courseDao = db.courseDao()
    private val scheduleDao = db.scheduleDao()

    // Active schedule
    private val _activeSchedule = MutableStateFlow<Schedule?>(null)
    val activeSchedule: StateFlow<Schedule?> = _activeSchedule.asStateFlow()

    // All schedules
    val allSchedules: StateFlow<List<Schedule>> = scheduleDao.getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Courses for active schedule
    val courses: StateFlow<List<Course>> = _activeSchedule.flatMapLatest { schedule ->
        if (schedule != null) {
            courseDao.getCoursesBySchedule(schedule.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current week number (academic week)
    var currentWeek by mutableIntStateOf(computeCurrentWeek())
        private set

    // Displayed week (for navigation)
    var displayedWeek by mutableIntStateOf(computeCurrentWeek())
        private set

    // Editing course
    var editingCourse by mutableStateOf<Course?>(null)
        private set

    // OCR state
    var isProcessingOCR by mutableStateOf(false)
        private set

    // Parsed courses from OCR
    var parsedCourses by mutableStateOf<List<Course>>(emptyList())
        private set

    // Snackbar message
    var snackbarMessage by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            // Initialize active schedule
            var active = scheduleDao.getActiveSchedule()
            if (active == null) {
                val id = scheduleDao.insert(Schedule(name = "默认课表", isActive = true))
                active = scheduleDao.getScheduleById(id)
            }
            _activeSchedule.value = active
        }
    }

    /**
     * Calculate the current academic week.
     * Week 1 starts from September 1st of the current year.
     */
    private fun computeCurrentWeek(): Int {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) // 0-based

        // Semester start: September 1 (month 8) or February 15 (month 1)
        val semesterStart = if (month >= Calendar.SEPTEMBER) {
            // Fall semester starts Sep 1
            val start = Calendar.getInstance()
            start.set(year, Calendar.SEPTEMBER, 1)
            start
        } else {
            // Spring semester starts Feb 15
            val start = Calendar.getInstance()
            start.set(year, Calendar.FEBRUARY, 15)
            start
        }

        val diffInMillis = calendar.timeInMillis - semesterStart.timeInMillis
        val week = (diffInMillis / (7 * 24 * 60 * 60 * 1000L)).toInt() + 1
        return week.coerceIn(1, 30)
    }

    /**
     * Navigate to a specific week.
     */
    fun navigateToWeek(week: Int) {
        displayedWeek = week.coerceIn(1, 30)
    }

    /**
     * Navigate to the current week.
     */
    fun goToCurrentWeek() {
        currentWeek = computeCurrentWeek()
        displayedWeek = currentWeek
    }

    /**
     * Switch active schedule.
     */
    fun switchSchedule(scheduleId: Long) {
        viewModelScope.launch {
            scheduleDao.deactivateAll()
            scheduleDao.setActive(scheduleId)
            val schedule = scheduleDao.getScheduleById(scheduleId)
            _activeSchedule.value = schedule
        }
    }

    /**
     * Create a new schedule.
     */
    fun createSchedule(name: String, semester: String? = null) {
        viewModelScope.launch {
            val id = scheduleDao.insert(
                Schedule(
                    name = name,
                    semester = semester,
                    isActive = false
                )
            )
            snackbarMessage = "课表「$name」已创建"
        }
    }

    /**
     * Rename a schedule.
     */
    fun renameSchedule(scheduleId: Long, newName: String) {
        viewModelScope.launch {
            scheduleDao.rename(scheduleId, newName)
            val schedule = scheduleDao.getScheduleById(scheduleId)
            if (schedule != null && schedule.isActive) {
                _activeSchedule.value = schedule
            }
            snackbarMessage = "已重命名为「$newName」"
        }
    }

    /**
     * Delete a schedule and its courses.
     */
    fun deleteSchedule(scheduleId: Long) {
        viewModelScope.launch {
            val schedule = scheduleDao.getScheduleById(scheduleId) ?: return@launch
            val name = schedule.name
            scheduleDao.delete(schedule)

            // If we deleted the active schedule, activate another one
            if (schedule.isActive) {
                val allSchedulesList = allSchedules.value
                if (allSchedulesList.isNotEmpty()) {
                    val first = allSchedulesList.first()
                    scheduleDao.setActive(first.id)
                    _activeSchedule.value = scheduleDao.getScheduleById(first.id) ?: first
                } else {
                    // Create default
                    val id = scheduleDao.insert(Schedule(name = "默认课表", isActive = true))
                    _activeSchedule.value = scheduleDao.getScheduleById(id)
                }
            }
            snackbarMessage = "课表「$name」已删除"
        }
    }

    /**
     * Add a course.
     */
    fun addCourse(course: Course) {
        viewModelScope.launch {
            val schedule = _activeSchedule.value
            if (schedule != null) {
                courseDao.insert(course.copy(scheduleId = schedule.id))
                snackbarMessage = "课程已添加"
            }
        }
    }

    /**
     * Update a course.
     */
    fun updateCourse(course: Course) {
        viewModelScope.launch {
            courseDao.update(course)
            editingCourse = null
            snackbarMessage = "课程已更新"
        }
    }

    /**
     * Delete a course.
     */
    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            courseDao.delete(course)
            snackbarMessage = "课程已删除"
        }
    }

    /**
     * Set the course being edited.
     */
    fun selectEditingCourse(course: Course?) {
        editingCourse = course
    }

    /**
     * Process a bitmap through OCR and parse courses.
     */
    fun processOCRBitmap(bitmap: android.graphics.Bitmap) {
        viewModelScope.launch {
            isProcessingOCR = true
            parsedCourses = emptyList()
            try {
                val processor = OCRProcessor()
                val result = processor.processBitmap(bitmap)
                processor.close()

                val parsed = parseCourseText(result.text)
                parsedCourses = parsed
                if (parsed.isEmpty()) {
                    snackbarMessage = "未能识别出课程信息，请重试"
                } else {
                    snackbarMessage = "识别到 ${parsed.size} 门课程"
                }
            } catch (e: Exception) {
                snackbarMessage = "识别失败: ${e.message}"
            } finally {
                isProcessingOCR = false
            }
        }
    }

    /**
     * Save parsed OCR courses to the active schedule.
     */
    fun saveParsedCourses(courses: List<Course>) {
        viewModelScope.launch {
            val schedule = _activeSchedule.value
            if (schedule != null) {
                courseDao.insertAll(courses.map { it.copy(scheduleId = schedule.id) })
                parsedCourses = emptyList()
                snackbarMessage = "已成功导入 ${courses.size} 门课程"
            }
        }
    }

    /**
     * Smart parsing of recognized text into course objects.
     * Supports patterns like:
     * - "高等数学 周一 8:00-9:35 教学楼A201"
     * - "Math 101 Mon 08:00-09:35 Room 201"
     * - "课程名称|教师|教室|星期|时间" (table-like)
     */
    private fun parseCourseText(text: String): List<Course> {
        val lines = text.trim().lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val courses = mutableListOf<Course>()
        val dayKeywords = mapOf(
            "周" to 0, // Will need contextual matching
            "周一" to 1, "星期" to 0, "一" to 1,
            "周二" to 2, "二" to 2,
            "周三" to 3, "三" to 3,
            "周四" to 4, "四" to 4,
            "周五" to 5, "五" to 5,
            "周六" to 6, "六" to 6,
            "周日" to 7, "日" to 7,
            "Mon" to 1, "MON" to 1, "monday" to 1, "Monday" to 1,
            "Tue" to 2, "TUE" to 2, "tuesday" to 2, "Tuesday" to 2,
            "Wed" to 3, "WED" to 3, "wednesday" to 3, "Wednesday" to 3,
            "Thu" to 4, "THU" to 4, "thursday" to 4, "Thursday" to 4,
            "Fri" to 5, "FRI" to 5, "friday" to 5, "Friday" to 5,
            "Sat" to 6, "SAT" to 6, "saturday" to 6, "Saturday" to 6,
            "Sun" to 7, "SUN" to 7, "sunday" to 7, "Sunday" to 7,
        )

        // Parse each line by trying different patterns
        for (line in lines) {
            var dayOfWeek = 0
            var courseName = ""
            var teacher = ""
            var classroom = ""
            var startTime = "08:00"
            var endTime = "09:35"
            var weekType = "ALL"

            // Try to find day of week
            for ((keyword, day) in dayKeywords) {
                if (line.contains(keyword, ignoreCase = true)) {
                    dayOfWeek = day
                    break
                }
            }

            if (dayOfWeek == 0) continue

            // Try to find time pattern HH:MM-HH:MM or HH:MM~HH:MM
            val timePattern = Regex("""(\d{1,2}):(\d{2})\s*[-~]\s*(\d{1,2}):(\d{2})""")
            val timeMatch = timePattern.find(line)
            if (timeMatch != null) {
                startTime = "${timeMatch.groupValues[1].padStart(2, '0')}:${timeMatch.groupValues[2]}"
                endTime = "${timeMatch.groupValues[3].padStart(2, '0')}:${timeMatch.groupValues[4]}"
            }

            // Check for week type patterns
            if (line.contains("单") && (line.contains("周") || line.contains("双"))) {
                weekType = if (line.contains("单周") || line.contains("单数")) "ODD" else "EVEN"
            }
            if (line.contains("双周") || line.contains("偶数")) {
                weekType = "EVEN"
            }
            if (line.contains("odd", ignoreCase = true)) weekType = "ODD"
            if (line.contains("even", ignoreCase = true)) weekType = "EVEN"

            // Extract week range
            val weekRangePattern = Regex("""(?:第)?(\d{1,2})\s*[-~]\s*(\d{1,2})\s*周""")
            val weekMatch = weekRangePattern.find(line)

            // Try to extract course name - everything before day/time pattern
            val cleanLine = line
                .replace(timePattern, "")
                .replace(Regex("""\d+:\d+"""), "")

            // Extract classroom - look for building/room patterns
            val roomPattern = Regex("""[A-Za-z0-9]+\s*(?:楼|栋|教学楼|教|室|Room|room|Rm)""")
            val roomMatch = roomPattern.find(cleanLine)

            // Simple heuristic: course name is typically the first recognizable text
            val tokens = cleanLine.split(Regex("""[\s,，\t|/\\]+""")).filter {
                it.isNotBlank() && !dayKeywords.keys.any { kw -> it.contains(kw) }
            }

            if (tokens.isNotEmpty()) {
                courseName = tokens.first()
            }

            // If we have enough info, create a course entry
            if (courseName.isNotEmpty()) {
                val course = Course(
                    name = courseName,
                    teacher = teacher,
                    classroom = classroom,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    startWeek = weekMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1,
                    endWeek = weekMatch?.groupValues?.get(2)?.toIntOrNull() ?: 20,
                    weekType = weekType,
                    color = listOf(
                        0xFF6750A4.toInt(), 0xFF4A90D9.toInt(), 0xFFE91E63.toInt(), 0xFF009688.toInt(),
                        0xFFFF9800.toInt(), 0xFF4CAF50.toInt(), 0xFFF44336.toInt(), 0xFF9C27B0.toInt()
                    ).random()
                )
                courses.add(course)
            }
        }

        return courses
    }

    /**
     * Clear snackbar message.
     */
    fun clearSnackbar() {
        snackbarMessage = null
    }

    override fun onCleared() {
        super.onCleared()
    }
}
