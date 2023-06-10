package edu.kit.tm.ps.embertalk.storage

import kotlinx.coroutines.flow.Flow

class OfflineMessageRepository(private val messageDao: MessageDao) : MessageRepository {

    override fun all(): Flow<List<Message>> = messageDao.all()
    override fun allExcept(hashes: Set<Int>): Flow<List<Message>> = messageDao.allExcept(hashes)

    override fun hashes(): Flow<List<Int>> = messageDao.hashes()

    override suspend fun insert(message: Message) = messageDao.insert(message)
}