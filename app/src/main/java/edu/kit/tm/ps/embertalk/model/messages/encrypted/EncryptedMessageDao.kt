package edu.kit.tm.ps.embertalk.model.messages.encrypted

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EncryptedMessageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(encryptedMessage: EncryptedMessage)

    @Query("SELECT * FROM messages")
    fun all(): Flow<List<EncryptedMessage>>

    @Query("SELECT hash FROM messages")
    fun hashes(): Flow<List<Int>>

    @Query("SELECT * FROM messages WHERE hash not in (:hashes)")
    fun allExcept(hashes: List<Int>): Flow<List<EncryptedMessage>>

    @Query("DELETE FROM messages")
    suspend fun deleteAll()
}