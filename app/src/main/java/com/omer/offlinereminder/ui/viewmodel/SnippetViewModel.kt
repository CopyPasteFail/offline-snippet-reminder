package com.omer.offlinereminder.ui.viewmodel

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewModelScope
import com.omer.offlinereminder.OfflineSnippetReminderApplication
import com.omer.offlinereminder.R
import com.omer.offlinereminder.data.local.SnippetListWithSnippets
import com.omer.offlinereminder.data.repository.SnippetRepository
import com.omer.offlinereminder.util.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SnippetViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as OfflineSnippetReminderApplication
    private val repository: SnippetRepository = app.repository
    private val scheduler: ReminderScheduler = app.scheduler

    private val _pendingDetailListId = MutableStateFlow<Int?>(null)
    val pendingDetailListId: StateFlow<Int?> = _pendingDetailListId.asStateFlow()

    val snippetLists: StateFlow<List<SnippetListUiModel>> = repository.observeListsWithSnippets()
        .map { lists -> lists.map { it.toListUiModel(app) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun observeList(listId: Int): Flow<SnippetDetailUiModel?> =
        repository.observeListWithSnippets(listId)
            .map { entry -> entry?.toDetailUiModel(app) }

    fun updateReminderState(listId: Int, enabled: Boolean) {
        viewModelScope.launch {
            repository.setListActive(listId, enabled)
            repository.getListById(listId)?.let { list ->
                if (enabled) {
                    scheduler.scheduleList(list)
                } else {
                    scheduler.cancelList(list.id)
                }
            }
        }
    }

    fun onCreateListRequest() {
        viewModelScope.launch {
            val newListId = repository.insertSampleList()
            repository.getListById(newListId)?.let { list ->
                if (list.isActive) {
                    scheduler.scheduleList(list)
                }
            }
        }
    }

    fun requestOpenList(listId: Int) {
        _pendingDetailListId.value = listId
    }

    fun consumePendingDetailRequest() {
        _pendingDetailListId.value = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                @Suppress("UNCHECKED_CAST")
                return SnippetViewModel(application) as T
            }
        }
    }
}

data class SnippetListUiModel(
    val id: Int,
    val name: String,
    val frequencyLabel: String,
    val snippetSummary: String,
    val statusLabel: String,
    val isActive: Boolean
)

data class SnippetDetailUiModel(
    val id: Int,
    val name: String,
    val frequencyLabel: String,
    val nextReminderDescription: String,
    val snippets: List<String>,
    val isActive: Boolean
)

private fun SnippetListWithSnippets.toListUiModel(app: OfflineSnippetReminderApplication): SnippetListUiModel {
    val resources = app.resources
    val frequencyLabel = formatFrequency(resources, list.frequencyMinutes)
    val summary = if (snippets.isEmpty()) {
        resources.getString(R.string.no_snippets_placeholder)
    } else {
        snippets.sortedBy { it.orderIndex }
            .take(3)
            .joinToString(separator = " | ") { it.text }
    }
    val statusLabel = if (list.isActive) {
        resources.getString(R.string.list_status_active)
    } else {
        resources.getString(R.string.list_status_paused)
    }
    return SnippetListUiModel(
        id = list.id,
        name = list.name,
        frequencyLabel = frequencyLabel,
        snippetSummary = summary,
        statusLabel = statusLabel,
        isActive = list.isActive
    )
}

private fun SnippetListWithSnippets.toDetailUiModel(app: OfflineSnippetReminderApplication): SnippetDetailUiModel {
    val resources = app.resources
    val frequencyLabel = formatFrequency(resources, list.frequencyMinutes)
    val nextReminder = resources.getString(R.string.next_notification_due, frequencyLabel)
    return SnippetDetailUiModel(
        id = list.id,
        name = list.name,
        frequencyLabel = resources.getString(R.string.snippet_list_frequency_label, frequencyLabel),
        nextReminderDescription = nextReminder,
        snippets = snippets.sortedBy { it.orderIndex }.map { it.text },
        isActive = list.isActive
    )
}

private fun formatFrequency(resources: Resources, minutes: Long): String {
    val minute = 1L
    val hour = 60L * minute
    val day = 24L * hour
    val week = 7L * day
    return when {
        minutes % week == 0L -> {
            val days = (minutes / day).toInt()
            resources.getQuantityString(R.plurals.frequency_days_plurals, days, days)
        }
        minutes % day == 0L -> {
            val days = (minutes / day).toInt()
            resources.getQuantityString(R.plurals.frequency_days_plurals, days, days)
        }
        minutes % hour == 0L -> {
            val hours = (minutes / hour).toInt()
            resources.getQuantityString(R.plurals.frequency_hours_plurals, hours, hours)
        }
        else -> {
            val mins = minutes.toInt()
            resources.getQuantityString(R.plurals.frequency_minutes_plurals, mins, mins)
        }
    }
}
