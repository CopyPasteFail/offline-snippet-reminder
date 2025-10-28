package com.copypastefail.offlinereminder.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.concurrent.TimeUnit

@Composable
fun AddSnippetDialog(
    onAddSnippet: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    val remainingCharacters = (SNIPPET_MAX_LENGTH - text.length).coerceAtLeast(0)

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Add Snippet", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
                TextField(
                    value = text,
                    onValueChange = {
                        text = it.take(SNIPPET_MAX_LENGTH)
                    },
                    label = { Text("Snippet Text") },
                    supportingText = { Text("$remainingCharacters characters remaining") },
                    maxLines = SNIPPET_TEXTFIELD_MAX_LINES
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onAddSnippet(text)
                            onDismiss()
                        }
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun AddMultipleSnippetsDialog(
    onAddMultipleSnippets: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    val remainingCharacters = (MULTI_SNIPPET_MAX_LENGTH - text.length).coerceAtLeast(0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Multiple Snippets") },
        text = {
            TextField(
                value = text,
                onValueChange = {
                    text = it.take(MULTI_SNIPPET_MAX_LENGTH)
                },
                placeholder = { Text("Enter snippets, separated by an empty line.") },
                supportingText = {
                    Column {
                        Text("Snippets are separated by at least one empty line.")
                        Text("$remainingCharacters characters remaining")
                    }
                },
                maxLines = MULTI_SNIPPET_TEXTFIELD_MAX_LINES
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val snippets = text
                        .split(Regex("\n\n+"))
                        .map { it.trim().take(SNIPPET_MAX_LENGTH) }
                        .filter { it.isNotBlank() }
                    onAddMultipleSnippets(snippets)
                    onDismiss()
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeFrequencyDialog(
    onFrequencyChange: (Long, TimeUnit) -> Unit,
    onDismiss: () -> Unit,
    currentFrequency: Long,
    currentTimeUnit: TimeUnit
) {
    var frequency by remember { mutableStateOf(currentFrequency.toString()) }
    var expanded by remember { mutableStateOf(false) }
    var selectedTimeUnit by remember { mutableStateOf(currentTimeUnit) }
    val timeUnitOptions = TimeUnit.values().filter { it >= TimeUnit.MINUTES }
    var isError by remember { mutableStateOf(false) }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Frequency") },
        text = {
            Column {
                TextField(
                    value = frequency,
                    onValueChange = {
                        val digitsOnly = it.filter(Char::isDigit)
                        if (digitsOnly.length <= FREQUENCY_MAX_LENGTH) {
                            frequency = digitsOnly
                        }
                        isError = false
                    },
                    label = { Text("Frequency") },
                    isError = isError,
                    singleLine = true
                )
                if (isError) {
                    Text(
                        text = "Frequency must be at least 15 minutes.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        TextField(
                            value = selectedTimeUnit.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Time Unit") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            timeUnitOptions.forEach { timeUnit ->
                                DropdownMenuItem(
                                    text = { Text(timeUnit.name) },
                                    onClick = {
                                        selectedTimeUnit = timeUnit
                                        isError = false
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newFrequency = frequency.toLongOrNull()
                    if (newFrequency != null) {
                        val frequencyMillis = selectedTimeUnit.toMillis(newFrequency)
                        if (frequencyMillis >= TimeUnit.MINUTES.toMillis(15)) {
                            onFrequencyChange(newFrequency, selectedTimeUnit)
                            onDismiss()
                        } else {
                            isError = true
                        }
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditSnippetDialog(
    onEditSnippet: (String, String) -> Unit,
    onDismiss: () -> Unit,
    initialText: String
) {
    var text by remember { mutableStateOf(initialText.take(SNIPPET_MAX_LENGTH)) }
    val remainingCharacters = (SNIPPET_MAX_LENGTH - text.length).coerceAtLeast(0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Snippet") },
        text = {
            TextField(
                value = text,
                onValueChange = {
                    text = it.take(SNIPPET_MAX_LENGTH)
                },
                label = { Text("Snippet Text") },
                supportingText = { Text("$remainingCharacters characters remaining") },
                maxLines = SNIPPET_TEXTFIELD_MAX_LINES
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onEditSnippet(initialText, text)
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete List") },
        text = { Text("Are you sure you want to delete this list and all its snippets?") },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private const val SNIPPET_MAX_LENGTH = 500
private const val MULTI_SNIPPET_MAX_LENGTH = 30000
private const val FREQUENCY_MAX_LENGTH = 4
private const val SNIPPET_TEXTFIELD_MAX_LINES = 6
private const val MULTI_SNIPPET_TEXTFIELD_MAX_LINES = 10
