package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "databases")
data class DatabaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val engine: String, // "PostgreSQL", "MySQL", "SQLite", "MongoDB", "Redis"
    val type: String,   // "CREATED", "CONNECTED"
    val host: String = "localhost",
    val port: Int = 5432,
    val dbName: String = "",
    val username: String = "admin",
    val status: String = "Active", // "Active", "Connected", "Syncing"
    val sizeMb: Double = 12.4,
    val tableCount: Int = 8,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAccessed: Long = System.currentTimeMillis()
)
