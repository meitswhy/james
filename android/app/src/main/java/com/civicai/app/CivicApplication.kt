package com.civicai.app

import android.app.Application
import com.civicai.app.data.local.AppDatabase

class CivicApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Room Database
        AppDatabase.getDatabase(this)
    }
}
