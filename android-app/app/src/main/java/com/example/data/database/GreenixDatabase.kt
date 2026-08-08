package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.DatabaseDao
import com.example.data.model.DatabaseEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DatabaseEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GreenixDatabase : RoomDatabase() {

    abstract fun databaseDao(): DatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: GreenixDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): GreenixDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GreenixDatabase::class.java,
                    "greenix_studio_db"
                )
                    .addCallback(GreenixDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class GreenixDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.databaseDao())
                    }
                }
            }

            suspend fun populateInitialData(dao: DatabaseDao) {
                val initialDbs = listOf(
                    DatabaseEntity(
                        name = "E-Commerce_Primary_DB",
                        engine = "PostgreSQL",
                        type = "CREATED",
                        host = "pg.greenix.cloud",
                        port = 5432,
                        dbName = "shop_prod",
                        username = "edgicode_admin",
                        status = "Active",
                        sizeMb = 148.2,
                        tableCount = 24,
                        createdAt = System.currentTimeMillis() - 86400000 * 5,
                        lastAccessed = System.currentTimeMillis() - 3600000
                    ),
                    DatabaseEntity(
                        name = "Analytics_Warehouse",
                        engine = "MySQL",
                        type = "CONNECTED",
                        host = "mysql-cluster.edgicode.io",
                        port = 3306,
                        dbName = "user_telemetry",
                        username = "data_analyst",
                        status = "Connected",
                        sizeMb = 512.6,
                        tableCount = 16,
                        createdAt = System.currentTimeMillis() - 86400000 * 12,
                        lastAccessed = System.currentTimeMillis() - 7200000
                    ),
                    DatabaseEntity(
                        name = "App_Local_Cache",
                        engine = "SQLite",
                        type = "CREATED",
                        host = "local_storage",
                        port = 0,
                        dbName = "cache_v1.db",
                        username = "root",
                        status = "Active",
                        sizeMb = 18.5,
                        tableCount = 6,
                        createdAt = System.currentTimeMillis() - 86400000 * 2,
                        lastAccessed = System.currentTimeMillis()
                    )
                )
                for (db in initialDbs) {
                    dao.insertDatabase(db)
                }
            }
        }
    }
}
