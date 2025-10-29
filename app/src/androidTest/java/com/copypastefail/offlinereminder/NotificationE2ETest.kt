package com.copypastefail.offlinereminder

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.copypastefail.offlinereminder.data.local.ReminderDao
import com.copypastefail.offlinereminder.data.local.ReminderDatabase
import com.copypastefail.offlinereminder.data.local.SnippetEntity
import com.copypastefail.offlinereminder.data.local.SnippetListEntity
import com.copypastefail.offlinereminder.worker.ReminderWorker
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationE2ETest {

    private lateinit var context: Context
    private lateinit var workManager: WorkManager
    private lateinit var uiDevice: UiDevice
    private lateinit var dao: ReminderDao
    private val notificationTimeout = 5000L

    @Before
    fun setUp() {
        runBlocking {
            context = ApplicationProvider.getApplicationContext()
            workManager = WorkManager.getInstance(context)
            val db = ReminderDatabase.getDatabase(context)
            dao = db.reminderDao()
            db.clearAllTables()
            uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            uiDevice.pressHome()
        }
    }

    @Test
    fun test_reminderWorker_postsNotification() {
        runBlocking {
            // Arrange
            val listId = 999
            val expectedNotificationText = "This is a test snippet"
            val testList = SnippetListEntity(id = listId, name = "Test List", frequencySeconds = 60L, isActive = true)
            val testSnippet = SnippetEntity(listId = listId, text = expectedNotificationText, orderIndex = 0)
            dao.insertList(testList)
            dao.insertSnippet(testSnippet)

            val inputData = workDataOf(ReminderWorker.KEY_LIST_ID to listId)
            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInputData(inputData)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            workManager.enqueue(workRequest)

            // Act
            uiDevice.openNotification()

            // Assert
            val notification = uiDevice.wait(
                Until.findObject(By.textContains(expectedNotificationText)),
                notificationTimeout
            )

            Assert.assertNotNull(
                "Notification with text containing '$expectedNotificationText' was not found.",
                notification
            )

            // Cleanup
            uiDevice.pressBack() // Close notification shade
        }
    }
}
