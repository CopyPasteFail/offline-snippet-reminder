package com.copypastefail.offlinereminder.ui.viewmodel

import android.app.Application
import android.content.res.Resources
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.copypastefail.offlinereminder.OfflineSnippetReminderApplication
import com.copypastefail.offlinereminder.R
import com.copypastefail.offlinereminder.data.local.SnippetListWithSnippets
import com.copypastefail.offlinereminder.data.repository.SnippetRepository
import com.copypastefail.offlinereminder.util.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SnippetViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as OfflineSnippetReminderApplication
    private val repository: SnippetRepository = app.repository
    private val scheduler: ReminderScheduler = app.scheduler

    private val _pendingDetailListId = MutableStateFlow<Int?>(null)
    val pendingDetailListId: StateFlow<Int?> = _pendingDetailListId.asStateFlow()

    private val _toastMessages = MutableSharedFlow<String>()
    val toastMessages = _toastMessages.asSharedFlow()

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
            val newListId = repository.createNewList()
            repository.getListById(newListId)?.let { list ->
                if (list.isActive) {
                    scheduler.scheduleList(list)
                }
            }
        }
    }

    fun deleteList(listId: Int) {
        viewModelScope.launch {
            repository.getListById(listId)?.let { list ->
                repository.deleteList(list)
                scheduler.cancelList(listId)
            }
        }
    }

    fun addSnippet(listId: Int, text: String) {
        viewModelScope.launch {
            repository.addSnippet(listId, text)
        }
    }

    fun addMultipleSnippets(listId: Int, snippets: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addSnippets(listId, snippets)
        }
    }

    fun deleteSnippet(listId: Int, text: String) {
        viewModelScope.launch {
            repository.deleteSnippet(listId, text)
        }
    }

    fun editSnippet(listId: Int, oldText: String, newText: String) {
        viewModelScope.launch {
            repository.editSnippet(listId, oldText, newText)
        }
    }

    fun renameList(listId: Int, newName: String) {
        viewModelScope.launch {
            repository.updateListName(listId, newName)
            _toastMessages.emit("List renamed to '$newName'")
        }
    }

    fun requestOpenList(listId: Int) {
        _pendingDetailListId.value = listId
    }

    fun consumePendingDetailRequest() {
        _pendingDetailListId.value = null
    }

    fun updateFrequency(listId: Int, frequency: Long, timeUnit: TimeUnit) {
        require(frequency > 0) { "Frequency must be positive" }
        viewModelScope.launch {
            val frequencySeconds = timeUnit.toSeconds(frequency)
            repository.updateFrequency(listId, frequencySeconds)
            repository.getListById(listId)?.let { list ->
                if (list.isActive) {
                    scheduler.scheduleList(list)
                }
            }
        }
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
    val statusLabel: String,
    val isActive: Boolean
)

data class SnippetDetailUiModel(
    val id: Int,
    val name: String,
    val frequencyLabel: String,
    val frequency: Long,
    val frequencySeconds: Long,
    val timeUnit: TimeUnit,
    val nextReminderDescription: String,
    val snippets: List<String>,
    val isActive: Boolean
)

private fun SnippetListWithSnippets.toListUiModel(app: OfflineSnippetReminderApplication): SnippetListUiModel {
    val resources = app.resources
    val frequencyLabel = formatFrequency(resources, list.frequencySeconds, TimeUnit.SECONDS)
    val statusLabel = if (list.isActive) {
        resources.getString(R.string.list_status_active)
    } else {
        resources.getString(R.string.list_status_paused)
    }
    return SnippetListUiModel(
        id = list.id,
        name = list.name,
        frequencyLabel = frequencyLabel,
        statusLabel = statusLabel,
        isActive = list.isActive
    )
}

private fun SnippetListWithSnippets.toDetailUiModel(app: OfflineSnippetReminderApplication): SnippetDetailUiModel {
    val resources = app.resources
    val (frequency, timeUnit) = list.frequencySeconds.toBestUnit()
    val frequencyLabel = formatFrequency(resources, frequency, timeUnit)
    val nextReminder = resources.getString(R.string.next_notification_due, frequencyLabel)
    return SnippetDetailUiModel(
        id = list.id,
        name = list.name,
        frequencyLabel = resources.getString(R.string.snippet_list_frequency_label, frequencyLabel),
        frequency = frequency,
        frequencySeconds = list.frequencySeconds,
        timeUnit = timeUnit,
        nextReminderDescription = nextReminder,
        snippets = snippets.sortedBy { it.orderIndex }.map { it.text },
        isActive = list.isActive
    )
}

private fun Long.toBestUnit(): Pair<Long, TimeUnit> {
    val minutes = TimeUnit.SECONDS.toMinutes(this)
    if (minutes > 0) {
        return minutes to TimeUnit.MINUTES
    }
    return this to TimeUnit.SECONDS
}

private fun formatFrequency(resources: Resources, frequency: Long, timeUnit: TimeUnit): String {
    val minutes = timeUnit.toMinutes(frequency)
    val minute = 1L
    val hour = 60L * minute
    val day = 24L * hour
    val week = 7L * day
    return when {
        minutes == 0L && timeUnit == TimeUnit.SECONDS -> {
            val seconds = frequency.toInt()
            resources.getQuantityString(R.plurals.frequency_seconds_plurals, seconds, seconds)
        }
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
