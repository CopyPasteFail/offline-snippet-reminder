package com.copypastefail.offlinereminder

import android.app.Application
import com.copypastefail.offlinereminder.data.local.ReminderDatabase
import com.copypastefail.offlinereminder.data.repository.SnippetRepository
import com.copypastefail.offlinereminder.util.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
