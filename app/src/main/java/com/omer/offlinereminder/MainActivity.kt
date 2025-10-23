package com.omer.offlinereminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.omer.offlinereminder.ui.OfflineSnippetReminderApp
import com.omer.offlinereminder.ui.viewmodel.SnippetViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SnippetViewModel by viewModels { SnippetViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntentExtras()
        setContent {
            OfflineSnippetReminderApp(viewModel = viewModel)
        }
    }

    override fun onNewIntent(intent: android.content.Intent?) {
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

    companion object {
        const val EXTRA_LIST_ID = "extra_list_id"
    }
}
