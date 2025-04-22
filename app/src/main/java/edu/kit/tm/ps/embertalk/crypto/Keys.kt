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
    private var private: PrivateKey
    private var public: PublicKey
    private var rolloverPrivate: PrivateKey
    private var rolloverPublic: PublicKey

    init {
        if (!prefs.contains(Preferences.PRIVATE_KEY) && !prefs.contains(Preferences.PUBLIC_KEY)) {
            val keys = this.regenerate()
            private = keys.privateKey()
            public = keys.publicKey()
            rolloverPrivate = private.clone()
            rolloverPublic = public.clone()
            storeKeys()
        } else {
            try {
                private = PrivateKey.deserialize(Base64.decode(prefs.getString(Preferences.PRIVATE_KEY, ""), Base64.URL_SAFE))
                public = PublicKey.deserialize(Base64.decode(prefs.getString(Preferences.PUBLIC_KEY, ""), Base64.URL_SAFE))
                rolloverPrivate = private.clone()
                rolloverPublic = public.clone()
                storeKeys()
            } catch (e: RatchetException) {
                Log.d(TAG, "Failed to deserialize keys... Regenerating...")
                val keys = this.regenerate()
                private = keys.privateKey()
                public = keys.publicKey()
                rolloverPrivate = private.clone()
                rolloverPublic = public.clone()
            }
        }
    }

    fun regenerate(): KeyGen.KeyPair {
        Log.d(TAG, "epochProvider.current %s".format(epochProvider.current()))
        val keyPair = KeyGen.generateKeypair(epochProvider.current())
        private = keyPair.privateKey()
        public = keyPair.publicKey()
        prefs.edit { putString(Preferences.USER_ID, UUID.randomUUID().toString()) }
        storeKeys()
        return keyPair
    }

    private fun storeKeys() {
        prefs.edit {
            putString(Preferences.PRIVATE_KEY, privKeyString(private))
                .putString(Preferences.PUBLIC_KEY, pubKeyString(public))
        }
    }

    fun inSync(): Boolean {
        return epochsLate() == 0L
    }

    fun epochsLate(): Long {
        return epochProvider.current() - private.currentEpoch()
    }

    fun fastForward() {
        val currentEpoch = epochProvider.current()
        if (currentEpoch > private.currentEpoch()) {
            rolloverPrivate = private.clone()
            private.fastForward(currentEpoch - private.currentEpoch())
        }
        if (currentEpoch > public.currentEpoch()) {
            rolloverPublic = public.clone()
            public.fastForward(currentEpoch - public.currentEpoch())
        }
        Log.d(TAG, "Rollover keys ffed, now at ${rolloverPrivate.currentEpoch()}")
        Log.d(TAG, "Keys ffed, now at ${private.currentEpoch()}")
        storeKeys()
    }

    fun fastForward(pub: PublicKey) {
        val currentEpoch = epochProvider.current()
        if (currentEpoch > pub.currentEpoch()) {
            pub.fastForward(currentEpoch - pub.currentEpoch())
        }
    }

    fun decrypt(message: ByteArray): Array<ByteArray> {
        return arrayOf(rolloverPrivate.decrypt(message), private.decrypt(message))
    }

    fun private(): PrivateKey {
        return private
    }

    fun public(): PublicKey {
        return public
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
        return pubKeyString(public).equals(pubKey)
    }

    companion object {
        private const val TAG = "Keys"
    }
}