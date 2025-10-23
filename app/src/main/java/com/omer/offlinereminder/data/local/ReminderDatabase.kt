package com.omer.offlinereminder.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.omer.offlinereminder.data.seed.SeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [SnippetListEntity::class, SnippetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ReminderDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao

    private class ReminderDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    SeedData.populate(database.reminderDao())
                }
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ReminderDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope? = null): ReminderDatabase {
            return INSTANCE ?: synchronized(this) {
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    ReminderDatabase::class.java,
                    "offline_snippet_reminder.db"
                ).fallbackToDestructiveMigration()

                scope?.let { builder.addCallback(ReminderDatabaseCallback(it)) }

                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }
    }
}
