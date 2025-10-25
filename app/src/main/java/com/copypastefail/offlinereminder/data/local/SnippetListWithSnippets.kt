package com.copypastefail.offlinereminder.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class SnippetListWithSnippets(
    @Embedded val list: SnippetListEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "list_id"
    )
    val snippets: List<SnippetEntity>
)
