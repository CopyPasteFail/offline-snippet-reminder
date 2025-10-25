package com.copypastefail.offlinereminder.data.seed

import com.copypastefail.offlinereminder.data.local.ReminderDao
import com.copypastefail.offlinereminder.data.local.SnippetEntity
import com.copypastefail.offlinereminder.data.local.SnippetListEntity
import java.util.concurrent.TimeUnit

object SeedData {

    private val predefinedLists = listOf(
        SeedList(
            name = "Mindfulness",
            frequencySeconds = TimeUnit.MINUTES.toSeconds(60),
            snippets = listOf(
                "Pause and take three deep breaths.",
                "Notice one thing you can see, hear, and feel.",
                "Relax your shoulders and unclench your jaw."
            )
        ),
        SeedList(
            name = "Motivation",
            frequencySeconds = TimeUnit.MINUTES.toSeconds(180),
            snippets = listOf(
                "Focus on the next small win.",
                "Progress beats perfection.",
                "Remember why you started."
            )
        ),
        SeedList(
            name = "Learning",
            frequencySeconds = TimeUnit.MINUTES.toSeconds(720),
            snippets = listOf(
                "Review one key note from today.",
                "Teach the concept aloud in your own words.",
                "List one question you still have."
            )
        )
    )

    suspend fun populate(dao: ReminderDao) {
        if (dao.getAllLists().isNotEmpty()) return
        predefinedLists.forEach { definition ->
            insertDefinition(dao, definition)
        }
    }

    private suspend fun insertDefinition(dao: ReminderDao, definition: SeedList): Int {
        val listId = dao.insertList(
            SnippetListEntity(
                name = definition.name,
                frequencySeconds = definition.frequencySeconds,
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
        val frequencySeconds: Long,
        val snippets: List<String>
    )
}