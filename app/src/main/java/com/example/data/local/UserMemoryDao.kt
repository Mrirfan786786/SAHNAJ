package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserMemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserMemoryDao {
    @Query("SELECT * FROM user_memory ORDER BY lastAccessed DESC")
    fun getAllMemoriesFlow(): Flow<List<UserMemoryEntity>>

    @Query("SELECT * FROM user_memory ORDER BY lastAccessed DESC")
    suspend fun getAllMemories(): List<UserMemoryEntity>

    @Query("SELECT * FROM user_memory WHERE category = :category ORDER BY lastAccessed DESC")
    suspend fun getMemoriesByCategory(category: String): List<UserMemoryEntity>

    @Query("SELECT * FROM user_memory WHERE memoryKey = :key LIMIT 1")
    suspend fun getMemoryByKey(key: String): UserMemoryEntity?

    @Query("SELECT * FROM user_memory WHERE category = 'USER_NAME' LIMIT 1")
    suspend fun getUserNameMemory(): UserMemoryEntity?

    @Query("SELECT * FROM user_memory WHERE category != 'CONVERSATION_SUMMARY' ORDER BY lastAccessed DESC LIMIT :limit")
    suspend fun getImportantFactsAndPreferences(limit: Int = 30): List<UserMemoryEntity>

    @Query("SELECT * FROM user_memory WHERE category = 'CONVERSATION_SUMMARY' ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentConversationSummaries(limit: Int = 5): List<UserMemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMemory(memory: UserMemoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memories: List<UserMemoryEntity>)

    @Update
    suspend fun updateMemory(memory: UserMemoryEntity)

    @Query("UPDATE user_memory SET lastAccessed = :currentTime, accessCount = accessCount + 1 WHERE id = :id")
    suspend fun touchMemory(id: Long, currentTime: Long = System.currentTimeMillis())

    @Query("DELETE FROM user_memory WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("DELETE FROM user_memory WHERE category = 'CONVERSATION_SUMMARY' AND timestamp < :cutoffTime")
    suspend fun cleanupOldSummaries(cutoffTime: Long): Int

    @Query("DELETE FROM user_memory")
    suspend fun clearAllMemories(): Int

    @Query("SELECT COUNT(*) FROM user_memory")
    suspend fun getMemoryCount(): Int
}
