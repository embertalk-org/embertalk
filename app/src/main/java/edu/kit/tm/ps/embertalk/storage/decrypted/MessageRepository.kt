package edu.kit.tm.ps.embertalk.storage.decrypted

import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun all(): Flow<List<Message>>

    suspend fun insert(message: Message)

    suspend fun deleteAll()
}