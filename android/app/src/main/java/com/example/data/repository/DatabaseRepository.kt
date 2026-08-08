package com.example.data.repository

import com.example.data.dao.DatabaseDao
import com.example.data.model.DatabaseEntity
import kotlinx.coroutines.flow.Flow

class DatabaseRepository(private val databaseDao: DatabaseDao) {

    val allDatabases: Flow<List<DatabaseEntity>> = databaseDao.getAllDatabases()
    val databaseCount: Flow<Int> = databaseDao.getDatabaseCount()

    fun getDatabaseById(id: Long): Flow<DatabaseEntity?> {
        return databaseDao.getDatabaseById(id)
    }

    suspend fun createDatabase(
        name: String,
        engine: String,
        dbName: String,
        category: String = "Relational",
        host: String = "localhost",
        port: Int = 5432,
        username: String = "admin"
    ): Long {
        val newDb = DatabaseEntity(
            name = name,
            engine = engine,
            category = category,
            type = "CREATED",
            host = host,
            port = port,
            dbName = dbName.ifBlank { name.lowercase().replace(" ", "_") },
            username = username,
            status = "Active",
            sizeMb = 4.2,
            tableCount = 1,
            createdAt = System.currentTimeMillis(),
            lastAccessed = System.currentTimeMillis()
        )
        return databaseDao.insertDatabase(newDb)
    }

    suspend fun connectDatabase(
        name: String,
        engine: String,
        category: String = "Relational",
        host: String,
        port: Int,
        dbName: String,
        username: String
    ): Long {
        val connectedDb = DatabaseEntity(
            name = name,
            engine = engine,
            category = category,
            type = "CONNECTED",
            host = host,
            port = port,
            dbName = dbName,
            username = username,
            status = "Connected",
            sizeMb = 32.8,
            tableCount = 12,
            createdAt = System.currentTimeMillis(),
            lastAccessed = System.currentTimeMillis()
        )
        return databaseDao.insertDatabase(connectedDb)
    }

    suspend fun updateDatabase(database: DatabaseEntity) {
        databaseDao.updateDatabase(database)
    }

    suspend fun deleteDatabase(database: DatabaseEntity) {
        databaseDao.deleteDatabase(database)
    }

    suspend fun deleteById(id: Long) {
        databaseDao.deleteDatabaseById(id)
    }
}
