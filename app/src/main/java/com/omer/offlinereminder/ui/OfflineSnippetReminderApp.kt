package com.omer.offlinereminder.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.omer.offlinereminder.ui.screens.DetailScreen
import com.omer.offlinereminder.ui.screens.SnippetListsScreen
import com.omer.offlinereminder.ui.theme.OfflineSnippetReminderTheme
import com.omer.offlinereminder.ui.viewmodel.SnippetViewModel

@Composable
fun OfflineSnippetReminderApp(
    viewModel: SnippetViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val pendingDetailListId by viewModel.pendingDetailListId.collectAsState()

    OfflineSnippetReminderTheme {
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Lists,
            modifier = modifier
        ) {
            composable(route = NavRoutes.Lists) {
                val listState by viewModel.snippetLists.collectAsState()
                SnippetListsScreen(
                    lists = listState,
                    onListSelected = { listId ->
                        navController.navigate(NavRoutes.detailRoute(listId))
                    },
                    onCreateNewList = viewModel::onCreateListRequest
                )
            }
            composable(
                route = NavRoutes.Detail,
                arguments = listOf(navArgument(NavRoutes.DetailArgs.listId) {
                    type = NavType.IntType
                })
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getInt(NavRoutes.DetailArgs.listId) ?: return@composable
                val listState by viewModel.observeList(listId).collectAsState(initial = null)
                DetailScreen(
                    list = listState,
                    onBack = { navController.popBackStack() },
                    onToggleReminders = { enabled -> viewModel.updateReminderState(listId, enabled) }
                )
            }
        }
    }

    LaunchedEffect(pendingDetailListId) {
        val id = pendingDetailListId ?: return@LaunchedEffect
        navController.navigate(NavRoutes.detailRoute(id)) {
            launchSingleTop = true
            restoreState = true
        }
        viewModel.consumePendingDetailRequest()
    }
}
