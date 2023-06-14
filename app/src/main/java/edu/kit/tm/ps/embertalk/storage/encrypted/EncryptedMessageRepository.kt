package edu.kit.tm.ps.embertalk.storage.encrypted

import kotlinx.coroutines.flow.Flow

interface EncryptedMessageRepository {
    fun all(): Flow<List<EncryptedMessage>>
    fun allExcept(hashes: Set<Int>): Flow<List<EncryptedMessage>>

    fun hashes(): Flow<List<Int>>

    suspend fun insert(encryptedMessage: EncryptedMessage)
}