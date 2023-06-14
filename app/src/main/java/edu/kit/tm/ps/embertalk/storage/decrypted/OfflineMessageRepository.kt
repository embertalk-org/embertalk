package edu.kit.tm.ps.embertalk.storage.decrypted

import edu.kit.tm.ps.embertalk.storage.EmberObserver
import kotlinx.coroutines.flow.Flow

class OfflineMessageRepository(private val messageDao: MessageDao) : MessageRepository {
    private val observers = HashSet<EmberObserver>()

    override fun all(): Flow<List<Message>> = messageDao.all()

    override suspend fun insert(message: Message) {
        messageDao.insert(message)
    }

    override fun register(observer: EmberObserver) {
        observers.add(observer)
    }

    override fun notifyObservers() {
        observers.forEach { it.notifyOfChange() }
    }
}