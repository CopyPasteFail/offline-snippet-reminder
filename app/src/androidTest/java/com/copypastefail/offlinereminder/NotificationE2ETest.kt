package com.copypastefail.offlinereminder

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.copypastefail.offlinereminder.worker.ReminderWorker
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class NotificationE2ETest {

    private lateinit var context: Context
    private lateinit var workManager: WorkManager
    private lateinit var uiDevice: UiDevice
    private val notificationTimeout = 5000L

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        workManager = WorkManager.getInstance(context)
        uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.pressHome()
    }

    @Test
    fun test_reminderWorker_postsNotification() {
        // Arrange
        val inputData = workDataOf(ReminderWorker.KEY_LIST_ID to 999)
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(inputData)
            .build()
        workManager.enqueue(workRequest)

        // Act
        uiDevice.openNotification()

        // Assert
        // TODO: Replace this with the actual text of your notification
        val expectedNotificationText = "Your notification text here"
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
