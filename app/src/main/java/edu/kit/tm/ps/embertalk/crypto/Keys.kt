package edu.kit.tm.ps.embertalk.crypto

import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import edu.kit.tm.ps.KeyGen
import edu.kit.tm.ps.PrivateKey
import edu.kit.tm.ps.PublicKey
import edu.kit.tm.ps.embertalk.Preferences
import edu.kit.tm.ps.embertalk.epoch.EpochProvider

internal class Keys(
    private val epochProvider: EpochProvider,
    private val prefs: SharedPreferences,
) {
    private var private: PrivateKey
    private var public: PublicKey

    init {
        if (!prefs.contains(Preferences.PRIVATE_KEY) && !prefs.contains(Preferences.PUBLIC_KEY)) {
            val keys = this.regenerate()
            private = keys.privateKey()
            public = keys.publicKey()
            storeKeys()
        } else {
            private = PrivateKey.deserialize(Base64.decode(prefs.getString(Preferences.PRIVATE_KEY, ""), Base64.URL_SAFE))
            public = PublicKey.deserialize(Base64.decode(prefs.getString(Preferences.PUBLIC_KEY, ""), Base64.URL_SAFE))
            storeKeys()
        }
    }

    fun regenerate(): KeyGen.KeyPair {
        Log.d("Keys", "epochProvider.current %s".format(epochProvider.current()))
        val keyPair = KeyGen.generateKeypair(epochProvider.current())
        private = keyPair.privateKey()
        public = keyPair.publicKey()
        storeKeys()
        return keyPair
    }

    private fun storeKeys() {
        prefs.edit()
            .putString(Preferences.PRIVATE_KEY, privKeyString(private))
            .putString(Preferences.PUBLIC_KEY, pubKeyString(public))
            .apply()
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
            private.fastForward(currentEpoch - private.currentEpoch())
        }
        storeKeys()
    }

    fun fastForward(pub: PublicKey) {
        val currentEpoch = epochProvider.current()
        if (currentEpoch > pub.currentEpoch()) {
            pub.fastForward(currentEpoch - pub.currentEpoch())
        }
    }

    fun private(): PrivateKey {
        return private
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
}