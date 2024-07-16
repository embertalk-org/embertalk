package edu.kit.tm.ps.embertalk.model.messages

import android.util.Log
import edu.kit.tm.ps.embertalk.crypto.CryptoService
import edu.kit.tm.ps.embertalk.epoch.EpochProvider
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
    private val epochProvider: EpochProvider
): EmberObservable {
    private val observers = HashSet<EmberObserver>()

    suspend fun handle(message: String, recipient: String, publicKey: String) {
        val msg = Message(message, true, recipient, epochProvider.current(), System.currentTimeMillis())
        messageRepository.insert(msg)
        val encrypted = cryptoService.encrypt(msg, publicKey)
        encryptedRepository.insert(encrypted)
    }

    suspend fun handle(encryptedMessage: EncryptedMessage) {
        val message = cryptoService.decrypt(encryptedMessage)
        encryptedRepository.insert(encryptedMessage.copy(epoch = cryptoService.currentEpoch()))
        if (message != null) {
            messageRepository.insert(message)
            notifyObservers()
            Log.d(TAG, "Message ${message.content} inserted")
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

    suspend fun deleteOld() {
        encryptedRepository.deleteOlderThan(cryptoService.currentEpoch())
    }

    companion object {
        private const val TAG = "MessageManager"
    }
}