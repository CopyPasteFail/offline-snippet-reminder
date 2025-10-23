package com.omer.offlinereminder.data.repository

import com.omer.offlinereminder.data.local.ReminderDao
import com.omer.offlinereminder.data.local.SnippetListEntity
import com.omer.offlinereminder.data.local.SnippetListWithSnippets
import com.omer.offlinereminder.data.seed.SeedData
import kotlinx.coroutines.flow.Flow

class SnippetRepository(private val dao: ReminderDao) {

    fun observeListsWithSnippets(): Flow<List<SnippetListWithSnippets>> = dao.observeListsWithSnippets()

    fun observeListWithSnippets(listId: Int): Flow<SnippetListWithSnippets?> = dao.observeListWithSnippets(listId)

    fun observeLists(): Flow<List<SnippetListEntity>> = dao.observeLists()

    suspend fun getListById(listId: Int): SnippetListEntity? = dao.getListById(listId)

    suspend fun getAllListsOnce(): List<SnippetListEntity> = dao.getAllLists()

    suspend fun setListActive(listId: Int, active: Boolean) {
        dao.updateActiveState(listId, active)
    }

    suspend fun insertSampleList(): Int = SeedData.insertSampleList(dao)
}
