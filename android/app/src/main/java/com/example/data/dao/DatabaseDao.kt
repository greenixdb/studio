package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DatabaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DatabaseDao {

    @Query("SELECT * FROM databases ORDER BY lastAccessed DESC")
    fun getAllDatabases(): Flow<List<DatabaseEntity>>

    @Query("SELECT * FROM databases WHERE id = :id")
    fun getDatabaseById(id: Long): Flow<DatabaseEntity?>

    @Query("SELECT COUNT(*) FROM databases")
    fun getDatabaseCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDatabase(database: DatabaseEntity): Long

    @Update
    suspend fun updateDatabase(database: DatabaseEntity)

    @Delete
    suspend fun deleteDatabase(database: DatabaseEntity)

    @Query("DELETE FROM databases WHERE id = :id")
    suspend fun deleteDatabaseById(id: Long)
}
