package edu.kit.tm.ps.embertalk.model.messages.decrypted

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: Message)

    @Query("SELECT * FROM messages")
    fun all(): Flow<List<Message>>

    @Query("DELETE from messages")
    suspend fun deleteAll()
}