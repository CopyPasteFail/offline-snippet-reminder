
package com.copypastefail.offlinereminder

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.copypastefail.offlinereminder.worker.ReminderWorker
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class ReminderWorkerTest {

    private lateinit var context: Context
    private lateinit var executor: Executor

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        executor = Executors.newSingleThreadExecutor()
    }

    @Test
    fun testReminderWorker() {
        val inputData = workDataOf(
            "id" to 1,
            "message" to "Test message"
        )

        val worker = TestListenableWorkerBuilder<ReminderWorker>(
            context = context,
            inputData = inputData
        ).build()

        val result = worker.startWork().get()

        assertEquals(ListenableWorker.Result.success(), result)
    }
}
