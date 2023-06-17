package edu.kit.tm.ps.embertalk.crypto

import android.content.SharedPreferences
import edu.kit.tm.ps.embertalk.epoch.EpochProvider
import edu.kit.tm.ps.embertalk.model.messages.decrypted.Message
import edu.kit.tm.ps.embertalk.model.messages.encrypted.EncryptedMessage
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class CryptoService(
    private val epochProvider: EpochProvider,
    private val sharedPreferences: SharedPreferences,
) {
    private val lock = ReentrantLock()
    private lateinit var keys: Keys

    suspend fun initialize() {
        keys = Keys(epochProvider, sharedPreferences)
        ratchetToCurrent()
    }

    private suspend fun ratchetToCurrent() {
        lock.withLock {
            keys.ratchetPrivateToCurrent()
        }
    }

    suspend fun encrypt(message: Message, publicKey: String): EncryptedMessage {
        val pubKey = keys.decode(publicKey)
        keys.ratchetPublicToCurrent(pubKey)
        return message.encode(pubKey::encrypt)
    }

    suspend fun decrypt(encryptedMessage: EncryptedMessage): Message? {
        ratchetToCurrent()
        return Message.decode(encryptedMessage, keys.private().currentEpoch(), keys.private()::decrypt)
    }

    fun currentEpoch(): Long {
        return epochProvider.current()
    }

    suspend fun regenerate() {
        keys.regenerate()
        keys.ratchetPrivateToCurrent()
    }

    fun isInitialized(): Boolean {
        return this::keys.isInitialized
    }

    fun syncState(): SyncState {
        return if (!this::keys.isInitialized) {
            SyncState.Initializing
        } else if (keys.inSync()) {
            SyncState.Synchronized
        } else {
            SyncState.Synchronizing(keys.remainingRatchets())
        }
    }
}