package com.example.util

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ChatTurn model representing a persistent conversation unit.
 */
data class ChatTurn(
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val role: String, // "user" | "assistant"
    val messageText: String
)

/**
 * PermanentChatMemoryEngine:
 * Zero-dependency, native SQLite-backed permanent chat memory engine.
 * Stores conversation turns across app restarts and formats history for Gemini API context.
 */
object PermanentChatMemoryEngine {

    private const val TAG = "PermanentChatMemory"
    private const val DB_NAME = "sahnaj_chat_memory.db"
    private const val DB_VERSION = 1
    private const val TABLE_NAME = "chat_turns"

    private const val COL_ID = "id"
    private const val COL_TIMESTAMP = "timestamp"
    private const val COL_ROLE = "role"
    private const val COL_MESSAGE = "message"

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var dbHelper: DBHelper? = null

    private val _recentTurnsFlow = MutableStateFlow<List<ChatTurn>>(emptyList())
    val recentTurnsFlow: StateFlow<List<ChatTurn>> = _recentTurnsFlow.asStateFlow()

    private class DBHelper(context: Context) : SQLiteOpenHelper(context.applicationContext, DB_NAME, null, DB_VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                    $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_TIMESTAMP INTEGER NOT NULL,
                    $COL_ROLE TEXT NOT NULL,
                    $COL_MESSAGE TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_chat_timestamp ON $TABLE_NAME($COL_TIMESTAMP)")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        }
    }

    /**
     * Initializes the chat memory engine with Application Context.
     */
    @Synchronized
    fun init(context: Context) {
        if (dbHelper == null) {
            dbHelper = DBHelper(context.applicationContext)
            refreshMemoryCache()
        }
    }

    /**
     * Records a user or assistant message to permanent storage in background.
     */
    fun recordTurn(role: String, messageText: String) {
        if (messageText.isBlank()) return
        scope.launch {
            saveTurnInternal(role.trim().lowercase(), messageText.trim())
            refreshMemoryCache()
        }
    }

    /**
     * Saves a turn synchronously / suspending on Dispatchers.IO.
     */
    suspend fun saveTurn(role: String, messageText: String) = withContext(Dispatchers.IO) {
        if (messageText.isBlank()) return@withContext
        saveTurnInternal(role.trim().lowercase(), messageText.trim())
        refreshMemoryCache()
    }

    private fun saveTurnInternal(role: String, messageText: String) {
        try {
            val db = dbHelper?.writableDatabase ?: return
            val values = ContentValues().apply {
                put(COL_TIMESTAMP, System.currentTimeMillis())
                put(COL_ROLE, role)
                put(COL_MESSAGE, messageText)
            }
            db.insert(TABLE_NAME, null, values)
            Log.d(TAG, "Saved chat turn [$role]: ${messageText.take(40)}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving chat turn", e)
        }
    }

    /**
     * Loads the last [limit] chat entries from permanent storage.
     */
    suspend fun getLastTurns(limit: Int = 30): List<ChatTurn> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ChatTurn>()
        try {
            val db = dbHelper?.readableDatabase ?: return@withContext emptyList()
            val cursor = db.query(
                TABLE_NAME,
                arrayOf(COL_ID, COL_TIMESTAMP, COL_ROLE, COL_MESSAGE),
                null,
                null,
                null,
                null,
                "$COL_ID DESC",
                limit.toString()
            )
            cursor.use { c ->
                val idIdx = c.getColumnIndex(COL_ID)
                val timeIdx = c.getColumnIndex(COL_TIMESTAMP)
                val roleIdx = c.getColumnIndex(COL_ROLE)
                val msgIdx = c.getColumnIndex(COL_MESSAGE)

                while (c.moveToNext()) {
                    val id = if (idIdx >= 0) c.getLong(idIdx) else 0L
                    val time = if (timeIdx >= 0) c.getLong(timeIdx) else System.currentTimeMillis()
                    val role = if (roleIdx >= 0) c.getString(roleIdx) ?: "user" else "user"
                    val msg = if (msgIdx >= 0) c.getString(msgIdx) ?: "" else ""
                    if (msg.isNotBlank()) {
                        results.add(ChatTurn(id, time, role, msg))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading chat turns", e)
        }
        // Return in chronological order (oldest first)
        results.reversed()
    }

    /**
     * Formats recent chat history for feeding directly into Gemini API system context or prompt.
     */
    suspend fun getFormattedHistoryForContext(limit: Int = 30): String = withContext(Dispatchers.IO) {
        val turns = getLastTurns(limit)
        if (turns.isEmpty()) return@withContext ""

        val sb = StringBuilder()
        sb.append("Recent Conversation History:\n")
        for (turn in turns) {
            val speaker = if (turn.role == "user") "User" else "SAHNAJ"
            sb.append("$speaker: ${turn.messageText}\n")
        }
        sb.toString().trim()
    }

    /**
     * Clears all stored chat memory.
     */
    suspend fun clearAllMemory() = withContext(Dispatchers.IO) {
        try {
            val db = dbHelper?.writableDatabase
            db?.delete(TABLE_NAME, null, null)
            _recentTurnsFlow.value = emptyList()
            Log.d(TAG, "Cleared all permanent chat memory")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing chat memory", e)
        }
    }

    private fun refreshMemoryCache() {
        scope.launch {
            val turns = getLastTurns(30)
            _recentTurnsFlow.value = turns
        }
    }
}
