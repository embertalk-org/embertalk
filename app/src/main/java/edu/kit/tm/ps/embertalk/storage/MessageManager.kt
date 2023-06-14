package edu.kit.tm.ps.embertalk.storage

import edu.kit.tm.ps.embertalk.storage.decrypted.Message
import edu.kit.tm.ps.embertalk.storage.decrypted.MessageRepository
import edu.kit.tm.ps.embertalk.storage.encrypted.EncryptedMessage
import edu.kit.tm.ps.embertalk.storage.encrypted.EncryptedMessageRepository
import kotlinx.coroutines.flow.Flow

class MessageManager(
    private val messageRepository: MessageRepository,
    private val encryptedRepository: EncryptedMessageRepository,
): EmberObservable {
    private val observers = HashSet<EmberObserver>()

    suspend fun handle(message: Message) {
        messageRepository.insert(message)
        encryptedRepository.insert(message.encode())
    }

    suspend fun handle(encryptedMessage: EncryptedMessage) {
        encryptedRepository.insert(encryptedMessage)
        val message = Message.decode(encryptedMessage, 0)
        message?.let { messageRepository.insert(it) }
    }

    fun messages(): Flow<List<Message>> = messageRepository.all()

    override fun register(observer: EmberObserver) {
        observers.add(observer)
    }

    override fun notifyObservers() {
        observers.forEach { it.notifyOfChange() }
    }

    fun hashes(): Flow<List<Int>> = encryptedRepository.hashes()

    fun allEncryptedExcept(theirHashes: Set<Int>): Flow<List<EncryptedMessage>> = encryptedRepository.allExcept(theirHashes)
}