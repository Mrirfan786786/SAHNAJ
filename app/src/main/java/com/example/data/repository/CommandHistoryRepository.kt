package com.example.data.repository

import com.example.data.local.CommandDao
import com.example.data.model.CommandEntity
import kotlinx.coroutines.flow.Flow

interface CommandHistoryRepository {
    fun getAllHistory(): Flow<List<CommandEntity>>
    fun getRecentHistory(limit: Int = 10): Flow<List<CommandEntity>>
    suspend fun addCommand(command: CommandEntity): Long
    suspend fun deleteCommand(id: Long)
    suspend fun clearHistory()
}

class RoomCommandHistoryRepository(
    private val commandDao: CommandDao
) : CommandHistoryRepository {
    override fun getAllHistory(): Flow<List<CommandEntity>> = commandDao.getAllCommands()

    override fun getRecentHistory(limit: Int): Flow<List<CommandEntity>> = commandDao.getRecentCommands(limit)

    override suspend fun addCommand(command: CommandEntity): Long = commandDao.insertCommand(command)

    override suspend fun deleteCommand(id: Long) = commandDao.deleteCommandById(id)

    override suspend fun clearHistory() = commandDao.clearAllHistory()
}
