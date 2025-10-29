package com.copypastefail.offlinereminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.copypastefail.offlinereminder.ui.OfflineSnippetReminderApp
import com.copypastefail.offlinereminder.ui.viewmodel.SnippetViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SnippetViewModel by viewModels { SnippetViewModel.Factory }

    private var pendingReminderListId: Int? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            val pendingListId = pendingReminderListId
            if (isGranted && pendingListId != null) {
                scheduleReminderForList(pendingListId)
            } else if (!isGranted) {
                Toast.makeText(
                    this,
                    getString(R.string.notification_permission_denied),
                    Toast.LENGTH_LONG
                ).show()
            }
            pendingReminderListId = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentExtras()
        setContent {
            OfflineSnippetReminderApp(
                viewModel = viewModel,
                onToggleReminders = ::onToggleReminders
            )
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntentExtras()
    }

    private fun handleIntentExtras() {
        val listId = intent?.getIntExtra(EXTRA_LIST_ID, -1) ?: -1
        if (listId != -1) {
            viewModel.requestOpenList(listId)
        }
    }

    private fun onToggleReminders(listId: Int, enabled: Boolean) {
        if (enabled) {
            askForNotificationPermission(listId)
        } else {
            viewModel.updateReminderState(listId, false)
        }
    }

    private fun askForNotificationPermission(listId: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    scheduleReminderForList(listId)
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    pendingReminderListId = listId
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    pendingReminderListId = listId
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            scheduleReminderForList(listId)
        }
    }

    private fun scheduleReminderForList(listId: Int) {
        pendingReminderListId = null
        viewModel.updateReminderState(listId, true)
    }

    companion object {
        const val EXTRA_LIST_ID = "extra_list_id"
    }
}
