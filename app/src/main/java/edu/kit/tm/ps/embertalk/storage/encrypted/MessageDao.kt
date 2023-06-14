package edu.kit.tm.ps.embertalk.storage.encrypted

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

    @Query("SELECT hash FROM messages")
    fun hashes(): Flow<List<Int>>

    @Query("SELECT * FROM messages WHERE hash not in (:hashes)")
    fun allExcept(hashes: List<Int>): Flow<List<Message>>
}