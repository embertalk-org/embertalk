package edu.kit.tm.ps.embertalk.storage.encrypted

import kotlinx.coroutines.flow.Flow

class OfflineEncryptedMessageRepository(private val encryptedMessageDao: EncryptedMessageDao) : EncryptedMessageRepository {

    override fun all(): Flow<List<EncryptedMessage>> = encryptedMessageDao.all()
    override fun allExcept(hashes: Set<Int>): Flow<List<EncryptedMessage>> =
        encryptedMessageDao.allExcept(hashes.toList())

    override fun hashes(): Flow<List<Int>> = encryptedMessageDao.hashes()

    override suspend fun insert(encryptedMessage: EncryptedMessage) =
        encryptedMessageDao.insert(encryptedMessage)

    override suspend fun deleteAll() = encryptedMessageDao.deleteAll()
}