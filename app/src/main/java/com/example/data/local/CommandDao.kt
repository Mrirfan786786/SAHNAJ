package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CommandEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommandDao {
    @Query("SELECT * FROM command_history ORDER BY timestamp DESC")
    fun getAllCommands(): Flow<List<CommandEntity>>

    @Query("SELECT * FROM command_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentCommands(limit: Int): Flow<List<CommandEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommand(command: CommandEntity): Long

    @Query("DELETE FROM command_history WHERE id = :id")
    suspend fun deleteCommandById(id: Long)

    @Query("DELETE FROM command_history")
    suspend fun clearAllHistory()
}
