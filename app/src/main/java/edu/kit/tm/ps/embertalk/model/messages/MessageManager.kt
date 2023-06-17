package edu.kit.tm.ps.embertalk.model.messages

import android.util.Log
import edu.kit.tm.ps.embertalk.crypto.CryptoService
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
    private val cryptoService: CryptoService,
): EmberObservable {
    private val observers = HashSet<EmberObserver>()

    suspend fun handle(message: Message, publicKey: String) {
        val messageWithEpoch = message.copy(epoch = cryptoService.currentEpoch())
        messageRepository.insert(messageWithEpoch)
        val encrypted = cryptoService.encrypt(messageWithEpoch, publicKey)
        encryptedRepository.insert(encrypted)
    }

    suspend fun handle(encryptedMessage: EncryptedMessage) {
        val message = cryptoService.decrypt(encryptedMessage)
        encryptedRepository.insert(encryptedMessage)
        if (message != null) {
            messageRepository.insert(message)
            Log.d(TAG, "Message inserted")
        } else {
            Log.d(TAG, "Message could not be decrypted")
        }
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

    companion object {
        private const val TAG = "MessageManager"
    }
}