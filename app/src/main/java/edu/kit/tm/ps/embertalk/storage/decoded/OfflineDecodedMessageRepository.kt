package edu.kit.tm.ps.embertalk.storage.decoded

import edu.kit.tm.ps.embertalk.storage.EmberObserver
import kotlinx.coroutines.flow.Flow

class OfflineDecodedMessageRepository(private val decodedMessageDao: DecodedMessageDao) : DecodedMessageRepository {
    private val observers = HashSet<EmberObserver>()

    override fun all(): Flow<List<DecodedMessage>> = decodedMessageDao.all()

    override suspend fun insert(message: DecodedMessage) {
        decodedMessageDao.insert(message)
    }

    override fun register(observer: EmberObserver) {
        observers.add(observer)
    }

    override fun notifyObservers() {
        observers.forEach { it.notifyOfChange() }
    }
}