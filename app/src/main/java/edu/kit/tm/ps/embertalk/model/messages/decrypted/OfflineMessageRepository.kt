package edu.kit.tm.ps.embertalk.model.messages.decrypted

import kotlinx.coroutines.flow.Flow

class OfflineMessageRepository(private val messageDao: MessageDao) : MessageRepository {

    override fun all(): Flow<List<Message>> = messageDao.all()

    override suspend fun insert(message: Message) {
        messageDao.insert(message)
    }

    override suspend fun deleteAll() = messageDao.deleteAll()
}