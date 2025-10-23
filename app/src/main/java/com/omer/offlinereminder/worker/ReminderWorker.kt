package com.omer.offlinereminder.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.omer.offlinereminder.data.local.ReminderDatabase
import com.omer.offlinereminder.notification.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val listId = inputData.getInt(KEY_LIST_ID, -1)
        if (listId == -1) {
            return@withContext Result.failure()
        }

        val dao = ReminderDatabase.getDatabase(applicationContext).reminderDao()
        val list = dao.getListById(listId) ?: return@withContext Result.success()
        if (!list.isActive) {
            return@withContext Result.success()
        }

        val snippets = dao.getSnippetsForList(listId).sortedBy { it.orderIndex }
        if (snippets.isEmpty()) {
            return@withContext Result.success()
        }

        val currentIndex = list.currentIndex.coerceIn(0, snippets.lastIndex)
        val snippet = snippets.getOrNull(currentIndex) ?: snippets.first()

        NotificationHelper.showNotification(
            context = applicationContext,
            listId = list.id,
            listName = list.name,
            snippetText = snippet.text
        )

        val nextIndex = (currentIndex + 1) % snippets.size
        dao.updateCurrentIndex(listId, nextIndex)

        Result.success()
    }

    companion object {
        const val KEY_LIST_ID = "listId"

        fun createInput(listId: Int) = workDataOf(KEY_LIST_ID to listId)
    }
}
