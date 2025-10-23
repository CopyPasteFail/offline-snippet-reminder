package com.omer.offlinereminder

import android.app.Application
import com.omer.offlinereminder.data.local.ReminderDatabase
import com.omer.offlinereminder.data.repository.SnippetRepository
import com.omer.offlinereminder.util.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

class OfflineSnippetReminderApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: ReminderDatabase by lazy {
        ReminderDatabase.getDatabase(this, applicationScope)
    }

    val repository: SnippetRepository by lazy {
        SnippetRepository(database.reminderDao())
    }

    val scheduler: ReminderScheduler by lazy {
        ReminderScheduler(this)
    }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            repository.observeLists().collectLatest { lists ->
                scheduler.syncSchedules(lists)
            }
        }
    }
}
