package com.copypastefail.offlinereminder.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.copypastefail.offlinereminder.ui.screens.DeleteConfirmationDialog
import com.copypastefail.offlinereminder.ui.screens.DetailScreen
import com.copypastefail.offlinereminder.ui.screens.SnippetListsScreen
import com.copypastefail.offlinereminder.ui.theme.OfflineSnippetReminderTheme
import com.copypastefail.offlinereminder.ui.viewmodel.SnippetViewModel

@Composable
fun OfflineSnippetReminderApp(viewModel: SnippetViewModel) {
    val navController = rememberNavController()
    val snippetLists by viewModel.snippetLists.collectAsState()
    var listIdToDelete by remember { mutableStateOf<Int?>(null) }


    OfflineSnippetReminderTheme {
        NavHost(navController = navController, startDestination = NavRoutes.Lists) {
            composable(NavRoutes.Lists) {
                SnippetListsScreen(
                    lists = snippetLists,
                    onListSelected = { listId -> navController.navigate(NavRoutes.detailRoute(listId)) },
                    onCreateNewList = { viewModel.onCreateListRequest() })
            }
            composable(NavRoutes.Detail) { backStackEntry ->
                val listId = backStackEntry.arguments?.getString(NavRoutes.DetailArgs.listId)?.toInt()
                if (listId != null) {
                    val list by viewModel.observeList(listId).collectAsState(initial = null)
                    DetailScreen(
                        list = list,
                        onBack = { navController.popBackStack() },
                        onToggleReminders = { enabled -> viewModel.updateReminderState(listId, enabled) },
                        onDeleteList = { listIdToDelete = listId },
                        onFrequencyChange = { frequency, timeUnit ->
                            viewModel.updateFrequency(listId, frequency, timeUnit)
                        },
                        onAddSnippet = { text -> viewModel.addSnippet(listId, text) },
                        onAddMultipleSnippets = { snippets -> viewModel.addMultipleSnippets(listId, snippets) },
                        onDeleteSnippet = { text -> viewModel.deleteSnippet(listId, text) },
                        onEditSnippet = { oldText, newText ->
                            viewModel.editSnippet(listId, oldText, newText)
                        },
                        onListNameChange = { newName -> viewModel.updateListName(listId, newName) })
                }
            }
        }

        if (listIdToDelete != null) {
            DeleteConfirmationDialog(onConfirm = {
                listIdToDelete?.let {
                    viewModel.deleteList(it)
                    navController.popBackStack()
                }
                listIdToDelete = null
            }, onDismiss = { listIdToDelete = null })
        }
    }
}