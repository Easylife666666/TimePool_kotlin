package com.example.timepool

import android.app.Application
import com.example.timepool.data.TimePoolDatabase
import com.example.timepool.data.TimePoolRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class TimePoolApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { TimePoolDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { TimePoolRepository(database.timePoolDao()) }
}
