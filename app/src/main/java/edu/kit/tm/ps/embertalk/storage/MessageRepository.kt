package edu.kit.tm.ps.embertalk.storage

import kotlinx.coroutines.flow.Flow

interface MessageRepository : EmberObservable {
    fun all(): Flow<List<Message>>
    fun allExcept(hashes: Set<Int>): Flow<List<Message>>

    fun hashes(): Flow<List<Int>>

    suspend fun insert(message: Message)
}