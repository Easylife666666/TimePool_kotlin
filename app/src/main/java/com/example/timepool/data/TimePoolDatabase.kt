package com.example.timepool.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(entities = [TimeBlock::class, Template::class, Category::class], version = 1, exportSchema = false)
abstract class TimePoolDatabase : RoomDatabase() {
    abstract fun timePoolDao(): TimePoolDao

    companion object {
        @Volatile
        private var INSTANCE: TimePoolDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): TimePoolDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TimePoolDatabase::class.java,
                    "time_pool_database"
                )
                .addCallback(TimePoolDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class TimePoolDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.timePoolDao())
                    }
                }
            }

            suspend fun populateDatabase(dao: TimePoolDao) {
                // Initialize Types
                val types = listOf(
                    Category("work", "工作", "#7e5bef"),
                    Category("sleep", "睡觉", "#00f2ff"),
                    Category("other", "其他", "#ffcc00")
                )
                types.forEach { dao.insertCategory(it) }

                // Initialize Templates
                val templates = listOf(
                    Template(name = "睡觉", duration = 8f, categoryId = "sleep", priority = 1, note = "每日保障"),
                    Template(name = "用餐", duration = 2f, categoryId = "other", priority = 2, note = "早午晚餐"),
                    Template(name = "专注工作", duration = 4f, categoryId = "work", priority = 1, note = "核心输出")
                )
                templates.forEach { dao.insertTemplate(it) }
            }
        }
    }
}
