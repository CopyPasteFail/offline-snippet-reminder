package com.copypastefail.offlinereminder.ui

object NavRoutes {
    const val Lists = "lists"
    private const val DetailBase = "detail"
    object DetailArgs {
        const val listId = "listId"
    }
    const val Detail = "$DetailBase/{${DetailArgs.listId}}"

    fun detailRoute(listId: Int): String = "$DetailBase/$listId"
}
