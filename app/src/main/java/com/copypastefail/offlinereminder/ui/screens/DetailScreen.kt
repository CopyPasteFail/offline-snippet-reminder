package com.copypastefail.offlinereminder.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.copypastefail.offlinereminder.R
import com.copypastefail.offlinereminder.ui.viewmodel.SnippetDetailUiModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    list: SnippetDetailUiModel?,
    onBack: () -> Unit,
    onToggleReminders: (Boolean) -> Unit,
    onDeleteList: () -> Unit,
    onFrequencyChange: (Long, TimeUnit) -> Unit,
    onAddSnippet: (String) -> Unit,
    onAddMultipleSnippets: (List<String>) -> Unit,
    onDeleteSnippet: (String) -> Unit,
    onEditSnippet: (String, String) -> Unit,
    onListNameChange: (String) -> Unit,
    pendingSnippetId: Int?,
    onConsumePendingSnippet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isShowingAddSnippetDialog by remember { mutableStateOf(false) }
    var isShowingAddMultipleSnippetsDialog by remember { mutableStateOf(false) }
    var isShowingFrequencyDialog by remember { mutableStateOf(false) }
    var isShowingEditSnippetDialog by remember { mutableStateOf(false) }
    var isShowingRenameDialog by remember { mutableStateOf(false) }
    var snippetToEdit by remember { mutableStateOf("") }

    var isFabMenuExpanded by remember { mutableStateOf(false) }
    var isSnippetSheetVisible by remember { mutableStateOf(false) }
    var snippetIdForSheet by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(pendingSnippetId, list?.snippets) {
        val pendingId = pendingSnippetId
        val hasSnippet = pendingId != null && list?.snippets?.any { it.id == pendingId } == true
        if (hasSnippet) {
            snippetIdForSheet = pendingId
            isSnippetSheetVisible = true
            onConsumePendingSnippet()
        } else if (pendingId != null && list != null) {
            onConsumePendingSnippet()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.screen_title_details)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isShowingRenameDialog = true }) {
                        Icon(Icons.Default.Edit, "Rename List")
                    }
                    IconButton(onClick = { isShowingFrequencyDialog = true }) {
                        Icon(Icons.Default.Schedule, "Change Frequency")
                    }
                    IconButton(onClick = onDeleteList) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (isFabMenuExpanded) {
                    ExtendedFloatingActionButton(
                        text = { Text(text = stringResource(R.string.add_multiple_snippets)) },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        onClick = {
                            isFabMenuExpanded = false
                            isShowingAddMultipleSnippetsDialog = true
                        },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    ExtendedFloatingActionButton(
                        text = { Text(text = stringResource(R.string.add_new_snippet)) },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        onClick = {
                            isFabMenuExpanded = false
                            isShowingAddSnippetDialog = true
                        },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                FloatingActionButton(
                    onClick = { isFabMenuExpanded = !isFabMenuExpanded }
                ) {
                    Icon(
                        imageVector = if (isFabMenuExpanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = stringResource(
                            id = if (isFabMenuExpanded) {
                                R.string.hide_add_snippet_options
                            } else {
                                R.string.show_add_snippet_options
                            }
                        )
                    )
                }
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(it)
        ) {
            Text(
                text = list?.name ?: "",
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(text = stringResource(R.string.reminders_enabled_label))
                Spacer(modifier = Modifier.weight(1f))
                Switch(checked = list?.isActive ?: false, onCheckedChange = onToggleReminders)
            }

            list?.let { it ->
                Text(
                    text = stringResource(
                        R.string.notification_frequency_label,
                        it.frequency,
                        it.timeUnit.name.lowercase()
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (list == null) {
                Text("List not found.")
            } else if (list.snippets.isEmpty()) {
                Text("No snippets in this list.")
            } else {
                LazyColumn {
                    items(list.snippets, key = { it.id }) { snippet ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    snippet.text,
                                    modifier = Modifier.weight(1f),
                                    maxLines = SNIPPET_DISPLAY_MAX_LINES,
                                    overflow = TextOverflow.Ellipsis
                                )
                                IconButton(onClick = {
                                    snippetToEdit = snippet.text
                                    isShowingEditSnippetDialog = true
                                }) {
                                    Icon(Icons.Default.Edit, "Edit")
                                }
                                IconButton(onClick = { onDeleteSnippet(snippet.text) }) {
                                    Icon(Icons.Default.Delete, "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isShowingAddSnippetDialog) {
        AddSnippetDialog(
            onAddSnippet = onAddSnippet,
            onDismiss = { isShowingAddSnippetDialog = false })
    }

    if (isShowingAddMultipleSnippetsDialog) {
        AddMultipleSnippetsDialog(
            onAddMultipleSnippets = onAddMultipleSnippets,
            onDismiss = { isShowingAddMultipleSnippetsDialog = false })
    }

    if (isShowingFrequencyDialog) {
        ChangeFrequencyDialog(
            onFrequencyChange = onFrequencyChange,
            onDismiss = { isShowingFrequencyDialog = false },
            currentFrequency = list?.frequency ?: 0,
            currentTimeUnit = list?.timeUnit ?: TimeUnit.SECONDS
        )
    }

    if (isShowingEditSnippetDialog) {
        EditSnippetDialog(
            onEditSnippet = onEditSnippet,
            onDismiss = { isShowingEditSnippetDialog = false },
            initialText = snippetToEdit
        )
    }

    if (isShowingRenameDialog) {
        list?.let {
            RenameListDialog(
                onDismiss = { isShowingRenameDialog = false },
                onRename = { it ->
                    onListNameChange(it)
                    isShowingRenameDialog = false
                },
                initialName = it.name
            )
        }
    }

    val snippetForSheet = list?.snippets?.firstOrNull { it.id == snippetIdForSheet }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (isSnippetSheetVisible && snippetForSheet != null) {
        ModalBottomSheet(
            onDismissRequest = { isSnippetSheetVisible = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = list?.name.orEmpty(),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = snippetForSheet.text,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.tap_outside_to_close),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

private const val SNIPPET_DISPLAY_MAX_LINES = 6
