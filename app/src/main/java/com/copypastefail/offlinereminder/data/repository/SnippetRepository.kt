package com.copypastefail.offlinereminder.data.repository

import com.copypastefail.offlinereminder.data.local.ReminderDao
import com.copypastefail.offlinereminder.data.local.SnippetEntity
import com.copypastefail.offlinereminder.data.local.SnippetListEntity
import com.copypastefail.offlinereminder.data.local.SnippetListWithSnippets
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class SnippetRepository(private val dao: ReminderDao) {

    fun observeListsWithSnippets(): Flow<List<SnippetListWithSnippets>> = dao.observeListsWithSnippets()

    fun observeListWithSnippets(listId: Int): Flow<SnippetListWithSnippets?> = dao.observeListWithSnippets(listId)

    fun observeLists(): Flow<List<SnippetListEntity>> = dao.observeLists()

    suspend fun getListById(listId: Int): SnippetListEntity? = dao.getListById(listId)

    suspend fun getSnippetsForList(listId: Int): List<SnippetEntity> = dao.getSnippetsForList(listId)

    suspend fun getAllListsOnce(): List<SnippetListEntity> = dao.getAllLists()

    suspend fun setListActive(listId: Int, active: Boolean) {
        dao.updateActiveState(listId, active)
    }

    suspend fun updateCurrentIndex(listId: Int, index: Int) {
        dao.updateCurrentIndex(listId, index)
    }

    suspend fun createNewList(): Int {
        val listId = dao.insertList(
            SnippetListEntity(
                name = "Unnamed List",
                frequencySeconds = TimeUnit.HOURS.toSeconds(1),
                currentIndex = 0,
                isActive = true
            )
        ).toInt()
        addSnippet(listId, "BlaBla")
        return listId
    }

    suspend fun deleteList(list: SnippetListEntity) = dao.deleteList(list)

    suspend fun updateFrequency(listId: Int, frequency: Long) = dao.updateFrequency(listId, frequency)

    suspend fun updateListName(listId: Int, newName: String) = dao.updateListName(listId, newName)

    suspend fun addSnippet(listId: Int, text: String) {
        val newIndex = dao.getSnippetsForList(listId).size
        dao.insertSnippet(SnippetEntity(0, listId, text, newIndex))
    }

    suspend fun addSnippets(listId: Int, snippets: List<String>) {
        val currentSize = dao.getSnippetsForList(listId).size
        val newSnippets = snippets.mapIndexed { index, text ->
            SnippetEntity(0, listId, text, currentSize + index)
        }
        dao.insertSnippets(newSnippets)
    }

    suspend fun deleteSnippet(listId: Int, text: String) {
        dao.getSnippetByText(listId, text)?.let { dao.deleteSnippet(it) }
    }

    suspend fun editSnippet(listId: Int, oldText: String, newText: String) {
        dao.getSnippetByText(listId, oldText)?.let {
            dao.updateSnippet(it.copy(text = newText))
        }
    }
}