package edu.kit.tm.ps.embertalk.model.messages.decrypted

import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun all(): Flow<List<Message>>

    suspend fun insert(message: Message)

    suspend fun deleteAll()
}