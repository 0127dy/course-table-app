package com.example.coursetable.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.coursetable.data.model.Course
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    @Query("SELECT * FROM courses WHERE scheduleId = :scheduleId ORDER BY dayOfWeek, startTime")
    fun getCoursesBySchedule(scheduleId: Long): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE scheduleId = :scheduleId AND dayOfWeek = :dayOfWeek ORDER BY startTime")
    fun getCoursesByDay(scheduleId: Long, dayOfWeek: Int): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE id = :courseId")
    suspend fun getCourseById(courseId: Long): Course?

    @Query("SELECT * FROM courses WHERE scheduleId = :scheduleId ORDER BY dayOfWeek, startTime")
    suspend fun getCoursesByScheduleOnce(scheduleId: Long): List<Course>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: Course): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(courses: List<Course>): List<Long>

    @Update
    suspend fun update(course: Course)

    @Delete
    suspend fun delete(course: Course)

    @Query("DELETE FROM courses WHERE id = :courseId")
    suspend fun deleteById(courseId: Long)

    @Query("DELETE FROM courses WHERE scheduleId = :scheduleId")
    suspend fun deleteBySchedule(scheduleId: Long)
}
