package com.copypastefail.offlinereminder.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Transaction
    @Query("SELECT * FROM snippet_lists ORDER BY name ASC")
    fun observeListsWithSnippets(): Flow<List<SnippetListWithSnippets>>

    @Transaction
    @Query("SELECT * FROM snippet_lists WHERE id = :listId")
    fun observeListWithSnippets(listId: Int): Flow<SnippetListWithSnippets?>

    @Query("SELECT * FROM snippet_lists ORDER BY name ASC")
    suspend fun getAllLists(): List<SnippetListEntity>

    @Query("SELECT * FROM snippet_lists WHERE id = :listId")
    suspend fun getListById(listId: Int): SnippetListEntity?

    @Query("SELECT * FROM snippet_lists ORDER BY id ASC")
    fun observeLists(): Flow<List<SnippetListEntity>>

    @Query("SELECT * FROM snippets WHERE list_id = :listId ORDER BY order_index ASC")
    suspend fun getSnippetsForList(listId: Int): List<SnippetEntity>

    @Query("SELECT * FROM snippets WHERE list_id = :listId AND text = :text")
    suspend fun getSnippetByText(listId: Int, text: String): SnippetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: SnippetListEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnippets(snippets: List<SnippetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnippet(snippet: SnippetEntity)

    @Update
    suspend fun updateList(list: SnippetListEntity)

    @Update
    suspend fun updateSnippet(snippet: SnippetEntity)

    @Delete
    suspend fun deleteList(list: SnippetListEntity)

    @Delete
    suspend fun deleteSnippet(snippet: SnippetEntity)

    @Query("UPDATE snippet_lists SET current_index = :index WHERE id = :listId")
    suspend fun updateCurrentIndex(listId: Int, index: Int)

    @Query("UPDATE snippet_lists SET is_active = :active WHERE id = :listId")
    suspend fun updateActiveState(listId: Int, active: Boolean)

    @Query("UPDATE snippet_lists SET frequency_seconds = :frequencySeconds WHERE id = :listId")
    suspend fun updateFrequency(listId: Int, frequencySeconds: Long)

    @Query("UPDATE snippet_lists SET name = :newName WHERE id = :listId")
    suspend fun updateListName(listId: Int, newName: String)
}
