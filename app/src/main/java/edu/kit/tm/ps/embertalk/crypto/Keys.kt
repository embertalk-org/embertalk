package edu.kit.tm.ps.embertalk.crypto

import android.content.SharedPreferences
import android.util.Base64
import edu.kit.tm.ps.KeyGen
import edu.kit.tm.ps.PrivateKey
import edu.kit.tm.ps.PublicKey
import edu.kit.tm.ps.embertalk.Preferences

class Keys(
    private val prefs: SharedPreferences,
) {
    private var private: PrivateKey
    private var public: PublicKey

    init {
        if (!prefs.contains(Preferences.PRIVATE_KEY) && !prefs.contains(Preferences.PUBLIC_KEY)) {
            val keys = Keys(prefs).regenerate()
            private = keys.privateKey()
            public = keys.publicKey()
        } else {
            private = PrivateKey.deserialize(Base64.decode(prefs.getString(Preferences.PRIVATE_KEY, ""), Base64.URL_SAFE))
            public = PublicKey.deserialize(Base64.decode(prefs.getString(Preferences.PUBLIC_KEY, ""), Base64.URL_SAFE))
        }
    }

    fun regenerate(): KeyGen.KeyPair {
        val keyPair = KeyGen.generateKeypair()
        prefs.edit()
            .putString(Preferences.PRIVATE_KEY, privKeyString(keyPair.privateKey()))
            .putString(Preferences.PUBLIC_KEY, pubKeyString(keyPair.publicKey()))
            .apply()
        return keyPair
    }

    fun ratchetPrivateTo(epoch: Long) {
        for (i in 0 until epoch - private.currentEpoch()) {
            private.ratchet()
        }
        prefs.edit().putString(Preferences.PRIVATE_KEY, privKeyString(private)).apply()
    }

    fun ratchetPublicTo(pub: PublicKey, epoch: Long) {
        if (epoch > pub.currentEpoch()) {
            pub.fastForward(epoch - pub.currentEpoch())
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