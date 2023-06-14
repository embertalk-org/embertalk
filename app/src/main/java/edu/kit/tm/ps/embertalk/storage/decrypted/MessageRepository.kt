package edu.kit.tm.ps.embertalk.storage.decrypted

import edu.kit.tm.ps.embertalk.storage.EmberObservable
import kotlinx.coroutines.flow.Flow

interface MessageRepository : EmberObservable {
    fun all(): Flow<List<Message>>

    suspend fun insert(message: Message)
}