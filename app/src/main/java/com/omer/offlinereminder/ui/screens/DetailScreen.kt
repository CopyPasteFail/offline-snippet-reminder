package com.omer.offlinereminder.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.omer.offlinereminder.R
import com.omer.offlinereminder.ui.viewmodel.SnippetDetailUiModel

@Composable
fun DetailScreen(
    list: SnippetDetailUiModel?,
    onBack: () -> Unit,
    onToggleReminders: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(text = list?.name ?: stringResource(id = R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.return_to_lists))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Future add snippet action */ }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.add_new_snippet))
            }
        }
    ) { paddingValues ->
        if (list == null) {
            LoadingState(modifier = modifier.padding(paddingValues))
        } else {
            DetailContent(
                list = list,
                onToggleReminders = onToggleReminders,
                modifier = modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(id = R.string.no_snippets_placeholder), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun DetailContent(
    list: SnippetDetailUiModel,
    onToggleReminders: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = list.frequencyLabel, style = MaterialTheme.typography.bodyLarge)
        FilledTonalButton(
            modifier = Modifier.padding(top = 12.dp),
            onClick = { onToggleReminders(!list.isActive) }
        ) {
            Text(text = if (list.isActive) stringResource(id = R.string.disable_reminders) else stringResource(id = R.string.enable_reminders))
        }
        Text(
            text = list.nextReminderDescription,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(list.snippets) { snippet ->
                Text(
                    text = "- $snippet",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
