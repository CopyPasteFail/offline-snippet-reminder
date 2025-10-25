package com.copypastefail.offlinereminder.util

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.copypastefail.offlinereminder.data.local.SnippetListEntity
import com.copypastefail.offlinereminder.worker.ReminderWorker
import java.util.concurrent.TimeUnit

class ReminderScheduler(private val context: Context) {

    private val workManager: WorkManager = WorkManager.getInstance(context)

    fun syncSchedules(lists: List<SnippetListEntity>) {
        lists.forEach { list ->
            if (list.isActive) {
                scheduleList(list)
            } else {
                cancelList(list.id)
            }
        }
    }

    fun scheduleList(list: SnippetListEntity) {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            list.frequencySeconds,
            TimeUnit.SECONDS
        ).setInputData(ReminderWorker.createInput(list.id))
            .setInitialDelay(list.frequencySeconds, TimeUnit.SECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName(list.id),
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelList(listId: Int) {
        workManager.cancelUniqueWork(uniqueWorkName(listId))
    }

    private fun uniqueWorkName(listId: Int): String = "reminder_$listId"
}
