package edu.kit.tm.ps.embertalk.model.messages

import edu.kit.tm.ps.embertalk.model.EmberObservable
import edu.kit.tm.ps.embertalk.model.EmberObserver
import edu.kit.tm.ps.embertalk.model.messages.decrypted.Message
import edu.kit.tm.ps.embertalk.model.messages.decrypted.MessageRepository
import edu.kit.tm.ps.embertalk.model.messages.encrypted.EncryptedMessage
import edu.kit.tm.ps.embertalk.model.messages.encrypted.EncryptedMessageRepository
import kotlinx.coroutines.flow.Flow

class MessageManager(
    private val messageRepository: MessageRepository,
    private val encryptedRepository: EncryptedMessageRepository,
): EmberObservable {
    private val observers = HashSet<EmberObserver>()

    suspend fun handle(message: Message) {
        messageRepository.insert(message)
        encryptedRepository.insert(message.encode { it })
    }

    suspend fun handle(encryptedMessage: EncryptedMessage) {
        encryptedRepository.insert(encryptedMessage)
        val message = Message.decode(encryptedMessage, 0) { it }
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
    suspend fun deleteAll() {
        messageRepository.deleteAll()
        encryptedRepository.deleteAll()
    }
}