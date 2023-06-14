package edu.kit.tm.ps.embertalk.storage.decoded

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DecodedMessageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: DecodedMessage)

    @Query("SELECT * FROM messages")
    fun all(): Flow<List<DecodedMessage>>
}