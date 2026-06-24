package com.example.coursetable.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "courses",
    foreignKeys = [
        ForeignKey(
            entity = Schedule::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("scheduleId")]
)
data class Course(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scheduleId: Long = 0,
    val name: String = "",
    val teacher: String = "",
    val classroom: String = "",
    val dayOfWeek: Int = 1, // 1=Monday ... 7=Sunday
    val startTime: String = "08:00",
    val endTime: String = "09:35",
    val startWeek: Int = 1,
    val endWeek: Int = 20,
    val color: Int = 0xFF6750A4.toInt(), // Default purple
    val notes: String? = null,
    val weekType: String = "ALL", // ALL, ODD, EVEN
    val orderIndex: Int = 0
) {
    enum class WeekType(val label: String) {
        ALL("每周"),
        ODD("单周"),
        EVEN("双周");

        companion object {
            fun fromString(value: String): WeekType {
                return entries.find { it.name == value } ?: ALL
            }
        }
    }

    /**
     * Check if this course occurs in the given week.
     */
    fun isInWeek(week: Int): Boolean {
        if (week < startWeek || week > endWeek) return false
        return when (weekType) {
            "ODD" -> week % 2 == 1
            "EVEN" -> week % 2 == 0
            else -> true
        }
    }

    companion object {
        val DAY_LABELS = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    }
}
