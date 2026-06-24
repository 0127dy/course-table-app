package com.example.coursetable.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.coursetable.data.model.Schedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedules ORDER BY createdDate DESC")
    fun getAllSchedules(): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSchedule(): Schedule?

    @Query("SELECT * FROM schedules WHERE isActive = 1 LIMIT 1")
    fun getActiveScheduleFlow(): Flow<Schedule?>

    @Query("SELECT * FROM schedules WHERE id = :scheduleId")
    suspend fun getScheduleById(scheduleId: Long): Schedule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: Schedule): Long

    @Update
    suspend fun update(schedule: Schedule)

    @Delete
    suspend fun delete(schedule: Schedule)

    @Query("UPDATE schedules SET isActive = 0 WHERE isActive = 1")
    suspend fun deactivateAll()

    @Query("UPDATE schedules SET isActive = 1 WHERE id = :scheduleId")
    suspend fun setActive(scheduleId: Long)

    @Query("UPDATE schedules SET name = :name WHERE id = :scheduleId")
    suspend fun rename(scheduleId: Long, name: String)

    @Query("SELECT COUNT(*) FROM schedules")
    suspend fun getCount(): Int
}
