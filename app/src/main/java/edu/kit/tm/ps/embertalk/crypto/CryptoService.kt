package edu.kit.tm.ps.embertalk.crypto

import android.content.SharedPreferences
import android.util.Log
import edu.kit.tm.ps.embertalk.Preferences
import edu.kit.tm.ps.embertalk.emberkeyd.EmberKeydClient
import edu.kit.tm.ps.embertalk.epoch.EpochProvider
import edu.kit.tm.ps.embertalk.model.EmberObservable
import edu.kit.tm.ps.embertalk.model.EmberObserver
import edu.kit.tm.ps.embertalk.model.messages.decrypted.Message
import edu.kit.tm.ps.embertalk.model.messages.encrypted.EncryptedMessage
import jakarta.inject.Inject
import java.util.LinkedList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Suppress("RedundantSuspendModifier")
class CryptoService @Inject constructor(
    private val epochProvider: EpochProvider,
    private val sharedPreferences: SharedPreferences,
) : EmberObservable {
    private val observers = LinkedList<EmberObserver>()

    private val lock = ReentrantLock()
    private var keys: Keys = Keys(epochProvider, sharedPreferences)

    fun emberKeydClient(): EmberKeydClient {
        return EmberKeydClient(
            sharedPreferences.getString(Preferences.KEY_SERVER_URL, "")!!,
            keys::keys
        )
    }

    suspend fun initialize() {
        fastForward()
    }

    private suspend fun fastForward() {
        lock.withLock {
            keys.fastForward()
        }
        notifyObservers()
    }

    suspend fun encrypt(message: Message, publicKey: String): EncryptedMessage {
        val pubKey = keys.decode(publicKey)
        keys.fastForward(pubKey)
        Log.d(TAG, "PublicKey ffed, now at ${pubKey.currentEpoch()}")
        fastForward()
        return message.encode(pubKey::encrypt)
    }

    suspend fun decrypt(encryptedMessage: EncryptedMessage): Message? {
        fastForward()
        return Message.decode(encryptedMessage, keys::decrypt)
    }

    suspend fun regenerate() {
        keys.regenerate()
        fastForward()
    }

    override fun register(observer: EmberObserver) {
        observers.add(observer)
    }

    override fun notifyObservers() {
        observers.forEach { it.notifyOfChange() }
    }

    fun isMyKey(pubKey: String): Boolean {
        return keys.isMyKey(pubKey)
    }

    companion object {
        private const val TAG = "CryptoService"
    }
}