package edu.kit.tm.ps.embertalk.crypto

import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import edu.kit.tm.ps.KeyGen
import edu.kit.tm.ps.PrivateKey
import edu.kit.tm.ps.PublicKey
import edu.kit.tm.ps.RatchetException
import edu.kit.tm.ps.embertalk.Preferences
import edu.kit.tm.ps.embertalk.epoch.EpochProvider
import java.util.UUID

internal class Keys(
    private val epochProvider: EpochProvider,
    private val prefs: SharedPreferences,
) {
    private var keys: KeyPair
    private var rolloverKeys: KeyPair

    init {
        if (!prefs.contains(Preferences.PRIVATE_KEY) && !prefs.contains(Preferences.PUBLIC_KEY)) {
            val newKeys = this.regenerate()
            keys = newKeys
            rolloverKeys = KeyPair(newKeys.publicKey.clone(), newKeys.privateKey.clone())
            store(newKeys)
        } else {
            try {
                val private = PrivateKey.deserialize(Base64.decode(prefs.getString(Preferences.PRIVATE_KEY, ""), Base64.URL_SAFE))
                val public = PublicKey.deserialize(Base64.decode(prefs.getString(Preferences.PUBLIC_KEY, ""), Base64.URL_SAFE))
                keys = KeyPair(public, private)
                rolloverKeys = keys.clone()
                store(keys)
            } catch (e: RatchetException) {
                Log.d(TAG, "Failed to deserialize keys... Regenerating...")
                keys = this.regenerate()
                rolloverKeys = keys.clone()
                store(keys)
            }
        }
    }

    fun regenerate(): KeyPair {
        val epoch = epochProvider.current()
        Log.d(TAG, "epochProvider.current %s".format(epoch))
        val keys = KeyGen.generateKeypair(epoch)
        prefs.edit { putString(Preferences.USER_ID, UUID.randomUUID().toString()) }
        val keyPair = KeyPair(keys.publicKey(), keys.privateKey())
        store(keyPair)
        return keyPair
    }

    private fun store(keyPair: KeyPair) {
        prefs.edit {
            putString(Preferences.PRIVATE_KEY, privKeyString(keyPair.privateKey))
                .putString(Preferences.PUBLIC_KEY, pubKeyString(keyPair.publicKey))
        }
    }

    fun inSync(): Boolean {
        return epochsLate() == 0L
    }

    fun epochsLate(): Long {
        return epochProvider.current() - keys.epoch()
    }

    fun fastForward() {
        val currentEpoch = epochProvider.current()
        if (currentEpoch > keys.epoch()) {
            rolloverKeys = keys.clone()
            keys = keys.fastForward(currentEpoch - keys.epoch())
        }
        Log.d(TAG, "Rollover keys ffed, now at ${rolloverKeys.epoch()}")
        Log.d(TAG, "Keys ffed, now at ${keys.epoch()}")
        store(keys)
    }

    fun fastForward(pub: PublicKey) {
        val currentEpoch = epochProvider.current()
        if (currentEpoch > pub.currentEpoch()) {
            pub.fastForward(currentEpoch - pub.currentEpoch())
        }
    }

    fun decrypt(message: ByteArray): Array<ByteArray> {
        return arrayOf(rolloverKeys.privateKey.decrypt(message), keys.privateKey.decrypt(message))
    }

    fun keys(): KeyPair {
        return keys
    }

    fun decode(pubKey: String): PublicKey {
        return PublicKey.deserialize(Base64.decode(pubKey, Base64.URL_SAFE))
    }

    private fun privKeyString(priv: PrivateKey): String? {
        return Base64.encodeToString(priv.serialize(), Base64.URL_SAFE)
    }

    private fun pubKeyString(pub: PublicKey): String? {
        return Base64.encodeToString(pub.serialize(), Base64.URL_SAFE)
    }

    fun isMyKey(pubKey: String): Boolean {
        return pubKeyString(keys.publicKey).equals(pubKey)
    }

    companion object {
        private const val TAG = "Keys"
    }
}