package com.copypastefail.offlinereminder.ui

object NavRoutes {
    const val LISTS = "lists"
    private const val DETAILBASE = "detail"
    object DetailArgs {
        const val LISTID = "listId"
    }
    const val DETAIL = "$DETAILBASE/{${DetailArgs.LISTID}}"

    fun detailRoute(listId: Int): String = "$DETAILBASE/$listId"
}
