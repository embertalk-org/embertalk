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

    fun regenerate() {
        val keyPair = KeyGen.generateKeypair()
        prefs.edit()
            .putString(Preferences.PRIVATE_KEY, Base64.encodeToString(keyPair.privateKey().serialize(), Base64.URL_SAFE))
            .putString(Preferences.PUBLIC_KEY, Base64.encodeToString(keyPair.publicKey().serialize(), Base64.URL_SAFE))
            .apply()
    }

    fun private(): PrivateKey {
        return PrivateKey.deserialize(Base64.decode(prefs.getString(Preferences.PRIVATE_KEY, ""), Base64.URL_SAFE))
    }

    fun public(): PublicKey {
        return PublicKey.deserialize(Base64.decode(prefs.getString(Preferences.PRIVATE_KEY, ""), Base64.URL_SAFE))
    }

    fun decode(pubKey: String): PublicKey {
        return PublicKey.deserialize(Base64.decode(pubKey, Base64.URL_SAFE))
    }
}