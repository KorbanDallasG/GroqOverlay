package com.groqoverlay.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    suspend fun getAllMessages(): List<Message>
    
    @Insert
    suspend fun insert(message: Message): Long
    
    @Query("DELETE FROM messages")
    suspend fun deleteAll()
    
    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteById(id: Long)
}
