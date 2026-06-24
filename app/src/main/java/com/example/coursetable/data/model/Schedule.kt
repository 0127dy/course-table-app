package com.example.coursetable.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "默认课表",
    val semester: String? = null,
    val isActive: Boolean = false,
    val createdDate: Long = System.currentTimeMillis()
)
