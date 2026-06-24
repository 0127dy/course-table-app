package com.example.coursetable

import android.app.Application
import com.example.coursetable.data.db.AppDatabase

class CourseTableApplication : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
    }
}
