package com.copypastefail.offlinereminder.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.copypastefail.offlinereminder.data.seed.SeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [SnippetListEntity::class, SnippetEntity::class],
    version = 4,
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
            val passphrase = KeyStoreManager.getOrCreateKey().encoded
            val factory = SupportFactory(passphrase)

            return INSTANCE ?: synchronized(this) {
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    ReminderDatabase::class.java,
                    "offline_snippet_reminder.db"
                )
                .openHelperFactory(factory)
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4)

                scope?.let { builder.addCallback(ReminderDatabaseCallback(it)) }

                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE snippet_lists RENAME COLUMN frequency_minutes TO frequency_seconds")
                db.execSQL("UPDATE snippet_lists SET frequency_seconds = frequency_seconds * 60")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX `index_snippets_list_id` ON `snippets` (`list_id`)")
            }
        }
    }
}
