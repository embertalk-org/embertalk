package edu.kit.tm.ps.embertalk.model.messages

import android.util.Log
import edu.kit.tm.ps.PublicKey
import edu.kit.tm.ps.embertalk.crypto.Keys
import edu.kit.tm.ps.embertalk.epoch.EpochProvider
import edu.kit.tm.ps.embertalk.model.EmberObservable
import edu.kit.tm.ps.embertalk.model.EmberObserver
import edu.kit.tm.ps.embertalk.model.messages.decrypted.Message
import edu.kit.tm.ps.embertalk.model.messages.decrypted.MessageRepository
import edu.kit.tm.ps.embertalk.model.messages.encrypted.EncryptedMessage
import edu.kit.tm.ps.embertalk.model.messages.encrypted.EncryptedMessageRepository
import kotlinx.coroutines.flow.Flow

class MessageManager(
    private val epochProvider: EpochProvider,
    private val messageRepository: MessageRepository,
    private val encryptedRepository: EncryptedMessageRepository,
    private val keys: Keys,
): EmberObservable {
    private val observers = HashSet<EmberObserver>()

    suspend fun handle(message: Message, publicKey: PublicKey) {
        val currentEpoch = epochProvider.current()
        Log.d(TAG, "Handling message at epoch %s".format(currentEpoch))
        keys.ratchetPublicTo(publicKey, currentEpoch)
        val messageWithEpoch = message.copy(epoch = currentEpoch)
        messageRepository.insert(messageWithEpoch)
        encryptedRepository.insert(messageWithEpoch.encode { publicKey.encrypt(it) })
    }

    suspend fun handle(encryptedMessage: EncryptedMessage) {
        val currentEpoch = epochProvider.current()
        Log.d(TAG, "Handling encrypted message at epoch %s".format(currentEpoch))
        keys.ratchetPrivateTo(currentEpoch)
        val message = Message.decode(encryptedMessage, currentEpoch) { keys.private().decrypt(it) }
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