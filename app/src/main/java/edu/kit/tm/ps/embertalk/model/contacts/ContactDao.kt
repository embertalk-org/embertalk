package edu.kit.tm.ps.embertalk.model.contacts

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts WHERE userId=:userId")
    suspend fun byId(userId: UUID): Contact

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: Contact)

    @Delete
    suspend fun delete(contact: Contact)

    @Query("SELECT * FROM contacts")
    fun all(): Flow<List<Contact>>
}