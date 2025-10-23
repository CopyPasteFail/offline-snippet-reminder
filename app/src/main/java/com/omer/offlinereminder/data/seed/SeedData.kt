package com.omer.offlinereminder.data.seed

import com.omer.offlinereminder.data.local.ReminderDao
import com.omer.offlinereminder.data.local.SnippetEntity
import com.omer.offlinereminder.data.local.SnippetListEntity
import kotlin.jvm.Volatile

object SeedData {

    private val predefinedLists = listOf(
        SeedList(
            name = "Mindfulness",
            frequencyMinutes = 60,
            snippets = listOf(
                "Pause and take three deep breaths.",
                "Notice one thing you can see, hear, and feel.",
                "Relax your shoulders and unclench your jaw."
            )
        ),
        SeedList(
            name = "Motivation",
            frequencyMinutes = 180,
            snippets = listOf(
                "Focus on the next small win.",
                "Progress beats perfection.",
                "Remember why you started."
            )
        ),
        SeedList(
            name = "Learning",
            frequencyMinutes = 720,
            snippets = listOf(
                "Review one key note from today.",
                "Teach the concept aloud in your own words.",
                "List one question you still have."
            )
        )
    )

    @Volatile
    private var nextSampleIndex = 0

    suspend fun populate(dao: ReminderDao) {
        if (dao.getAllLists().isNotEmpty()) return
        predefinedLists.forEach { definition ->
            insertDefinition(dao, definition)
        }
    }

    suspend fun insertSampleList(dao: ReminderDao): Int {
        val definition = nextSampleDefinition()
        return insertDefinition(dao, definition)
    }

    private fun nextSampleDefinition(): SeedList {
        val index = synchronized(this) {
            val current = nextSampleIndex
            nextSampleIndex = (nextSampleIndex + 1) % predefinedLists.size
            current
        }
        return predefinedLists[index]
    }

    private suspend fun insertDefinition(dao: ReminderDao, definition: SeedList): Int {
        val listId = dao.insertList(
            SnippetListEntity(
                name = definition.name,
                frequencyMinutes = definition.frequencyMinutes,
                currentIndex = 0,
                isActive = true
            )
        ).toInt()
        val snippets = definition.snippets.mapIndexed { index, text ->
            SnippetEntity(
                listId = listId,
                text = text,
                orderIndex = index
            )
        }
        dao.insertSnippets(snippets)
        return listId
    }

    private data class SeedList(
        val name: String,
        val frequencyMinutes: Long,
        val snippets: List<String>
    )
}
