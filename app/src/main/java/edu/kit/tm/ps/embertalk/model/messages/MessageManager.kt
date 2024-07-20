package edu.kit.tm.ps.embertalk.model.messages

import android.util.Log
import edu.kit.tm.ps.embertalk.crypto.CryptoService
import edu.kit.tm.ps.embertalk.epoch.EpochProvider
import edu.kit.tm.ps.embertalk.model.EmberObservable
import edu.kit.tm.ps.embertalk.model.EmberObserver
import edu.kit.tm.ps.embertalk.model.contacts.ContactManager
import edu.kit.tm.ps.embertalk.model.messages.decrypted.Message
import edu.kit.tm.ps.embertalk.model.messages.decrypted.MessageRepository
import edu.kit.tm.ps.embertalk.model.messages.encrypted.EncryptedMessage
import edu.kit.tm.ps.embertalk.model.messages.encrypted.EncryptedMessageRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class MessageManager(
    private val contactManager: ContactManager,
    private val messageRepository: MessageRepository,
    private val encryptedRepository: EncryptedMessageRepository,
    private val cryptoService: CryptoService
): EmberObservable {
    private val observers = HashSet<EmberObserver>()

    suspend fun handle(message: String, recipient: UUID, publicKey: String) {
        val msg = Message(message, contactManager.me().userId, recipient, System.currentTimeMillis())
        messageRepository.insert(msg)
        val encrypted = cryptoService.encrypt(msg, publicKey)
        encryptedRepository.insert(encrypted)
    }

    suspend fun handle(encryptedMessage: EncryptedMessage) {
        val message = cryptoService.decrypt(encryptedMessage)
        encryptedRepository.insert(encryptedMessage.copy(timestamp = System.currentTimeMillis()))
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
        encryptedRepository.deleteOlderThan(
            System.currentTimeMillis() - EpochProvider.EPOCH_LENGTH
        )
    }

    companion object {
        private const val TAG = "MessageManager"
    }
}